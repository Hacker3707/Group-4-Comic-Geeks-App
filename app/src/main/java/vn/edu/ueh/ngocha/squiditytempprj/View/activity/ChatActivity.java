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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.MessageAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ChatViewModel;

/**
 * Màn nhắn tin: danh sách tin nhắn, ô nhập chữ (có bàn phím), nút gửi và nút gửi hình ảnh.
 * Tin nhắn lấy từ ChatViewModel qua LiveData nên có tin mới là màn hình tự cập nhật.
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ROOM_ID = "extra_chat_room_id";
    public static final String EXTRA_CURRENT_USER_ID = "extra_current_user_id";
    public static final String EXTRA_CHAT_TITLE = "extra_chat_title";

    private ChatViewModel chatViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView rcvMessages;
    private EditText edtMessage;

    private String chatRoomId;
    private String currentUserId;

    // Sao chép ảnh vào bộ nhớ app trên luồng nền để không làm đơ giao diện
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Photo Picker của Android: chọn ảnh mà không cần xin quyền đọc bộ nhớ
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                saveImageAndSend(uri);
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
        String title = getIntent().getStringExtra(EXTRA_CHAT_TITLE);

        if (chatRoomId == null || currentUserId == null) {
            Toast.makeText(this, "Thiếu thông tin phòng chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

    private void sendMessageContent(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, currentUserId, chatRoomId);
        chatViewModel.sendMessage(message);
    }

    // Tin của người kia mà mình chưa đọc thì đánh dấu đã đọc khi mở màn hình
    private void markIncomingAsRead(List<Message> messages) {
        for (Message message : messages) {
            if (!message.isRead() && !currentUserId.equals(message.getSenderId())) {
                chatViewModel.markMessageAsRead(message.getMessageId());
            }
        }
    }

    // Ảnh từ Photo Picker chỉ có quyền tạm thời, nên chép vào bộ nhớ app rồi mới lưu đường dẫn
    private void saveImageAndSend(final Uri source) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(getFilesDir(), "chat_images");
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IOException("Không tạo được thư mục lưu ảnh");
                    }
                    File target = new File(dir, UUID.randomUUID().toString() + ".jpg");

                    InputStream in = getContentResolver().openInputStream(source);
                    if (in == null) {
                        throw new IOException("Không mở được ảnh đã chọn");
                    }
                    OutputStream out = new FileOutputStream(target);
                    try {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    } finally {
                        in.close();
                        out.close();
                    }

                    final String content = MessageAdapter.IMAGE_PREFIX + Uri.fromFile(target).toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageContent(content);
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatActivity.this, "Gửi ảnh thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}