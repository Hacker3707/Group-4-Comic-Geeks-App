package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "stamps")
public class Stamp {
    @PrimaryKey
    @NonNull
    private String stampId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "post_id")
    private String postId;

    @ColumnInfo(name = "comment_id")
    private String commentId;

    @ColumnInfo(name = "stamp_type")
    private StampType type;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    public Stamp(@NonNull String stampId, String userId, StampType type) {
        this.stampId = stampId;
        this.userId = userId;
        this.type = type;
        this.timestamp = new Date();
    }

    public void changeStampType(StampType newType) {
        this.type = newType;
        this.timestamp = new Date();
    }

    // --- GETTERS ---
    @NonNull
    public String getStampId() {
        return stampId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPostId() {
        return postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public StampType getType() {
        return type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // --- SETTERS ---
    public void setStampId(@NonNull String stampId) {
        this.stampId = stampId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setType(StampType type) {
        this.type = type;
    }
}