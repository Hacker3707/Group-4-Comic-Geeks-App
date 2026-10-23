package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupMemberDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;

public class GroupRepository {

    private final AppDatabase db;
    private final GroupDao groupDao;
    private final GroupMemberDao groupMemberDao;
    private final GroupSyncManager syncManager;

    public GroupRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        syncManager = GroupSyncManager.getInstance(application);
        groupDao = db.groupDao();
        groupMemberDao = db.groupMemberDao();
    }

    public void insert(Group group) {
        groupDao.insert(group);
        syncManager.pushGroup(group);
    }

    public void update(Group group) {
        groupDao.update(group);
        syncManager.pushGroup(group);
    }

    public void delete(Group group) {
        groupDao.delete(group);
        syncManager.deleteGroupRemote(group.getGroupId());
    }

    public List<Group> getAll() {
        return groupDao.getAll();
    }

    public Group getById(String groupId) {
        return groupDao.getById(groupId);
    }

    public List<Group> searchByName(String keyword) {
        return groupDao.searchByName(keyword);
    }

    public List<Group> getGroupsOfUser(String userId) {
        return groupDao.getGroupsOfUser(userId);
    }

    public void deleteAll() {
        groupDao.deleteAll();
    }

    // Tạo group và thêm người tạo làm ADMIN trong cùng 1 transaction
    // (insert group trước vì GroupMember có foreign key tới Group)
    public void createGroupWithAdmin(final Group group, final GroupMember admin) {
        db.runInTransaction(new Runnable() {
            @Override
            public void run() {
                groupDao.insert(group);
                groupMemberDao.insert(admin);
            }
        });
        // Đưa lên Firestore để tài khoản/máy khác cũng thấy nhóm này
        syncManager.pushGroup(group);
        syncManager.pushMember(admin);
    }

    // Xoá toàn bộ thành viên rồi xoá group trong cùng 1 transaction
    public void deleteGroupWithMembers(final Group group) {
        db.runInTransaction(new Runnable() {
            @Override
            public void run() {
                groupMemberDao.deleteByGroupId(group.getGroupId());
                groupDao.delete(group);
            }
        });
        syncManager.deleteGroupRemote(group.getGroupId());
    }
}