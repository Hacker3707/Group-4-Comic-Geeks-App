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

    public GroupRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        groupDao = db.groupDao();
        groupMemberDao = db.groupMemberDao();
    }

    public void insert(Group group) {
        groupDao.insert(group);
    }

    public void update(Group group) {
        groupDao.update(group);
    }

    public void delete(Group group) {
        groupDao.delete(group);
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
    public void createGroupWithAdmin(Group group, GroupMember admin) {
        db.runInTransaction(() -> {
            groupDao.insert(group);
            groupMemberDao.insert(admin);
        });
    }

    // Xoá toàn bộ thành viên rồi xoá group trong cùng 1 transaction
    public void deleteGroupWithMembers(Group group) {
        db.runInTransaction(() -> {
            groupMemberDao.deleteByGroupId(group.getGroupId());
            groupDao.delete(group);
        });
    }
}