package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.FollowRemoteRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserRepository;

/**
 * Dữ liệu cho trang chủ (Home). Feed gồm:
 *  - bài của những người mình đang theo dõi (bài đăng ngoài group),
 *  - bài đăng trong các group mình đã tham gia,
 *  - bài của chính mình.
 * Chưa đăng nhập thì feed rỗng.
 *
 * Ngoài feed, ViewModel còn đưa ra danh sách group (để vẽ avatar + tên nhóm)
 * và tên + ảnh của các tác giả (cập nhật theo thời gian thực).
 */
public class HomeFeedViewModel extends AndroidViewModel {

    private static final class GroupState {
        final Set<String> joinedIds;
        final Map<String, Group> groups;

        GroupState(Set<String> joinedIds, Map<String, Group> groups) {
            this.joinedIds = joinedIds;
            this.groups = groups;
        }
    }

    private final AppDatabase db;
    private final UserRepository userRepository;
    private final FollowRemoteRepository followRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LiveData<List<Post>> allPosts;
    private final MutableLiveData<Set<String>> followingIds =
            new MutableLiveData<>(new HashSet<String>());
    private final MutableLiveData<GroupState> groupState = new MutableLiveData<>(
            new GroupState(new HashSet<String>(), new HashMap<String, Group>()));

    private final MediatorLiveData<List<Post>> feed = new MediatorLiveData<>();
    private final MediatorLiveData<Map<String, Group>> groups = new MediatorLiveData<>();
    private final MediatorLiveData<Map<String, User>> users = new MediatorLiveData<>();
    private LiveData<Map<String, User>> usersSource = null;

    private ListenerRegistration followRegistration;
    private String watchedUid = null;
    private boolean uidInitialized = false;
    private String authorKey = null;

    private final GroupSyncManager.OnSyncListener syncListener =
            new GroupSyncManager.OnSyncListener() {
                @Override
                public void onGroupsSynced() {
                    loadGroupState();
                }
            };

    public HomeFeedViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        userRepository = new UserRepository(application);
        followRepository = new FollowRemoteRepository(application);
        allPosts = db.postDao().getAllPosts();

        feed.addSource(allPosts, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                recompute();
            }
        });
        feed.addSource(followingIds, new Observer<Set<String>>() {
            @Override
            public void onChanged(Set<String> ids) {
                recompute();
            }
        });
        feed.addSource(groupState, new Observer<GroupState>() {
            @Override
            public void onChanged(GroupState state) {
                recompute();
            }
        });

        groups.addSource(groupState, new Observer<GroupState>() {
            @Override
            public void onChanged(GroupState state) {
                groups.setValue(state.groups);
            }
        });

        GroupSyncManager.getInstance(application).addListener(syncListener);
        refreshUser();
    }

    // =================================
    // LiveData cho View
    // =================================

    public LiveData<List<Post>> getFeed() {
        return feed;
    }

    public LiveData<Map<String, Group>> getGroups() {
        return groups;
    }

    public LiveData<Map<String, User>> getUsers() {
        return users;
    }

    // =================================
    // Theo dõi tài khoản đang đăng nhập
    // =================================

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Gọi ở onResume: đăng nhập / đăng xuất / đổi tài khoản thì nghe lại follow và group
    public void refreshUser() {
        String uid = currentUid();

        if (!uidInitialized || !Objects.equals(uid, watchedUid)) {
            uidInitialized = true;
            watchedUid = uid;

            if (followRegistration != null) {
                followRegistration.remove();
                followRegistration = null;
            }
            followingIds.setValue(new HashSet<String>());

            if (uid != null) {
                followRegistration = followRepository.listenFollowingIds(uid,
                        new FollowRemoteRepository.Callback<Set<String>>() {
                            @Override
                            public void onResult(Set<String> ids) {
                                followingIds.postValue(ids);
                            }
                        });
            }
        }

        // Group cần Firestore đã đăng nhập mới đồng bộ được
        GroupSyncManager.getInstance(getApplication()).start();
        loadGroupState();
    }

    // Đọc group + các group mình đã join từ Room (chạy nền)
    private void loadGroupState() {
        final String uid = currentUid();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Set<String> joined = new HashSet<>();
                if (uid != null) {
                    for (GroupMember member : db.groupMemberDao().getActiveMembershipsOfUser(uid)) {
                        if (member.getGroupId() != null) {
                            joined.add(member.getGroupId());
                        }
                    }
                }
                Map<String, Group> map = new HashMap<>();
                for (Group group : db.groupDao().getAll()) {
                    map.put(group.getGroupId(), group);
                }
                groupState.postValue(new GroupState(joined, map));
            }
        });
    }

    // =================================
    // Lọc feed
    // =================================

    private void recompute() {
        List<Post> posts = allPosts.getValue();
        Set<String> following = followingIds.getValue();
        GroupState state = groupState.getValue();
        String uid = currentUid();

        List<Post> result = new ArrayList<>();
        if (posts != null && uid != null && following != null && state != null) {
            for (Post post : posts) {
                if (shouldShow(post, uid, following, state.joinedIds)) {
                    result.add(post);
                }
            }
        }
        feed.setValue(result);
        updateAuthors(result);
    }

    private boolean shouldShow(Post post, String uid, Set<String> following, Set<String> joined) {
        // Bài của chính mình luôn hiện
        if (uid.equals(post.getUserId())) {
            return true;
        }
        // Bài trong group: chỉ hiện nếu mình đã join group đó
        if (!TextUtils.isEmpty(post.getGroupId())) {
            return joined.contains(post.getGroupId());
        }
        // Bài thường: chỉ hiện nếu mình đang theo dõi người đăng
        return post.getUserId() != null && following.contains(post.getUserId());
    }

    // Chỉ đăng ký lại việc theo dõi tên/ảnh khi danh sách tác giả đổi
    private void updateAuthors(List<Post> posts) {
        TreeSet<String> ids = new TreeSet<>();
        for (Post post : posts) {
            if (!TextUtils.isEmpty(post.getUserId())) {
                ids.add(post.getUserId());
            }
        }
        String key = String.join(",", ids);
        if (key.equals(authorKey)) {
            return;
        }
        authorKey = key;
        if (usersSource != null) {
            users.removeSource(usersSource);
        }
        usersSource = userRepository.watchUsersByIds(new ArrayList<>(ids));
        users.addSource(usersSource, new Observer<Map<String, User>>() {
            @Override
            public void onChanged(Map<String, User> map) {
                users.setValue(map);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        GroupSyncManager.getInstance(getApplication()).removeListener(syncListener);
        if (followRegistration != null) {
            followRegistration.remove();
            followRegistration = null;
        }
        userRepository.shutdown();
        executor.shutdown();
    }
}
