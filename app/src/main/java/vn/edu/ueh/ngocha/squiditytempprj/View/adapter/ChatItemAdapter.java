package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ChatItemViewHolder;

/**
 * Adapter danh sách cuộc trò chuyện. Mỗi dòng hiện người còn lại trong phòng chat
 * (không phải mình), lấy tên và avatar từ danh sách User.
 */
public class ChatItemAdapter extends RecyclerView.Adapter<ChatItemViewHolder> {

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom, String otherUserId, String otherUserName);
    }

    private List<ChatRoom> chatRooms = new ArrayList<>();
    private Map<String, User> users = new HashMap<>();
    private final String currentUserId;
    private final OnChatRoomClickListener listener;

    public ChatItemAdapter(String currentUserId, OnChatRoomClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setChatRooms(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms != null ? chatRooms : new ArrayList<ChatRoom>();
        notifyDataSetChanged();
    }

    public void setUsers(List<User> userList) {
        Map<String, User> map = new HashMap<>();
        if (userList != null) {
            for (User user : userList) {
                map.put(user.getUserId(), user);
            }
        }
        this.users = map;
        notifyDataSetChanged();
    }

    private String getOtherUserId(ChatRoom chatRoom) {
        if (currentUserId != null && currentUserId.equals(chatRoom.getUser1Id())) {
            return chatRoom.getUser2Id();
        }
        return chatRoom.getUser1Id();
    }

    private String getDisplayName(String otherUserId) {
        User user = users.get(otherUserId);
        if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        }
        return otherUserId;
    }

    @NonNull
    @Override
    public ChatItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatItemViewHolder holder, int position) {
        final ChatRoom chatRoom = chatRooms.get(position);
        final String otherUserId = getOtherUserId(chatRoom);
        final String displayName = getDisplayName(otherUserId);

        holder.tvChatName.setText(displayName);

        User other = users.get(otherUserId);
        if (other != null && other.getAvatarUrl() != null && !other.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(other.getAvatarUrl())
                    .circleCrop()
                    .into(holder.imgChatAvatar);
        } else {
            holder.imgChatAvatar.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChatRoomClick(chatRoom, otherUserId, displayName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }
}