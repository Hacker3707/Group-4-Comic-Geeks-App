package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.PostDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import java.util.HashMap;
import java.util.Map;

public class PostRepository {
    private PostDao mPostDao;
    private LiveData<List<Post>> mAllPosts;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final PostSyncManager syncManager;

    public PostRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        syncManager = PostSyncManager.getInstance(application);
        mPostDao = db.postDao();
        mAllPosts = mPostDao.getAllPosts();
    }
    public LiveData<List<Post>> getAllPosts() {
        return mAllPosts;
    }
    public LiveData<List<Post>> getPostsByComic(String comicId) {
        return mPostDao.getPostsByComic(comicId);
    }
    public void insert(Post post) {
        new insertAsyncTask(mPostDao).execute(post);
    }

    public void update(Post post) {
        new updateAsyncTask(mPostDao).execute(post);
    }

    public void delete(Post post) {
        new deleteAsyncTask(mPostDao).execute(post);
    }

    //======================== AsyncTask cho các thao tác trong Post ========================
    private static class insertAsyncTask extends AsyncTask<Post, Void, Void> {
        private PostDao mAsyncTaskDao;
        insertAsyncTask(PostDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Post... params) {
            mAsyncTaskDao.insertPost(params[0]);
            return null;
        }
    }
    private static class updateAsyncTask extends AsyncTask<Post, Void, Void> {
        private PostDao mAsyncTaskDao;
        updateAsyncTask(PostDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Post... params) {
            mAsyncTaskDao.updatePost(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Post, Void, Void> {
        private PostDao mAsyncTaskDao;
        deleteAsyncTask(PostDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Post... params) {
            mAsyncTaskDao.deletePost(params[0]);
            return null;
        }
    }

    public void uploadPostToFirestore(Post post, OnFirestoreUploadListener listener) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postId", post.getPostId());
        postMap.put("userId", post.getUserId());
        postMap.put("groupId", post.getGroupId());
        postMap.put("comicId", post.getComicId());
        postMap.put("comicTitle", post.getComicTitle());
        postMap.put("hashtag", post.getHashtag());
        postMap.put("content", post.getContent());
        postMap.put("imageUris", post.getImageUris());
        postMap.put("timestamp", post.getTimestamp());
        postMap.put("commentCount", post.getCommentCount());
        postMap.put("hashtag", post.getHashtag());
        postMap.put("comicTitle", post.getComicTitle());

        firestore.collection("posts").document(post.getPostId())
                .set(postMap)
                .addOnSuccessListener(aVoid -> {
                    insert(post); // Lưu cục bộ Room
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    public void deletePost(Post post, String postId) {
        firestore.collection("posts").document(postId).delete();
        syncManager.deleteCommentsOfPostRemote(postId); // Xóa luôn bình luận của bài
        delete(post); // Xóa cục bộ
    }

    // 👉 Hàm cập nhật bài viết chuẩn, đồng bộ cả Firebase lẫn Room Database cục bộ
    public void updatePostInFirestore(Post post, OnFirestoreUploadListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("content", post.getContent());
        updates.put("comicId", post.getComicId());
        updates.put("imageUris", post.getImageUris()); // 👉 Sửa thành imageUris (số nhiều)
        updates.put("hashtag", post.getHashtag());
        updates.put("comicTitle", post.getComicTitle());

        firestore.collection("posts").document(post.getPostId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    update(post); // Cập nhật đồng thời vào Room Database cục bộ
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    public interface OnFirestoreUploadListener {
        void onSuccess();
        void onFailure(String error);
    }
}