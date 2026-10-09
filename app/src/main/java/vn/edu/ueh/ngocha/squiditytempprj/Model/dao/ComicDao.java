package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;

@Dao
public interface ComicDao {
    @Insert
    void insertComic(Comic comic);

    @Query("SELECT * FROM comics")
    LiveData<List<Comic>> getAllComics();

    @Delete
    void deleteComic(Comic comic);

    @Query("DELETE FROM comics")
    void deleteAll();

    @Query("SELECT * FROM comics WHERE comic_id = :comicId")
    Comic getComicById(String comicId);

}
