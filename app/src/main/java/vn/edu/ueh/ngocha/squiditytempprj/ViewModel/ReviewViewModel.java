package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ReviewRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserRepository;

public class ReviewViewModel extends AndroidViewModel {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    public final LiveData<List<Review>> allReviews;
    public final LiveData<List<User>> allUsers;

    public ReviewViewModel(@NonNull Application application) {
        super(application);

        this.reviewRepository = new ReviewRepository(application);
        this.userRepository = new UserRepository(application);
        this.allReviews = reviewRepository.getAllReviews();
        this.allUsers = userRepository.getAllUsers();
    }

    public LiveData<List<Review>> getAllReviews() {
        return allReviews;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<Review>> getReviewsByComicId(String comicId) {
        return reviewRepository.getReviewsByComicIdLive(comicId);
    }

    public LiveData<List<User>> getUsersByIds(List<String> userIds) {
        return userRepository.getUsersByIds(userIds);
    }

    public LiveData<List<User>> getUsersForComic(String comicId) {
        MediatorLiveData<List<User>> result =
                new MediatorLiveData<>();

        LiveData<List<Review>> reviewsLiveData =
                reviewRepository.getReviewsByComicIdLive(comicId);

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
                    return;
                }

                Set<String> uniqueIds = new LinkedHashSet<>();

                for (Review review : reviews) {
                    String userId = review.getUserId();

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

    public void insert(User user) {
        userRepository.insert(user);
    }

    public void insert(Review review) {
        reviewRepository.insert(review);
    }

}
