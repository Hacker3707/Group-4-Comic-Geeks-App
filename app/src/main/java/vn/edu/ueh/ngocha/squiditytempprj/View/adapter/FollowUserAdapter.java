package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;

/**
 * Danh sách người theo dõi / đang theo dõi, layout giống khung bình luận.
 * Mỗi dòng có nút Follow / Unfollow (không hiện ở dòng của chính mình).
 */
public class FollowUserAdapter extends RecyclerView.Adapter<FollowUserAdapter.FollowViewHolder> {

    public static class Item {
        public final String userId;
        public final String name;
        public final String avatarUrl;

        public Item(String userId, String name, String avatarUrl) {
            this.userId = userId;
            this.name = name;
            this.avatarUrl = avatarUrl;
        }
    }

    public interface OnFollowActionListener {
        // Bấm vào nút Follow / Unfollow. currentlyFollowing = trạng thái trước khi bấm
        void onToggleFollow(Item item, boolean currentlyFollowing);

        // Bấm vào dòng để mở trang cá nhân
        void onUserClick(Item item);
    }

    private final List<Item> items = new ArrayList<>();
    // Những người mà tài khoản hiện tại đang follow
    private final Set<String> myFollowingIds = new HashSet<>();
    private String myUserId;
    private OnFollowActionListener listener;

    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    public void setListener(OnFollowActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Item> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setMyFollowingIds(Set<String> ids) {
        myFollowingIds.clear();
        if (ids != null) {
            myFollowingIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow_user, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        final Item item = items.get(position);
        holder.tvName.setText(item.name);

        boolean isMe = myUserId != null && myUserId.equals(item.userId);
        if (isMe) {
            holder.btnToggle.setVisibility(View.GONE);
        } else {
            holder.btnToggle.setVisibility(View.VISIBLE);
            final boolean following = myFollowingIds.contains(item.userId);
            if (following) {
                holder.btnToggle.setText(Str.t(R.string.follow_list_unfollow));
            } else {
                holder.btnToggle.setText(Str.t(R.string.follow_list_follow));
            }
            holder.btnToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onToggleFollow(item, following);
                    }
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onUserClick(item);
                }
            }
        });

        if (item.avatarUrl != null && !item.avatarUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.avatarUrl.trim())
                    .centerCrop()
                    .into(holder.imgAvatar);
        } else {
            Glide.with(holder.itemView.getContext()).clear(holder.imgAvatar);
            holder.imgAvatar.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FollowViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView imgAvatar;
        final TextView tvName;
        final TextView btnToggle;

        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgFollowAvatar);
            tvName = itemView.findViewById(R.id.tvFollowUsername);
            btnToggle = itemView.findViewById(R.id.btnFollowToggle);
        }
    }
}
