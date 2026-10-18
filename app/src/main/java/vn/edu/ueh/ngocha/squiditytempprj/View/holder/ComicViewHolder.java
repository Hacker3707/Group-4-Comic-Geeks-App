package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class ComicViewHolder extends RecyclerView.ViewHolder {

    public ImageView imgcomiccover;
    public TextView txtcomicname;

    public ComicViewHolder(@NonNull View itemView) {
        super(itemView);
        imgcomiccover = itemView.findViewById(R.id.imgcomic_cover);
        txtcomicname = itemView.findViewById(R.id.txtcomic_name);
    }
}