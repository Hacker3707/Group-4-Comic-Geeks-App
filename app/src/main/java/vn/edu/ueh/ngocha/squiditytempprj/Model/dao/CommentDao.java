package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;

@Dao
public interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComment(Comment comment);

    @Update
    void updateComment(Comment comment);

    @Delete
    void deleteComment(Comment comment);

    /* Bỏ tạm điều kiện ORDER BY timestamp nếu bảng của nhóm chưa cấu hình trường timestamp đầy đủ, tránh bị rỗng data
    @Query("SELECT * FROM comments WHERE post_id = :postId")
    LiveData<List<Comment>> getCommentsByPost(String postId);
    */
    @Query("SELECT * FROM comments WHERE post_id = :postId")
    LiveData<List<Comment>> getCommentsByPostId(String postId);

    @Query("SELECT * FROM comments")
    LiveData<List<Comment>> getAllComments();

    @Query("SELECT * FROM comments")
    List<Comment> getAllSync();

    @Query("SELECT * FROM comments WHERE commentId = :commentId LIMIT 1")
    Comment getByIdSync(String commentId);
}