package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.PostDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;

public class PostRepository {
    private PostDao mPostDao;
    private LiveData<List<Post>> mAllPosts;

    public PostRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
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
}