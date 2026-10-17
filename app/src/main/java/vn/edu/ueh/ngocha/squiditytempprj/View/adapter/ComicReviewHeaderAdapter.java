package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ComicReviewHeaderHolder;

public class ComicReviewHeaderAdapter
        extends RecyclerView.Adapter<ComicReviewHeaderHolder> {

    private String comicName;
    private String coverUrl;
    private float averageRating;
    private int reviewCount;

    private final View.OnClickListener onReviewClickListener;

    public ComicReviewHeaderAdapter(
            View.OnClickListener onReviewClickListener) {
        this.onReviewClickListener = onReviewClickListener;
    }

    public void setComicInfo(
            String comicName,
            String coverUrl) {

        this.comicName = comicName;
        this.coverUrl = coverUrl;

        notifyItemChanged(0);
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
        notifyItemChanged(0);
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
        notifyItemChanged(0);
    }

    @NonNull
    @Override
    public ComicReviewHeaderHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.comic_review_header_item,
                        parent,
                        false
                );

        return new ComicReviewHeaderHolder(
                view,
                onReviewClickListener
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ComicReviewHeaderHolder holder, int position) {

        holder.txtComicName.setText(comicName);

        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(holder.imgComicCover.getContext())
                    .load(coverUrl)
                    .placeholder(R.drawable.comic_background)
                    .into(holder.imgComicCover);
        } else {
            holder.imgComicCover.setImageResource(
                    R.drawable.comic_background
            );
        }

        holder.ratingBar.setRating(averageRating);

        if (reviewCount == 0) {
            holder.txtComicRating.setText("Not rated yet");
        } else {
            holder.txtComicRating.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f / 5 (%d reviews)",
                            averageRating,
                            reviewCount
                    )
            );
        }

        String[] filters = {
                "Newest",
                "Highest Rating",
                "Lowest Rating"
        };

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        holder.itemView.getContext(),
                        android.R.layout.simple_spinner_item,
                        filters
                );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        holder.spinnerReviewFilter.setAdapter(spinnerAdapter);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

}