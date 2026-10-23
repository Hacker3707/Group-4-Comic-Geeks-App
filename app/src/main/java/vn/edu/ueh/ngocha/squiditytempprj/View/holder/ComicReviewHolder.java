package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class ComicReviewHolder extends RecyclerView.ViewHolder {

    public ShapeableImageView imgvAvatar;
    public TextView txtReviewerName;
    public TextView txtReviewDate;
    public TextView txtReviewContent;
    public RatingBar ratingBarReview;

    public ComicReviewHolder(@NonNull View itemView) {
        super(itemView);

        imgvAvatar =
                itemView.findViewById(R.id.imgProfileCover);

        txtReviewerName =
                itemView.findViewById(R.id.txtReviewerName);

        txtReviewDate =
                itemView.findViewById(R.id.txtReviewDate);

        txtReviewContent =
                itemView.findViewById(R.id.txtReviewContent);

        ratingBarReview =
                itemView.findViewById(R.id.ratingBarReview);
    }

}
