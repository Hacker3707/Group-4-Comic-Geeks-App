package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.CreatePostActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;

/**
 * Chức năng của bài viết dùng chung (stamp, bình luận, xem bình luận, sửa, xóa)
 * cho mọi màn hình dùng PostAdapter, ví dụ trang group.
 */
public class SharedPostInteraction implements PostAdapter.OnPostInteractionListener {

    private final AppCompatActivity activity;
    private final PostViewModel postViewModel;

    public SharedPostInteraction(AppCompatActivity activity, PostViewModel postViewModel) {
        this.activity = activity;
        this.postViewModel = postViewModel;
    }

    private String currentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return "guest";
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onStampClick(Post post) {
        Stamp stamp = new Stamp(UUID.randomUUID().toString(), currentUid(), StampType.LIKE);
        stamp.setPostId(post.getPostId());
        postViewModel.addStamp(stamp, post);
        Toast.makeText(activity, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCommentSend(Post post, String commentText) {
        if (commentText == null || commentText.trim().isEmpty()) {
            Toast.makeText(activity, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
            return;
        }
        Comment comment = new Comment(UUID.randomUUID().toString(), currentUid(),
                post.getPostId(), commentText.trim());
        if (currentUid().equals(post.getUserId())) {
            // Bình luận bài của chính mình thì không cần báo cho mình
            postViewModel.addComment(comment);
        } else {
            postViewModel.addComment(comment, post);
        }
        Toast.makeText(activity, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCommentsClick(Post post) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        sheet.setContentView(R.layout.layout_comment_sheet);

        RecyclerView rvComments = sheet.findViewById(R.id.recyclerViewComments);
        if (rvComments != null) {
            rvComments.setLayoutManager(new LinearLayoutManager(activity));
        }
        CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
        if (rvComments != null) {
            rvComments.setAdapter(commentAdapter);
        }

        CommentUsersWatcher usersWatcher = new CommentUsersWatcher(
                activity,
                new ViewModelProvider(activity).get(UserViewModel.class),
                commentAdapter);
        sheet.setOnDismissListener(d -> usersWatcher.stop());

        if (post.getPostId() != null) {
            postViewModel.getCommentsByPostId(post.getPostId()).observe(activity, realComments -> {
                if (realComments != null) {
                    commentAdapter.setComments(realComments);
                    usersWatcher.update(realComments);
                    if (!realComments.isEmpty() && rvComments != null) {
                        rvComments.scrollToPosition(realComments.size() - 1);
                    }
                }
            });
        }
        sheet.show();
    }

    @Override
    public void onDeletePost(Post post) {
        if (post != null) {
            postViewModel.deletePost(post);
            Toast.makeText(activity, "Đã xóa bài viết!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditPost(Post post) {
        Intent intent = new Intent(activity, CreatePostActivity.class);
        intent.putExtra("is_edit_mode", true);
        intent.putExtra("post_id", post.getPostId());
        intent.putExtra("post_content", post.getContent());
        intent.putExtra("post_comic_id", post.getComicId());
        intent.putStringArrayListExtra("post_image_urls",
                post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
        activity.startActivity(intent);
    }
}
