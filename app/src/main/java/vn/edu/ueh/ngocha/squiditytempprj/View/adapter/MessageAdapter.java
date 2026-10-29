package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.MessageViewHolder;

/**
 * Adapter hiển thị tin nhắn. Có 2 kiểu dòng: tin mình gửi (bên phải) và tin nhận (bên trái).
 * Tin nhắn hình ảnh được lưu trong content với tiền tố "img:" theo sau là đường dẫn ảnh.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    public static final String IMAGE_PREFIX = "img:";

    private static final int TYPE_RECEIVED = 0;
    private static final int TYPE_SENT = 1;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message, boolean sentByMe);
    }

    private OnMessageLongClickListener longClickListener;

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    private List<Message> messages = new ArrayList<>();
    private final String currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<Message>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (currentUserId != null && currentUserId.equals(message.getSenderId())) {
            return TYPE_SENT;
        }
        return TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_SENT) ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        String content = message.getContent();

        if (content != null && content.startsWith(IMAGE_PREFIX)) {
            holder.tvMessage.setVisibility(View.GONE);
            holder.imgMessage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(content.substring(IMAGE_PREFIX.length())))
                    .into(holder.imgMessage);
        } else {
            holder.imgMessage.setVisibility(View.GONE);
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(content);
        }

        final boolean sentByMe = currentUserId != null && currentUserId.equals(message.getSenderId());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onMessageLongClick(message, sentByMe);
                }
                return true;
            }
        });

        if (message.getTimestamp() != null) {
            String time = timeFormat.format(message.getTimestamp());
            if (sentByMe) {
                // Tin của mình: hiện trạng thái gửi
                if (message.getStatus() == Message.STATUS_SENDING) {
                    time = time + " · " + Str.t(R.string.chat_status_sending);
                } else if (message.getStatus() == Message.STATUS_FAILED) {
                    time = time + " · " + Str.t(R.string.chat_status_failed);
                } else {
                    time = time + " ✓";
                }
            }
            holder.tvMessageTime.setText(time);
        } else {
            holder.tvMessageTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}