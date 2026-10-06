package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;

public class GroupRepository {

    private final GroupDao groupDao;

    public GroupRepository(GroupDao groupDao) {
        this.groupDao = groupDao;
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
}