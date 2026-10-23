package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;

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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.CommentUsersWatcher;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ChatViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.FollowViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;

public class ProfileActivity extends BaseActivity {

    // Truyền id người muốn xem profile. Không truyền thì xem profile của chính mình.
    public static final String EXTRA_USER_ID = "user_id";

    // Follow
    private FollowViewModel followViewModel;
    private ChatViewModel chatViewModel;
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
    private LinearLayout profileActionButtons;
    private TextView tvProfileEmail;
    private androidx.constraintlayout.widget.ConstraintLayout appBarLayout;
    private RecyclerView recyclerViewProfilePosts;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel;
    private MaterialButton btnEditProfile;
    private TextView tvProfilePostCount;
    private String currentProfileUserId = "";
    private String currentProfileUsername = "";
    private MaterialButton btnMessage;
    private String currentAvatarUrl = "";
    private String currentCoverUrl = "";
    private ImageButton btnEditAvatarImage;
    private ImageButton btnEditCoverImage;
    private TextView tvProfileBio;
    private TextView tvIntroUsername;
    private TextView tvIntroEmail;
    private TextView tvIntroGender;
    private TextView tvIntroBirthDate;
    private TextView tvIntroBio;
    private ListenerRegistration profileDetailsListener;
    private View layoutIntroductionDetails;
    private com.google.android.material.tabs.TabLayout tabLayout;
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
    // Id của tài khoản đang đăng nhập, chưa đăng nhập thì dùng "guest"
    private String currentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return "guest";
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageButton btnBackHome = findViewById(R.id.btnBackHome);

        if (btnBackHome != null) {
            btnBackHome.setVisibility(View.VISIBLE);
            btnBackHome.setOnClickListener(v -> finish());
        }

        // Ánh xạ các view Profile
        appBarLayout = findViewById(R.id.appBar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileCover = findViewById(R.id.imgProfileCover);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditAvatarImage = findViewById(R.id.btnEditAvatarImage);
        btnEditCoverImage = findViewById(R.id.btnEditCoverImage);
        tvProfileBio = findViewById(R.id.tvProfileBio);

        layoutIntroductionDetails =
                findViewById(R.id.layoutIntroductionDetails);

        tvIntroUsername = findViewById(R.id.tvIntroUsername);
        tvIntroEmail = findViewById(R.id.tvIntroEmail);
        tvIntroGender = findViewById(R.id.tvIntroGender);
        tvIntroBirthDate = findViewById(R.id.tvIntroBirthDate);
        tvIntroBio = findViewById(R.id.tvIntroBio);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);

        tabLayout = findViewById(R.id.tabLayout);
        profileActionButtons = findViewById(R.id.profileActionButtons);

        btnEditProfile = findViewById(R.id.btnEditProfile);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        followViewModel = new ViewModelProvider(this).get(FollowViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        tvProfilePostCount = findViewById(R.id.tvProfilePostCount);

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentProfileUserId = currentUser.getUid();

            currentProfileUsername = currentUser.getDisplayName() == null
                    ? ""
                    : currentUser.getDisplayName();
        }

        if (tvProfilePostCount != null) {
            tvProfilePostCount.setText("0");
        }

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(
                    new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {

                        @Override
                        public void onTabSelected(
                                com.google.android.material.tabs.TabLayout.Tab tab) {

                            if (tab == null) return;

                            boolean showIntroduction = tab.getPosition() == 0;

                            if (layoutIntroductionDetails != null) {
                                layoutIntroductionDetails.setVisibility(
                                        showIntroduction ? View.VISIBLE : View.GONE
                                );
                            }

                            if (recyclerViewProfilePosts != null) {
                                recyclerViewProfilePosts.setVisibility(
                                        showIntroduction ? View.GONE : View.VISIBLE
                                );
                            }
                        }

                        @Override
                        public void onTabUnselected(
                                com.google.android.material.tabs.TabLayout.Tab tab) {
                        }

                        @Override
                        public void onTabReselected(
                                com.google.android.material.tabs.TabLayout.Tab tab) {
                        }
                    }
            );

            // Mặc định mở tab POSTS.
            tabLayout.selectTab(tabLayout.getTabAt(1));
        }
        // Mở màn hình chỉnh sửa thông tin cá nhân
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                    startActivity(new Intent(
                            ProfileActivity.this,
                            EditProfileActivity.class
                    ))
            );
        }

