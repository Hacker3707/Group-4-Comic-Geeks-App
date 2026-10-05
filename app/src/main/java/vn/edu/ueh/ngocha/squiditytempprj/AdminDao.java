package vn.edu.ueh.ngocha.squiditytempprj;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Admin;

@Dao
public interface AdminDao {

    @Insert
    void insert(Admin admin);

    @Update
    void update(Admin admin);

    @Delete
    void delete(Admin admin);

    @Query("SELECT * FROM admins")
    List<Admin> getAll();

    @Query("SELECT * FROM admins WHERE userId = :userId LIMIT 1")
    Admin getById(String userId);

    @Query("DELETE FROM admins")
    void deleteAll();
}