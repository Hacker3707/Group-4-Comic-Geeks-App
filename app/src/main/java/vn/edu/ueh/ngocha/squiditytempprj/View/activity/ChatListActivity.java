
package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ChatSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ChatItemAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ChatViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.FollowViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;

/**
 * Màn danh sách chat: hiện các cuộc trò chuyện của tài khoản đang đăng nhập.
 * Nút "+" cho chọn người để chat (chỉ những người theo dõi qua lại) hoặc tìm bạn bằng Gmail để theo dõi.
 * Dữ liệu follow, phòng chat, tin nhắn được ChatSyncManager đồng bộ từ Firestore về Room.
 * Activity chỉ gọi ViewModel, không gọi DAO trực tiếp.
 */
public class ChatListActivity extends BaseActivity {

    private ChatViewModel chatViewModel;
    private UserViewModel userViewModel;
    private FollowViewModel followViewModel;

    private ChatItemAdapter chatRoomAdapter;
    private RecyclerView rcvChatRooms;
    private TextView tvChatListEmpty;

    private String currentUserId;
    private List<ChatRoom> currentChatRooms = new ArrayList<>();

    // Dữ liệu follow tải bất đồng bộ, chỉ hiện hộp thoại khi đã có đủ cả hai danh sách
    private List<User> followers;
    private List<User> following;
    private boolean waitingForFollowData = false;

    private ChatItemAdapter.OnChatRoomClickListener onChatRoomClickListener =
            new ChatItemAdapter.OnChatRoomClickListener() {
                @Override
                public void onChatRoomClick(ChatRoom chatRoom, String otherUserId, String otherUserName) {
                    openChat(chatRoom.getChatRoomId(), otherUserId, otherUserName);
                }
            };

    // Thanh trên và thanh bên do BaseActivity lo, Activity chỉ cung cấp layout nội dung
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_chat_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chat cần tài khoản thật: chưa đăng nhập thì chuyển sang màn đăng nhập
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để dùng chat", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        currentUserId = user.getUid();

        // Bắt đầu nhận follow, phòng chat và tin nhắn từ Firestore
        ChatSyncManager.getInstance(this).start();

