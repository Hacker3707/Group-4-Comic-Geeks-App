package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import android.app.Application;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;

/**
 * Follow lưu trên Firestore (collection "follows") để 2 tài khoản trên 2 máy khác nhau thấy nhau.
 *
 * 1 document = 1 lượt follow, id = "{followerId}_{followingId}" nên không bao giờ follow trùng.
 *   followerId  : người đi follow
 *   followingId : người được follow
 *
 * Số "Followers" của A = số document có followingId = A
 * Số "Following" của A = số document có followerId  = A
 *
 * Dùng addSnapshotListener nên khi 1 máy follow/unfollow, máy kia cập nhật số ngay không cần reload.
 */
public class FollowRemoteRepository {

    private static final String TAG = "FollowRemoteRepository";
    private static final String COLLECTION = "follows";

    public interface Callback<T> {
        void onResult(T value);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final NotificationRepository notificationRepository;

    public FollowRemoteRepository(Application application) {
        notificationRepository = new NotificationRepository(application);
    }

    private static String docId(String followerId, String followingId) {
        return followerId + "_" + followingId;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Follow 1 người. Đã follow rồi hoặc tự follow chính mình thì bỏ qua. */
    public void follow(final String followerId, final String followingId) {
        if (isBlank(followerId) || isBlank(followingId) || followerId.equals(followingId)) {
            return;
        }
        final com.google.firebase.firestore.DocumentReference ref =
                firestore.collection(COLLECTION).document(docId(followerId, followingId));

        ref.get().addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot != null && snapshot.exists()) {
                    return; // đã follow rồi, không gửi thông báo lần nữa
                }
                Map<String, Object> data = new HashMap<>();
                data.put("followerId", followerId);
                data.put("followingId", followingId);
                data.put("timestamp", new Date());
                ref.set(data);

                // Báo cho người được follow
                String actorName = notificationRepository.currentActorName(followerId);
                notificationRepository.sendNotification(followingId, followerId, actorName,
                        NotificationType.FOLLOW, null, Str.t(R.string.notif_followed_you, actorName));
            }
        }).addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Follow thất bại: " + e.getMessage());
            }
        });
    }

    public void unfollow(String followerId, String followingId) {
        if (isBlank(followerId) || isBlank(followingId)) {
            return;
        }
        firestore.collection(COLLECTION).document(docId(followerId, followingId)).delete();
    }

    /** Lắng nghe SỐ NGƯỜI ĐANG FOLLOW userId (số Followers). */
    public ListenerRegistration listenFollowerCount(String userId, final Callback<Integer> callback) {
        return firestore.collection(COLLECTION)
                .whereEqualTo("followingId", userId)
                .addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onEvent(com.google.firebase.firestore.QuerySnapshot snapshots,
                                        com.google.firebase.firestore.FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Lỗi đếm followers: " + error.getMessage());
                            return;
                        }
                        if (snapshots != null) {
                            callback.onResult(snapshots.size());
                        }
                    }
                });
    }

    /**
     * Lắng nghe DANH SÁCH id mà userId đang follow.
     * Dùng để đếm Following và để biết "mình có đang follow người này không" (đổi nút Follow/Following).
     */
    public ListenerRegistration listenFollowingIds(String userId, final Callback<Set<String>> callback) {
        return firestore.collection(COLLECTION)
                .whereEqualTo("followerId", userId)
                .addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onEvent(com.google.firebase.firestore.QuerySnapshot snapshots,
                                        com.google.firebase.firestore.FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Lỗi đọc following: " + error.getMessage());
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        Set<String> ids = new HashSet<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String id = doc.getString("followingId");
                            if (id != null) {
                                ids.add(id);
                            }
                        }
                        callback.onResult(ids);
                    }
                });
    }
}
