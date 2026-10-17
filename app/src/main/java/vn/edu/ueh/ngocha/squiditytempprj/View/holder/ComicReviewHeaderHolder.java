package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.dialogFragment.ReviewPostDialog;

public class ComicReviewHeaderHolder extends RecyclerView.ViewHolder {
    public final ImageView imgComicCover;
    public final TextView txtComicName;
    public final TextView txtComicRating;
    public final RatingBar ratingBar;
    public final Button btnwriteReview;
    public final Spinner spinnerReviewFilter;

    public ComicReviewHeaderHolder(
            @NonNull View itemView,
            View.OnClickListener onReviewClickListener) {

        super(itemView);

        imgComicCover = itemView.findViewById(R.id.imgComicCover);
        txtComicName = itemView.findViewById(R.id.txtComicName);
        txtComicRating = itemView.findViewById(R.id.txtComicRating);
        ratingBar = itemView.findViewById(R.id.ratingBar);

        spinnerReviewFilter =
                itemView.findViewById(R.id.spinnerReviewFilter);

        btnwriteReview =
                itemView.findViewById(R.id.btnwriteReview);

        btnwriteReview.setOnClickListener(onReviewClickListener);
    }

}
