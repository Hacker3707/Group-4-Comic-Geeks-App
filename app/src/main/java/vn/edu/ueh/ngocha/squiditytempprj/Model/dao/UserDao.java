package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
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
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    User getById(String userId);

    @Query("SELECT * FROM users WHERE role = 'USER'")
    LiveData<List<User>> getNormalUsers();

    @Query("SELECT * FROM users WHERE role = 'ADMIN'")
    LiveData<List<User>> getAdmins();

    @Query("SELECT * FROM users WHERE role = 'CREATOR'")
    LiveData<List<User>> getCreators();

    @Query("DELETE FROM users")
    void deleteAll();
}