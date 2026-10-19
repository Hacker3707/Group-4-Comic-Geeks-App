package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.NotificationViewHolder;

/**
 * Adapter danh sách thông báo trong popup. Thông báo chưa đọc có chấm tím và chữ đậm.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<Notification>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        final Notification notification = notifications.get(position);

        holder.tvNotificationContent.setText(notification.getContent());
        holder.tvNotificationContent.setTypeface(null,
                notification.isRead() ? Typeface.NORMAL : Typeface.BOLD);
        holder.viewUnreadDot.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

        String type = (notification.getType() != null) ? notification.getType().name() : "";
        String time = (notification.getTimestamp() != null) ? timeFormat.format(notification.getTimestamp()) : "";
        holder.tvNotificationMeta.setText(type + "  " + time);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}