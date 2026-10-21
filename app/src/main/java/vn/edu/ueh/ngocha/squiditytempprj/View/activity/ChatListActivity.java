package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ChatItemAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ChatViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.FollowViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;

/**
 * Màn danh sách chat: hiện các cuộc trò chuyện của người dùng hiện tại.
 * Nút "+" cho chọn người để chat, chỉ gồm những người theo dõi qua lại (mutual follow).
 * Activity chỉ gọi ViewModel, không gọi DAO trực tiếp.
 */
public class ChatListActivity extends BaseActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";

    // Chưa có đăng nhập thật nên tạm dùng tài khoản demo.
    // Khi nhóm nối đăng nhập: truyền userId qua Intent (EXTRA_USER_ID) và đặt DEMO_MODE = false.
    private static final boolean DEMO_MODE = true;
    private static final String DEMO_USER_ID = "demo_me";
    private static final String PREFS_NAME = "chat_demo_prefs";
    private static final String KEY_SEEDED = "chat_demo_seeded";

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
                    openChat(chatRoom.getChatRoomId(), otherUserName);
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

        String userIdFromIntent = getIntent().getStringExtra(EXTRA_USER_ID);
        currentUserId = (userIdFromIntent != null) ? userIdFromIntent : DEMO_USER_ID;

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

        if (DEMO_MODE) {
            seedDemoDataOnce();
        }
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

        if (mutualUsers.isEmpty()) {
            Toast.makeText(this, "Chưa có ai theo dõi qua lại với bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[mutualUsers.size()];
        for (int i = 0; i < mutualUsers.size(); i++) {
            names[i] = mutualUsers.get(i).getUsername();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn người để chat")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startChatWith(mutualUsers.get(which));
                    }
                })
                .show();
    }

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
        openChat(chatRoomId, other.getUsername());
    }

    // Id phòng chat cố định theo cặp người dùng, nên A chat B và B chat A là cùng một phòng
    private static String buildChatRoomId(String userA, String userB) {
        if (userA.compareTo(userB) <= 0) {
            return userA + "_" + userB;
        }
        return userB + "_" + userA;
    }

    private void openChat(String chatRoomId, String title) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId);
        intent.putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, currentUserId);
        intent.putExtra(ChatActivity.EXTRA_CHAT_TITLE, title);
        startActivity(intent);
    }

    // Dữ liệu mẫu để chạy thử khi chưa có đăng nhập. Chỉ tạo một lần cho mỗi lần cài app.
    private void seedDemoDataOnce() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            return;
        }
        prefs.edit().putBoolean(KEY_SEEDED, true).apply();

        userViewModel.insertUser(new User(DEMO_USER_ID, "Minh Khang", "khang@demo.com", "", ""));
        userViewModel.insertUser(new User("demo_meilin", "Meilin Zhang", "meilin@demo.com", "", ""));
        userViewModel.insertUser(new User("demo_nghia", "Nghia", "nghia@demo.com", "", ""));
        userViewModel.insertUser(new User("demo_toan", "Gia Toan", "toan@demo.com", "", ""));

        // Meilin và Nghia theo dõi qua lại với mình. Toan chỉ được mình theo dõi một chiều, nên không hiện trong danh sách chọn
        followViewModel.follow(DEMO_USER_ID, "demo_meilin");
        followViewModel.follow("demo_meilin", DEMO_USER_ID);
        followViewModel.follow(DEMO_USER_ID, "demo_nghia");
        followViewModel.follow("demo_nghia", DEMO_USER_ID);
        followViewModel.follow(DEMO_USER_ID, "demo_toan");

        String roomId = buildChatRoomId(DEMO_USER_ID, "demo_meilin");
        chatViewModel.createChatRoom(new ChatRoom(roomId, DEMO_USER_ID, "demo_meilin"));
        chatViewModel.sendMessage(new Message(UUID.randomUUID().toString(),
                "Chào bạn, bên mình mới đọc chap mới nè!", "demo_meilin", roomId));
    }
}