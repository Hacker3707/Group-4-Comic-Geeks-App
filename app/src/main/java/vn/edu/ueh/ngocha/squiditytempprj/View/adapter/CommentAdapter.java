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

    // 👉 Thêm listener để bắt sự kiện bấm nút trả lời bình luận
    private OnCommentReplyListener replyListener;

    public interface OnCommentReplyListener {
        void onReplyClick(Comment comment);
    }

    // Constructor hỗ trợ cả 2 trường hợp (có hoặc không có listener)
    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
        this.replyListener = null;
    }

    public CommentAdapter(List<Comment> commentList, OnCommentReplyListener replyListener) {
        this.commentList = commentList;
        this.replyListener = replyListener;
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

        // 👉 Xử lý thụt lề (Indentation) giống phong cách Facebook cho bình luận trả lời (reply)
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;

        if (comment.getParentId() != null && !comment.getParentId().trim().isEmpty()) {
            // Nếu là bình luận con -> Thụt lề sang phải 36dp
            layoutParams.leftMargin = (int) (36 * density);
            holder.tvReplyInfo.setVisibility(View.VISIBLE);

            // Lấy thông tin User của người được trả lời
            User targetUser = users.get(comment.getReplyToUsername());
            String fallbackName = holder.itemView.getContext().getString(R.string.group_member_unknown_name);
            String targetDisplayName = UserDisplayUtils.displayName(targetUser, fallbackName);

            if (targetUser == null && comment.getReplyToUsername() != null && !comment.getReplyToUsername().contains("-")) {
                targetDisplayName = comment.getReplyToUsername();
            }

            holder.tvReplyInfo.setText("Trả lời @" + targetDisplayName);
        } else {
            // Nếu là bình luận gốc -> Sát lề trái hoàn toàn
            layoutParams.leftMargin = (int) (0 * density);
            holder.tvReplyInfo.setVisibility(View.GONE);
        }
        holder.itemView.setLayoutParams(layoutParams);

        // 👉 Bắt sự kiện khi người dùng bấm vào nút "Trả lời"
        if (holder.btnReplyComment != null) {
            holder.btnReplyComment.setOnClickListener(v -> {
                if (replyListener != null) {
                    replyListener.onReplyClick(comment);
                }
            });
        }

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
        TextView tvCommentUsername, tvCommentContent, tvReplyInfo;
        ImageView imgCommentAvatar;
        TextView btnReplyComment; // Nút bấm trả lời

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            imgCommentAvatar = itemView.findViewById(R.id.imgCommentAvatar);

            tvReplyInfo = itemView.findViewById(R.id.tvReplyInfo);
            btnReplyComment = itemView.findViewById(R.id.btnReplyComment);
        }
    }
}