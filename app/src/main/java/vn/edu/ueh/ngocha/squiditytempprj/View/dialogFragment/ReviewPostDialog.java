package vn.edu.ueh.ngocha.squiditytempprj.View.dialogFragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
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
    private TextView txtReviewDate;
    private ImageView imgProfileAvt;
    private EditText etReviewContent;
    private Button btnConfirm;
    private ImageButton btnExit;
    private Button btnEdit;
    private Button btnSave;
    private ImageButton btnPopupMenu;
    private LinearLayout lnEditSaveReview;
    private String comicId;
    private String userId;
    private Review oldReview;

    private Boolean isEditing = false;

    ReviewViewModel reviewViewModel;
    AuthViewModel authViewModel;

    public View.OnClickListener onExitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    public View.OnClickListener onPopupMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view);
            popupMenu.inflate(R.menu.menu_review);
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(onMenuItemClickListener);
        }
    };

    public PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_delete_rv) {
                reviewViewModel.delete(oldReview);
                Toast.makeText(
                        requireContext(),
                        "Review deleted!",
                        Toast.LENGTH_SHORT
                ).show();
                dismiss();
                return true;
            }
            return false;
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

    public View.OnClickListener onEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isEditing) {
                Toast.makeText(v.getContext(), "You're in Edit Mode!", Toast.LENGTH_SHORT).show();
                ratingBar.setClickable(true);
                etReviewContent.setEnabled(true);
                txtReviewDate.setVisibility(View.GONE);
                btnEdit.setText("Cancel");
                btnSave.setEnabled(true);
                isEditing = true;
            }
            else {
                btnSave.setEnabled(false);
                btnEdit.setText("Edit");

                ratingBar.setClickable(false);
                ratingBar.setRating(oldReview.getReviewRating());

                etReviewContent.setEnabled(false);
                etReviewContent.setText(oldReview.getReviewText());

                txtReviewDate.setVisibility(View.VISIBLE);
                txtReviewDate.setText(
                        "Reviewed on: " + oldReview.getReviewDate()
                );
                isEditing = false;
            }

        }
    };

    public View.OnClickListener onSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            float rating = ratingBar.getRating();
            String content = etReviewContent
                    .getText()
                    .toString()
                    .trim();

            reviewViewModel.update(new Review(comicId, userId, content, rating));
            btnSave.setEnabled(false);
            btnSave.setText("Saved");
            Toast.makeText(v.getContext(), "Review saved!", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    };

    public Observer<Review> reviewObserver = new Observer<Review>() {
        @Override
        public void onChanged(Review review) {
            if (review == null) {
                // Chưa có review -> chế độ Post
                btnConfirm.setVisibility(View.VISIBLE);
                lnEditSaveReview.setVisibility(View.GONE);

                ratingBar.setClickable(true);
                etReviewContent.setEnabled(true);
                txtReviewDate.setVisibility(View.GONE);

            } else {
                // Đã có review -> chế độ xem/edit
                btnConfirm.setVisibility(View.GONE);
                lnEditSaveReview.setVisibility(View.VISIBLE);

                btnEdit.setText("Edit");
                btnEdit.setEnabled(true);

                btnSave.setText("Save");
                btnSave.setEnabled(false);

                ratingBar.setRating(review.getReviewRating());
                ratingBar.setClickable(false);

                etReviewContent.setText(review.getReviewText());
                etReviewContent.setEnabled(false);

                if (review.getReviewDate() != null) {
                    txtReviewDate.setVisibility(View.VISIBLE);
                    txtReviewDate.setText(
                            "Reviewed on: " + review.getReviewDate()
                    );
                }

                oldReview = review;
            }
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
        txtReviewDate = view.findViewById(R.id.txtReviewDate_dg);
        lnEditSaveReview = view.findViewById(R.id.lnEditSaveReview);
        btnEdit = view.findViewById(R.id.btneditReview);
        btnSave = view.findViewById(R.id.btnSaveReview);
        btnPopupMenu = view.findViewById(R.id.btnPopupMenuReview);

        // Nút mở menu
        btnPopupMenu.setOnClickListener(onPopupMenuClickListener);

        // Nút đóng dialog
        btnExit.setOnClickListener(onExitClickListener);

        // Nút đăng review
        btnConfirm.setOnClickListener(onConfirmClickListener);

        // Nút sửa review
        btnEdit.setOnClickListener(onEditClickListener);

        // Nút lưu review
        btnSave.setOnClickListener(onSaveClickListener);

        // Khởi tạo ViewModel
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Hiển thị thông tin người dùng
        String username = authViewModel.getCurrentUser().getDisplayName();
        String avatarUrl = authViewModel.getCurrentUser().getPhotoUrl().toString();
        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.no_image)
                .into(imgProfileAvt);

        txtUsername.setText(username);

        // Get Review from currentUser in this comic (if exist)
        reviewViewModel.getReview(comicId, userId)
                .observe(getViewLifecycleOwner(), reviewObserver);
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
