package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey
    @NonNull
    private String commentId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "post_id")
    private String postId;

    @ColumnInfo(name = "comment_content")
    private String content;

    @ColumnInfo(name = "timestamp")
    @TypeConverters(Converters.class)
    private Date timestamp;

    // 👉 Bổ sung thêm 2 trường phục vụ tính năng Reply Comment
    @ColumnInfo(name = "parent_id")
    private String parentId;       // ID của bình luận gốc được trả lời (null nếu là bình luận cấp 1)

    @ColumnInfo(name = "reply_to_username")
    private String replyToUsername;// Tên hiển thị của người được trả lời (để hiện nhãn @username)

    public Comment() {
        // Constructor rỗng bắt buộc cho Firebase và Room
    }

    public Comment(@NonNull String commentId, String userId, String postId, String content) {
        this.commentId = commentId;
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.timestamp = new Date();
        this.parentId = null;
        this.replyToUsername = null;
    }

    // --- GETTERS ---
    @NonNull
    public String getCommentId() { return commentId; }
    public String getUserId() { return userId; }
    public String getPostId() { return postId; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public String getParentId() { return parentId; }
    public String getReplyToUsername() { return replyToUsername; }

    // --- SETTERS ---
    public void setCommentId(@NonNull String commentId) { this.commentId = commentId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setReplyToUsername(String replyToUsername) { this.replyToUsername = replyToUsername; }

    public void editContent(String newContent) {
        this.content = newContent;
    }
}