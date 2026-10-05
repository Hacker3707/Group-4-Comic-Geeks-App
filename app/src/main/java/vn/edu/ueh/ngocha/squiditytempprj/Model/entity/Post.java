package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "posts")
public class Post {
    @PrimaryKey
    @NonNull
    private String postId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "group_id")
    private String groupId;

    @ColumnInfo(name = "comic_id")
    private String comicId;

    @ColumnInfo(name = "post_content")
    private String content;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    public Post(@NonNull String postId, String userId, String comicId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.comicId = comicId;
        this.content = content;
        this.timestamp = new Date();
    }


    @NonNull public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getGroupId() { return groupId; }
    public String getComicId() { return comicId; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }


    public void setPostId(@NonNull String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setComicId(String comicId) { this.comicId = comicId; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }


    public void editContent(String newContent) {
        this.content = newContent;
    }
}