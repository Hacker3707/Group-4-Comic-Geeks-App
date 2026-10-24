package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

@Entity (tableName = "reviews")
public class Review {
    @PrimaryKey
    @NonNull
    @ColumnInfo (name = "review_id")
    private String reviewId;
    @ColumnInfo (name = "comic_id")
    private String comicId;
    @ColumnInfo(name = "user_id")
    private String userId;
    @ColumnInfo (name = "review_text")
    private String reviewText;
    @ColumnInfo (name = "review_rating")
    private float reviewRating;
    @ColumnInfo (name = "review_date")
    @TypeConverters(Converters.class)
    private Date reviewDate;

    public Review(@NonNull String reviewId, @NonNull String comicId,@NonNull String userId,
                  String reviewText, float reviewRating, Date reviewDate) {
        this.reviewId = reviewId;
        this.comicId = comicId;
        this.userId = userId;
        this.reviewText = reviewText;
        this.reviewRating = reviewRating;
        this.reviewDate = reviewDate;
    }

    @Ignore
    public Review(String comicId, String userId, String reviewText, float reviewRating) {
        this.comicId = comicId;
        this.userId = userId;
        this.reviewText = reviewText;
        this.reviewRating = reviewRating;
    }

    @Ignore
    public Review(String comicId, String userId, String reviewText) {
        this.comicId = comicId;
        this.userId = userId;
        this.reviewText = reviewText;
    }

    @Ignore
    public Review() {
    }



    // =================================
    // Getter methods for each field
    // =================================

    @NonNull
    public String getReviewId() {
        return reviewId;
    }
    @NonNull
    public String getComicId() {
        return comicId;
    }
    @NonNull
    public String getUserId() {
        return userId;
    }
    public String getReviewText() {
        return reviewText;
    }
    public float getReviewRating() {
        return reviewRating;
    }
    public Date getReviewDate() {
        return reviewDate;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setReviewId(@NonNull String reviewId) {
        this.reviewId = reviewId;
    }

    public void setComicId(@NonNull String comicId) {
        this.comicId = comicId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    public void setReviewRating(float reviewRating) {
        this.reviewRating = reviewRating;
    }
    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

}
