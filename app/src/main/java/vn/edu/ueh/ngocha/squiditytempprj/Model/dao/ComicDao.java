package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;

@Dao
public interface ComicDao {
    @Insert
    void insertComic(Comic comic);

    @Query("SELECT * FROM comics")
    void getAllComics();

}
