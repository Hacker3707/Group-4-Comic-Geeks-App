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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
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

        reviewData.put("comicId", review.getComicId());
        reviewData.put("userId", review.getUserId());
        reviewData.put("reviewText", review.getReviewText());
        reviewData.put("reviewRating", review.getReviewRating());

        // Firebase tự ghi thời gian tạo trên server
        reviewData.put("reviewDate", FieldValue.serverTimestamp());

        firestore.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("ReviewRepository",
                                "Review added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ReviewRepository",
                                "Error adding review", e);
                    }
                });
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
}
