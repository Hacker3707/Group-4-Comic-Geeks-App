package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupMemberRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupRepository;

/**
 * ViewModel gộp chung Group + GroupMember (theo yêu cầu nhóm trưởng).
 * Repository trả dữ liệu đồng bộ nên mọi thao tác chạy trên thread nền,
 * kết quả đẩy ra View qua LiveData (postValue).
 *
 * Cách dùng trong Activity/Fragment:
 *   GroupViewModel vm = new ViewModelProvider(this).get(GroupViewModel.class);
 *   vm.getGroups().observe(this, list -> { ... });
 *   vm.loadAllGroups();
 */
public class GroupViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<Group> selectedGroup = new MutableLiveData<>();
    private final MutableLiveData<List<GroupMember>> members = new MutableLiveData<>();
    private final MutableLiveData<Integer> memberCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdmin = new MutableLiveData<>();

    public GroupViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        groupRepository = new GroupRepository(db.GroupDao());
        groupMemberRepository = new GroupMemberRepository(db.GroupMemberDao());
    }

    // =================================
    // LiveData cho View observe
    // =================================

    public LiveData<List<Group>> getGroups() {
        return groups;
    }

    public LiveData<Group> getSelectedGroup() {
        return selectedGroup;
    }

    public LiveData<List<GroupMember>> getMembers() {
        return members;
    }

    public LiveData<Integer> getMemberCount() {
        return memberCount;
    }

    public LiveData<Boolean> getIsAdmin() {
        return isAdmin;
    }

    // =================================
    // Group: load dữ liệu
    // =================================

    public void loadAllGroups() {
        executor.execute(() -> groups.postValue(groupRepository.getAll()));
    }

    public void searchGroups(String keyword) {
        executor.execute(() -> groups.postValue(groupRepository.searchByName(keyword)));
    }

    // Các group mà user đang tham gia (chỉ tính thành viên còn active)
    public void loadGroupsOfUser(String userId) {
        executor.execute(() -> groups.postValue(groupRepository.getGroupsOfUser(userId)));
    }

    public void loadGroup(String groupId) {
        executor.execute(() -> selectedGroup.postValue(groupRepository.getById(groupId)));
    }

    // =================================
    // Group: tạo / sửa / xoá
    // =================================

    // Tạo group mới, người tạo tự động là ADMIN của group
    public void createGroup(String groupName, String description, String coverImageUrl,
                            String creatorUserId) {
        executor.execute(() -> {
            String groupId = UUID.randomUUID().toString();
            Group group = new Group(groupId, groupName, description, coverImageUrl);
            GroupMember admin = new GroupMember(
                    UUID.randomUUID().toString(), creatorUserId, groupId, GroupRole.ADMIN);

            // Insert group trước vì GroupMember có foreign key tới group
            db.runInTransaction(() -> {
                groupRepository.insert(group);
                groupMemberRepository.insert(admin);
            });
            groups.postValue(groupRepository.getGroupsOfUser(creatorUserId));
        });
    }

    public void updateGroupInfo(Group group, String groupName, String description,
                                String coverImageUrl) {
        executor.execute(() -> {
            group.updateGroupInfo(groupName, description, coverImageUrl);
            groupRepository.update(group);
            selectedGroup.postValue(group);
        });
    }

    // Xoá group và toàn bộ thành viên của nó
    public void deleteGroup(Group group) {
        executor.execute(() -> {
            db.runInTransaction(() -> {
                groupMemberRepository.deleteByGroupId(group.getGroupId());
                groupRepository.delete(group);
            });
            groups.postValue(groupRepository.getAll());
        });
    }

    // =================================
    // GroupMember: thành viên
    // =================================

    public void loadMembers(String groupId) {
        executor.execute(() -> {
            members.postValue(groupMemberRepository.getActiveMembersByGroup(groupId));
            memberCount.postValue(groupMemberRepository.countActiveMembers(groupId));
        });
    }

    // Tham gia group. Nếu trước đó đã rời group thì kích hoạt lại bản ghi cũ.
    public void joinGroup(String userId, String groupId) {
        executor.execute(() -> {
            GroupMember existing = groupMemberRepository.getMembership(userId, groupId);
            if (existing == null) {
                groupMemberRepository.insert(new GroupMember(
                        UUID.randomUUID().toString(), userId, groupId, GroupRole.MEMBER));
            } else if (!existing.isActive()) {
                existing.setActive(true);
                existing.setRole(GroupRole.MEMBER);
                groupMemberRepository.update(existing);
            }
            // Đã là thành viên active thì không làm gì
            loadMembers(groupId);
        });
    }

    // Rời group hoặc admin xoá thành viên (đánh dấu active = false, không xoá hẳn)
    public void removeMember(String membershipId, String groupId) {
        executor.execute(() -> {
            groupMemberRepository.removeMember(membershipId);
            loadMembers(groupId);
        });
    }

    public void changeRole(String membershipId, GroupRole newRole, String groupId) {
        executor.execute(() -> {
            groupMemberRepository.changeRole(membershipId, newRole);
            loadMembers(groupId);
        });
    }

    // Kiểm tra user có phải admin của group không (để ẩn/hiện nút quản lý)
    public void checkAdmin(String userId, String groupId) {
        executor.execute(() -> isAdmin.postValue(groupMemberRepository.isAdmin(userId, groupId)));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}