// Bấm avatar để xem ảnh lớn
        if (imgProfileAvatar != null) {
            imgProfileAvatar.setOnClickListener(v ->
                    showLargeImage(currentAvatarUrl, true)
            );
        }

// Bấm ảnh bìa để xem ảnh lớn
        if (imgProfileCover != null) {
            imgProfileCover.setOnClickListener(v ->
                    showLargeImage(currentCoverUrl, false)
            );
        }

// Nút riêng để đổi avatar
        if (btnEditAvatarImage != null) {
            btnEditAvatarImage.setOnClickListener(v ->
                    avatarPicker.launch("image/*")
            );
        }

// Nút riêng để đổi ảnh bìa
        if (btnEditCoverImage != null) {
            btnEditCoverImage.setOnClickListener(v ->
                    coverPicker.launch("image/*")
            );
        }

        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) return;

            String username = state.username == null ? "" : state.username.trim();
            if (username.isEmpty()) username = "Người dùng";

            String email = state.email == null ? "" : state.email.trim();


            tvProfileName.setText(username);

            if (tvIntroUsername != null) {
                tvIntroUsername.setText(username);
            }

            if (tvIntroEmail != null) {
                tvIntroEmail.setText(
                        email.isEmpty() ? "Chưa cập nhật" : email
                );
            }

            if (tvIntroGender != null) {
                tvIntroGender.setText(
                        state.gender == null || state.gender.trim().isEmpty()
                                ? "Chưa cập nhật"
                                : state.gender
                );
            }

            if (tvIntroBirthDate != null) {
                tvIntroBirthDate.setText(
                        state.birthDate == null || state.birthDate.trim().isEmpty()
                                ? "Chưa cập nhật"
                                : state.birthDate
                );
            }

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

            // Chỉ là chủ profile khi đang xem profile của chính mình
            boolean isOwner =
                    isOwnProfile
                            && FirebaseAuth.getInstance().getCurrentUser() != null
                            && state.loggedIn;

            if (btnEditProfile != null) {
                btnEditProfile.setVisibility(
                        isOwner ? View.VISIBLE : View.GONE
                );
            }

            if (btnEditAvatarImage != null) {
                btnEditAvatarImage.setVisibility(
                        isOwner ? View.VISIBLE : View.GONE
                );
            }

            if (btnEditCoverImage != null) {
                btnEditCoverImage.setVisibility(
                        isOwner ? View.VISIBLE : View.GONE
                );
            }
            if (isOwner) {
                profileActionButtons.setVisibility(View.GONE);
            }
            else {
                profileActionButtons.setVisibility(View.VISIBLE);
            }

            currentAvatarUrl = state.avatarUrl == null
                    ? "" : state.avatarUrl;

            currentCoverUrl = state.coverUrl == null
                    ? "" : state.coverUrl;
        });


        profileViewModel.getSaving().observe(this, saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);

            if (btnEditProfile != null) {
                btnEditProfile.setEnabled(!isSaving);
            }

            if (btnEditAvatarImage != null) {
                btnEditAvatarImage.setEnabled(!isSaving);
            }

            if (btnEditCoverImage != null) {
                btnEditCoverImage.setEnabled(!isSaving);
            }
        });

        profileViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewProfilePosts = findViewById(R.id.recyclerViewProfilePosts);

        if (recyclerViewProfilePosts != null) {
            recyclerViewProfilePosts.setLayoutManager(
                    new LinearLayoutManager(this)
            );
        }

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
                Stamp newStamp = new Stamp(randomStampId, currentUid(), StampType.LIKE);
                newStamp.setPostId(post.getPostId());
                postViewModel.addStamp(newStamp, post);
                Toast.makeText(ProfileActivity.this, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, currentUid(), post.getPostId(), commentText.trim());
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
                intent.putExtra("post_comic_title", post.getComicTitle());
                intent.putExtra("post_hashtag", post.getHashtag());
                intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                startActivity(intent);
            }
        });

        if (recyclerViewProfilePosts != null) {
            recyclerViewProfilePosts.setAdapter(postAdapter);
        }

        postViewModel.getAllPosts().observe(this, realPosts -> {
            if (realPosts == null) {
                if (tvProfilePostCount != null) {
                    tvProfilePostCount.setText("0");
                }
                return;
            }

            // Chỉ hiện bài của chủ profile đang xem và đếm số bài
            List<Post> profilePosts = new ArrayList<>();
            int postCount = 0;

            for (Post post : realPosts) {
                if (post == null || post.getUserId() == null) {
                    continue;
                }

                String authorId = post.getUserId().trim();

                boolean isCurrentUserPost =
                        authorId.equals(currentProfileUserId)
                                || (!currentProfileUsername.isEmpty()
                                && authorId.equalsIgnoreCase(currentProfileUsername));

                if (isCurrentUserPost) {
                    postCount++;
                    profilePosts.add(post);
                }
            }

            postAdapter.setPosts(profilePosts);

            if (tvProfilePostCount != null) {
                tvProfilePostCount.setText(String.valueOf(postCount));
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
        currentProfileUserId = targetUserId;
        if (!isOwnProfile) {
            currentProfileUsername = "";
        }

        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnFollow = findViewById(R.id.btnFollow);

        if (isOwnProfile) {
            // Không tự follow chính mình
            btnFollow.setVisibility(View.GONE);
        } else {
            // Xem profile người khác: ẩn nút đăng bài
            View fab = findViewById(R.id.fabCreatePost);
            if (fab != null) fab.setVisibility(View.GONE);

            // Nút Message: mở thẳng khung chat với người này
            if (btnMessage != null) {
                btnMessage.setOnClickListener(v -> openChatWithTarget());
            }

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
    private boolean isOwnerProfile(String profileUid) {
        com.google.firebase.auth.FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        return currentUser != null
                && profileUid != null
                && currentUser.getUid().equals(profileUid);
    }

    // Mở khung chat với chủ profile đang xem (phòng chat cố định theo cặp người dùng)
    private void openChatWithTarget() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || targetUserId == null || targetUserId.isEmpty()
                || targetUserId.equals(me.getUid())) {
            return;
        }

        String myId = me.getUid();
        String chatRoomId = myId.compareTo(targetUserId) <= 0
                ? myId + "_" + targetUserId
                : targetUserId + "_" + myId;

        // Chưa có phòng thì tạo, đã có thì Room bỏ qua
        chatViewModel.createChatRoom(new ChatRoom(chatRoomId, myId, targetUserId));

        String title = tvProfileName != null
                ? tvProfileName.getText().toString()
                : "";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId);
        intent.putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, myId);
        intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, targetUserId);
        intent.putExtra(ChatActivity.EXTRA_CHAT_TITLE, title);
        startActivity(intent);
    }

    private void showLargeImage(String imageUrl, boolean isAvatar) {
        Dialog dialog = new Dialog(this);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(android.graphics.Color.BLACK);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(android.graphics.Color.BLACK);
        container.addView(imageView);

        dialog.setContentView(container);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.black
            );
        }

        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profileViewModel != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadTargetProfile();
        }
    }


    private void listenProfileDetails() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = (targetUserId != null && !targetUserId.isEmpty())
                ? targetUserId
                : FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (profileDetailsListener != null) {
            profileDetailsListener.remove();
        }

        profileDetailsListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .addSnapshotListener((document, error) -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (error != null) {
                        Toast.makeText(
                                this,
                                "Không tải được Bio và giới thiệu.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (document == null || !document.exists()) {
                        return;
                    }

                    String bio = document.getString("bio");
                    String introduction = document.getString("introduction");

                    if (tvProfileBio != null) {
                        tvProfileBio.setText(
                                bio == null || bio.trim().isEmpty()
                                        ? "Chưa có bio"
                                        : bio.trim()
                        );
                    }

                    if (tvIntroBio != null) {
                        tvIntroBio.setText(
                                introduction == null
                                        || introduction.trim().isEmpty()
                                        ? "Chưa có giới thiệu bản thân"
                                        : introduction.trim()
                        );
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenProfileDetails();
    }
    @Override
    protected void onStop() {
        super.onStop();

        if (profileDetailsListener != null) {
            profileDetailsListener.remove();
            profileDetailsListener = null;
        }
    }
}