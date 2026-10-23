package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.CommentUsersWatcher;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.FollowViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    // Truyền id người muốn xem profile. Không truyền thì xem profile của chính mình.
    public static final String EXTRA_USER_ID = "user_id";

    // Follow
    private FollowViewModel followViewModel;
    private String targetUserId;
    private boolean isOwnProfile = true;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;
    private MaterialButton btnFollow;

    // ViewModel & UI Profile (Phần của Nghĩa)
    private UserProfileViewModel profileViewModel;
    private ShapeableImageView imgProfileAvatar;
    private ImageView imgProfileCover;
    private EditText etProfileUsername;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private androidx.constraintlayout.widget.ConstraintLayout appBarLayout;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các view Profile
        appBarLayout = findViewById(R.id.appBar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileCover = findViewById(R.id.imgProfileCover);
        etProfileUsername = findViewById(R.id.etProfileUsername);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnChangeCover = findViewById(R.id.btnChangeCover);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        followViewModel = new ViewModelProvider(this).get(FollowViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        btnChangeAvatar.setOnClickListener(v -> avatarPicker.launch("image/*"));
        btnChangeCover.setOnClickListener(v -> coverPicker.launch("image/*"));
        btnSaveProfile.setOnClickListener(v -> {
            String username = etProfileUsername.getText().toString().trim();
            if (username.isEmpty()) {
                etProfileUsername.setError("Vui lòng nhập tên hiển thị");
                etProfileUsername.requestFocus();
                return;
            }
            profileViewModel.updateUsername(username);
        });

        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) return;

            String username = state.username == null ? "" : state.username.trim();
            if (username.isEmpty()) username = "Người dùng";

            String email = state.email == null ? "" : state.email.trim();

            if (!username.equals(etProfileUsername.getText().toString())) {
                etProfileUsername.setText(username);
            }

            tvProfileName.setText(username);

            tvProfileEmail.setText(email);
            tvProfileEmail.setVisibility(email.isEmpty() ? View.GONE : View.VISIBLE);

            if (state.avatarUrl != null && !state.avatarUrl.isEmpty()) {
                Glide.with(this).load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop().into(imgProfileAvatar);
            } else {
                imgProfileAvatar.setImageResource(R.drawable.ic_launcher_background);
            }

            if (state.coverUrl != null && !state.coverUrl.isEmpty()) {
                Glide.with(this).load(state.coverUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop().into(imgProfileCover);
            } else {
                imgProfileCover.setImageResource(R.drawable.ic_launcher_background);
            }
        });


        profileViewModel.getSaving().observe(this, saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);
            btnSaveProfile.setEnabled(!isSaving);
            btnChangeAvatar.setEnabled(!isSaving);
            btnChangeCover.setEnabled(!isSaving);
        });

        profileViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        View rootLayout = findViewById(R.id.profileRoot);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        recyclerViewProfilePosts = findViewById(R.id.recyclerViewProfilePosts);
        recyclerViewProfilePosts.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        if (fabCreatePost != null) {
            fabCreatePost.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        postAdapter = new PostAdapter(this, new ArrayList<>(), new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onStampClick(Post post) {
                String randomStampId = UUID.randomUUID().toString();
                Stamp newStamp = new Stamp(randomStampId, GroupActivity.currentUserId(), StampType.LIKE);
                newStamp.setPostId(post.getPostId());
                postViewModel.addStamp(newStamp, post);                Toast.makeText(ProfileActivity.this, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, GroupActivity.currentUserId(), post.getPostId(), commentText.trim());
                    postViewModel.addComment(newComment, post);
                    Toast.makeText(ProfileActivity.this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onViewCommentsClick(Post post) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);
                bottomSheetDialog.setContentView(R.layout.layout_comment_sheet);

                RecyclerView rvComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);
                if (rvComments != null) {
                    rvComments.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
                }

                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
                if (rvComments != null) {
                    rvComments.setAdapter(commentAdapter);
                }
                // Hiện đúng tên + ảnh tài khoản của người bình luận
                CommentUsersWatcher usersWatcher = new CommentUsersWatcher(
                        ProfileActivity.this,
                        new ViewModelProvider(ProfileActivity.this).get(UserViewModel.class),
                        commentAdapter);
                bottomSheetDialog.setOnDismissListener(d -> usersWatcher.stop());

                if (post.getPostId() != null) {
                    postViewModel.getCommentsByPostId(post.getPostId()).observe(ProfileActivity.this, realComments -> {
                        if (realComments != null) {
                            commentAdapter.setComments(realComments);
                            usersWatcher.update(realComments);
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
                intent.putExtra("post_id", post.getPostId());
                intent.putExtra("post_content", post.getContent());
                intent.putExtra("post_comic_id", post.getComicId());
                intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                startActivity(intent);
            }
        });

        recyclerViewProfilePosts.setAdapter(postAdapter);
        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> realPosts) {
                if (realPosts != null) {
                    postAdapter.setPosts(realPosts);
                }
            }
        });

        setupFollow();
        loadTargetProfile();
    }

    private void loadTargetProfile() {
        if (isOwnProfile) {
            profileViewModel.loadCurrentProfile();
        } else {
            profileViewModel.loadProfile(targetUserId);
        }
    }

    // =================================
    // FOLLOW
    // =================================
    private void setupFollow() {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fromIntent = getIntent().getStringExtra(EXTRA_USER_ID);
        targetUserId = (fromIntent == null || fromIntent.isEmpty()) ? myUid : fromIntent;
        isOwnProfile = targetUserId.equals(myUid);

        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnFollow = findViewById(R.id.btnFollow);

        if (isOwnProfile) {
            // Không tự follow chính mình
            btnFollow.setVisibility(View.GONE);
        } else {
            // Xem profile người khác: ẩn phần chỉnh sửa hồ sơ và nút đăng bài
            View editCard = findViewById(R.id.editProfileCard);
            if (editCard != null) editCard.setVisibility(View.GONE);
            View fab = findViewById(R.id.fabCreatePost);
            if (fab != null) fab.setVisibility(View.GONE);

            btnFollow.setOnClickListener(v -> {
                Boolean following = followViewModel.getIsFollowing().getValue();
                if (Boolean.TRUE.equals(following)) {
                    followViewModel.unfollowUser(targetUserId);
                } else {
                    followViewModel.followUser(targetUserId);
                }
            });
        }

        // Số Followers = số người đang follow profile này, Following = số người profile này đang follow
        followViewModel.getFollowerCount().observe(this, count ->
                tvFollowersCount.setText(String.valueOf(count == null ? 0 : count)));
        followViewModel.getFollowingCount().observe(this, count ->
                tvFollowingCount.setText(String.valueOf(count == null ? 0 : count)));
        followViewModel.getIsFollowing().observe(this, following ->
                btnFollow.setText(Boolean.TRUE.equals(following) ? "Following" : "+ Follow"));

        followViewModel.observeProfile(targetUserId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profileViewModel != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadTargetProfile();
        }
    }
}