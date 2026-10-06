package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;

/**
 * Truy vấn bảng "groups". Tên bảng được bọc trong dấu ` ` để tránh trùng từ khoá SQL.
 * Tên cột theo @ColumnInfo trong entity Group / GroupMember.
 */
@Dao
public interface GroupDao {

    @Insert
    void insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT * FROM `groups`")
    List<Group> getAll();

    @Query("SELECT * FROM `groups` WHERE groupId = :groupId LIMIT 1")
    Group getById(String groupId);

    @Query("SELECT * FROM `groups` WHERE group_name LIKE '%' || :keyword || '%'")
    List<Group> searchByName(String keyword);

    // Các group mà một user đang tham gia (chỉ tính thành viên còn active)
    @Query("SELECT g.* FROM `groups` g " +
            "INNER JOIN group_members gm ON g.groupId = gm.group_id " +
            "WHERE gm.user_id = :userId AND gm.active = 1")
    List<Group> getGroupsOfUser(String userId);

    @Query("DELETE FROM `groups`")
    void deleteAll();
}