package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ChatRoomDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;

public class ChatRoomRepository {
    private ChatRoomDao mChatRoomDao;

    public ChatRoomRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mChatRoomDao = db.chatRoomDao();
    }

    public LiveData<List<ChatRoom>> getChatRoomsByUserId(String userId) {
        return mChatRoomDao.getChatRoomsByUserId(userId);
    }

    public LiveData<ChatRoom> getChatRoomById(String chatRoomId) {
        return mChatRoomDao.getChatRoomById(chatRoomId);
    }

    public void insert(ChatRoom chatRoom) {
        new insertAsyncTask(mChatRoomDao).execute(chatRoom);
    }

    public void delete(ChatRoom chatRoom) {
        new deleteAsyncTask(mChatRoomDao).execute(chatRoom);
    }

    //======================== AsyncTask cho các thao tác trong ChatRoom ========================
    private static class insertAsyncTask extends AsyncTask<ChatRoom, Void, Void> {
        private ChatRoomDao mAsyncTaskDao;
        insertAsyncTask(ChatRoomDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final ChatRoom... params) {
            mAsyncTaskDao.insertChatRoom(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<ChatRoom, Void, Void> {
        private ChatRoomDao mAsyncTaskDao;
        deleteAsyncTask(ChatRoomDao dao) { mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final ChatRoom... params) {
            mAsyncTaskDao.deleteChatRoom(params[0]);
            return null;
        }
    }
}