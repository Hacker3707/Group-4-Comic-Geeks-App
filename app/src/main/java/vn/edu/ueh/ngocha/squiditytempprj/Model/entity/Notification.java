package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import java.util.Date;

public class Notification {
    private String notifId;
    private String content;
    private NotificationType type;
    private boolean isRead;
    private Date timestamp;

    public Notification() {
    }

    public Notification(String notifId, String content, NotificationType type) {
        this.notifId = notifId;
        this.content = content;
        this.type = type;
        this.isRead = false;
        this.timestamp = new Date();
    }

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
        // TODO: goi database xoa thong bao theo notifId khi nhom noi du lieu
    }

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
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
