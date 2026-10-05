package vn.edu.ueh.ngocha.squiditytempprj;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Creator;

@Dao
public interface CreatorDao {

    @Insert
    void insert(Creator creator);

    @Update
    void update(Creator creator);

    @Delete
    void delete(Creator creator);

    @Query("SELECT * FROM creators")
    List<Creator> getAll();

    @Query("SELECT * FROM creators WHERE userId = :userId LIMIT 1")
    Creator getById(String userId);

    @Query("DELETE FROM creators")
    void deleteAll();
}