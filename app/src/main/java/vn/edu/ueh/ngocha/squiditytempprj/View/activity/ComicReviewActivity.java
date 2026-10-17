package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ReviewViewModel;

public class ComicReviewActivity extends AppCompatActivity {

    public static String COMIC_ID = "comic_id";
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

    public View.OnClickListener onExitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    public ReviewViewModel reviewViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comic_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        COMIC_ID = getIntent().getStringExtra("comic_id");
        String comic_name = getIntent().getStringExtra("comic_name");
        String comic_cover = getIntent().getStringExtra("comic_cover");


        spnReviewfilter = findViewById(R.id.spinnerReviewFilter);
        ratingBar = findViewById(R.id.ratingBar);
        btnBack = findViewById(R.id.imgbtnBack);

        // ----------- Set up Button -----------
        btnBack.setOnClickListener(onExitClickListener);

        // ----------- Set up RecyclerView -----------
        recyclerView = findViewById(R.id.rcvComicReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reviewAdapter = new ComicReviewAdapter();
        headerAdapter = new ComicReviewHeaderAdapter(onReviewClickListener);
        headerAdapter.setComicInfo(comic_name, comic_cover);

        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, reviewAdapter);

        recyclerView.setAdapter(concatAdapter);

        // ------------ Set up ViewModel ------------

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        final Observer<List<Review>> reviewsObserver = new Observer<List<Review>>() {
            @Override
            public void onChanged(List<Review> reviews) {
                reviewAdapter.setReviews(reviews);
                headerAdapter.setReviewCount(reviews.size());
                headerAdapter.setAverageRating(calculateAverageRating(reviews));
            }
        };
        reviewViewModel.getReviewsByComicId(COMIC_ID).observe(this, reviewsObserver);

        final Observer<List<User>> usersObserver = new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                reviewAdapter.setUsers(users);
            }
        };
        reviewViewModel.getUsersForComic(COMIC_ID).observe(this, usersObserver);


    }

    public void showReviewDialog() {
        ReviewPostDialog dialog =
                new ReviewPostDialog(COMIC_ID, "4v8JeFqSaTObxGn7zePLdsroA5l1");

        dialog.show(
                getSupportFragmentManager(),
                "ReviewPostDialog");
    }

    public float calculateAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0f;
        }

        float sum = 0f;

        for (Review review : reviews) {
            sum += review.getReviewRating();
        }

        return sum / reviews.size();
    }

}