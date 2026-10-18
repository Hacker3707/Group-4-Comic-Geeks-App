package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ComicListActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.PostViewHolder;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onStampClick(Post post);
        void onCommentSend(Post post, String commentText);
        void onViewCommentsClick(Post post);
        void onEditPost(Post post);
        void onDeletePost(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, OnPostInteractionListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
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

        holder.tvPostContent.setText(post.getContent());
        holder.tvUserName.setText(post.getUserId());
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        if (post.getComicId() != null && !post.getComicId().isEmpty()) {
            holder.tvHashtag.setText("# " + post.getComicId());
        } else {
            holder.tvHashtag.setText("# Discussion");
        }

        holder.tvHashtag.setOnClickListener(v -> {
            String comicName = post.getComicId();
            if (comicName != null && !comicName.isEmpty() && !comicName.equals("C01") && !comicName.equals("Discussion")) {
                Intent intent = new Intent(context, ComicListActivity.class);
                intent.putExtra("SEARCH_QUERY", comicName);
                context.startActivity(intent);
            }
        });

        holder.btnStamp.setOnClickListener(v -> {
            boolean isStamped = holder.btnStamp.getTag() != null && (boolean) holder.btnStamp.getTag();
            if (isStamped) {
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#A890D3"));
                holder.btnStamp.setTag(false);
            } else {
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#493373"));
                holder.btnStamp.setTag(true);
                if (listener != null) {
                    listener.onStampClick(post);
                }
            }
        });

        holder.tvCommentCount.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewCommentsClick(post);
            }
        });

        holder.btnSendComment.setOnClickListener(v -> {
            String commentText = holder.edtQuickComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                if (listener != null) listener.onCommentSend(post, commentText);
                post.setCommentCount(post.getCommentCount() + 1);
                holder.edtQuickComment.setText("");
                notifyItemChanged(position);
            }
        });

        holder.btnOptions.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(context, holder.btnOptions);
            popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditPost(post);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeletePost(post);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // 👉 Xử lý hiển thị danh sách ảnh dạng lưới 2 cột tối ưu
        if (post.getImageUris() != null && !post.getImageUris().isEmpty()) {
            holder.rvPostImages.setVisibility(View.VISIBLE);

            if (holder.rvPostImages.getLayoutManager() == null) {
                holder.rvPostImages.setLayoutManager(new GridLayoutManager(context, 2));
            }

            PostImageAdapter imageAdapter = new PostImageAdapter(context, post.getImageUris());
            holder.rvPostImages.setAdapter(imageAdapter);
        } else {
            holder.rvPostImages.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }
}