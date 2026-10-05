package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;

/**
 * Truy vấn bảng "group_members".
 * Lưu ý: GroupMember có Date (joinDate) nên cần TypeConverter ở AppRoomDatabase.
 * GroupRole là enum, Room 2.3+ tự lưu được.
 */
@Dao
public interface GroupMemberDao {

    @Insert
    void insert(GroupMember member);

    @Update
    void update(GroupMember member);

    @Delete
    void delete(GroupMember member);

    @Query("SELECT * FROM group_members")
    List<GroupMember> getAll();

    @Query("SELECT * FROM group_members WHERE membershipId = :membershipId LIMIT 1")
    GroupMember getById(String membershipId);

    // Thành viên còn hoạt động của một group
    @Query("SELECT * FROM group_members WHERE group_id = :groupId AND active = 1")
    List<GroupMember> getActiveMembersByGroup(String groupId);

    // Tất cả bản ghi tham gia group của một user
    @Query("SELECT * FROM group_members WHERE user_id = :userId AND active = 1")
    List<GroupMember> getActiveMembershipsOfUser(String userId);

    // Tìm bản ghi của 1 user trong 1 group
    @Query("SELECT * FROM group_members WHERE user_id = :userId AND group_id = :groupId LIMIT 1")
    GroupMember getMembership(String userId, String groupId);

    // changeRole()
    @Query("UPDATE group_members SET role = :newRole WHERE membershipId = :membershipId")
    void updateRole(String membershipId, GroupRole newRole);

    // removeMember(): đánh dấu active = false, không xoá hẳn khỏi DB
    @Query("UPDATE group_members SET active = 0 WHERE membershipId = :membershipId")
    void deactivate(String membershipId);

    @Query("SELECT EXISTS(SELECT 1 FROM group_members " +
            "WHERE user_id = :userId AND group_id = :groupId AND role = :role AND active = 1)")
    boolean hasRole(String userId, String groupId, GroupRole role);

    @Query("SELECT COUNT(*) FROM group_members WHERE group_id = :groupId AND active = 1")
    int countActiveMembers(String groupId);

    // Dùng khi xoá một group (xoá luôn thành viên của nó)
    @Query("DELETE FROM group_members WHERE group_id = :groupId")
    void deleteByGroupId(String groupId);

    @Query("DELETE FROM group_members")
    void deleteAll();
}