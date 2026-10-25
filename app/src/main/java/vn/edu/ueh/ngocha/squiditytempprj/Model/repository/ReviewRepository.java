package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ReviewDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;

public class ReviewRepository {
    public final ReviewDao reviewDao;
    public final LiveData<List<Review>> allReviews;
    private final FirebaseFirestore firestore;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ReviewRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        firestore = FirebaseFirestore.getInstance();
        this.reviewDao = db.reviewDao();
        allReviews = reviewDao.getAllReviews();
    }


    public void insert(Review review) {
        Map<String, Object> reviewData = new HashMap<>();

        String comicId = review.getComicId();
        String userId = review.getUserId();
        String reviewId = comicId + "_" + userId;

        reviewData.put("comicId", comicId);
        reviewData.put("userId", userId);
        reviewData.put("reviewText", review.getReviewText());
        reviewData.put("reviewRating", review.getReviewRating());
        reviewData.put("reviewDate", FieldValue.serverTimestamp());

        firestore.collection("reviews")
                .document(reviewId)
                .set(reviewData)
                .addOnSuccessListener(unused -> {
                    Log.d("ReviewRepository",
                            "Review saved: " + reviewId);
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewRepository",
                            "Error saving review", e);
                });
    }

    public void update(Review review) {
        Map<String, Object> reviewData = new HashMap<>();

        reviewData.put("reviewText", review.getReviewText());
        reviewData.put("reviewRating", review.getReviewRating());
        reviewData.put("reviewDate", FieldValue.serverTimestamp());

        String reviewId =
                review.getComicId() + "_" + review.getUserId();

        firestore.collection("reviews")
                .document(reviewId)
                .update(reviewData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ReviewRepository",
                                "Review updated with ID: " + review.getReviewId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ReviewRepository",
                                "Error updating review", e);
                    }
                });

    }

    public LiveData<Review> getReview(
            String comicId, String userId) {

        return new LiveData<Review>() {
            private ListenerRegistration reviewListener;

            @Override
            protected void onActive() {
                super.onActive();

                if (reviewListener != null) return;

                String reviewId = comicId + "_" + userId;

                reviewListener = firestore.collection("reviews")
                        .document(reviewId)
                        .addSnapshotListener((document, error) -> {

                            if (error != null) {
                                Log.e("ReviewRepository",
                                        "Error getting review", error);
                                return;
                            }

                            if (document == null || !document.exists()) {
                                postValue(null);
                                return;
                            }

                            Review review = document.toObject(Review.class);

                            Object reviewDate = document.get("reviewDate");
                            if (reviewDate instanceof com.google.firebase.Timestamp) {
                                review.setReviewDate(
                                        ((com.google.firebase.Timestamp) reviewDate).toDate()
                                );
                            }

                            if (review != null) {
                                review.setReviewId(document.getId());
                                postValue(review);
                            }

                        });
            }

            @Override
            protected void onInactive() {
                super.onInactive();

                if (reviewListener != null) {
                    reviewListener.remove();
                    reviewListener = null;
                }
            }
        };
    }

    public LiveData<List<Review>> getAllReviews() {
        return reviewDao.getAllReviews();
    }

    public void deleteAll() {
        reviewDao.deleteAll();
    }

    public LiveData<List<Review>> getReviewsByComicIdLive(String comicId) {
        return new LiveData<List<Review>>() {
            private ListenerRegistration reviewListener;

            @Override
            protected void onActive() {
                super.onActive();

                if (reviewListener == null) {
                    reviewListener = firestore.collection("reviews")
                            .whereEqualTo("comicId", comicId)
                            .addSnapshotListener(
                                    new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(
                                                @Nullable QuerySnapshot snapshots,
                                                @Nullable FirebaseFirestoreException error) {

                                            if (error != null) {
                                                Log.e("ReviewRepository",
                                                        "Error getting reviews",
                                                        error);
                                                return;
                                            }

                                            if (snapshots == null) {
                                                return;
                                            }

                                            List<Review> reviews = new ArrayList<>();

                                            for (QueryDocumentSnapshot document : snapshots) {
                                                Map<String, Object> data = document.getData();

                                                Review review = new Review(
                                                        data.get("comicId").toString(),
                                                        data.get("userId").toString(),
                                                        data.get("reviewText").toString()
                                                );

                                                float rating = 0F;
                                                if (data.get("reviewRating") != null) {
                                                    rating = Float.parseFloat(
                                                            data.get("reviewRating").toString()
                                                    );
                                                }

                                                review.setReviewRating(rating);
                                                review.setReviewText(document.getString("reviewText"));
                                                review.setReviewId(document.getId());

                                                Object reviewDate = data.get("reviewDate");
                                                if (reviewDate instanceof com.google.firebase.Timestamp) {
                                                    review.setReviewDate(
                                                            ((com.google.firebase.Timestamp) reviewDate).toDate()
                                                    );
                                                }

                                                reviews.add(review);
                                            }

                                            postValue(reviews);

                                            executorService.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (Review review : reviews) {
                                                        reviewDao.insertorUpdateReview(review);
                                                    }
                                                }
                                            });
                                        }
                                    }
                            );
                }
            }

            @Override
            protected void onInactive() {
                super.onInactive();

                if (reviewListener != null) {
                    reviewListener.remove();
                    reviewListener = null;
                }
            }

        };
    }


    public LiveData<Float> getAverageRatingLive(String comicId) {
        return new LiveData<Float>() {

            private ListenerRegistration ratingListener;

            @Override
            protected void onActive() {
                super.onActive();

                if (ratingListener == null) {
                    ratingListener = firestore.collection("reviews")
                            .whereEqualTo("comicId", comicId)
                            .addSnapshotListener(
                                    new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(
                                                @Nullable QuerySnapshot snapshots,
                                                @Nullable FirebaseFirestoreException error) {

                                            if (error != null) {
                                                Log.e(
                                                        "ReviewRepository",
                                                        "Error getting average rating",
                                                        error
                                                );
                                                return;
                                            }

                                            if (snapshots == null) {
                                                return;
                                            }

                                            float totalRating = 0f;
                                            int count = 0;

                                            for (QueryDocumentSnapshot document
                                                    : snapshots) {

                                                Object ratingValue =
                                                        document.get("reviewRating");

                                                if (ratingValue instanceof Number) {
                                                    float rating =
                                                            ((Number) ratingValue)
                                                                    .floatValue();

                                                    totalRating += rating;
                                                    count++;
                                                }
                                            }

                                            float averageRating = 0f;

                                            if (count > 0) {
                                                averageRating =
                                                        totalRating / count;
                                            }

                                            postValue(averageRating);
                                        }
                                    }
                            );
                }
            }

            @Override
            protected void onInactive() {
                super.onInactive();

                if (ratingListener != null) {
                    ratingListener.remove();
                    ratingListener = null;
                }
            }
        };
    }

    public void delete (Review review) {
        String reviewId = review.getComicId() + "_" + review.getUserId();
        firestore.collection("reviews")
                .document(reviewId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d("ReviewRepository",
                            "Review deleted: " + reviewId);
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewRepository",
                            "Error deleting review", e);
                });

    }


    public LiveData<List<Review>> getReviewsByUserId(String userId) {
        return new LiveData<List<Review>>() {

            private ListenerRegistration reviewListener;

            @Override
            protected void onActive() {
                super.onActive();

                if (userId == null || userId.trim().isEmpty()) {
                    postValue(new ArrayList<Review>());
                    return;
                }

                if (reviewListener != null) {
                    return;
                }

                reviewListener = firestore.collection("reviews")
                        .whereEqualTo("userId", userId)
                        .addSnapshotListener(
                                new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(
                                            @Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException error) {

                                        if (error != null) {
                                            Log.e(
                                                    "ReviewRepository",
                                                    "Error getting reviews by user ID",
                                                    error
                                            );
                                            return;
                                        }

                                        if (snapshots == null) {
                                            postValue(new ArrayList<Review>());
                                            return;
                                        }

                                        List<Review> reviews = new ArrayList<>();

                                        for (QueryDocumentSnapshot document : snapshots) {
                                            String comicId =
                                                    document.getString("comicId");

                                            String reviewUserId =
                                                    document.getString("userId");

                                            String reviewText =
                                                    document.getString("reviewText");

                                            if (comicId == null || reviewUserId == null) {
                                                continue;
                                            }

                                            Review review = new Review(
                                                    comicId,
                                                    reviewUserId,
                                                    reviewText == null ? "" : reviewText
                                            );

                                            review.setReviewId(document.getId());

                                            Object ratingValue =
                                                    document.get("reviewRating");

                                            if (ratingValue instanceof Number) {
                                                review.setReviewRating(
                                                        ((Number) ratingValue).floatValue()
                                                );
                                            } else if (ratingValue != null) {
                                                try {
                                                    review.setReviewRating(
                                                            Float.parseFloat(
                                                                    ratingValue.toString()
                                                            )
                                                    );
                                                } catch (NumberFormatException e) {
                                                    review.setReviewRating(0f);
                                                }
                                            }

                                            Object dateValue =
                                                    document.get("reviewDate");

                                            if (dateValue instanceof com.google.firebase.Timestamp) {
                                                review.setReviewDate(
                                                        ((com.google.firebase.Timestamp)
                                                                dateValue).toDate()
                                                );
                                            }

                                            reviews.add(review);
                                        }

                                        postValue(reviews);
                                    }
                                }
                        );
            }

            @Override
            protected void onInactive() {
                super.onInactive();

                if (reviewListener != null) {
                    reviewListener.remove();
                    reviewListener = null;
                }
            }
        };
    }


}
