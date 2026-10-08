package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

import java.util.Date;

@Entity(tableName = "messages")
@TypeConverters(Converters.class)
public class Message {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "message_id")
    private String messageId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    @ColumnInfo(name = "sender_id")
    private String senderId;

    @ColumnInfo(name = "chat_room_id")
    private String chatRoomId;

    public Message() {
    }

    @Ignore
    public Message(String messageId, String content, String senderId, String chatRoomId) {
        this.messageId = messageId;
        this.content = content;
        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
        this.isRead = false;
        this.timestamp = new Date();
    }

    @Ignore
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
        // TODO: goi MessageDao.insertMessage(this) khi nhom noi du lieu
    }

    public void deleteMessage() {
        // TODO: goi MessageDao.deleteMessage(this) khi nhom noi du lieu
    }

    public void markAsRead() {
        this.isRead = true;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
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