package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ReviewDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;

public class ReviewRepository {
    public final ReviewDao reviewDao;
    public final LiveData<List<Review>> allReviews;

    public ReviewRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.reviewDao = db.reviewDao();
        allReviews = reviewDao.getAllReviews();
    }

    public void insert(Review review) {
        reviewDao.insertReview(review);
    }

    public void getAll() {
        reviewDao.getAllReviews();
    }

    public void deleteAll() {
        reviewDao.deleteAll();
    }
    public List<Review> getReviewsByComicId(String comicId) {
        return reviewDao.getReviewsByComicId(comicId);
    }
    public LiveData<List<Review>> getAllReviews() {
        return reviewDao.getAllReviews();
    }

}
