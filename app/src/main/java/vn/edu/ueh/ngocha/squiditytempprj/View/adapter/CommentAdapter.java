package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.UserDisplayUtils;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    // Tên + ảnh người bình luận (key = userId), cập nhật theo thời gian thực
    private final Map<String, User> users = new HashMap<>();

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        User user = users.get(comment.getUserId());
        String fallback = holder.itemView.getContext().getString(R.string.group_member_unknown_name);
        holder.tvCommentUsername.setText(UserDisplayUtils.displayName(user, fallback));
        holder.tvCommentContent.setText(comment.getContent());

        String avatarUrl = user != null ? user.getAvatarUrl() : null;
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl.trim())
                    .centerCrop()
                    .into(holder.imgCommentAvatar);
        } else {
            Glide.with(holder.itemView.getContext()).clear(holder.imgCommentAvatar);
            holder.imgCommentAvatar.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    public void setUsers(Map<String, User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.putAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public void setComments(List<Comment> comments) {
        this.commentList = comments;
        notifyDataSetChanged();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUsername, tvCommentContent;
        ImageView imgCommentAvatar;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            imgCommentAvatar = itemView.findViewById(R.id.imgCommentAvatar);
        }
    }
}