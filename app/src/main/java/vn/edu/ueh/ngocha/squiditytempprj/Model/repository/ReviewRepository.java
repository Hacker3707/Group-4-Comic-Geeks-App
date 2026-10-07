package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ReviewDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;

public class ReviewRepository {
    public final ReviewDao reviewDao;

    public ReviewRepository(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
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

}
