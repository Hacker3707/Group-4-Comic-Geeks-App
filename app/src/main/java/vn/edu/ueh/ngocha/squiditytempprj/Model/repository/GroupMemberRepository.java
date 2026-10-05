package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupMemberDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;

public class GroupMemberRepository {

    private final GroupMemberDao groupMemberDao;

    public GroupMemberRepository(GroupMemberDao groupMemberDao) {
        this.groupMemberDao = groupMemberDao;
    }

    public void insert(GroupMember member) {
        groupMemberDao.insert(member);
    }

    public void update(GroupMember member) {
        groupMemberDao.update(member);
    }

    public void delete(GroupMember member) {
        groupMemberDao.delete(member);
    }

    public List<GroupMember> getAll() {
        return groupMemberDao.getAll();
    }

    public GroupMember getById(String membershipId) {
        return groupMemberDao.getById(membershipId);
    }

    public List<GroupMember> getActiveMembersByGroup(String groupId) {
        return groupMemberDao.getActiveMembersByGroup(groupId);
    }

    public List<GroupMember> getActiveMembershipsOfUser(String userId) {
        return groupMemberDao.getActiveMembershipsOfUser(userId);
    }

    public GroupMember getMembership(String userId, String groupId) {
        return groupMemberDao.getMembership(userId, groupId);
    }

    public void changeRole(String membershipId, GroupRole newRole) {
        groupMemberDao.updateRole(membershipId, newRole);
    }

    public void removeMember(String membershipId) {
        groupMemberDao.deactivate(membershipId);
    }

    public boolean isAdmin(String userId, String groupId) {
        return groupMemberDao.hasRole(userId, groupId, GroupRole.ADMIN);
    }

    public int countActiveMembers(String groupId) {
        return groupMemberDao.countActiveMembers(groupId);
    }

    public void deleteByGroupId(String groupId) {
        groupMemberDao.deleteByGroupId(groupId);
    }

    public void deleteAll() {
        groupMemberDao.deleteAll();
    }
}