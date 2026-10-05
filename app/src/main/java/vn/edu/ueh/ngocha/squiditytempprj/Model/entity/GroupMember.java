package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import java.util.Date;

public class GroupMember {
    private String membershipId;
    private String userId;      // id của User (không dùng object User)
    private String groupId;     // id của Group (không dùng object Group)
    private GroupRole role;
    private Date joinDate;
    private boolean active;     // false = đã bị xoá khỏi group (removeMember)

    public GroupMember(String membershipId, String userId, String groupId, GroupRole role) {
        this.membershipId = membershipId;
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
        this.joinDate = new Date();
        this.active = true;
    }

    // =================================
    // Methods theo class diagram
    // =================================

    public void changeRole(GroupRole newRole) {
        this.role = newRole;
    }

    public void removeMember() {
        this.active = false;
    }

    // =================================
    // Getter methods for each field
    // =================================

    public String getMembershipId() {
        return membershipId;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public GroupRole getRole() {
        return role;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public boolean isActive() {
        return active;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}