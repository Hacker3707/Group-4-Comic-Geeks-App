package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

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
    private Date timestamp;

    public Comment(@NonNull String commentId, String userId, String postId, String content) {
        this.commentId = commentId;
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.timestamp = new Date();
    }

    // --- GETTERS ---
    @NonNull public String getCommentId() { return commentId; }
    public String getUserId() { return userId; }
    public String getPostId() { return postId; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }

    // --- SETTERS ---
    public void setCommentId(@NonNull String commentId) { this.commentId = commentId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public void editContent(String newContent) {
        this.content = newContent;
    }
}