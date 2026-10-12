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
 *   GroupViewModel viewModel = new ViewModelProvider(this).get(GroupViewModel.class);
 *   viewModel.getGroups().observe(this, new Observer<List<Group>>() {
 *       public void onChanged(List<Group> groupList) { ... }
 *   });
 *   viewModel.loadAllGroups();
 */
public class GroupViewModel extends AndroidViewModel {

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
        groupRepository = new GroupRepository(application);
        groupMemberRepository = new GroupMemberRepository(application);
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Group> result = groupRepository.getAll();
                groups.postValue(result);
            }
        });
    }

    public void searchGroups(final String keyword) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Group> result = groupRepository.searchByName(keyword);
                groups.postValue(result);
            }
        });
    }

    // Các group mà user đang tham gia (chỉ tính thành viên còn active)
    public void loadGroupsOfUser(final String userId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Group> result = groupRepository.getGroupsOfUser(userId);
                groups.postValue(result);
            }
        });
    }

    public void loadGroup(final String groupId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Group result = groupRepository.getById(groupId);
                selectedGroup.postValue(result);
            }
        });
    }

    // =================================
    // Group: tạo / sửa / xoá
    // =================================

    // Tạo group mới, người tạo tự động là ADMIN của group
    public void createGroup(final String groupName, final String description,
                            final String coverImageUrl, final String creatorUserId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String groupId = UUID.randomUUID().toString();
                Group group = new Group(groupId, groupName, description, coverImageUrl);
                GroupMember admin = new GroupMember(
                        UUID.randomUUID().toString(), creatorUserId, groupId, GroupRole.ADMIN);

                // Insert group + admin trong cùng 1 transaction (xử lý trong repository)
                groupRepository.createGroupWithAdmin(group, admin);
                groups.postValue(groupRepository.getGroupsOfUser(creatorUserId));
            }
        });
    }

    public void updateGroupInfo(final Group group, final String groupName,
                                final String description, final String coverImageUrl) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                group.updateGroupInfo(groupName, description, coverImageUrl);
                groupRepository.update(group);
                selectedGroup.postValue(group);
            }
        });
    }

    // Xoá group và toàn bộ thành viên của nó
    public void deleteGroup(final Group group) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                groupRepository.deleteGroupWithMembers(group);
                groups.postValue(groupRepository.getAll());
            }
        });
    }

    // =================================
    // GroupMember: thành viên
    // =================================

    public void loadMembers(final String groupId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<GroupMember> memberList = groupMemberRepository.getActiveMembersByGroup(groupId);
                int total = groupMemberRepository.countActiveMembers(groupId);
                members.postValue(memberList);
                memberCount.postValue(total);
            }
        });
    }

    // Tham gia group. Nếu trước đó đã rời group thì kích hoạt lại bản ghi cũ.
    public void joinGroup(final String userId, final String groupId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    // Rời group hoặc admin xoá thành viên (đánh dấu active = false, không xoá hẳn)
    public void removeMember(final String membershipId, final String groupId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                groupMemberRepository.removeMember(membershipId);
                loadMembers(groupId);
            }
        });
    }

    public void changeRole(final String membershipId, final GroupRole newRole,
                           final String groupId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                groupMemberRepository.changeRole(membershipId, newRole);
                loadMembers(groupId);
            }
        });
    }

    // Kiểm tra user có phải admin của group không (để ẩn/hiện nút quản lý)
    public void checkAdmin(final String userId, final String groupId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean result = groupMemberRepository.isAdmin(userId, groupId);
                isAdmin.postValue(result);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}