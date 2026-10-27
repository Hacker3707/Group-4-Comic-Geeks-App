package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.CommentRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.NotificationRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.StampRepository;

public class PostViewModel extends AndroidViewModel {
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private StampRepository stampRepository;
    private NotificationRepository notificationRepository;

    private LiveData<List<Post>> allPosts;

    public PostViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
        commentRepository = new CommentRepository(application);
        stampRepository = new StampRepository(application);
        notificationRepository = new NotificationRepository(application);
        allPosts = postRepository.getAllPosts();

        // Lắng nghe bài viết và bình luận trên Firestore, chép về Room cho mọi màn hình dùng chung
        PostSyncManager.getInstance(application).start();
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public void insertPost(Post post) {
        postRepository.insert(post);
    }

    public void deletePost(Post post) {
        if (postRepository != null && post != null) {
            postRepository.deletePost(post, post.getPostId());
        }
    }

    public void updatePost(Post post) {
        if (postRepository != null && post != null) {
            postRepository.updatePostInFirestore(post, new PostRepository.OnFirestoreUploadListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(String error) {
                }
            });
        }
    }

    public void addComment(Comment comment) {
        commentRepository.uploadCommentToFirestore(comment, null);
    }
    public void addComment(Comment comment, Post post) {
        commentRepository.uploadCommentToFirestore(comment, new CommentRepository.OnCommentUploadListener() {
            @Override
            public void onSuccess() {
                if (post != null && post.getPostId() != null) {
                    // Lấy tổng số lượng comment thực tế trên Firestore để cập nhật chính xác
                    FirebaseFirestore.getInstance().collection("comments")
                            .whereEqualTo("postId", post.getPostId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                int actualCount = queryDocumentSnapshots.size();
                                post.setCommentCount(actualCount);

                                FirebaseFirestore.getInstance().collection("posts")
                                        .document(post.getPostId())
                                        .update("commentCount", actualCount);
                            });
                }
            }

            @Override
            public void onFailure(String error) {
            }
        });

        String actorName = notificationRepository.currentActorName(comment.getUserId());
        String text = comment.getContent();
        if (text != null && text.length() > 40) {
            text = text.substring(0, 40) + "...";
        }
        final String actorId = comment.getUserId();
        final String postOwnerId = post.getUserId();
        final String postId = post.getPostId();
        final String shownText = text;
        final String actor = actorName;
        if (comment.getParentId() == null || comment.getParentId().trim().isEmpty()) {
            notificationRepository.sendNotification(postOwnerId, actorId, actor,
                    NotificationType.COMMENT, postId,
                    Str.t(R.string.notif_commented, actor, shownText));
            return;
        }
        // Đây là câu trả lời một bình luận: báo cho người viết bình luận gốc
        FirebaseFirestore.getInstance().collection("comments").document(comment.getParentId()).get()
                .addOnCompleteListener(task -> {
                    String parentAuthor = null;
                    if (task.isSuccessful() && task.getResult() != null) {
                        parentAuthor = task.getResult().getString("userId");
                    }
                    if (parentAuthor != null) {
                        notificationRepository.sendNotification(parentAuthor, actorId, actor,
                                NotificationType.COMMENT, postId,
                                Str.t(R.string.notif_replied, actor, shownText));
                    }
                    // Chủ bài viết vẫn được báo có bình luận mới (trừ khi đã nhận thông báo trả lời ở trên)
                    if (postOwnerId != null && !postOwnerId.equals(parentAuthor)) {
                        notificationRepository.sendNotification(postOwnerId, actorId, actor,
                                NotificationType.COMMENT, postId,
                                Str.t(R.string.notif_commented, actor, shownText));
                    }
                });
    }

    public LiveData<List<Comment>> getAllComments() {
        return commentRepository.getAllComments();
    }

    public LiveData<List<Comment>> getCommentsByPostId(String postId) {
        return commentRepository.getCommentsByPostId(postId);
    }

    public void addStamp(Stamp stamp) {
        stampRepository.uploadStampToFirestore(stamp, null);
    }

    public void addStamp(Stamp stamp, Post post) {
        stampRepository.uploadStampToFirestore(stamp, new StampRepository.OnStampUploadListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(String error) {
            }
        });

        String actorName = notificationRepository.currentActorName(stamp.getUserId());
        notificationRepository.sendNotification(post.getUserId(), stamp.getUserId(), actorName,
                NotificationType.LIKE, post.getPostId(),
                Str.t(R.string.notif_stamped, actorName));
    }

    public void uploadPostToFirestore(Post post, PostRepository.OnFirestoreUploadListener listener) {
        if (postRepository != null) {
            postRepository.uploadPostToFirestore(post, listener);
        }
    }
}