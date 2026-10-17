package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ComicReviewHolder;

public class ComicReviewAdapter
        extends RecyclerView.Adapter<ComicReviewHolder> {

    private final List<Review> reviewList =
            new ArrayList<>();

    private final List<User> userList = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        reviewList.clear();

        if (reviews != null) {
            reviewList.addAll(reviews);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComicReviewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.comic_review_item,
                        parent,
                        false
                );

        return new ComicReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ComicReviewHolder holder, int position) {

        Review review = reviewList.get(position);

        // Lấy nội dung review
        holder.txtReviewContent.setText(review.getReviewText());

        Date reviewDate = review.getReviewDate();

        if (reviewDate != null) {
            SimpleDateFormat reviewDateFormat =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            String formattedDate = reviewDateFormat.format(reviewDate);
            holder.txtReviewDate.setText(formattedDate);
        } else {
            holder.txtReviewDate.setText("Đang cập nhật...");
        }

        // Chuyển điểm từ String sang float
        float rating = review.getReviewRating();

        holder.ratingBarReview.setRating(rating);

        // Tìm User dựa vào userId của Review
        User user = findUserById(review.getUserId());

        if (user != null) {
            // Đổi tên getter cho khớp với User.java của bạn
            holder.txtReviewerName.setText(user.getUsername());

            String avatarUrl = user.getAvatarUrl();

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(holder.imgvAvatar.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.comic_background)
                        .into(holder.imgvAvatar);
            } else {
                holder.imgvAvatar.setImageResource(
                        R.drawable.comic_background
                );
            }

        } else {
            holder.txtReviewerName.setText("Người dùng");
            holder.imgvAvatar.setImageResource(
                    R.drawable.comic_background
            );
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void setUsers(List<User> users) {
        userList.clear();

        if (users != null) {
            userList.addAll(users);
        }

        notifyDataSetChanged();
    }

    private User findUserById(String userId) {
        for (User user : userList) {
            if (user.getUserId() != null
                    && user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

}