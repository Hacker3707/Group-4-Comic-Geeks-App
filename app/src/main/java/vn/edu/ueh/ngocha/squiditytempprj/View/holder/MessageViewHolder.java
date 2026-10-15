package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView tvMessage;
    public ImageView imgMessage;
    public TextView tvMessageTime;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        tvMessage = itemView.findViewById(R.id.tvMessage);
        imgMessage = itemView.findViewById(R.id.imgMessage);
        tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
    }
}