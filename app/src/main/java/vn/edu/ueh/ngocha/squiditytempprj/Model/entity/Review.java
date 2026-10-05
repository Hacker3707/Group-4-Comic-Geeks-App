package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "reviews")
public class Review {
    @PrimaryKey (autoGenerate = true)
    @NonNull
    private String reviewId;
    @ColumnInfo (name = "comic_id")
    private String comicId;
    @ColumnInfo(name = "user_id")
    private String userId;
    @ColumnInfo (name = "review_text")
    private String reviewText;
    @ColumnInfo (name = "review_rating")
    private String reviewRating;
    @ColumnInfo (name = "review_date")
    private String reviewDate;

    public Review(String reviewId, String comicId, String userId,
                  String reviewText, String reviewRating, String reviewDate) {
        this.reviewId = reviewId;
        this.comicId = comicId;
        this.userId = userId;
        this.reviewText = reviewText;
        this.reviewRating = reviewRating;
        this.reviewDate = reviewDate;
    }

    // =================================
    // Getter methods for each field
    // =================================

    public String getReviewId() {
        return reviewId;
    }

    public String getComicId() {
        return comicId;
    }

    public String getUserId() {
        return userId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getReviewRating() {
        return reviewRating;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public void setComicId(String comicId) {
        this.comicId = comicId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setReviewRating(String reviewRating) {
        this.reviewRating = reviewRating;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

}
