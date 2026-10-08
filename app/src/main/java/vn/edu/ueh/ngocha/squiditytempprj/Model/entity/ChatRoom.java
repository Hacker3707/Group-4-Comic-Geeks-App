package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity(tableName = "chat_rooms")
public class ChatRoom {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "chat_room_id")
    private String chatRoomId;

    @ColumnInfo(name = "user1_id")
    private String user1Id;

    @ColumnInfo(name = "user2_id")
    private String user2Id;

    // Room khong luu List vao cot, tin nhan nam o messages (truy van theo chat_room_id)
    @Ignore
    private List<Message> messages = new ArrayList<>();

    public ChatRoom() {
    }

    @Ignore
    public ChatRoom(String chatRoomId, String user1Id, String user2Id) {
        this.chatRoomId = chatRoomId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getChatHistory() {
        List<Message> history = new ArrayList<>(messages);
        Collections.sort(history, new Comparator<Message>() {
            @Override
            public int compare(Message a, Message b) {
                if (a.getTimestamp() == null || b.getTimestamp() == null) {
                    return 0;
                }
                return a.getTimestamp().compareTo(b.getTimestamp());
            }
        });
        return history;
    }

    public void deleteConversation() {
        messages.clear();
        // TODO: goi MessageDao.deleteMessagesByChatRoomId(chatRoomId) khi nhom noi du lieu
    }

    @NonNull
    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(@NonNull String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}