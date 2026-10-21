package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ComicDetailActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.CreatePostActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.SearchActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.PostViewHolder;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private OnPostInteractionListener listener;

    private final Map<String, Boolean> expandedStates = new HashMap<>();

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

        holder.tvUserName.setText(post.getUserId());

        // Lắng nghe và hiển thị chính xác số lượng comment thực tế từ cơ sở dữ liệu
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            PostViewModel postViewModel = new ViewModelProvider(activity).get(PostViewModel.class);
            if (post.getPostId() != null) {
                postViewModel.getCommentsByPostId(post.getPostId()).observe(activity, comments -> {
                    if (comments != null) {
                        int realCount = comments.size();
                        holder.tvCommentCount.setText(String.valueOf(realCount));
                        post.setCommentCount(realCount);
                    } else {
                        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
                    }
                });
            } else {
                holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
            }
        } else {
            holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
        }

        String content = post.getContent();
        String postId = post.getPostId() != null ? post.getPostId() : String.valueOf(position);
        boolean isExpanded = expandedStates.containsKey(postId) && Boolean.TRUE.equals(expandedStates.get(postId));

        if (content != null && !content.isEmpty()) {
            holder.tvPostContent.setVisibility(View.VISIBLE);

            if (isExpanded) {
                holder.tvPostContent.setMaxLines(Integer.MAX_VALUE);
                holder.tvPostContent.setEllipsize(null);
                holder.tvPostContent.setText(content + " Thu gọn");
            } else {
                holder.tvPostContent.setMaxLines(3);
                holder.tvPostContent.setEllipsize(TextUtils.TruncateAt.END);
                holder.tvPostContent.setText(content);
            }

            holder.tvPostContent.setOnClickListener(v -> {
                boolean currentExpanded = expandedStates.containsKey(postId) && Boolean.TRUE.equals(expandedStates.get(postId));
                expandedStates.put(postId, !currentExpanded);
                notifyItemChanged(position);
            });
        } else {
            holder.tvPostContent.setVisibility(View.GONE);
        }

        if (holder.tvLinkedComic != null) {
            String comicTitle = post.getComicTitle();
            String comicId = post.getComicId();

            if (comicTitle != null && !comicTitle.isEmpty()) {
                holder.tvLinkedComic.setVisibility(View.VISIBLE);
                holder.tvLinkedComic.setText("📖 " + comicTitle);

                holder.tvLinkedComic.setOnClickListener(v -> {
                    if (comicId != null && !comicId.isEmpty() && !comicId.equals("Discussion")) {
                        Intent intent = new Intent(context, ComicDetailActivity.class);
                        intent.putExtra("comic_id", comicId);
                        intent.putExtra("search_option", "MangaDex");
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Không tìm thấy mã ID truyện này!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.tvLinkedComic.setVisibility(View.GONE);
            }
        }

        String hashtag = post.getHashtag();
        if (hashtag != null && !hashtag.isEmpty()) {
            holder.tvHashtag.setText(hashtag);
        } else {
            holder.tvHashtag.setText("# Discussion");
        }

        holder.tvHashtag.setOnClickListener(v -> {
            String tagQuery = post.getHashtag();
            Intent intent = new Intent(context, SearchActivity.class);
            if (tagQuery != null && !tagQuery.isEmpty()) {
                intent.putExtra("SEARCH_QUERY", tagQuery);
            } else {
                intent.putExtra("SEARCH_QUERY", "Discussion");
            }
            context.startActivity(intent);
        });

        holder.btnStamp.setOnClickListener(v -> {
            boolean isStamped = holder.btnStamp.getTag() != null && (boolean) holder.btnStamp.getTag();
            if (isStamped) {
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#A890D3"));
                holder.btnStamp.setImageResource(R.drawable.ic_splash_unfill);
                holder.btnStamp.setTag(false);
                // Nếu muốn xử lý hủy stamp trên Firebase bạn có thể viết thêm ở đây sau
            } else {
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#493373"));
                holder.btnStamp.setImageResource(R.drawable.post_ink_splat);
                holder.btnStamp.setTag(true);

                // 👉 BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ GỌI ĐẨY STAMP LÊN FIREBASE
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
                    Intent intent = new Intent(context, CreatePostActivity.class);
                    intent.putExtra("is_edit_mode", true);
                    intent.putExtra("post_id", post.getPostId());
                    intent.putExtra("post_content", post.getContent());
                    intent.putExtra("post_comic_id", post.getComicId());
                    intent.putExtra("post_comic_title", post.getComicTitle());
                    intent.putExtra("post_hashtag", post.getHashtag());
                    intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                    context.startActivity(intent);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeletePost(post);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        List<String> imageUris = post.getImageUris();

        if (imageUris != null && !imageUris.isEmpty()) {
            holder.rvPostImages.setVisibility(View.VISIBLE);
            int imageCount = imageUris.size();

            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);

            // 👉 Đã sửa từ spanSize thành getSpanSize đúng chuẩn Android
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (imageCount == 1) {
                        return 2;
                    }
                    return 1;
                }
            });

            holder.rvPostImages.setLayoutManager(layoutManager);
            PostImageAdapter imageAdapter = new PostImageAdapter(context, imageUris);
            holder.rvPostImages.setAdapter(imageAdapter);
        } else {
            holder.rvPostImages.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { // 👉 Đã thêm từ khóa public vào đây
        return postList != null ? postList.size() : 0;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }
}