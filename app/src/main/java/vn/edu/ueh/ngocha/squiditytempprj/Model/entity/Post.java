package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import java.util.Date;

public class Post {
    private String postId;
    private String userId;
    private String groupId;
    private String comicId;
    private String content;
    private Date timestamp;

    public Post(String postId, String userId, String comicId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.comicId = comicId;
        this.content = content;
        this.timestamp = new Date();
    }

    // --- GETTERS ---
    public String getPostId() {
        return postId;
    }
    public String getUserId() {
        return userId;
    }
    public String getGroupId() {
        return groupId;
    }
    public String getComicId() {
        return comicId;
    }
    public String getContent() {
        return content;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    // --- SETTERS ---
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public void setComicId(String comicId) {
        this.comicId = comicId;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // --- XỬ LÝ POST ---
    public void editContent(String newContent) {
        this.content = newContent;
    }

}