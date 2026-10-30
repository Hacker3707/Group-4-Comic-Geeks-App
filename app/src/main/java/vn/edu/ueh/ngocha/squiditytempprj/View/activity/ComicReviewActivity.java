package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicReviewAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicReviewHeaderAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.dialogFragment.ReviewPostDialog;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ReviewViewModel;

public class ComicReviewActivity extends BaseActivity {

    public static String COMIC_ID = "comic_id";
    private static final String KEY_COMIC_ID = "saved_comic_id";
    private static final String KEY_COMIC_NAME = "saved_comic_name";
    private static final String KEY_COMIC_COVER = "saved_comic_cover";

    private String comicId;
    private String comicName;
    private String comicCover;
    public RecyclerView recyclerView;
    public RatingBar ratingBar;
    public ImageButton btnBack;
    public Spinner spnReviewfilter;
    public ComicReviewAdapter reviewAdapter;
    public ComicReviewHeaderAdapter headerAdapter;
    private final View.OnClickListener onReviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showReviewDialog();
        }
    };

    public ComicReviewAdapter.OnReviewClickListener reviewInteractionListener = new ComicReviewAdapter.OnReviewClickListener() {
        @Override
        public void onUserClick(String userId) {
            if (userId == null || userId.trim().isEmpty()) {
                return;
            }

            Intent intent = new Intent(
                    ComicReviewActivity.this,
                    ProfileActivity.class
            );

            intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        }

        @Override
        public void onComicChipClick(String comicId) {
            Intent intent = new Intent(
                    ComicReviewActivity.this,
                    ComicDetailActivity.class
            );

            intent.putExtra("comic_id", comicId);
            intent.putExtra("search_option", "MangaDex");

            startActivity(intent);
        }

    };

    public AdapterView.OnItemSelectedListener onReviewFilterSelectedListener =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(
                        AdapterView<?> parent,
                        View view,
                        int position,
                        long id) {

                    String[] filterKeys = {
                            "newest",
                            "oldest",
                            "highest",
                            "lowest"
                    };

                    reviewViewModel.loadReviews(comicId, filterKeys[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };


    public View.OnClickListener onExitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    // ----------------- Observer -------------------

    private ReviewViewModel reviewViewModel;
    private AuthViewModel authViewModel;

    private final Observer<List<User>> usersObserver = new Observer<List<User>>() {
        @Override
        public void onChanged(List<User> users) {
            reviewAdapter.setUsers(users);
        }
    };

    private final Observer<Boolean> registeredObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean registered) {
            if (registered != null && registered) {
                showReviewDialog();
            }
        }
    };

    private final Observer<List<Review>> reviewsObserver = new Observer<List<Review>>() {
        @Override
        public void onChanged(List<Review> reviews) {
            reviewAdapter.setReviews(reviews);
            headerAdapter.setReviewCount(
                    reviews == null ? 0 : reviews.size()
            );
        }
    };

    private final Observer<Review> myReviewObserver =
            new Observer<Review>() {
                @Override
                public void onChanged(Review review) {
                    headerAdapter.setIsSameUser(review != null);
                }
            };

    private final Observer<Float> averageRatingObserver = new Observer<Float>() {
        @Override
        public void onChanged(Float averageRating) {
            headerAdapter.setAverageRating(averageRating);
        }
    };

    private final Observer<Boolean> isSameUserObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean isSameUser) {
            headerAdapter.setIsSameUser(isSameUser);
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_COMIC_ID, comicId);
        outState.putString(KEY_COMIC_NAME, comicName);
        outState.putString(KEY_COMIC_COVER, comicCover);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_comic_review;
    }

    @Override
    protected boolean shouldShowBackHomeButton() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            comicId = savedInstanceState.getString(KEY_COMIC_ID);
            comicName = savedInstanceState.getString(KEY_COMIC_NAME);
            comicCover = savedInstanceState.getString(KEY_COMIC_COVER);
        } else {
            comicId = getIntent().getStringExtra("comic_id");
            comicName = getIntent().getStringExtra("comic_name");
            comicCover = getIntent().getStringExtra("comic_cover");
        }

        COMIC_ID = comicId;

        spnReviewfilter = findViewById(R.id.spinnerReviewFilter);
        ratingBar = findViewById(R.id.ratingBar);
        btnBack = findViewById(R.id.imgbtnBack);

        // ----------- Set up Button -----------
        btnBack.setOnClickListener(onExitClickListener);

        // ----------- Set up RecyclerView -----------
        recyclerView = findViewById(R.id.rcvComicReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reviewAdapter = new ComicReviewAdapter(reviewInteractionListener);
        headerAdapter = new ComicReviewHeaderAdapter(onReviewClickListener, onReviewFilterSelectedListener);
        headerAdapter.setComicInfo(comicName, comicCover);

        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, reviewAdapter);

        recyclerView.setAdapter(concatAdapter);

        // ------------ Set up reviewViewModel ------------

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        reviewViewModel.getReview().observe(this, reviewsObserver);

        reviewViewModel.loadReviews(comicId, "newest");

        reviewViewModel.getUsersForComic(comicId)
                .observe(this, usersObserver);

        reviewViewModel.getAverageRating(comicId)
                .observe(this, averageRatingObserver);
        reviewViewModel.isSameUser.observe(this, isSameUserObserver);

        // ------------ Set up authViewModel ------------

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getRegistered().observe(this, registeredObserver);
        String userId = authViewModel.getCurrentUserId();

        if (userId != null && !userId.isEmpty()) {
            reviewViewModel.getReview(comicId, userId)
                    .observe(this, myReviewObserver);
        }
    }

    public void showReviewDialog() {
        String userId = authViewModel.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(
                    this,
                    Str.t(R.string.comic_review_not_logged_in),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        ReviewPostDialog dialog =
                new ReviewPostDialog(comicId, userId);

        dialog.show(
                getSupportFragmentManager(),
                "ReviewPostDialog"
        );
    }

}