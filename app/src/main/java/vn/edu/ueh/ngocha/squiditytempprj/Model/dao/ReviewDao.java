package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;

@Dao
public interface ReviewDao {
    @Insert
    void insertReview(Review review);

    @Query("SELECT * FROM reviews")
    List<Review> getAllReviews();

    @Query("SELECT * FROM reviews WHERE comic_id = :comicId")
    List<Review> getReviewsByComicId(String comicId);

    @Query("DELETE FROM reviews")
    void deleteAll();
}
