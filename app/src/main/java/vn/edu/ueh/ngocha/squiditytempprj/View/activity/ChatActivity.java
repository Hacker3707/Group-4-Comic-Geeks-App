package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.net.Uri;
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

        if (chatRoomId == null || currentUserId == null) {
            Toast.makeText(this, "Thiếu thông tin phòng chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Đảm bảo đang nhận tin nhắn mới từ Firestore
        ChatSyncManager.getInstance(this).start();

        TextView tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatTitle.setText(title != null ? title : "Chat");

        rcvMessages = findViewById(R.id.rcvMessages);
        edtMessage = findViewById(R.id.edtMessage);

        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvMessages.setLayoutManager(layoutManager);
        rcvMessages.setAdapter(messageAdapter);

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
                    rcvMessages.scrollToPosition(messages.size() - 1);
                    markIncomingAsRead(messages);
                }
            }
        });
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
        String preview = content.startsWith(MessageAdapter.IMAGE_PREFIX) ? "[Hình ảnh]" : content;
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
        Toast.makeText(this, "Đang gửi ảnh...", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(ChatActivity.this, "Gửi ảnh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}