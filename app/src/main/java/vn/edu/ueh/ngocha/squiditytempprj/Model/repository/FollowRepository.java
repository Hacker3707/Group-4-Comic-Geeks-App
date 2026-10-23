package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.FollowDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Follow;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

/**
 * Follow được lưu ở 2 nơi:
 * - Firestore (collection "follows"): để hai tài khoản trên hai máy khác nhau thấy số follow của nhau.
 *   Mỗi lượt follow là 1 document, id = followerId + "_" + followingId.
 * - Room (bảng follows trong máy): giữ như cũ cho các chức năng đang dùng (ví dụ danh sách chat),
 *   và là dữ liệu dự phòng khi Firestore lỗi (ví dụ chưa mở quyền ghi).
 */
public class FollowRepository {

    private static final String TAG = "FollowRepository";
    private static final String COLLECTION = "follows";
    private static final String FIELD_FOLLOWER = "followerId";
    private static final String FIELD_FOLLOWING = "followingId";
    private static final String FIELD_TIMESTAMP = "timestamp";

    // Báo về danh sách id mỗi khi dữ liệu trên Firestore thay đổi
    public interface OnIdsChangedListener {
        void onIdsChanged(Set<String> ids);

        void onError();
    }

    // Báo về con số mỗi khi dữ liệu trên Firestore thay đổi
    public interface OnCountChangedListener {
        void onCountChanged(int count);

        void onError();
    }

    private final FollowDao followDao;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public FollowRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        followDao = db.followDao();
    }

    private static String documentId(String followerId, String followingId) {
        return followerId + "_" + followingId;
    }

    // =================================
    // Follow / Unfollow (ghi vào Firestore và Room)
    // =================================

    /**
     * Follow một người.
     * @return true nếu là lượt follow mới; false nếu tự follow chính mình hoặc đã follow rồi.
     */
    public boolean follow(String followerId, String followingId) {
        if (followerId == null || followingId == null || followerId.equals(followingId)) {
            return false;
        }

        pushFollowToFirestore(followerId, followingId);

        if (followDao.isFollowing(followerId, followingId)) {
            return false;
        }
        Follow follow = new Follow(UUID.randomUUID().toString(), followerId, followingId);
        return followDao.insert(follow) != -1;
    }

    public void unfollow(String followerId, String followingId) {
        if (followerId != null && followingId != null) {
            firestore.collection(COLLECTION)
                    .document(documentId(followerId, followingId))
                    .delete()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Không xoá được follow trên Firestore", e);
                        }
                    });
        }
        followDao.unfollow(followerId, followingId);
    }

    private void pushFollowToFirestore(String followerId, String followingId) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_FOLLOWER, followerId);
        data.put(FIELD_FOLLOWING, followingId);
        data.put(FIELD_TIMESTAMP, FieldValue.serverTimestamp());

        firestore.collection(COLLECTION)
                .document(documentId(followerId, followingId))
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Không ghi được follow lên Firestore", e);
                    }
                });
    }

    // =================================
    // Lắng nghe thời gian thực từ Firestore
    // =================================

    // Danh sách id những người userId đang follow
    public ListenerRegistration listenFollowingIds(String userId,
                                                   final OnIdsChangedListener listener) {
        return firestore.collection(COLLECTION)
                .whereEqualTo(FIELD_FOLLOWER, userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshots == null) {
                            Log.w(TAG, "Lỗi lắng nghe danh sách đang follow", error);
                            listener.onError();
                            return;
                        }
                        Set<String> ids = new HashSet<>();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            String followingId = document.getString(FIELD_FOLLOWING);
                            if (followingId != null) {
                                ids.add(followingId);
                            }
                        }
                        listener.onIdsChanged(ids);
                    }
                });
    }

    // Số người đang follow userId (Followers)
    public ListenerRegistration listenFollowerCount(String userId,
                                                    final OnCountChangedListener listener) {
        return listenCount(FIELD_FOLLOWING, userId, listener);
    }

    // Số người userId đang follow (Following)
    public ListenerRegistration listenFollowingCount(String userId,
                                                     final OnCountChangedListener listener) {
        return listenCount(FIELD_FOLLOWER, userId, listener);
    }

    private ListenerRegistration listenCount(String field, String userId,
                                             final OnCountChangedListener listener) {
        return firestore.collection(COLLECTION)
                .whereEqualTo(field, userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshots == null) {
                            Log.w(TAG, "Lỗi lắng nghe số follow", error);
                            listener.onError();
                            return;
                        }
                        listener.onCountChanged(snapshots.size());
                    }
                });
    }

    // =================================
    // Dữ liệu trong máy (Room)
    // =================================

    public boolean isFollowing(String followerId, String followingId) {
        return followDao.isFollowing(followerId, followingId);
    }

    public List<User> getFollowers(String userId) {
        return followDao.getFollowers(userId);
    }

    public List<User> getFollowing(String userId) {
        return followDao.getFollowing(userId);
    }

    public List<String> getFollowingIds(String userId) {
        return followDao.getFollowingIds(userId);
    }

    public int countFollowers(String userId) {
        return followDao.countFollowers(userId);
    }

    public int countFollowing(String userId) {
        return followDao.countFollowing(userId);
    }

    public void deleteAll() {
        followDao.deleteAll();
    }
}
