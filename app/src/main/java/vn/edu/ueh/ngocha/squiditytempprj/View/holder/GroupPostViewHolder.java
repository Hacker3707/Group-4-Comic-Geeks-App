package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupHashtagUtils;

public class GroupPostViewHolder extends RecyclerView.ViewHolder {

    private final ImageView imgPostAvatar;
    private final TextView txtAuthorName;
    private final TextView txtAuthorHandle;
    private final TextView txtContent;

    public GroupPostViewHolder(@NonNull View itemView) {
        super(itemView);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        txtAuthorName = itemView.findViewById(R.id.txtAuthorName);
        txtAuthorHandle = itemView.findViewById(R.id.txtAuthorHandle);
        txtContent = itemView.findViewById(R.id.txtContent);
    }

    public void bind(Post post, String authorName, String authorHandle, String avatarUrl) {
        txtAuthorName.setText(authorName);
        txtAuthorHandle.setText(authorHandle);
        int hashtagColor = ContextCompat.getColor(itemView.getContext(), R.color.group_tab_selected);
        txtContent.setText(GroupHashtagUtils.highlight(post.getContent(), hashtagColor));

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.group_avatar_sample)
                    .error(R.drawable.group_avatar_sample)
                    .into(imgPostAvatar);
        } else {
            Glide.with(itemView.getContext()).clear(imgPostAvatar);
            imgPostAvatar.setImageResource(R.drawable.group_avatar_sample);
        }
    }
}
