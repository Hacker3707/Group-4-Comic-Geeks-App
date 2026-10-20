package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.CommentRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.NotificationRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
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
        commentRepository.insert(comment);
    }

    // Bình luận và báo cho chủ bài viết
    public void addComment(Comment comment, Post post) {
        commentRepository.insert(comment);
        String actorName = notificationRepository.currentActorName(comment.getUserId());
        String text = comment.getContent();
        if (text != null && text.length() > 40) {
            text = text.substring(0, 40) + "...";
        }
        notificationRepository.sendNotification(post.getUserId(), comment.getUserId(), actorName,
                NotificationType.COMMENT, post.getPostId(),
                actorName + " đã bình luận bài viết của bạn: " + text);
    }

    public LiveData<List<Comment>> getCommentsByPostId(String postId) {
        return commentRepository.getCommentsByPostId(postId);
    }

    public void addStamp(Stamp stamp) {
        stampRepository.insert(stamp);
    }

    // Thả stamp và báo cho chủ bài viết
    public void addStamp(Stamp stamp, Post post) {
        stampRepository.insert(stamp);
        String actorName = notificationRepository.currentActorName(stamp.getUserId());
        notificationRepository.sendNotification(post.getUserId(), stamp.getUserId(), actorName,
                NotificationType.LIKE, post.getPostId(),
                actorName + " đã thả stamp vào bài viết của bạn");
    }

    public void uploadPostToFirestore(Post post, PostRepository.OnFirestoreUploadListener listener) {
        if (postRepository != null) {
            postRepository.uploadPostToFirestore(post, listener);
        }
    }
}