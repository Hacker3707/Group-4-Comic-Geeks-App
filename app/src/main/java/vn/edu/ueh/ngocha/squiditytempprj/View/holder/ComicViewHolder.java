package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class ComicViewHolder extends RecyclerView.ViewHolder {

    public ImageView imgcomiccover;
    public TextView txtcomicname;
    public TextView txtcomicauthor;

    public ComicViewHolder(@NonNull View itemView, int orientation) {
        super(itemView);

        imgcomiccover = itemView.findViewById(R.id.imgcomic_cover);
        txtcomicname = itemView.findViewById(R.id.txtcomic_name);

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            txtcomicauthor = itemView.findViewById(R.id.txt_authorcomicitem);
        } else {
            txtcomicauthor = null;
        }
    }
}