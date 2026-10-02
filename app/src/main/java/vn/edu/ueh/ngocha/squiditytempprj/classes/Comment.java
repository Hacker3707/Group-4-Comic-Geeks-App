package vn.edu.ueh.ngocha.squiditytempprj.classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {
    private String commentId;
    private String authorId;
    private String content;
    private Date timestamp;
    private List<Stamp> stamps;

    public Comment(String commentId, String authorId, String content) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = new Date();
        this.stamps = new ArrayList<>();
    }

    public void editContent(String newContent) {
        this.content = newContent;
    }

    public void addStamp(Stamp stamp) {
        this.stamps.add(stamp);
    }


    public void removeStamp(String userId) {
        this.stamps.removeIf(s -> s.getUserId().equals(userId));
    }
}