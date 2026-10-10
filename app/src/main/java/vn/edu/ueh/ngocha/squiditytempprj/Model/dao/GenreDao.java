package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;

@Dao
public interface GenreDao {

    @Insert
    void insertGenre(Genre genre);

    @Update
    void updateGenreName(Genre genre);

    @Query("DELETE FROM genres WHERE genre_id = :genreId")
    void deleteGenreById(String genreId);

    @Query("SELECT * FROM genres WHERE genre_id = :genreId")
    Genre getGenreById(String genreId);

    @Query("SELECT * FROM genres")
    LiveData<List<Genre>> getAllGenres();

    @Query("DELETE FROM genres")
    void deleteAll();


}
