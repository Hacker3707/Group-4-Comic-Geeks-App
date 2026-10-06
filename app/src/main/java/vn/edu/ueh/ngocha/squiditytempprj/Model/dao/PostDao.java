package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;

@Dao
public interface PostDao {
    @Insert
    void insertPost(Post post);

    @Update
    void updatePost(Post post);

    @Delete
    void deletePost(Post post);

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<Post>> getAllPosts();
}