package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

@Entity(tableName = "stamps")
public class Stamp {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "stamp_id")
    private String stampId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "post_id")
    private String postId;

    @ColumnInfo(name = "comment_id")
    private String commentId;

    @ColumnInfo(name = "stamp_type")
    @TypeConverters(Converters.class)
    private StampType type;

    @ColumnInfo(name = "timestamp")
    @TypeConverters(Converters.class)
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

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
