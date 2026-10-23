package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.NotificationDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;

/**
 * Thông báo lưu trên Firestore (collection "notifications") và đồng bộ về Room để hiện trong popup.
 *
 * Gửi:  khi có người thả stamp, bình luận, follow hoặc nhắn tin, gọi sendNotification() để ghi 1 document lên Firestore.
 * Nhận: startListening() lắng nghe các document có recipientId là mình (addSnapshotListener như bài 10.1),
 *       document mới thì chép vào Room, NotificationPopup đang observe Room nên tự cập nhật.
 *
 * Bảng notifications trong Room không tách theo người dùng, nên khi đổi tài khoản
 * thì xóa thông báo cũ rồi nhận lại từ Firestore theo tài khoản mới.
 */
public class NotificationRepository {

    private static final String TAG = "NotificationRepository";
    private static final String COLLECTION = "notifications";
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_OWNER_UID = "owner_uid";

    // Chỉ để chạy thử khi chưa có 2 tài khoản:
    // - cho phép tự thông báo cho chính mình
    // - Post hiện đang lưu userId cứng "Meringoc_17" nên cũng nhận thông báo gửi cho id này
    // Khi Post dùng userId thật (Firebase uid) thì đổi TEST_MODE = false.
    private static final boolean TEST_MODE = false;
    private static final String TEST_POST_OWNER_ID = "Meringoc_17";

    private final Application application;
    private NotificationDao mNotificationDao;
    private LiveData<List<Notification>> mAllNotifications;
    private LiveData<List<Notification>> mUnreadNotifications;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private ListenerRegistration snapshotRegistration;

    public NotificationRepository(Application application) {
        this.application = application;
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

    // Đánh dấu đã đọc ở Room, đồng thời cập nhật lên Firestore để lần cài lại không bị hiện là chưa đọc
    public void markAsRead(String notifId) {
        new markAsReadAsyncTask(mNotificationDao).execute(notifId);
        firestore.collection(COLLECTION).document(notifId)
                .update("isRead", true)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Thông báo mẫu chỉ có ở máy (không có trên Firestore) thì bỏ qua
                        Log.d(TAG, "Không cập nhật được isRead trên Firestore: " + e.getMessage());
                    }
                });
    }

    // =================================
    // Gửi thông báo lên Firestore
    // =================================

    /**
     * @param recipientId id người nhận (chủ bài viết, người được follow, hoặc người được nhắn tin)
     * @param actorId     id người thực hiện hành động
     * @param actorName   tên hiển thị của người thực hiện
     * @param type        LIKE, COMMENT, FOLLOW hoặc MESSAGE
     * @param postId      bài viết liên quan (null nếu là FOLLOW hoặc MESSAGE)
     * @param content     nội dung hiển thị
     */
    public void sendNotification(String recipientId, String actorId, String actorName,
                                 NotificationType type, String postId, String content) {
        if (recipientId == null) {
            return;
        }
        // Không tự báo cho chính mình, trừ khi đang chạy thử
        if (!TEST_MODE && recipientId.equals(actorId)) {
            return;
        }

        String notifId = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("notifId", notifId);
        data.put("recipientId", recipientId);
        data.put("actorId", actorId);
        data.put("actorName", actorName);
        data.put("type", type.name());
        data.put("postId", postId);
        data.put("content", content);
        data.put("isRead", false);
        data.put("timestamp", new Date());

        firestore.collection(COLLECTION).document(notifId)
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Gửi thông báo thất bại: " + e.getMessage());
                    }
                });
    }

    // Tên hiển thị của người đang đăng nhập, nếu chưa có thì dùng id dự phòng
    public String currentActorName(String fallbackId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        return fallbackId;
    }

    // =================================
    // Nhận thông báo từ Firestore
    // =================================

    // Gọi một lần. Tự bật/tắt lắng nghe theo trạng thái đăng nhập (đăng nhập xong là nhận được ngay)
    public void startListening() {
        if (authStateListener != null) {
            return;
        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                stopSnapshotListener();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    prepareAndListen(user.getUid());
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    public void stopListening() {
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
            authStateListener = null;
        }
        stopSnapshotListener();
    }

    // Nếu máy đang giữ thông báo của tài khoản khác thì xóa đi rồi mới nghe tài khoản này
    private void prepareAndListen(final String uid) {
        final SharedPreferences prefs =
                application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String owner = prefs.getString(KEY_OWNER_UID, null);
        if (uid.equals(owner)) {
            listenForUser(uid);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mNotificationDao.deleteAll();
                prefs.edit().putString(KEY_OWNER_UID, uid).apply();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Chỉ nghe nếu tài khoản vẫn là tài khoản này (tránh đổi tài khoản giữa chừng)
                        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
                        if (authStateListener != null && current != null
                                && uid.equals(current.getUid())) {
                            stopSnapshotListener();
                            listenForUser(uid);
                        }
                    }
                });
            }
        }).start();
    }

    private void listenForUser(String uid) {
        List<String> recipientIds = new ArrayList<>();
        recipientIds.add(uid);
        if (TEST_MODE) {
            recipientIds.add(TEST_POST_OWNER_ID);
        }

        snapshotRegistration = firestore.collection(COLLECTION)
                .whereIn("recipientId", recipientIds)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Lỗi nhận thông báo: " + error.getMessage());
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                insert(toNotification(change.getDocument()));
                            }
                        }
                    }
                });
    }

    private void stopSnapshotListener() {
        if (snapshotRegistration != null) {
            snapshotRegistration.remove();
            snapshotRegistration = null;
        }
    }

    private Notification toNotification(QueryDocumentSnapshot doc) {
        NotificationType type = NotificationType.SYSTEM;
        String typeName = doc.getString("type");
        if (typeName != null) {
            try {
                type = NotificationType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                type = NotificationType.SYSTEM;
            }
        }
        Boolean read = doc.getBoolean("isRead");
        Date time = doc.getDate("timestamp");
        return new Notification(
                doc.getId(),
                doc.getString("content"),
                type,
                read != null && read,
                time != null ? time : new Date());
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