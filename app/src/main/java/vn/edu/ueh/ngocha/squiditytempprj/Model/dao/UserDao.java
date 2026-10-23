package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    // Thêm mới hoặc ghi đè nếu đã có (dùng khi tải thông tin người dùng từ Firestore)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    User getById(String userId);

    @Query("SELECT * FROM users WHERE role = 'USER'")
    LiveData<List<User>> getNormalUsers();

    @Query("SELECT * FROM users WHERE role = 'ADMIN'")
    LiveData<List<User>> getAdmins();

    @Query("SELECT * FROM users WHERE role = 'CREATOR'")
    LiveData<List<User>> getCreators();

    @Query("SELECT * FROM users WHERE user_id IN (:userIds)")
    LiveData<List<User>> getUsersByIds(List<String> userIds);

    @Query("SELECT DISTINCT u.* " +
            "FROM users AS u " +
            "INNER JOIN reviews AS r " +
            "ON u.user_id = r.user_id " +
            "WHERE r.comic_id = :comicId")
    LiveData<List<User>> getUsersByComicId(String comicId);


    @Query("DELETE FROM users")
    void deleteAll();

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);
}