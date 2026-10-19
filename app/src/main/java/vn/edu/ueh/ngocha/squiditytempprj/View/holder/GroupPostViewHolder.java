package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupHashtagUtils;

public class GroupPostViewHolder extends RecyclerView.ViewHolder {

    private final TextView txtAuthorName;
    private final TextView txtAuthorHandle;
    private final TextView txtContent;

    public GroupPostViewHolder(@NonNull View itemView) {
        super(itemView);
        txtAuthorName = itemView.findViewById(R.id.txtAuthorName);
        txtAuthorHandle = itemView.findViewById(R.id.txtAuthorHandle);
        txtContent = itemView.findViewById(R.id.txtContent);
    }

    public void bind(Post post) {
        String userId = post.getUserId();
        if (userId == null) {
            userId = "";
        }
        txtAuthorName.setText(userId);
        txtAuthorHandle.setText("@" + userId);
        int hashtagColor = ContextCompat.getColor(itemView.getContext(), R.color.group_tab_selected);
        txtContent.setText(GroupHashtagUtils.highlight(post.getContent(), hashtagColor));
    }
}
