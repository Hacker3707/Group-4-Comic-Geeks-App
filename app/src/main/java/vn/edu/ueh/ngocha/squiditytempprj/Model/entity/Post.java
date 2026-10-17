package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

@Entity(tableName = "posts")
public class Post {
    @PrimaryKey
    @NonNull
    @ColumnInfo (name = "post_id")
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
    @TypeConverters(Converters.class)
    private Date timestamp;
    @ColumnInfo(name = "comment_count")
    private int commentCount = 0;
    @ColumnInfo(name = "image_uri")
    private String imageUri;

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int count) { this.commentCount = count; }

    public Post(@NonNull String postId, String userId, String comicId, String content, String imageUri) {
        this.postId = postId;
        this.userId = userId;
        this.comicId = comicId;
        this.content = content;
        this.imageUri = imageUri;
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

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public void editContent(String newContent) {
        this.content = newContent;
    }
}