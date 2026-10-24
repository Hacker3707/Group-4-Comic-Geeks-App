
package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(
        tableName = "user_favorite_genres",
        primaryKeys = {"user_id", "genre_id"}
)
public class UserFavoriteGenre {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "genre_id")
    private String genreId;

    public UserFavoriteGenre(
            @NonNull String userId,
            @NonNull String genreId
    ) {
        this.userId = userId;
        this.genreId = genreId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getGenreId() {
        return genreId;
    }

    public void setGenreId(@NonNull String genreId) {
        this.genreId = genreId;
    }
}
