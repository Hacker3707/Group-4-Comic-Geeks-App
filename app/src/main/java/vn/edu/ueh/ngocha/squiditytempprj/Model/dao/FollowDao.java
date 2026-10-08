package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Follow;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

/**
 * Truy vấn bảng "follows".
 * Tên cột theo @ColumnInfo trong entity Follow / User.
 */
@Dao
public interface FollowDao {

    // Trùng (follower, following) thì bỏ qua và trả về -1, không crash
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Follow follow);

    @Query("DELETE FROM follows WHERE follower_id = :followerId AND following_id = :followingId")
    void unfollow(String followerId, String followingId);

    // followerId có đang follow followingId không
    @Query("SELECT EXISTS(SELECT 1 FROM follows " +
            "WHERE follower_id = :followerId AND following_id = :followingId)")
    boolean isFollowing(String followerId, String followingId);

    // Những người đang follow userId
    @Query("SELECT u.* FROM users u " +
            "INNER JOIN follows f ON u.user_id = f.follower_id " +
            "WHERE f.following_id = :userId")
    List<User> getFollowers(String userId);

    // Những người mà userId đang follow
    @Query("SELECT u.* FROM users u " +
            "INNER JOIN follows f ON u.user_id = f.following_id " +
            "WHERE f.follower_id = :userId")
    List<User> getFollowing(String userId);

    @Query("SELECT COUNT(*) FROM follows WHERE following_id = :userId")
    int countFollowers(String userId);

    @Query("SELECT COUNT(*) FROM follows WHERE follower_id = :userId")
    int countFollowing(String userId);

    @Query("DELETE FROM follows")
    void deleteAll();
}