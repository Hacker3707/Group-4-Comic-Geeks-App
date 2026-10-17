package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class ChatItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView imgChatAvatar;
    public TextView tvChatName;
    public TextView tvChatSubtitle;

    public ChatItemViewHolder(@NonNull View itemView) {
        super(itemView);
        imgChatAvatar = itemView.findViewById(R.id.imgChatAvatar);
        tvChatName = itemView.findViewById(R.id.tvChatName);
        tvChatSubtitle = itemView.findViewById(R.id.tvChatSubtitle);
    }
}