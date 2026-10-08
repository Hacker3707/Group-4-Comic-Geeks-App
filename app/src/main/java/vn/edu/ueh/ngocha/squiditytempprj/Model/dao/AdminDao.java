package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

@Dao
public interface AdminDao {

    @Insert
    void insert(User admin);

    @Update
    void update(User admin);

    @Delete
    void delete(User admin);

    @Query("SELECT * FROM users WHERE role = 'ADMIN'")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE userId = :userId AND role = 'ADMIN' LIMIT 1")
    User getById(String userId);

    @Query("DELETE FROM users WHERE role = 'ADMIN'")
    void deleteAll();
}