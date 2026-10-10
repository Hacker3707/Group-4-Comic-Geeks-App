package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicAdapter;

public class ComicViewHolder extends RecyclerView.ViewHolder
implements View.OnClickListener {

    private ComicAdapter adapter;
    private Comic comic;
    public ImageView imgcomiccover;
    public TextView txtcomicname;


    public ComicViewHolder(@NonNull View itemView, ComicAdapter adapter) {
        super(itemView);
        imgcomiccover = itemView.findViewById(R.id.imgcomic_cover);
        txtcomicname = itemView.findViewById(R.id.txtcomic_name);
        this.adapter = adapter;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
