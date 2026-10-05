package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "group_members",
        foreignKeys = @ForeignKey(
                entity = Group.class,
                parentColumns = "groupId",
                childColumns = "group_id",
                onDelete = ForeignKey.CASCADE   // xoá Group thì xoá luôn thành viên của nó
        ),
        indices = {@Index("group_id"), @Index("user_id")}
)
public class GroupMember {
    @PrimaryKey
    @NonNull
    private String membershipId;

    @ColumnInfo(name = "user_id")
    private String userId;      // id của User (không dùng object User)

    @ColumnInfo(name = "group_id")
    private String groupId;     // id của Group (không dùng object Group)

    @ColumnInfo(name = "role")
    private GroupRole role;

    @ColumnInfo(name = "join_date")
    private Date joinDate;      // cần TypeConverter Date <-> Long

    @ColumnInfo(name = "active")
    private boolean active;     // false = đã bị xoá khỏi group (removeMember)

    public GroupMember(@NonNull String membershipId, String userId, String groupId, GroupRole role) {
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

    @NonNull
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

    public void setMembershipId(@NonNull String membershipId) {
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