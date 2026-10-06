package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "comics")
public class Comic {

    @PrimaryKey
    @NonNull
    String comicId;
    @ColumnInfo (name = "comic_name")
    String comicName;
    @ColumnInfo (name = "comic_author")
    String comicAuthor;
    @ColumnInfo (name = "comic_description")
    String comicDescription;
    @ColumnInfo (name = "cover_image_url")
    String coverImageurl;
    @ColumnInfo (name = "comic_status")
    String comicStatus;
    @ColumnInfo (name = "comic_genres")
    String comicGenres;
    @ColumnInfo (name = "comic_rating")
    String comicRating;

    public Comic(String comicId, String comicName, String comicAuthor, String comicDescription, String coverImageurl,
                 String comicStatus, String comicGenres, String comicRating) {
        this.comicId = comicId;
        this.comicName = comicName;
        this.comicAuthor = comicAuthor;
        this.comicDescription = comicDescription;
        this.coverImageurl = coverImageurl;
        this.comicStatus = comicStatus;
        this.comicGenres = comicGenres;
        this.comicRating = comicRating;
    }

    // =================================
    // Getter methods for each field
    // =================================

    public String getComicId() {
        return comicId;
    }

    public String getComicName() {
        return comicName;
    }

    public String getComicAuthor() {
        return comicAuthor;
    }

    public String getComicDescription() {
        return comicDescription;
    }

    public String getCoverImageurl() {
        return coverImageurl;
    }

    public String getComicStatus() {
        return comicStatus;
    }

    public String getComicGenres() {
        return comicGenres;
    }

    public String getComicRating() {
        return comicRating;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setComicId(String comicId) {
        this.comicId = comicId;
    }

    public void setComicName(String comicName) {
        this.comicName = comicName;
    }

    public void setComicAuthor(String comicAuthor) {
        this.comicAuthor = comicAuthor;
    }

    public void setComicDescription(String comicDescription) {
        this.comicDescription = comicDescription;
    }

    public void setCoverImageurl(String coverImageurl) {
        this.coverImageurl = coverImageurl;
    }

    public void setComicStatus(String comicStatus) {
        this.comicStatus = comicStatus;
    }

    public void setComicGenres(String comicGenres) {
        this.comicGenres = comicGenres;
    }

    public void setComicRating(String comicRating) {
        this.comicRating = comicRating;
    }

}
