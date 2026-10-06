package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

@Dao
public interface CreatorDao {

    @Insert
    void insert(User creator);

    @Update
    void update(User creator);

    @Delete
    void delete(User creator);

    @Query("SELECT * FROM users WHERE role = 'CREATOR'")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE userId = :userId AND role = 'CREATOR' LIMIT 1")
    User getById(String userId);

    @Query("DELETE FROM users WHERE role = 'CREATOR'")
    void deleteAll();
}