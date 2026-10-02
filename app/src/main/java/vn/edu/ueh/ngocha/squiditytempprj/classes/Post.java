package vn.edu.ueh.ngocha.squiditytempprj.classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private String postId;
    private String authorId;
    private String comicId;
    private String content;
    private Date timestamp;

    private List<Comment> comments;
    private List<Stamp> stamps;

    public Post(String postId, String authorId, String comicId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.comicId = comicId;
        this.content = content;
        this.timestamp = new Date();
        this.comments = new ArrayList<>();
        this.stamps = new ArrayList<>();
    }

    // --- XỬ LÝ COMMENT ---
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void deleteComment(Comment comment) {
        this.comments.remove(comment);
    }


    public void addOrUpdateStamp(Stamp newStamp) {

        for (Stamp existingStamp : stamps) {
            if (existingStamp.getUserId().equals(newStamp.getUserId())) {
                existingStamp.changeStampType(newStamp.getType());
                return;
            }
        }

        this.stamps.add(newStamp);
    }

    public void removeStamp(String userId) {
        this.stamps.removeIf(s -> s.getUserId().equals(userId));
    }

    public int countStampsByType(StampType type) {
        int count = 0;
        for (Stamp s : stamps) {
            if (s.getType() == type) count++;
        }
        return count;
    }
}