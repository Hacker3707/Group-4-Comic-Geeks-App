package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.CommentDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;

public class CommentRepository {
    private CommentDao mCommentDao;

    public CommentRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mCommentDao = db.commentDao();
    }

    public LiveData<List<Comment>> getCommentsByPost(String postId) {
        return mCommentDao.getCommentsByPost(postId);
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
}