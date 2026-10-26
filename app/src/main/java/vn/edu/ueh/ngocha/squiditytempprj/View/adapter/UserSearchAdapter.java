package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;

/**
 * Adapter hiển thị kết quả tìm người dùng (mỗi dòng dùng layout item_search_user).
 */
public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final List<User> users = new ArrayList<User>();
    private OnUserClickListener onUserClickListener;

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    public void setUsers(List<User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final User user = users.get(position);

        String name = user.getUsername();
        if (name == null || name.trim().isEmpty()) {
            name = Str.t(R.string.common_user);
        }
        holder.tvUserName.setText(name);

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.no_image)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.no_image)
                    .circleCrop()
                    .into(holder.imgAvatar);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onUserClickListener != null) {
                    onUserClickListener.onUserClick(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgAvatar;
        final TextView tvUserName;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgSearchUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvSearchUserName);
        }
    }
}
