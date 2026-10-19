package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder {
    public View viewUnreadDot;
    public TextView tvNotificationContent;
    public TextView tvNotificationMeta;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        tvNotificationContent = itemView.findViewById(R.id.tvNotificationContent);
        tvNotificationMeta = itemView.findViewById(R.id.tvNotificationMeta);
    }
}