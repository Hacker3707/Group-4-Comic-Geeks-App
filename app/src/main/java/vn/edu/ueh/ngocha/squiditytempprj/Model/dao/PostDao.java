package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;

@Dao
public interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPost(Post post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPosts(List<Post> posts);

    @Update
    void updatePost(Post post);

    @Delete
    void deletePost(Post post);

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<Post>> getAllPosts();

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    List<Post> getAllPostsSync(); // Dùng khi cần lấy danh sách trực tiếp trên luồng nền

    @Query("SELECT * FROM posts WHERE post_id = :postId LIMIT 1")
    Post getByIdSync(String postId);

    @Query("SELECT * FROM posts WHERE comic_id = :comicId ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsByComic(String comicId);
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    List<Post> getAllSync(); // Thêm dòng này để khớp với PostSyncManager
}