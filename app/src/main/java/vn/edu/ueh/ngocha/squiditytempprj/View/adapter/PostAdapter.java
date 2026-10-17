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
    private OnPostInteractionListener listener; // 1. Khai báo Listener

    // 2. Tạo Interface định nghĩa các hành động
    public interface OnPostInteractionListener {
        void onStampClick(Post post);
        void onCommentSend(Post post, String commentText);
        void onViewCommentsClick(Post post);
    }

    // 3. Cập nhật Constructor để nhận Listener
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




        holder.btnStamp.setOnClickListener(v -> {
            holder.btnStamp.setColorFilter(context.getResources().getColor(android.R.color.holo_red_light));
            if (listener != null) listener.onStampClick(post);
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



            if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
                holder.imgPostImage.setVisibility(View.VISIBLE);
                try {

                    holder.imgPostImage.setImageURI(android.net.Uri.parse(post.getImageUri()));
                } catch (Exception e) {

                    holder.imgPostImage.setVisibility(View.GONE);
                }
            } else {

                holder.imgPostImage.setVisibility(View.GONE);
            }
        });
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