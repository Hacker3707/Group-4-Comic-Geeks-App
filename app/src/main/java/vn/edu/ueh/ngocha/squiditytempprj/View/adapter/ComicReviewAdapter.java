package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ComicReviewHolder;

public class ComicReviewAdapter
        extends RecyclerView.Adapter<ComicReviewHolder> {

    private final List<Review> reviewList =
            new ArrayList<>();
    private boolean isProfile = false;
    private final List<User> userList = new ArrayList<>();
    private final List<MangaData> comicList = new ArrayList<>();
    private User only_user;
    private OnReviewClickListener onReviewClickListener;

    public void setReviews(List<Review> reviews) {
        reviewList.clear();

        if (reviews != null) {
            reviewList.addAll(reviews);
        }

        notifyDataSetChanged();
    }

    public void setUser(User user) {
        this.only_user = user;
        notifyDataSetChanged();
    }

    public void setUsers(List<User> users) {
        userList.clear();
        if (users != null) {
            userList.addAll(users);
        }
        notifyDataSetChanged();
    }

    public interface OnReviewClickListener {
        void onUserClick(String userId);
        void onComicChipClick(String comicId);
    }

    public void setComics(List<MangaData> comics) {
        comicList.clear();

        if (comics != null) {
            comicList.addAll(comics);
        }

        notifyDataSetChanged();
    }

    public void setupInProfile() {
        this.isProfile = true;
    }

    public ComicReviewAdapter(OnReviewClickListener listener) {
        this.onReviewClickListener = listener;
    }

    private View.OnClickListener userClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();

                    if (tag == null || onReviewClickListener == null) {
                        return;
                    }

                    String userId = tag.toString();
                    if (!userId.trim().isEmpty() && onReviewClickListener != null) {
                        onReviewClickListener.onUserClick(userId);
                    }
                }
            };

    private final View.OnClickListener onComicChipClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();

                    if (tag == null || onReviewClickListener == null) {
                        return;
                    }

                    String comicId = tag.toString();
                    onReviewClickListener.onComicChipClick(comicId);
                }
            };

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

        return new ComicReviewHolder(view, isProfile);
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

        if (!isProfile) {
            holder.ratingBarReview.setRating(rating);
            holder.ratingBarReview.setIsIndicator(true);
        }
        else {
            holder.ratingBarPf.setRating(rating);
            holder.ratingBarPf.setIsIndicator(true);
        }

        // Tìm User dựa vào userId của Review
        User user = findUserById(review.getUserId());

        // Tìm Comic dựa vào comicId của Review
        MangaData comic = findComicById(review.getComicId());

        if (user != null) {
            // Đổi tên getter cho khớp với User.java của bạn
            holder.txtReviewerName.setText(user.getUsername());
            holder.txtReviewerName.setTag(user.getUserId());
            holder.txtReviewerName.setOnClickListener(userClickListener);

            String avatarUrl = user.getAvatarUrl();

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(holder.imgvAvatar.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.comic_background)
                        .into(holder.imgvAvatar);

                holder.imgvAvatar.setTag(user.getUserId());
                holder.imgvAvatar.setOnClickListener(userClickListener);

            } else {
                holder.imgvAvatar.setImageResource(
                        R.drawable.comic_background
                );
            }

        } else {
            holder.txtReviewerName.setText("Anonymous");
            holder.imgvAvatar.setImageResource(
                    R.drawable.comic_background
            );
        }

        if (comic != null
                && comic.getAttributes() != null
                && comic.getAttributes().getTitle() != null
                && comic.getId() != null) {

            Map<String, String> titles =
                    comic.getAttributes().getTitle();

            String title = null;

            if (titles != null) {
                title = titles.get("en");

                if (title == null || title.trim().isEmpty()) {
                    for (String value : titles.values()) {
                        if (value != null && !value.trim().isEmpty()) {
                            title = value;
                            break;
                        }
                    }
                }
            }

            holder.chipComic.setText(
                    title == null ? "Unknown Comic" : "📖 " + title
            );

            holder.chipComic.setTag(comic.getId());
            holder.chipComic.setEnabled(true);
            holder.chipComic.setOnClickListener(onComicChipClickListener);

        } else {
            holder.chipComic.setText("Comic Unfound");
            holder.chipComic.setTag(null);
            holder.chipComic.setEnabled(false);
            holder.chipComic.setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }


    private User findUserById(String userId) {
        if (only_user != null
                && only_user.getUserId() != null
                && only_user.getUserId().equals(userId)) {
            return only_user;
        }

        for (User user : userList) {
            if (user != null
                    && user.getUserId() != null
                    && user.getUserId().equals(userId)) {
                return user;
            }
        }

        return null;
    }

    private MangaData findComicById(String comicId) {
        for (MangaData comic : comicList) {
            if (comic.getAttributes().getTitle() != null
                    && comic.getId().equals(comicId)) {
                return comic;
            }
        }
        return null;
    }


}