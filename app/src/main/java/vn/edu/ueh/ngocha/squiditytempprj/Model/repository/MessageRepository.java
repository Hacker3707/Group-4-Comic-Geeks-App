package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.MessageDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;

public class MessageRepository {
    private MessageDao mMessageDao;

    public MessageRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mMessageDao = db.messageDao();
    }

    public LiveData<List<Message>> getMessagesByChatRoomId(String chatRoomId) {
        return mMessageDao.getMessagesByChatRoomId(chatRoomId);
    }

    public void insert(Message message) {
        new insertAsyncTask(mMessageDao).execute(message);
    }

    public void delete(Message message) {
        new deleteAsyncTask(mMessageDao).execute(message);
    }

    public void markAsRead(String messageId) {
        new markAsReadAsyncTask(mMessageDao).execute(messageId);
    }

    public void deleteMessagesByChatRoomId(String chatRoomId) {
        new deleteByChatRoomAsyncTask(mMessageDao).execute(chatRoomId);
    }

    //======================== AsyncTask cho các thao tác trong Message ========================
    private static class insertAsyncTask extends AsyncTask<Message, Void, Void> {
        private MessageDao mAsyncTaskDao;
        insertAsyncTask(MessageDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.insertMessage(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Message, Void, Void> {
        private MessageDao mAsyncTaskDao;
        deleteAsyncTask(MessageDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.deleteMessage(params[0]);
            return null;
        }
    }

    private static class markAsReadAsyncTask extends AsyncTask<String, Void, Void> {
        private MessageDao mAsyncTaskDao;
        markAsReadAsyncTask(MessageDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.markAsRead(params[0]);
            return null;
        }
    }

    private static class deleteByChatRoomAsyncTask extends AsyncTask<String, Void, Void> {
        private MessageDao mAsyncTaskDao;
        deleteByChatRoomAsyncTask(MessageDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteMessagesByChatRoomId(params[0]);
            return null;
        }
    }
}