package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;

@Dao
public interface StampDao {
    @Insert
    void insertStamp(Stamp stamp);

    @Update
    void updateStamp(Stamp stamp);

    @Delete
    void deleteStamp(Stamp stamp);

    @Query("SELECT * FROM stamps WHERE post_id = :postId")
    LiveData<List<Stamp>> getStampsByPost(String postId);
}