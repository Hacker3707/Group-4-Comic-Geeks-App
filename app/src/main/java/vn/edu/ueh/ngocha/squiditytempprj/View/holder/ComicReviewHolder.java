package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class ComicReviewHolder extends RecyclerView.ViewHolder {

    public ShapeableImageView imgvAvatar;
    public TextView txtReviewerName;
    public TextView txtReviewDate;
    public TextView txtReviewContent;
    public RatingBar ratingBarReview;
    public LinearLayout layoutReviewProfile;
    public RatingBar ratingBarPf;
    public Chip chipComic;
    public TextView txtAdditionPf;

    public ComicReviewHolder(@NonNull View itemView, boolean isProfile) {
        super(itemView);

        imgvAvatar =
                itemView.findViewById(R.id.imgAvtReview);

        txtReviewerName =
                itemView.findViewById(R.id.txtReviewerName);

        txtReviewDate =
                itemView.findViewById(R.id.txtReviewDate);

        txtReviewContent =
                itemView.findViewById(R.id.txtReviewContent);

        ratingBarReview =
                itemView.findViewById(R.id.ratingBarReview);

        layoutReviewProfile =
                itemView.findViewById(R.id.lnly_reviewProfile);

        ratingBarPf =
                itemView.findViewById(R.id.ratingBarReview_pf);

        chipComic =
                itemView.findViewById(R.id.chipLinkedComic_pf);

        txtAdditionPf =
                itemView.findViewById(R.id.txtReviewAddition_pf);

        if(isProfile) {
            layoutReviewProfile.setVisibility(View.VISIBLE);
            ratingBarReview.setVisibility(View.GONE);
            txtAdditionPf.setVisibility(View.VISIBLE);
        }
        else {
            layoutReviewProfile.setVisibility(View.GONE);
            ratingBarReview.setVisibility(View.VISIBLE);
            txtAdditionPf.setVisibility(View.GONE);
        }
    }
}
