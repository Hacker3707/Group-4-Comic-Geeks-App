package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;

@Dao
public interface ReviewDao {

    @Insert
    void insertReview(Review review);

    @Upsert
    void insertorUpdateReview(Review review);

    @Query("SELECT * FROM reviews")
    LiveData<List<Review>> getAllReviews();

    @Query("SELECT * FROM reviews WHERE comic_id = :comicId")
    LiveData<List<Review>> getReviewsByComicId(String comicId);

    // Newest
    @Query("SELECT * FROM reviews WHERE comic_id = :comicId ORDER BY review_date DESC")
    LiveData<List<Review>> getReviewsNewest(String comicId);

    // Oldest
    @Query("SELECT * FROM reviews WHERE comic_id = :comicId ORDER BY review_date ASC")
    LiveData<List<Review>> getReviewsOldest(String comicId);

    // Highest Rating
    @Query("SELECT * FROM reviews WHERE comic_id = :comicId ORDER BY review_rating DESC")
    LiveData<List<Review>> getReviewsHighestRating(String comicId);

    // Lowest Rating
    @Query("SELECT * FROM reviews WHERE comic_id = :comicId ORDER BY review_rating ASC")
    LiveData<List<Review>> getReviewsLowestRating(String comicId);

    @Query("DELETE FROM reviews")
    void deleteAll();

    @Query("DELETE FROM reviews WHERE review_id = :reviewId")
    void deleteReviewById(String reviewId);
}

