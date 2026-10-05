package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

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
    void updateGenreName(String genreId, String name);

    @Query("DELETE FROM genres WHERE genreId = :genreId")
    void deleteGenreById(String genreId);

    @Query("SELECT * FROM genres WHERE genreId = :genreId")
    Genre getGenreById(String genreId);

    @Query("SELECT * FROM genres")
    List<Genre> getAllGenres();


}