        rcvChatRooms = findViewById(R.id.rcvChatRooms);
        tvChatListEmpty = findViewById(R.id.tvChatListEmpty);
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);

        chatRoomAdapter = new ChatItemAdapter(currentUserId, onChatRoomClickListener);
        rcvChatRooms.setLayoutManager(new LinearLayoutManager(this));
        rcvChatRooms.setAdapter(chatRoomAdapter);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        followViewModel = new ViewModelProvider(this).get(FollowViewModel.class);

        observeData();

        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMutualFollowDialog();
            }
        });
    }

    // Observer đặt trong Activity: ViewModel cấp LiveData, Activity lắng nghe và cập nhật giao diện
    private void observeData() {
        chatViewModel.getChatRooms(currentUserId).observe(this, new Observer<List<ChatRoom>>() {
            @Override
            public void onChanged(List<ChatRoom> chatRooms) {
                currentChatRooms = (chatRooms != null) ? chatRooms : new ArrayList<ChatRoom>();
                chatRoomAdapter.setChatRooms(currentChatRooms);
                boolean isEmpty = currentChatRooms.isEmpty();
                tvChatListEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                rcvChatRooms.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        userViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                chatRoomAdapter.setUsers(users);
            }
        });

        followViewModel.getFollowers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                followers = users;
                showMutualFollowDialogIfReady();
            }
        });

        followViewModel.getFollowing().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                following = users;
                showMutualFollowDialogIfReady();
            }
        });
    }

    private void requestMutualFollowDialog() {
        waitingForFollowData = true;
        followers = null;
        following = null;
        followViewModel.loadFollowers(currentUserId);
        followViewModel.loadFollowing(currentUserId);
    }

    private void showMutualFollowDialogIfReady() {
        if (!waitingForFollowData || followers == null || following == null) {
            return;
        }
        waitingForFollowData = false;

        // Mutual follow: mình theo dõi họ VÀ họ theo dõi lại mình
        Set<String> followerIds = new HashSet<>();
        for (User user : followers) {
            followerIds.add(user.getUserId());
        }
        final List<User> mutualUsers = new ArrayList<>();
        for (User user : following) {
            if (followerIds.contains(user.getUserId())) {
                mutualUsers.add(user);
            }
        }

        // Mục đầu tiên là tìm bạn bằng Gmail, các mục sau là người theo dõi qua lại
        String[] names = new String[mutualUsers.size() + 1];
        names[0] = "Tìm bạn bằng Gmail";
        for (int i = 0; i < mutualUsers.size(); i++) {
            names[i + 1] = mutualUsers.get(i).getUsername();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn người để chat")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            showFindByEmailDialog();
                        } else {
                            startChatWith(mutualUsers.get(which - 1));
                        }
                    }
                })
                .show();
    }

    // =================================
    // Tìm bạn bằng Gmail
    // =================================

    private void showFindByEmailDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nhập Gmail của bạn bè");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Tìm bạn bằng Gmail")
                .setView(container)
                .setPositiveButton("Tìm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        searchUserByEmail(input.getText().toString().trim(), true);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Tìm trong collection "users" của Firestore. Không thấy thì thử lại với chữ thường
    private void searchUserByEmail(final String email, final boolean retryLowerCase) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Hãy nhập Gmail", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        if (snapshots.isEmpty()) {
                            String lower = email.toLowerCase(Locale.ROOT);
                            if (retryLowerCase && !lower.equals(email)) {
                                searchUserByEmail(lower, false);
                            } else {
                                Toast.makeText(ChatListActivity.this,
                                        "Không tìm thấy tài khoản với Gmail này",
                                        Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        showFoundUser(snapshots.getDocuments().get(0), email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatListActivity.this,
                                "Tìm thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showFoundUser(DocumentSnapshot doc, String email) {
        String foundId = doc.getString("userId");
        if (foundId == null || foundId.isEmpty()) {
            foundId = doc.getId();
        }
        if (foundId.equals(currentUserId)) {
            Toast.makeText(this, "Đây là tài khoản của bạn", Toast.LENGTH_SHORT).show();
            return;
        }
        String foundName = doc.getString("username");
        if (foundName == null || foundName.trim().isEmpty()) {
            foundName = email;
        }

        final String targetId = foundId;
        final String targetName = foundName;
        new AlertDialog.Builder(this)
                .setTitle("Tìm thấy tài khoản")
                .setMessage(targetName + "\n" + email
                        + "\n\nTheo dõi để chat. Khi người này theo dõi lại bạn, hai bạn sẽ nhắn tin được.")
                .setPositiveButton("Theo dõi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        followViewModel.follow(currentUserId, targetId);
                        Toast.makeText(ChatListActivity.this,
                                "Đã theo dõi " + targetName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // =================================
    // Mở phòng chat
    // =================================

    private void startChatWith(User other) {
        String chatRoomId = buildChatRoomId(currentUserId, other.getUserId());

        boolean exists = false;
        for (ChatRoom room : currentChatRooms) {
            if (chatRoomId.equals(room.getChatRoomId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            chatViewModel.createChatRoom(new ChatRoom(chatRoomId, currentUserId, other.getUserId()));
        }
        openChat(chatRoomId, other.getUserId(), other.getUsername());
    }

    // Id phòng chat cố định theo cặp người dùng, nên A chat B và B chat A là cùng một phòng
    private static String buildChatRoomId(String userA, String userB) {
        if (userA.compareTo(userB) <= 0) {
            return userA + "_" + userB;
        }
        return userB + "_" + userA;
    }

    private void openChat(String chatRoomId, String otherUserId, String title) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId);
        intent.putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, currentUserId);
        intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUserId);
        intent.putExtra(ChatActivity.EXTRA_CHAT_TITLE, title);
        startActivity(intent);
    }
}