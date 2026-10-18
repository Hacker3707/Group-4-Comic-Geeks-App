package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.widget.ImageView;
import android.net.Uri;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AuthActivity;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

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
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    private UserProfileViewModel profileViewModel;
    private ShapeableImageView imgProfileAvatar;
    private ImageView imgProfileCover;
    private EditText etProfileUsername;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private Toolbar toolbar;
    private MaterialButton btnChangeAvatar;
    private MaterialButton btnChangeCover;
    private MaterialButton btnSaveProfile;

    private RecyclerView recyclerViewProfilePosts;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel;

    private final ActivityResultLauncher<String> avatarPicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && profileViewModel != null) {
                            profileViewModel.updateAvatar(uri);
                        }
                    }
            );

    private final ActivityResultLauncher<String> coverPicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && profileViewModel != null) {
                            profileViewModel.updateCover(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recyclerViewProfilePosts = findViewById(R.id.recyclerViewProfilePosts);
        recyclerViewProfilePosts.setLayoutManager(new LinearLayoutManager(this));
        toolbar = findViewById(R.id.toolbar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // Chưa đăng nhập thì chuyển sang màn hình đăng nhập.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        profileViewModel = new ViewModelProvider(this)
                .get(UserProfileViewModel.class);

        // Toolbar
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Ánh xạ các view.
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileCover = findViewById(R.id.imgProfileCover);
        etProfileUsername = findViewById(R.id.etProfileUsername);

        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnChangeCover = findViewById(R.id.btnChangeCover);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Chọn ảnh đại diện.
        btnChangeAvatar.setOnClickListener(
                v -> avatarPicker.launch("image/*")
        );

        // Chọn ảnh bìa.
        btnChangeCover.setOnClickListener(
                v -> coverPicker.launch("image/*")
        );

        // Lưu tên người dùng.
        btnSaveProfile.setOnClickListener(v -> {
            String username =
                    etProfileUsername.getText().toString().trim();

            if (username.isEmpty()) {
                etProfileUsername.setError(
                        "Vui lòng nhập tên hiển thị"
                );
                etProfileUsername.requestFocus();
                return;
            }

            profileViewModel.updateUsername(username);
        });

        // Quan sát dữ liệu hồ sơ.
        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) {
                return;
            }

            // Tên hiển thị của người dùng
            String username = state.username == null
                    ? ""
                    : state.username.trim();

            if (username.isEmpty()) {
                username = "Người dùng";
            }

            // Email của tài khoản
            String email = state.email == null
                    ? ""
                    : state.email.trim();

            // Cập nhật tên trong ô chỉnh sửa
            if (!username.equals(etProfileUsername.getText().toString())) {
                etProfileUsername.setText(username);
            }

            // Cập nhật tên dưới ảnh đại diện
            tvProfileName.setText(username);

            // Cập nhật tên trên thanh Toolbar
            if (toolbar != null) {
                toolbar.setTitle(username);
            }

            // Hiển thị email nhỏ hơn dưới tên
            tvProfileEmail.setText(email);
            tvProfileEmail.setVisibility(
                    email.isEmpty() ? View.GONE : View.VISIBLE
            );

            // Cập nhật ảnh đại diện
            if (state.avatarUrl != null && !state.avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgProfileAvatar);
            } else {
                imgProfileAvatar.setImageResource(
                        R.drawable.ic_launcher_background
                );
            }

            // Cập nhật ảnh bìa
            if (state.coverUrl != null && !state.coverUrl.isEmpty()) {
                Glide.with(this)
                        .load(state.coverUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(imgProfileCover);
            } else {
                imgProfileCover.setImageResource(
                        R.drawable.ic_launcher_background
                );
            }
        });

        // Vô hiệu hóa nút khi đang lưu.
        profileViewModel.getSaving().observe(this, saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);

            btnSaveProfile.setEnabled(!isSaving);
            btnChangeAvatar.setEnabled(!isSaving);
            btnChangeCover.setEnabled(!isSaving);
        });

        // Hiển thị thông báo.
        profileViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(
                        ProfileActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Cập nhật khoảng cách hệ thống trên root layout.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.profileRoot),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // RecyclerView bài viết.
        recyclerViewProfilePosts =
                findViewById(R.id.recyclerViewProfilePosts);

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

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
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = java.util.UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, "Meringoc_17", post.getPostId(), commentText.trim());
        recyclerViewProfilePosts.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Giữ nguyên dữ liệu mẫu ban đầu.
        List<Post> dummyPosts = new ArrayList<>();

                    postViewModel.addComment(newComment);
                    Toast.makeText(ProfileActivity.this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                }
            }
        Post baiSo1 = new Post(
                "P01",
                "Meilin Zhang",
                "C01",
                "Trời ơi truyện này hay xỉu luôn! Nam chính ngầu bá cháy."
        );

            @Override
            public void onViewCommentsClick(Post post) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);
                bottomSheetDialog.setContentView(R.layout.layout_comment_sheet);

                RecyclerView rvComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);
                if (rvComments != null) {
                    rvComments.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
                }
        Post baiSo2 = new Post(
                "P02",
                "Meilin Zhang",
                "C02",
                "Hóng chap mới quá đi mất 😭"
        );

                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
                if (rvComments != null) {
                    rvComments.setAdapter(commentAdapter);
                }

                if (post.getPostId() != null) {
                    postViewModel.getCommentsByPostId(post.getPostId()).observe(ProfileActivity.this, realComments -> {
                        if (realComments != null) {
                            commentAdapter.setComments(realComments);
                            if (!realComments.isEmpty() && rvComments != null) {
                                rvComments.scrollToPosition(realComments.size() - 1);
                            }
                        }
                    });
                }

                bottomSheetDialog.show();
            }

            @Override
            public void onDeletePost(Post post) {
                if (post != null) {
                    postViewModel.deletePost(post);
                    Toast.makeText(ProfileActivity.this, "Đã xóa bài viết!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditPost(Post post) {
                Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("post_id", post.getPostId()); // Dùng post.getPostId() để lấy đúng ID bài viết
                intent.putExtra("post_content", post.getContent());
                intent.putExtra("post_comic_id", post.getComicId());
                intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                startActivity(intent);
            }
        });

        recyclerViewProfilePosts.setAdapter(postAdapter);

        // Tải hồ sơ của tài khoản đang đăng nhập.
        profileViewModel.loadCurrentProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (profileViewModel != null
                && FirebaseAuth.getInstance().getCurrentUser() != null) {
            profileViewModel.loadCurrentProfile();
        }

        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> realPosts) {
                if (realPosts != null) {
                    postAdapter.setPosts(realPosts);
                }
            }
        });
    }
}