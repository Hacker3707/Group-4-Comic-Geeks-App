package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.PostViewHolder;
public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private List<Post> postList;
    private Context context;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvPostContent.setText(post.getContent());holder.tvUserName.setText("User: " + post.getUserId());
        holder.tvHandle.setText("@" + post.getUserId());

        if (post.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String timeString = sdf.format(post.getTimestamp());
        }
    }

    @Override
    public int getItemCount() {
        if (postList != null) {
            return postList.size();
        }
        return 0;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }
}