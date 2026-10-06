package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    User getById(String userId);

    @Query("SELECT * FROM users WHERE role = 'USER'")
    List<User> getNormalUsers();

    @Query("SELECT * FROM users WHERE role = 'ADMIN'")
    List<User> getAdmins();

    @Query("SELECT * FROM users WHERE role = 'CREATOR'")
    List<User> getCreators();

    @Query("DELETE FROM users")
    void deleteAll();
}