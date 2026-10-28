package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupMemberDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;

/**
 * Đồng bộ Group và GroupMember giữa Firestore và Room.
 *
 * - Firestore giữ dữ liệu chung cho mọi tài khoản/máy: collection "groups" và "group_members".
 * - Room vẫn là nơi giao diện đọc (GroupDao, GroupMemberDao giữ nguyên), nhưng được cập nhật
 *   tự động mỗi khi Firestore thay đổi.
 *
 * Ghi: GroupRepository / GroupMemberRepository gọi pushGroup, pushMember, deleteGroupRemote...
 * Đọc: start() lắng nghe 2 collection, chép về Room rồi báo cho các màn hình đang đăng ký
 *      (addListener) để tải lại danh sách.
 *
 * Group/Member chỉ có ở máy (tạo trước khi có đồng bộ) sẽ được đẩy lên Firestore ở lần đồng bộ đầu tiên.
 */
public class GroupSyncManager {

    private static final String TAG = "GroupSyncManager";
    private static final String GROUPS = "groups";
    private static final String MEMBERS = "group_members";

    public interface OnSyncListener {
        void onGroupsSynced();
    }

    private static GroupSyncManager instance;

    public static synchronized GroupSyncManager getInstance(Application application) {
        if (instance == null) {
            instance = new GroupSyncManager(application);
        }
        return instance;
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final GroupDao groupDao;
    private final GroupMemberDao memberDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<OnSyncListener> listeners = new CopyOnWriteArrayList<>();

    private ListenerRegistration groupsRegistration;
    private ListenerRegistration membersRegistration;

    // Các biến dưới đây chỉ được dùng trên executor
    private final Map<String, Group> remoteGroups = new HashMap<>();
    private final Map<String, GroupMember> remoteMembers = new HashMap<>();
    private final Set<String> knownRemoteGroupIds = new HashSet<>();
    private boolean groupsLoaded = false;
    private boolean membersLoaded = false;
    private boolean uploadedLocalOnly = false;

    private GroupSyncManager(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        groupDao = db.groupDao();
        memberDao = db.groupMemberDao();
    }

    // =================================
    // Đăng ký nhận thông báo "đã đồng bộ xong"
    // =================================

    public void addListener(OnSyncListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnSyncListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnSyncListener listener : listeners) {
                    listener.onGroupsSynced();
                }
            }
        });
    }

    // =================================
    // Bắt đầu / dừng lắng nghe Firestore
    // =================================

    // Gọi bao nhiêu lần cũng được, chỉ lắng nghe một lần. Chưa đăng nhập thì chưa làm gì.
    public synchronized void start() {
        if (groupsRegistration != null) {
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        groupsRegistration = firestore.collection(GROUPS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshots == null) {
                            Log.w(TAG, "Lỗi lắng nghe groups", error);
                            return;
                        }
                        final List<Group> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Group group = toGroup(doc);
                            if (group != null) {
                                list.add(group);
                            }
                        }
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                remoteGroups.clear();
                                for (Group group : list) {
                                    remoteGroups.put(group.getGroupId(), group);
                                }
                                groupsLoaded = true;
                                reconcile();
                            }
                        });
                    }
                });

        membersRegistration = firestore.collection(MEMBERS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshots == null) {
                            Log.w(TAG, "Lỗi lắng nghe group_members", error);
                            return;
                        }
                        final List<GroupMember> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            GroupMember member = toMember(doc);
                            if (member != null) {
                                list.add(member);
                            }
                        }
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                remoteMembers.clear();
                                for (GroupMember member : list) {
                                    remoteMembers.put(member.getMembershipId(), member);
                                }
                                membersLoaded = true;
                                reconcile();
                            }
                        });
                    }
                });
    }

    public synchronized void stop() {
        if (groupsRegistration != null) {
            groupsRegistration.remove();
            groupsRegistration = null;
        }
        if (membersRegistration != null) {
            membersRegistration.remove();
            membersRegistration = null;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                remoteGroups.clear();
                remoteMembers.clear();
                knownRemoteGroupIds.clear();
                groupsLoaded = false;
                membersLoaded = false;
                uploadedLocalOnly = false;
            }
        });
    }

    // =================================
    // Chép dữ liệu Firestore về Room (chạy trên executor)
    // =================================

    private void reconcile() {
        // Chờ cả 2 collection tải xong lần đầu để không chép member khi chưa có group
        if (!groupsLoaded || !membersLoaded) {
            return;
        }

        // Lần đầu: đẩy lên Firestore những group/member chỉ có ở máy này
        if (!uploadedLocalOnly) {
            uploadedLocalOnly = true;
            for (Group local : groupDao.getAll()) {
                if (!remoteGroups.containsKey(local.getGroupId())) {
                    pushGroup(local);
                }
            }
            for (GroupMember local : memberDao.getAll()) {
                if (!remoteMembers.containsKey(local.getMembershipId())) {
                    pushMember(local);
                }
            }
        }

        // Group: thêm mới hoặc cập nhật
        for (Group remote : remoteGroups.values()) {
            Group local = groupDao.getById(remote.getGroupId());
            if (local == null) {
                groupDao.insert(remote);
            } else {
                groupDao.update(remote);
            }
            knownRemoteGroupIds.add(remote.getGroupId());
        }

        // Group đã bị xoá ở máy khác: xoá khỏi Room (chỉ xoá những group từng thấy trên Firestore)
        List<String> removedIds = new ArrayList<>();
        for (String knownId : knownRemoteGroupIds) {
            if (!remoteGroups.containsKey(knownId)) {
                removedIds.add(knownId);
            }
        }
        for (String removedId : removedIds) {
            memberDao.deleteByGroupId(removedId);
            Group local = groupDao.getById(removedId);
            if (local != null) {
                groupDao.delete(local);
            }
            knownRemoteGroupIds.remove(removedId);
        }

        // Member: chỉ chép khi group của nó đã có trong Room (GroupMember có khoá ngoại tới Group)
        for (GroupMember remote : remoteMembers.values()) {
            if (groupDao.getById(remote.getGroupId()) == null) {
                continue;
            }
            GroupMember local = memberDao.getById(remote.getMembershipId());
            if (local == null) {
                memberDao.insert(remote);
            } else {
                memberDao.update(remote);
            }
        }

        notifyListeners();
    }

    // =================================
    // Ghi lên Firestore
    // =================================

    public void pushGroup(Group group) {
        if (group == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", group.getGroupId());
        data.put("groupName", group.getGroupName());
        data.put("description", group.getDescription());
        data.put("coverImageUrl", group.getCoverImageUrl());
        data.put("avatarUrl", group.getAvatarUrl());
        data.put("creatorId", group.getCreatorId());

        firestore.collection(GROUPS).document(group.getGroupId())
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Không ghi được group lên Firestore", e);
                    }
                });
    }

    public void pushMember(GroupMember member) {
        if (member == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("membershipId", member.getMembershipId());
        data.put("userId", member.getUserId());
        data.put("groupId", member.getGroupId());
        data.put("role", member.getRole() == null ? GroupRole.MEMBER.name() : member.getRole().name());
        data.put("joinDate", member.getJoinDate());
        data.put("active", member.isActive());

        firestore.collection(MEMBERS).document(member.getMembershipId())
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Không ghi được member lên Firestore", e);
                    }
                });
    }

    public void deleteMemberRemote(String membershipId) {
        if (membershipId == null) {
            return;
        }
        firestore.collection(MEMBERS).document(membershipId).delete();
    }

    // Xoá group trên Firestore: bình luận của các bài trong group, bài viết, thành viên rồi tới group
    public void deleteGroupRemote(String groupId) {
        if (groupId == null) {
            return;
        }
        GroupCascadeDelete.deleteGroup(firestore, groupId, null);
    }

    // =================================
    // Đổi document Firestore thành entity
    // =================================

    private Group toGroup(DocumentSnapshot doc) {
        String id = doc.getId();
        if (id == null || id.isEmpty()) {
            return null;
        }
        Group group = new Group(id, doc.getString("groupName"),
                doc.getString("description"), doc.getString("coverImageUrl"));
        group.setAvatarUrl(doc.getString("avatarUrl"));
        group.setCreatorId(doc.getString("creatorId"));
        return group;
    }

    private GroupMember toMember(DocumentSnapshot doc) {
        String id = doc.getId();
        String groupId = doc.getString("groupId");
        if (id == null || id.isEmpty() || groupId == null) {
            return null;
        }
        GroupRole role = GroupRole.MEMBER;
        String roleName = doc.getString("role");
        if (roleName != null) {
            try {
                role = GroupRole.valueOf(roleName);
            } catch (IllegalArgumentException e) {
                role = GroupRole.MEMBER;
            }
        }
        GroupMember member = new GroupMember(id, doc.getString("userId"), groupId, role);
        Date joinDate = doc.getDate("joinDate");
        if (joinDate != null) {
            member.setJoinDate(joinDate);
        }
        Boolean active = doc.getBoolean("active");
        member.setActive(active == null || active);
        return member;
    }
}
