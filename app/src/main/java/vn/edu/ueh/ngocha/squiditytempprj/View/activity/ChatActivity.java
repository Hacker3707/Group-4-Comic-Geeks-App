package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.net.NetworkCapabilities;
import android.net.Network;
import android.net.ConnectivityManager;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.BlockList;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.net.Uri;
import android.os.Parcelable;
import android.content.DialogInterface;
import android.widget.PopupMenu;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ChatSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.MessageAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ChatViewModel;

/**
 * Màn nhắn tin: danh sách tin nhắn, ô nhập chữ (có bàn phím), nút gửi và nút gửi hình ảnh.
 * Tin nhắn lấy từ ChatViewModel qua LiveData nên có tin mới là màn hình tự cập nhật.
 * Ảnh được tải lên Firebase Storage, tin nhắn lưu đường dẫn ảnh để người kia xem được.
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ROOM_ID = "extra_chat_room_id";
    public static final String EXTRA_CURRENT_USER_ID = "extra_current_user_id";
    public static final String EXTRA_OTHER_USER_ID = "extra_other_user_id";
    public static final String EXTRA_CHAT_TITLE = "extra_chat_title";

    private ChatViewModel chatViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView rcvMessages;
    private EditText edtMessage;

    private String chatRoomId;
    private String currentUserId;
    private String otherUserId;
    private String chatTitle;
    private Parcelable savedListState;
    private static final String STATE_LIST = "state_list";

    // Photo Picker của Android: chọn ảnh mà không cần xin quyền đọc bộ nhớ
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                uploadImageAndSend(uri);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // Chừa chỗ cho thanh hệ thống và bàn phím để ô nhập không bị che
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            }
        });

        chatRoomId = getIntent().getStringExtra(EXTRA_CHAT_ROOM_ID);
        currentUserId = getIntent().getStringExtra(EXTRA_CURRENT_USER_ID);
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        String title = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        chatTitle = title;
        if (savedInstanceState != null) {
            savedListState = savedInstanceState.getParcelable(STATE_LIST);
        }

        if (chatRoomId == null || currentUserId == null) {
            Toast.makeText(this, Str.t(R.string.chat_missing_room), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Đảm bảo đang nhận tin nhắn mới từ Firestore
        ChatSyncManager.getInstance(this).start();

        TextView tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatTitle.setText(title != null ? title : Str.t(R.string.chat_title_default));

        rcvMessages = findViewById(R.id.rcvMessages);
        edtMessage = findViewById(R.id.edtMessage);

        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvMessages.setLayoutManager(layoutManager);
        rcvMessages.setAdapter(messageAdapter);
        messageAdapter.setOnMessageLongClickListener(new MessageAdapter.OnMessageLongClickListener() {
            @Override
            public void onMessageLongClick(final Message message, boolean sentByMe) {
                showDeleteMessageDialog(message, sentByMe);
            }
        });
        watchNetwork();
        findViewById(R.id.btnChatMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChatMenu(v);
            }
        });

        findViewById(R.id.btnChatBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

        findViewById(R.id.btnPickImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.getMessages(chatRoomId).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                messageAdapter.setMessages(messages);
                if (messages != null && !messages.isEmpty()) {
                    if (savedListState != null) {
                        // Vừa xoay màn hình: giữ nguyên vị trí đang đọc
                        rcvMessages.getLayoutManager().onRestoreInstanceState(savedListState);
                        savedListState = null;
                    } else {
                        rcvMessages.scrollToPosition(messages.size() - 1);
                    }
                    markIncomingAsRead(messages);
                }
            }
        });
    }

    // Báo mất mạng ngay trong khung chat
    private ConnectivityManager.NetworkCallback networkCallback;

    private void watchNetwork() {
        final TextView banner = findViewById(R.id.tvChatOffline);
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (banner == null || cm == null) {
            return;
        }
        banner.setVisibility(isOnline(cm) ? View.GONE : View.VISIBLE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> banner.setVisibility(View.GONE));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> banner.setVisibility(View.VISIBLE));
            }
        };
        cm.registerDefaultNetworkCallback(networkCallback);
    }

    private boolean isOnline(ConnectivityManager cm) {
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            try {
                cm.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException ignored) {
                // đã gỡ rồi
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (rcvMessages != null && rcvMessages.getLayoutManager() != null) {
            outState.putParcelable(STATE_LIST, rcvMessages.getLayoutManager().onSaveInstanceState());
        }
    }

    // Giữ lâu một tin nhắn: xóa ở phía mình, hoặc xóa với mọi người nếu là tin của mình
    private void showDeleteMessageDialog(final Message message, boolean sentByMe) {
        final boolean canResend = sentByMe && message.getStatus() == Message.STATUS_FAILED;
        final String[] options;
        if (canResend) {
            options = new String[]{Str.t(R.string.chat_resend), Str.t(R.string.chat_msg_delete_for_me), Str.t(R.string.chat_msg_delete_everyone)};
        } else if (sentByMe) {
            options = new String[]{Str.t(R.string.chat_msg_delete_for_me), Str.t(R.string.chat_msg_delete_everyone)};
        } else {
            options = new String[]{Str.t(R.string.chat_msg_delete_for_me)};
        }
        new AlertDialog.Builder(this)
                .setTitle(Str.t(R.string.chat_msg_delete_title))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int choice = canResend ? which - 1 : which;
                        if (choice < 0) {
                            chatViewModel.resendMessage(message, otherUserId);
                            return;
                        }
                        if (choice == 0) {
                            chatViewModel.deleteMessageForMe(message);
                        } else {
                            chatViewModel.deleteMessageForEveryone(message);
                        }
                        Toast.makeText(ChatActivity.this, Str.t(R.string.chat_msg_deleted), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Str.t(R.string.common_cancel), null)
                .show();
    }

    // Nút ba chấm trên thanh tiêu đề: xóa cuộc trò chuyện, chặn / bỏ chặn
    private void showChatMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, Str.t(R.string.chat_menu_delete_conversation));
        if (otherUserId != null) {
            boolean blocked = BlockList.isBlocked(this, otherUserId);
            popup.getMenu().add(0, 2, 1, Str.t(blocked ? R.string.chat_menu_unblock : R.string.chat_menu_block));
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                if (item.getItemId() == 1) {
                    confirmDeleteConversation();
                } else if (item.getItemId() == 2) {
                    toggleBlock();
                }
                return true;
            }
        });
        popup.show();
    }

    private void confirmDeleteConversation() {
        String name = chatTitle != null ? chatTitle : Str.t(R.string.common_user);
        new AlertDialog.Builder(this)
                .setTitle(Str.t(R.string.chat_delete_conv_title))
                .setMessage(Str.t(R.string.chat_delete_conv_msg, name))
                .setNegativeButton(Str.t(R.string.common_cancel), null)
                .setPositiveButton(Str.t(R.string.common_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chatViewModel.clearConversation(chatRoomId);
                        Toast.makeText(ChatActivity.this, Str.t(R.string.chat_conv_deleted), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .show();
    }

    private void toggleBlock() {
        if (BlockList.isBlocked(this, otherUserId)) {
            BlockList.remove(this, otherUserId);
            Toast.makeText(this, Str.t(R.string.chat_unblocked), Toast.LENGTH_SHORT).show();
            return;
        }
        String name = chatTitle != null ? chatTitle : Str.t(R.string.common_user);
        new AlertDialog.Builder(this)
                .setTitle(Str.t(R.string.chat_block_title, name))
                .setMessage(Str.t(R.string.chat_block_msg))
                .setNegativeButton(Str.t(R.string.common_cancel), null)
                .setPositiveButton(Str.t(R.string.chat_menu_block), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BlockList.add(ChatActivity.this, otherUserId);
                        chatViewModel.clearConversation(chatRoomId);
                        Toast.makeText(ChatActivity.this, Str.t(R.string.chat_blocked), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .show();
    }

    private void sendTextMessage() {
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        sendMessageContent(text);
        edtMessage.setText("");
    }

    // Lưu tin, đẩy lên Firestore cho người nhận và gửi thông báo lên chuông của họ
    private void sendMessageContent(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, currentUserId, chatRoomId);
        String preview = content.startsWith(MessageAdapter.IMAGE_PREFIX) ? Str.t(R.string.chat_image_preview) : content;
        chatViewModel.sendMessage(message, otherUserId, preview);
    }

    // Tin của người kia mà mình chưa đọc thì đánh dấu đã đọc khi mở màn hình
    private void markIncomingAsRead(List<Message> messages) {
        for (Message message : messages) {
            if (!message.isRead() && !currentUserId.equals(message.getSenderId())) {
                chatViewModel.markMessageAsRead(message.getMessageId());
            }
        }
    }

    // Tải ảnh lên Firebase Storage, lấy đường dẫn rồi gửi như một tin nhắn ảnh
    private void uploadImageAndSend(Uri source) {
        Toast.makeText(this, Str.t(R.string.chat_sending_image), Toast.LENGTH_SHORT).show();

        final StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("chat_images")
                .child(chatRoomId)
                .child(UUID.randomUUID().toString() + ".jpg");

        imageRef.putFile(source)
                .continueWithTask(new Continuation<com.google.firebase.storage.UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<com.google.firebase.storage.UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        sendMessageContent(MessageAdapter.IMAGE_PREFIX + downloadUri.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, Str.t(R.string.chat_send_image_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }
}