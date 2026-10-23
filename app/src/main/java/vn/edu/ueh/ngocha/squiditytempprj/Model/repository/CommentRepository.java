package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.CommentDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;

public class CommentRepository {
    private CommentDao mCommentDao;

    public CommentRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mCommentDao = db.commentDao();
    }
    public LiveData<List<Comment>> getCommentsByPostId(String postId) {
        return mCommentDao.getCommentsByPostId(postId);
    }

    public void insert(Comment comment) {
        new insertAsyncTask(mCommentDao).execute(comment);
    }

    public void update(Comment comment) {
        new updateAsyncTask(mCommentDao).execute(comment);
    }

    public void delete(Comment comment) {
        new deleteAsyncTask(mCommentDao).execute(comment);
    }

    private static class insertAsyncTask extends AsyncTask<Comment, Void, Void> {
        private CommentDao mAsyncTaskDao;
        insertAsyncTask(CommentDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Comment... params) {
            mAsyncTaskDao.insertComment(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Comment, Void, Void> {
        private CommentDao mAsyncTaskDao;
        updateAsyncTask(CommentDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Comment... params) {
            mAsyncTaskDao.updateComment(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Comment, Void, Void> {
        private CommentDao mAsyncTaskDao;
        deleteAsyncTask(CommentDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Comment... params) {
            mAsyncTaskDao.deleteComment(params[0]);
            return null;
        }
    }
    public void uploadCommentToFirestore(Comment comment, OnCommentUploadListener listener) {
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("commentId", comment.getCommentId());
        commentMap.put("userId", comment.getUserId());
        commentMap.put("postId", comment.getPostId());
        commentMap.put("content", comment.getContent());
        commentMap.put("timestamp", comment.getTimestamp());

        FirebaseFirestore.getInstance().collection("comments")
                .document(comment.getCommentId())
                .set(commentMap)
                .addOnSuccessListener(aVoid -> {
                    insert(comment); // Lưu vào Room cục bộ
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    public interface OnCommentUploadListener {
        void onSuccess();
        void onFailure(String error);
    }
}