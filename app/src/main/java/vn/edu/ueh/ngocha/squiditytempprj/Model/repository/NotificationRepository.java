package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.NotificationDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;

public class NotificationRepository {
    private NotificationDao mNotificationDao;
    private LiveData<List<Notification>> mAllNotifications;
    private LiveData<List<Notification>> mUnreadNotifications;

    public NotificationRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mNotificationDao = db.notificationDao();
        mAllNotifications = mNotificationDao.getAllNotifications();
        mUnreadNotifications = mNotificationDao.getUnreadNotifications();
    }

    public LiveData<List<Notification>> getAllNotifications() {
        return mAllNotifications;
    }

    public LiveData<List<Notification>> getUnreadNotifications() {
        return mUnreadNotifications;
    }

    public void insert(Notification notification) {
        new insertAsyncTask(mNotificationDao).execute(notification);
    }

    public void update(Notification notification) {
        new updateAsyncTask(mNotificationDao).execute(notification);
    }

    public void delete(Notification notification) {
        new deleteAsyncTask(mNotificationDao).execute(notification);
    }

    public void markAsRead(String notifId) {
        new markAsReadAsyncTask(mNotificationDao).execute(notifId);
    }

    //======================== AsyncTask cho các thao tác trong Notification ========================
    private static class insertAsyncTask extends AsyncTask<Notification, Void, Void> {
        private NotificationDao mAsyncTaskDao;
        insertAsyncTask(NotificationDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Notification... params) {
            mAsyncTaskDao.insertNotification(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Notification, Void, Void> {
        private NotificationDao mAsyncTaskDao;
        updateAsyncTask(NotificationDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Notification... params) {
            mAsyncTaskDao.updateNotification(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Notification, Void, Void> {
        private NotificationDao mAsyncTaskDao;
        deleteAsyncTask(NotificationDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Notification... params) {
            mAsyncTaskDao.deleteNotification(params[0]);
            return null;
        }
    }

    private static class markAsReadAsyncTask extends AsyncTask<String, Void, Void> {
        private NotificationDao mAsyncTaskDao;
        markAsReadAsyncTask(NotificationDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.markAsRead(params[0]);
            return null;
        }
    }
}