package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.CommentRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.StampRepository;

public class PostViewModel extends AndroidViewModel {
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private StampRepository stampRepository;

    private LiveData<List<Post>> allPosts;

    public PostViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
        commentRepository = new CommentRepository(application);
        stampRepository = new StampRepository(application);
        allPosts = postRepository.getAllPosts();
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public void insertPost(Post post) {
        postRepository.insert(post);
    }

    // 👉 HÀM XÓA BÀI VIẾT
    public void deletePost(Post post) {
        if (postRepository != null && post != null) {
            postRepository.deletePost(post, post.getPostId());
        }
    }

    // 👉 HÀM CẬP NHẬT/CHỈNH SỬA BÀI VIẾT
    public void updatePost(Post post) {
        if (postRepository != null && post != null) {
            postRepository.updatePostInFirestore(post);
        }
    }

    public void addComment(Comment comment) {
        commentRepository.insert(comment);
    }

    public LiveData<List<Comment>> getCommentsByPostId(String postId) {
        return commentRepository.getCommentsByPostId(postId);
    }

    public void addStamp(Stamp stamp) {
        stampRepository.insert(stamp);
    }

    // 👉 HÀM ĐẨY BÀI LÊN FIRESTORE KHI TẠO MỚI
    public void uploadPostToFirestore(Post post, PostRepository.OnFirestoreUploadListener listener) {
        if (postRepository != null) {
            postRepository.uploadPostToFirestore(post, listener);
        }
    }
}