package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ChatRoomRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MessageRepository;

public class ChatViewModel extends AndroidViewModel {
    private MessageRepository messageRepository;
    private ChatRoomRepository chatRoomRepository;

    public ChatViewModel(Application application) {
        super(application);
        messageRepository = new MessageRepository(application);
        chatRoomRepository = new ChatRoomRepository(application);
    }

    // --- MESSAGE ---
    public LiveData<List<Message>> getMessages(String chatRoomId) {
        return messageRepository.getMessagesByChatRoomId(chatRoomId);
    }

    public void sendMessage(Message message) {
        message.send();
        messageRepository.insert(message);
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
