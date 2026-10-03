package vn.edu.ueh.ngocha.squiditytempprj.classes;

import java.util.Date;

public class Message {
    private String messageId;
    private String content;
    private Date timestamp;
    private boolean isRead;
    private String senderId;
    private String chatRoomId;

    public Message() {
    }

    public Message(String messageId, String content, String senderId, String chatRoomId) {
        this.messageId = messageId;
        this.content = content;
        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
        this.isRead = false;
        this.timestamp = new Date();
    }

    public Message(String messageId, String content, Date timestamp, boolean isRead,
                   String senderId, String chatRoomId) {
        this.messageId = messageId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
    }

    public void send() {
        this.timestamp = new Date();
        this.isRead = false;
        // TODO: day tin nhan len database khi nhom noi du lieu
    }

    public void deleteMessage() {
        // TODO: goi database xoa tin nhan theo messageId khi nhom noi du lieu
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}