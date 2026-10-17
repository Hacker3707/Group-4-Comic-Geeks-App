package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ComicListActivity; // Hoặc Activity tìm kiếm truyện của nhóm trưởng
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.PostViewHolder;
import com.bumptech.glide.Glide;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private OnPostInteractionListener listener; // Khai báo Listener

    // Interface định nghĩa các hành động tương tác bài viết
    public interface OnPostInteractionListener {
        void onStampClick(Post post);
        void onCommentSend(Post post, String commentText);
        void onViewCommentsClick(Post post);
        void onEditPost(Post post);   // Thêm sự kiện Sửa
        void onDeletePost(Post post); // Thêm sự kiện Xóa
    }

    // Constructor nhận Listener
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

        // 👉 HIỂN THỊ TÊN BỘ TRUYỆN LÊN HASHTAG (# Tên truyện)
        if (post.getComicId() != null && !post.getComicId().isEmpty()) {
            holder.tvHashtag.setText("# " + post.getComicId());
        } else {
            holder.tvHashtag.setText("# Discussion");
        }

        // 👉 SỰ KIỆN 5: BẤM VÀO HASHTAG TÊN TRUYỆN -> MỞ TRANG TÌM KIẾM TRUYỆN (COMIC ZONE)
        holder.tvHashtag.setOnClickListener(v -> {
            String comicName = post.getComicId(); // Lấy tên truyện user đã nhập (ví dụ: "Naruto")
            if (comicName != null && !comicName.isEmpty() && !comicName.equals("C01") && !comicName.equals("Discussion")) {
                Intent intent = new Intent(context, ComicListActivity.class);
                // Truyền từ khóa tìm kiếm qua Intent để trang Comic Zone tự động lọc kết quả
                intent.putExtra("SEARCH_QUERY", comicName);
                context.startActivity(intent);
            }
        });

        // SỰ KIỆN 1: BẤM NÚT STAMP (Bật/Tắt màu tím - Toggle)
        holder.btnStamp.setOnClickListener(v -> {
            boolean isStamped = holder.btnStamp.getTag() != null && (boolean) holder.btnStamp.getTag();

            if (isStamped) {
                // Hủy stamp: Đổi về màu xám nhạt ban đầu
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#A890D3"));
                holder.btnStamp.setTag(false);
            } else {
                // Thả stamp: Đổi sang màu tím chủ đạo (#493373)
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#493373"));
                holder.btnStamp.setTag(true);
                if (listener != null) {
                    listener.onStampClick(post);
                }
            }
        });

        // SỰ KIỆN 2: BẤM SỐ ĐẾM BÌNH LUẬN (MỞ BOTTOM SHEET)
        holder.tvCommentCount.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewCommentsClick(post);
            }
        });

        // SỰ KIỆN 3: BẤM NÚT GỬI BÌNH LUẬN NHANH
        holder.btnSendComment.setOnClickListener(v -> {
            String commentText = holder.edtQuickComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                if (listener != null) listener.onCommentSend(post, commentText);
                post.setCommentCount(post.getCommentCount() + 1);
                holder.edtQuickComment.setText("");
                notifyItemChanged(position);
            }
        });

        // SỰ KIỆN 4: BẤM NÚT 3 CHẤM (MỞ MENU XÓA / SỬA)
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

        // ĐOẠN CODE HIỂN THỊ ẢNH BÀI VIẾT QUA GLIDE
        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            holder.imgPostImage.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUri())
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.imgPostImage);
        } else {
            holder.imgPostImage.setVisibility(View.GONE);
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