package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comics")
public class Comic {

    // MangaDex manga UUID
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "comic_id")
    private String comicId;

    // MangaDex: attributes.title
    @ColumnInfo(name = "comic_name")
    private String comicName;

    // MangaDex: relationship type = author
    @ColumnInfo(name = "comic_author")
    private String comicAuthor;

    // MangaDex: attributes.description
    @ColumnInfo(name = "comic_description")
    private String comicDescription;

    // URL generated from MangaDex cover_art
    @ColumnInfo(name = "cover_image_url")
    private String coverImageUrl;

    // MangaDex: attributes.status
    // Example: ongoing, completed, hiatus
    @ColumnInfo(name = "comic_status")
    private String comicStatus;

    // MangaDex: tags where group = genre
    @ColumnInfo(name = "comic_genres")
    private String comicGenres;

    // MangaDex: attributes.contentRating
    // Example: safe, suggestive, erotica, pornographic
    @ColumnInfo(name = "content_rating")
    private String contentRating;

    // MangaDex: attributes.originalLanguage
    // Example: ja, en, ko
    @ColumnInfo(name = "original_language")
    private String originalLanguage;

    // MangaDex: attributes.year
    @ColumnInfo(name = "release_year")
    private Integer releaseYear;

    // MangaDex: attributes.lastVolume
    @ColumnInfo(name = "last_volume")
    private String lastVolume;

    // MangaDex: attributes.lastChapter
    @ColumnInfo(name = "last_chapter")
    private String lastChapter;


    public Comic(
            @NonNull String comicId,
            String comicName,
            String comicAuthor,
            String comicDescription,
            String coverImageUrl,
            String comicStatus,
            String comicGenres,
            String contentRating,
            String originalLanguage,
            Integer releaseYear,
            String lastVolume,
            String lastChapter
    ) {
        this.comicId = comicId;
        this.comicName = comicName;
        this.comicAuthor = comicAuthor;
        this.comicDescription = comicDescription;
        this.coverImageUrl = coverImageUrl;
        this.comicStatus = comicStatus;
        this.comicGenres = comicGenres;
        this.contentRating = contentRating;
        this.originalLanguage = originalLanguage;
        this.releaseYear = releaseYear;
        this.lastVolume = lastVolume;
        this.lastChapter = lastChapter;
    }


    // =================================
    // Getter methods
    // =================================

    @NonNull
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getComicStatus() {
        return comicStatus;
    }

    public String getComicGenres() {
        return comicGenres;
    }

    public String getContentRating() {
        return contentRating;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public String getLastVolume() {
        return lastVolume;
    }

    public String getLastChapter() {
        return lastChapter;
    }


    // =================================
    // Setter methods
    // =================================

    public void setComicId(@NonNull String comicId) {
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

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setComicStatus(String comicStatus) {
        this.comicStatus = comicStatus;
    }

    public void setComicGenres(String comicGenres) {
        this.comicGenres = comicGenres;
    }

    public void setContentRating(String contentRating) {
        this.contentRating = contentRating;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setLastVolume(String lastVolume) {
        this.lastVolume = lastVolume;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }
}
