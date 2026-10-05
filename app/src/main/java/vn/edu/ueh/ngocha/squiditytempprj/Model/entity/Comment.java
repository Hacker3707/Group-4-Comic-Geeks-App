package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import java.util.Date;

public class Comment {
    private String commentId;
    private String userId;
    private String postId;
    private String content;
    private Date timestamp;


    public Comment(String commentId, String userId, String content) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.timestamp = new Date();
    }

    public void editContent(String newContent) {
        this.content = newContent;
    }


}