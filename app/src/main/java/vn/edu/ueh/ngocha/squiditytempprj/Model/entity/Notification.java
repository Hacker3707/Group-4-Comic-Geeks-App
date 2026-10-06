package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

import java.util.Date;

@Entity(tableName = "notifications")
@TypeConverters(Converters.class)
public class Notification {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "notif_id")
    private String notifId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "type")
    private NotificationType type;

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    public Notification() {
    }

    @Ignore
    public Notification(String notifId, String content, NotificationType type) {
        this.notifId = notifId;
        this.content = content;
        this.type = type;
        this.isRead = false;
        this.timestamp = new Date();
    }

    @Ignore
    public Notification(String notifId, String content, NotificationType type,
                        boolean isRead, Date timestamp) {
        this.notifId = notifId;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void deleteNotif() {
        // TODO: goi NotificationDao.deleteNotification(this) khi nhom noi du lieu
    }

    @NonNull
    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(@NonNull String notifId) {
        this.notifId = notifId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}