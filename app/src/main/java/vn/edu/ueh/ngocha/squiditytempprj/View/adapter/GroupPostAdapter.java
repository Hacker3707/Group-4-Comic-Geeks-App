package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.GroupPostViewHolder;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.UserDisplayUtils;

public class GroupPostAdapter extends RecyclerView.Adapter<GroupPostViewHolder> {

    private final List<Post> posts = new ArrayList<>();
    // Tên + ảnh của các tài khoản (key = userId), cập nhật theo thời gian thực
    private final Map<String, User> users = new HashMap<>();

    public void setUsers(Map<String, User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.putAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public void setPosts(List<Post> newPosts) {
        posts.clear();
        if (newPosts != null) {
            posts.addAll(newPosts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_post_item, parent, false);
        return new GroupPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupPostViewHolder holder, int position) {
        Post post = posts.get(position);

        User user = users.get(post.getUserId());
        String name = UserDisplayUtils.displayName(
                user, holder.itemView.getContext().getString(R.string.group_member_unknown_name));
        // Chưa biết tài khoản thì không hiện @handle cho khỏi lạ
        String handle = "";
        String avatarUrl = null;
        if (user != null) {
            handle = "@" + name.replace(" ", "").toLowerCase();
            avatarUrl = user.getAvatarUrl();
        }
        holder.bind(post, name, handle, avatarUrl);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
