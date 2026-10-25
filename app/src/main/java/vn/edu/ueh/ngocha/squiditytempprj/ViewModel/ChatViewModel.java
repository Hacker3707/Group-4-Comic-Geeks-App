package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ChatRoomRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ChatSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MessageRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.NotificationRepository;

public class ChatViewModel extends AndroidViewModel {
    private MessageRepository messageRepository;
    private ChatRoomRepository chatRoomRepository;
    private NotificationRepository notificationRepository;
    private ChatSyncManager chatSyncManager;

    public ChatViewModel(Application application) {
        super(application);
        messageRepository = new MessageRepository(application);
        chatRoomRepository = new ChatRoomRepository(application);
        notificationRepository = new NotificationRepository(application);
        chatSyncManager = ChatSyncManager.getInstance(application);
    }

    // --- MESSAGE ---
    public LiveData<List<Message>> getMessages(String chatRoomId) {
        return messageRepository.getMessagesByChatRoomId(chatRoomId);
    }

    // Chỉ lưu ở máy (Room)
    public void sendMessage(Message message) {
        message.send();
        messageRepository.insert(message);
    }

    // Lưu ở máy, đẩy lên Firestore cho người nhận, và báo thông báo lên chuông của người nhận.
    // preview là nội dung ngắn hiện trong thông báo
    public void sendMessage(Message message, String recipientId, String preview) {
        sendMessage(message);

        if (recipientId == null || recipientId.isEmpty()) {
            return;
        }
        chatSyncManager.pushMessage(message, recipientId);

        String senderId = message.getSenderId();
        String actorName = notificationRepository.currentActorName(senderId);

        String snippet = (preview == null) ? "" : preview;
        if (snippet.length() > 40) {
            snippet = snippet.substring(0, 40) + "...";
        }
        notificationRepository.sendNotification(recipientId, senderId, actorName,
                NotificationType.MESSAGE, null, Str.t(R.string.notif_messaged, actorName, snippet));
    }

    public void markMessageAsRead(String messageId) {
        messageRepository.markAsRead(messageId);
    }

    public void deleteMessage(Message message) {
        messageRepository.delete(message);
    }

    // --- CHAT ROOM ---
    public LiveData<List<ChatRoom>> getChatRooms(String userId) {
        return chatRoomRepository.getChatRoomsByUserId(userId);
    }

    public LiveData<ChatRoom> getChatRoom(String chatRoomId) {
        return chatRoomRepository.getChatRoomById(chatRoomId);
    }

    public void createChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.insert(chatRoom);
    }

    public void deleteConversation(String chatRoomId) {
        messageRepository.deleteMessagesByChatRoomId(chatRoomId);
    }

    public void deleteChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.delete(chatRoom);
    }

}