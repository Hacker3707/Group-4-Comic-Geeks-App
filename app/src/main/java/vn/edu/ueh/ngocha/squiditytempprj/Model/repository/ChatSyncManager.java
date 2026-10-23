package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Follow;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

/**
 * Đồng bộ follow, phòng chat và tin nhắn giữa Firestore và Room.
 *
 * Firestore là nơi lưu chung của mọi tài khoản:
 *   follows/{followerId_followingId}  : ai theo dõi ai
 *   chatRooms/{chatRoomId}            : phòng chat giữa 2 người
 *   messages/{messageId}              : từng tin nhắn (chữ hoặc ảnh)
 * Mỗi máy lắng nghe (addSnapshotListener) các document có mình trong "participants"
 * rồi chép vào Room, màn hình đang observe Room nên tự cập nhật.
 *
 * Cách dùng: ChatSyncManager.getInstance(context).start() (gọi nhiều lần cũng chỉ chạy một lần).
 */
public class ChatSyncManager {

    private static final String TAG = "ChatSyncManager";
    private static final String FOLLOWS = "follows";
    private static final String CHAT_ROOMS = "chatRooms";
    private static final String MESSAGES = "messages";
    private static final String USERS = "users";

    private static ChatSyncManager instance;

    public static synchronized ChatSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new ChatSyncManager(context.getApplicationContext());
        }
        return instance;
    }

    private final AppDatabase db;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ListenerRegistration> registrations = new ArrayList<>();
    // Các user đã tải thông tin trong lần chạy app này, để không tải lặp lại
    private final Set<String> fetchedUserIds = new HashSet<>();
    private FirebaseAuth.AuthStateListener authStateListener;

    private ChatSyncManager(Context appContext) {
        db = AppDatabase.getDatabase(appContext);
    }

    // =================================
    // Nhận dữ liệu từ Firestore
    // =================================

    // Tự bật/tắt lắng nghe theo trạng thái đăng nhập
    public synchronized void start() {
        if (authStateListener != null) {
            return;
        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                stopListeners();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    listenFollows(user.getUid());
                    listenChatRooms(user.getUid());
                    listenMessages(user.getUid());
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    private void stopListeners() {
        for (ListenerRegistration registration : registrations) {
            registration.remove();
        }
        registrations.clear();
    }

    private void listenFollows(String uid) {
        registrations.add(firestore.collection(FOLLOWS)
                .whereArrayContains("participants", uid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Lỗi nhận follow: " + error.getMessage());
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            final String docId = change.getDocument().getId();
                            final String followerId = change.getDocument().getString("followerId");
                            final String followingId = change.getDocument().getString("followingId");
                            if (followerId == null || followingId == null) {
                                continue;
                            }
                            final boolean removed = change.getType() == DocumentChange.Type.REMOVED;
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (removed) {
                                        db.followDao().unfollow(followerId, followingId);
                                    } else {
                                        db.followDao().insert(new Follow(docId, followerId, followingId));
                                    }
                                }
                            });
                            if (!removed) {
                                fetchUser(followerId);
                                fetchUser(followingId);
                            }
                        }
                    }
                }));
    }

    private void listenChatRooms(String uid) {
        registrations.add(firestore.collection(CHAT_ROOMS)
                .whereArrayContains("participants", uid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Lỗi nhận phòng chat: " + error.getMessage());
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.REMOVED) {
                                continue;
                            }
                            final String roomId = change.getDocument().getId();
                            final String user1Id = change.getDocument().getString("user1Id");
                            final String user2Id = change.getDocument().getString("user2Id");
                            if (user1Id == null || user2Id == null) {
                                continue;
                            }
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    db.chatRoomDao().insertChatRoom(new ChatRoom(roomId, user1Id, user2Id));
                                }
                            });
                            fetchUser(user1Id);
                            fetchUser(user2Id);
                        }
                    }
                }));
    }

    private void listenMessages(final String uid) {
        registrations.add(firestore.collection(MESSAGES)
                .whereArrayContains("participants", uid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Lỗi nhận tin nhắn: " + error.getMessage());
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() != DocumentChange.Type.ADDED) {
                                continue;
                            }
                            QueryDocumentSnapshot doc = change.getDocument();
                            String senderId = doc.getString("senderId");
                            String chatRoomId = doc.getString("chatRoomId");
                            if (senderId == null || chatRoomId == null) {
                                continue;
                            }
                            Date time = doc.getDate("timestamp");
                            // Tin của chính mình coi như đã đọc
                            final Message message = new Message(
                                    doc.getId(),
                                    doc.getString("content"),
                                    time != null ? time : new Date(),
                                    uid.equals(senderId),
                                    senderId,
                                    chatRoomId);
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    db.messageDao().insertMessage(message);
                                }
                            });
                        }
                    }
                }));
    }

    // Lấy tên và avatar của người dùng từ Firestore (collection "users") lưu vào Room để hiện trong danh sách chat
    private void fetchUser(final String userId) {
        if (userId == null) {
            return;
        }
        synchronized (fetchedUserIds) {
            if (!fetchedUserIds.add(userId)) {
                return;
            }
        }
        firestore.collection(USERS).document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc == null || !doc.exists()) {
                            return;
                        }
                        String username = doc.getString("username");
                        String email = doc.getString("email");
                        String avatarUrl = doc.getString("avatarUrl");
                        if (username == null || username.trim().isEmpty()) {
                            if (email != null && email.contains("@")) {
                                username = email.substring(0, email.indexOf('@'));
                            } else {
                                username = userId;
                            }
                        }
                        final User user = new User(userId, username,
                                email != null ? email : "", "",
                                avatarUrl != null ? avatarUrl : "");
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                db.userDao().upsert(user);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        synchronized (fetchedUserIds) {
                            fetchedUserIds.remove(userId);
                        }
                        Log.e(TAG, "Không tải được user " + userId + ": " + e.getMessage());
                    }
                });
    }

    // =================================
    // Ghi dữ liệu lên Firestore
    // =================================

    public void pushFollow(String followerId, String followingId) {
        if (followerId == null || followingId == null || followerId.equals(followingId)) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("followerId", followerId);
        data.put("followingId", followingId);
        data.put("participants", Arrays.asList(followerId, followingId));
        data.put("timestamp", new Date());
        firestore.collection(FOLLOWS).document(followerId + "_" + followingId)
                .set(data)
                .addOnFailureListener(logFailure("Gửi follow thất bại"));
    }

    public void pushUnfollow(String followerId, String followingId) {
        if (followerId == null || followingId == null) {
            return;
        }
        firestore.collection(FOLLOWS).document(followerId + "_" + followingId)
                .delete()
                .addOnFailureListener(logFailure("Hủy follow thất bại"));
    }

    // Ghi tin nhắn và phòng chat để người nhận (máy khác) nhận được
    public void pushMessage(Message message, String recipientId) {
        if (message == null || recipientId == null || recipientId.isEmpty()) {
            return;
        }
        String senderId = message.getSenderId();
        List<String> participants = Arrays.asList(senderId, recipientId);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", message.getMessageId());
        messageData.put("chatRoomId", message.getChatRoomId());
        messageData.put("senderId", senderId);
        messageData.put("content", message.getContent());
        messageData.put("timestamp", message.getTimestamp() != null ? message.getTimestamp() : new Date());
        messageData.put("participants", participants);
        firestore.collection(MESSAGES).document(message.getMessageId())
                .set(messageData)
                .addOnFailureListener(logFailure("Gửi tin nhắn thất bại"));

        boolean senderFirst = senderId.compareTo(recipientId) <= 0;
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("chatRoomId", message.getChatRoomId());
        roomData.put("user1Id", senderFirst ? senderId : recipientId);
        roomData.put("user2Id", senderFirst ? recipientId : senderId);
        roomData.put("participants", participants);
        firestore.collection(CHAT_ROOMS).document(message.getChatRoomId())
                .set(roomData, SetOptions.merge())
                .addOnFailureListener(logFailure("Tạo phòng chat thất bại"));
    }

    private OnFailureListener logFailure(final String label) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, label + ": " + e.getMessage());
            }
        };
    }
}