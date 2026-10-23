package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.StampDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;

public class StampRepository {
    private StampDao mStampDao;

    public StampRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mStampDao = db.stampDao();
    }
    public LiveData<List<Stamp>> getStampsByPost(String postId) {
        return mStampDao.getStampsByPost(postId);
    }

    public void insert(Stamp stamp) {
        new insertAsyncTask(mStampDao).execute(stamp);
    }

    public void update(Stamp stamp) {
        new updateAsyncTask(mStampDao).execute(stamp);
    }

    public void delete(Stamp stamp) {
        new deleteAsyncTask(mStampDao).execute(stamp);
    }

    private static class insertAsyncTask extends AsyncTask<Stamp, Void, Void> {
        private StampDao mAsyncTaskDao;
        insertAsyncTask(StampDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Stamp... params) {
            mAsyncTaskDao.insertStamp(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Stamp, Void, Void> {
        private StampDao mAsyncTaskDao;
        updateAsyncTask(StampDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Stamp... params) {
            mAsyncTaskDao.updateStamp(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Stamp, Void, Void> {
        private StampDao mAsyncTaskDao;
        deleteAsyncTask(StampDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Stamp... params) {
            mAsyncTaskDao.deleteStamp(params[0]);
            return null;
        }
    }
    public void uploadStampToFirestore(Stamp stamp, OnStampUploadListener listener) {
        Map<String, Object> stampMap = new HashMap<>();
        stampMap.put("stampId", stamp.getStampId());
        stampMap.put("userId", stamp.getUserId());
        stampMap.put("postId", stamp.getPostId());
        stampMap.put("commentId", stamp.getCommentId());
        stampMap.put("type", stamp.getType() != null ? stamp.getType().name() : "");
        stampMap.put("timestamp", stamp.getTimestamp());

        FirebaseFirestore.getInstance().collection("stamps")
                .document(stamp.getStampId())
                .set(stampMap)
                .addOnSuccessListener(aVoid -> {
                    insert(stamp); // Lưu vào Room cục bộ
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    public interface OnStampUploadListener {
        void onSuccess();
        void onFailure(String error);
    }
}