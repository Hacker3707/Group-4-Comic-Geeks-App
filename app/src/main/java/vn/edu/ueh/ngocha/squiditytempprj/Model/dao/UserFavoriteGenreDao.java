
package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.UserFavoriteGenre;

@Dao
public interface UserFavoriteGenreDao {

    @Query("SELECT genre_id FROM user_favorite_genres WHERE user_id = :userId")
    LiveData<List<String>> getFavoriteGenreIds(String userId);

    @Query("SELECT genre_id FROM user_favorite_genres WHERE user_id = :userId")
    List<String> getFavoriteGenreIdsNow(String userId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<UserFavoriteGenre> favorites);

    @Query("DELETE FROM user_favorite_genres WHERE user_id = :userId")
    void deleteByUserId(String userId);

    @Query("DELETE FROM user_favorite_genres WHERE user_id = :userId AND genre_id = :genreId")
    void deleteOne(String userId, String genreId);
}
