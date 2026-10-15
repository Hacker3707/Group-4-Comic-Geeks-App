package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProfilePosts;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel; // 1. Khai báo ViewModel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recyclerViewProfilePosts = findViewById(R.id.recyclerViewProfilePosts);
        recyclerViewProfilePosts.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(v -> {
            // Tạm thời hiển thị thông báo
            Toast.makeText(ProfileActivity.this, "Mở màn hình viết bài!", Toast.LENGTH_SHORT).show();

    // BƯỚC TIẾP THEO: Dùng Intent để chuyển sang trang Tạo Bài Viết
    Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
    startActivity(intent);

        });

        // 2. Khởi tạo ViewModel
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // 3. Khởi tạo Adapter với danh sách rỗng (Không dùng dummyPosts nữa)
        postAdapter = new PostAdapter(this, new ArrayList<>(), new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onStampClick(Post post) {
                String randomStampId = java.util.UUID.randomUUID().toString();
                Stamp newStamp = new Stamp(randomStampId, "Meringoc_17", StampType.LIKE);
                newStamp.setPostId(post.getPostId());
                postViewModel.addStamp(newStamp);

                Toast.makeText(ProfileActivity.this, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                String randomCommentId = java.util.UUID.randomUUID().toString();
                Comment newComment = new Comment(randomCommentId, "Meringoc_17", post.getPostId(), commentText);
                postViewModel.addComment(newComment);
                Toast.makeText(ProfileActivity.this, "Gửi bình luận thành công!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewCommentsClick(Post post) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);
                bottomSheetDialog.setContentView(R.layout.layout_comment_sheet);

                RecyclerView rvComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);
                rvComments.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));

                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
                rvComments.setAdapter(commentAdapter);

                // LẤY COMMENT THẬT TỪ DATABASE LÊN BẢNG BOTTOM SHEET
                postViewModel.getCommentsByPostId(post.getPostId()).observe(ProfileActivity.this, new Observer<List<Comment>>() {
                    @Override
                    public void onChanged(List<Comment> realComments) {
                        // Nạp dữ liệu thật vào CommentAdapter
                        commentAdapter.setComments(realComments);
                        // Cuộn xuống dòng cuối cùng để thấy bình luận mới nhất
                        if (!realComments.isEmpty()) {
                            rvComments.scrollToPosition(realComments.size() - 1);
                        }
                    }
                });

                bottomSheetDialog.show();
            }
        });
        recyclerViewProfilePosts.setAdapter(postAdapter);

        // 4. LẤY TOÀN BỘ POST THẬT ĐỔ VÀO TRANG CÁ NHÂN
        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> realPosts) {
                postAdapter.setPosts(realPosts);
            }
        });
    }
}