package vn.edu.ueh.ngocha.squiditytempprj.View.dialogFragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ReviewViewModel;

public class ReviewPostDialog extends DialogFragment {

    private RatingBar ratingBar;
    private TextView txtGuide;
    private TextView txtUsername;
    private ImageView imgProfileAvt;
    private EditText etReviewContent;
    private Button btnConfirm;
    private ImageButton btnExit;
    private String comicId;
    private String userId;

    ReviewViewModel reviewViewModel;
    AuthViewModel authViewModel;

    public View.OnClickListener onExitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    public View.OnClickListener onConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            float rating = ratingBar.getRating();

            String content = etReviewContent
                    .getText()
                    .toString()
                    .trim();

            if (rating == 0) {
                txtGuide.setVisibility(View.VISIBLE);
                txtGuide.setText("Please rate the comic!");
                Animation shake = AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                );

                ratingBar.startAnimation(shake);
                txtGuide.startAnimation(shake);
                ratingBar.requestFocus();
                return;
            }

            Review review = new Review(comicId, userId, content, rating);
            reviewViewModel.insert(review);

            Toast.makeText(
                    requireContext(),
                    "Review information is ready!",
                    Toast.LENGTH_SHORT
            ).show();

            dismiss();
        }
    };

    public ReviewPostDialog(String comicId, String userId) {
        this.comicId = comicId;
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.review_post_dialog,
                container,
                false
        );

    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các thành phần giao diện
        ratingBar = view.findViewById(R.id.ratingBarReview);
        etReviewContent = view.findViewById(R.id.etReviewcontent);
        btnConfirm = view.findViewById(R.id.btnpostReview);
        btnExit = view.findViewById(R.id.btnexitReviewdg);
        txtGuide = view.findViewById(R.id.txtvGuide);
        txtUsername = view.findViewById(R.id.txtReviewerName);
        imgProfileAvt = view.findViewById(R.id.imgAvtReview_dg);

        // Nút đóng dialog
        btnExit.setOnClickListener(onExitClickListener);

        // Nút đăng review
        btnConfirm.setOnClickListener(onConfirmClickListener);

        // Khởi tạo ViewModel
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        String username = authViewModel.getCurrentUser().getDisplayName();
        String avatarUrl = authViewModel.getCurrentUser().getPhotoUrl().toString();
        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.no_image)
                .into(imgProfileAvt);

        txtUsername.setText(username);
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();

        if (window != null) {
            window.setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            int screenWidth = getResources()
                    .getDisplayMetrics().widthPixels;

            window.setLayout(
                    (int) (screenWidth * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
