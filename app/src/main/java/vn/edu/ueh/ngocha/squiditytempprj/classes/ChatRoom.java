package vn.edu.ueh.ngocha.squiditytempprj.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatRoom {
    private String chatRoomId;
    private String user1Id;
    private String user2Id;
    private List<Message> messages;

    public ChatRoom() {
        this.messages = new ArrayList<>();
    }

    public ChatRoom(String chatRoomId, String user1Id, String user2Id) {
        this.chatRoomId = chatRoomId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.messages = new ArrayList<>();
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
        // TODO: goi database xoa toan bo tin nhan cua phong khi nhom noi du lieu
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
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
