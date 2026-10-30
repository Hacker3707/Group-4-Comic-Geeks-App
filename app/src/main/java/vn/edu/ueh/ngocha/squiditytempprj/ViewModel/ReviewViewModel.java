package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.AuthRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MangaDexRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ReviewRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserRepository;

public class ReviewViewModel extends AndroidViewModel {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    public final LiveData<List<User>> allUsers;
    public final LiveData<Float> averageRating;
    public final MediatorLiveData<Boolean> isSameUser;

    private final MutableLiveData<List<Review>> reviews =
            new MutableLiveData<>();

    private LiveData<List<Review>> currentReviews;

    private String currentComicId;
    private String currentFilter;

    public ReviewViewModel(@NonNull Application application) {
        super(application);

        this.reviewRepository = new ReviewRepository(application);
        this.userRepository = new UserRepository(application);
        this.authRepository = new AuthRepository();
        this.allUsers = userRepository.getAllUsers();
        this.averageRating = reviewRepository.getAverageRatingLive(null);
        this.isSameUser = new MediatorLiveData<>();
    }


    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<Review>> getReviewsByComicId(String comicId, String filter) {
        return reviewRepository.getReviewsByComicIdLive(comicId, filter);
    }

    public LiveData<List<User>> getUsersForComic(String comicId) {
        MediatorLiveData<List<User>> result =
                new MediatorLiveData<>();

        LiveData<List<Review>> reviewsLiveData =
                reviewRepository.getReviewsByComicIdLive(comicId, "newest");

        result.addSource(reviewsLiveData, new Observer<List<Review>>() {
            private LiveData<List<User>> currentUsersLiveData;

            @Override
            public void onChanged(List<Review> reviews) {
                if (currentUsersLiveData != null) {
                    result.removeSource(currentUsersLiveData);
                    currentUsersLiveData = null;
                }

                if (reviews == null || reviews.isEmpty()) {
                    result.setValue(new ArrayList<User>());
                    isSameUser.postValue(false);
                    return;
                }

                Set<String> uniqueIds = new LinkedHashSet<>();

                isSameUser.postValue(false);

                for (Review review : reviews) {
                    String userId = review.getUserId();

                    if (userId != null &&
                            userId.equals(authRepository.getCurrentUserId())) {
                        isSameUser.postValue(true);
                    }

                    if (userId != null && !userId.isEmpty()) {
                        uniqueIds.add(userId);
                    }
                }

                if (uniqueIds.isEmpty()) {
                    result.setValue(new ArrayList<User>());
                    return;
                }

                List<String> userIds = new ArrayList<>(uniqueIds);

                currentUsersLiveData =
                        userRepository.getUsersByIds(userIds);

                result.addSource(currentUsersLiveData,
                        new Observer<List<User>>() {
                            @Override
                            public void onChanged(List<User> users) {
                                result.setValue(users);
                            }
                        });
            }
        });

        return result;
    }

    public LiveData<Float> getAverageRating(String comicId) {
        return reviewRepository.getAverageRatingLive(comicId);
    }

    public LiveData<Review> getReview(String comicId, String userId) {
        return reviewRepository.getReview(comicId, userId);
    }

    public void delete(Review review) {
        reviewRepository.delete(review);
    }

    public void update(Review review) {
        reviewRepository.update(review);
    }

    public void insert(User user) {
        userRepository.insert(user);
    }

    public void insert(Review review) {
        reviewRepository.insert(review);
    }

    public LiveData<List<Review>> getAllReviewsforUserId(String userId) {
        return reviewRepository.getReviewsByUserId(userId);
    }

    public void loadReviews(String comicId, String filter) {

        if (comicId.equals(currentComicId)
                && filter.equals(currentFilter)) {
            return;
        }

        currentComicId = comicId;
        currentFilter = filter;

        if (currentReviews != null) {
            currentReviews.removeObserver(reviewObserver);
        }

        currentReviews =
                reviewRepository.getReviewsByComicIdLive(
                        comicId, filter
                );

        currentReviews.observeForever(reviewObserver);
    }

    private final Observer<List<Review>> reviewObserver =
            new Observer<List<Review>>() {
                @Override
                public void onChanged(List<Review> reviewList) {
                    reviews.setValue(reviewList);
                }
            };

    public LiveData<List<Review>> getReview() {
        return reviews;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (currentReviews != null) {
            currentReviews.removeObserver(reviewObserver);
        }
    }

}
