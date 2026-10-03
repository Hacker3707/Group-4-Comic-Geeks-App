package vn.edu.ueh.ngocha.squiditytempprj.classes;


import java.util.Date;

public class Stamp {
    private String stampId;
    private String userId;
    private String postId;
    private String commentId;
    private StampType type;
    private Date timestamp;

    public Stamp(String stampId, String userId, StampType type) {
        this.stampId = stampId;
        this.userId = userId;
        this.type = type;
        this.timestamp = new Date();
    }

    public void changeStampType(StampType newType) {
        this.type = newType;
        this.timestamp = new Date();
    }

    // Getters
    public StampType getType() { return type; }
    public String getUserId() { return userId; }
    public String getStampId() { return stampId; }
    public String getPostId() { return postId; }
    public String getCommentId() { return commentId; }
    public Date getTimestamp() { return timestamp; }

    // Setters
    public void setType(StampType type) { this.type = type; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setStampId(String stampId) { this.stampId = stampId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

}