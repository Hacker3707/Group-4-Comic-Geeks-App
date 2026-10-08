package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;

@Dao
public interface CommentDao {
    @Insert
    void insertComment(Comment comment);

    @Update
    void updateComment(Comment comment);

    @Delete
    void deleteComment(Comment comment);

    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY timestamp ASC")
    LiveData<List<Comment>> getCommentsByPost(String postId);
}