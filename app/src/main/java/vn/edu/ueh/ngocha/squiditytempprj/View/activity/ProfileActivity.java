package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.FollowListDialog;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicReviewAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GenreViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.CommentUsersWatcher;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ChatViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ReviewViewModel;
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
    private TextView tvProfileName;
    private LinearLayout profileActionButtons;
    private TextView tvProfileEmail;
    private RecyclerView recyclerViewProfilePosts;
    private RecyclerView recyclerViewProfileComments;
    private PostAdapter postAdapter;
    private ComicReviewAdapter reviewAdapter;
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
    private View layoutIntroductionDetails;
    private com.google.android.material.tabs.TabLayout tabLayout;
    private ChipGroup chipGenreProfile;
    private GenreViewModel genreViewModel;
    private ReviewViewModel reviewViewModel;
    private ComicViewModel comicViewModel;
    private List<Genre> allGenres = new ArrayList<>();
    private List<String> selectedGenreIds = new ArrayList<>();
    private User currentProfile;
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

    private Observer<List<Genre>> genreObserver = new Observer<List<Genre>>() {
        @Override
        public void onChanged(List<Genre> genres) {
            allGenres = genres == null
                    ? new ArrayList<>()
                    : new ArrayList<>(genres);

            renderSelectedGenres();
        }
    };

    private Observer<List<Review>> reviewObserver =
            new Observer<List<Review>>() {
                @Override
                public void onChanged(List<Review> reviews) {
                    reviewAdapter.setReviews(reviews);
                    reviewAdapter.setUser(currentProfile);

                    List<String> mangaIds = new ArrayList<>();

                    if (reviews != null) {
                        for (Review review : reviews) {
                            if (review != null
                                    && review.getComicId() != null
                                    && !mangaIds.contains(review.getComicId())) {
                                mangaIds.add(review.getComicId());
                            }
                        }
                    }

                    comicViewModel.getComicbyIdList(mangaIds).observe(ProfileActivity.this, comicObserver);
                }
            };

    private View.OnClickListener onclickGenreChip = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Chip chip = (Chip) v;
            String genreId = (String) chip.getTag();
            if (genreId == null) return;
            Intent intent = new Intent(ProfileActivity.this, ComicListActivity.class);
            intent.putExtra("genre_id", genreId);
            startActivity(intent);
        }
    };

    private Observer<List<MangaData>> comicObserver = new Observer<List<MangaData>>() {
        @Override
        public void onChanged(List<MangaData> comics) {
            reviewAdapter.setComics(comics);
        }
    };

    private Observer<User> profileObserver = new Observer<User>() {
        @Override
        public void onChanged(User user) {
            currentProfile = user;
        }
    };

    private void renderSelectedGenres() {
        if (chipGenreProfile == null) {
            return;
        }

        chipGenreProfile.setSingleSelection(false);
        chipGenreProfile.setSelectionRequired(false);
        chipGenreProfile.removeAllViews();

        Set<String> selectedIds = new HashSet<>(selectedGenreIds);

        for (Genre genre : allGenres) {
            if (genre == null || genre.getGenreId() == null) {
                continue;
            }

            // Chỉ hiển thị genre đã được người dùng chọn.
            if (!selectedIds.contains(genre.getGenreId())) {
                continue;
            }

            Chip chip = new Chip(this);
            chip.setText(genre.getGenreName());
            chip.setTag(genre.getGenreId());

            // Chip trên Profile chỉ dùng để hiển thị.
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setFocusable(false);

            chip.setOnClickListener(onclickGenreChip);

            chipGenreProfile.addView(chip);
        }

        chipGenreProfile.setVisibility(
                selectedIds.isEmpty() || chipGenreProfile.getChildCount() == 0
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    public ComicReviewAdapter.OnReviewClickListener reviewInteractionListener = new ComicReviewAdapter.OnReviewClickListener() {
        @Override
        public void onUserClick(String userId) {
            if (userId == null || userId.trim().isEmpty()) {
                return;
            }

            Intent intent = new Intent(
                    ProfileActivity.this,
                    ProfileActivity.class
            );

            intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        }

        @Override
        public void onComicChipClick(String comicId) {
            Intent intent = new Intent(
                    ProfileActivity.this,
                    ComicDetailActivity.class
            );

            intent.putExtra("comic_id", comicId);
            intent.putExtra("search_option", "MangaDex");

            startActivity(intent);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageButton btnBackHome = findViewById(R.id.btnBackHome);

        if (btnBackHome != null) {
            btnBackHome.setVisibility(View.VISIBLE);
            btnBackHome.setOnClickListener(v -> finish());
        }

        // Ánh xạ các view Profile
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileCover = findViewById(R.id.imgProfileCover);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditAvatarImage = findViewById(R.id.btnEditAvatarImage);
        btnEditCoverImage = findViewById(R.id.btnEditCoverImage);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        chipGenreProfile = findViewById(R.id.chipGroupProfile);

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

        if (tabLayout != null && tabLayout.getTabAt(1) != null) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        }

        profileActionButtons = findViewById(R.id.profileActionButtons);

        btnEditProfile = findViewById(R.id.btnEditProfile);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        genreViewModel = new ViewModelProvider(this)
                .get(GenreViewModel.class);

        genreViewModel.getAllGenres().observe(this, genreObserver);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        profileViewModel.getLiveBio().observe(this, bio -> {
            if (tvProfileBio != null) {
                tvProfileBio.setText(
                        bio == null || bio.trim().isEmpty()
                                ? "No signature here~ Got nothing to say~.~"
                                : bio.trim()
                );
            }
        });

        profileViewModel.getLiveIntroduction().observe(this, introduction -> {
            if (tvIntroBio != null) {
                tvIntroBio.setText(
                        introduction == null || introduction.trim().isEmpty()
                                ? Str.t(R.string.profile_no_about)
                                : introduction.trim()
                );
            }
        });

        profileViewModel.getLiveIntroduction().observe(this, introduction -> {
            // Cập nhật TextView giới thiệu bản thân riêng tại đây
            // khi bạn đã xác định đúng ID của TextView trong layout.
        });
        followViewModel = new ViewModelProvider(this).get(FollowViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        FirebaseUser me = FirebaseAuth.getInstance()
                .getCurrentUser();

        String fromIntent = getIntent()
                .getStringExtra(EXTRA_USER_ID);

        targetUserId = (fromIntent == null || fromIntent.trim().isEmpty())
                ? me.getUid()
                : fromIntent.trim();

        isOwnProfile = targetUserId.equals(me.getUid());
        currentProfileUserId = targetUserId;

        // 1. Khởi tạo RecyclerView
        recyclerViewProfileComments = findViewById(R.id.rcvProfileReviews);

        reviewAdapter = new ComicReviewAdapter(reviewInteractionListener);
        reviewAdapter.setupInProfile();


        if (recyclerViewProfileComments != null) {
            recyclerViewProfileComments.setLayoutManager(
                    new LinearLayoutManager(this)
            );
            recyclerViewProfileComments.setAdapter(reviewAdapter);
        }

        // 2. Sau đó mới đăng ký Observer
        reviewViewModel = new ViewModelProvider(this)
                .get(ReviewViewModel.class);
        comicViewModel = new ViewModelProvider(this).get(ComicViewModel.class);

        profileViewModel.getUserById(targetUserId).observe(this, profileObserver);

        reviewViewModel.getAllReviewsforUserId(targetUserId)
                .observe(this, reviewObserver);

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

                            int position = tab.getPosition();

                            if (layoutIntroductionDetails != null) {
                                layoutIntroductionDetails.setVisibility(
                                        position == 0 ? View.VISIBLE : View.GONE
                                );
                            }

                            if (recyclerViewProfilePosts != null) {
                                recyclerViewProfilePosts.setVisibility(
                                        position == 1 ? View.VISIBLE : View.GONE
                                );
                            }

                            if (recyclerViewProfileComments != null) {
                                recyclerViewProfileComments.setVisibility(
                                        position == 3 ? View.VISIBLE : View.GONE
                                );
                            }

                            // Tab COLLECTION (position 2) cần có view riêng
                            // và được hiển thị tại đây khi bạn đã xác định view đó.
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

            selectedGenreIds = state.favGenres == null
                    ? new ArrayList<>()
                    : new ArrayList<>(state.favGenres);

            renderSelectedGenres();

            String username = state.username == null ? "" : state.username.trim();
            if (username.isEmpty()) username = Str.t(R.string.common_user);

            String email = state.email == null ? "" : state.email.trim();


            tvProfileName.setText(username);

            if (tvIntroUsername != null) {
                tvIntroUsername.setText(username);
            }

            if (tvIntroEmail != null) {
                tvIntroEmail.setText(
                        email.isEmpty() ? Str.t(R.string.common_not_updated) : email
                );
            }

            if (tvIntroGender != null) {
                tvIntroGender.setText(
                        state.gender == null || state.gender.trim().isEmpty()
                                ? Str.t(R.string.common_not_updated)
                                : Str.gender(state.gender)
                );
            }

            if (tvIntroBirthDate != null) {
                tvIntroBirthDate.setText(
                        state.birthDate == null || state.birthDate.trim().isEmpty()
                                ? Str.t(R.string.common_not_updated)
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
                Toast.makeText(ProfileActivity.this, Str.t(R.string.post_stamped), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, currentUid(), post.getPostId(), commentText.trim());
                    postViewModel.addComment(newComment, post);
                    Toast.makeText(ProfileActivity.this, Str.t(R.string.post_comment_sent), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, Str.t(R.string.post_comment_empty), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ProfileActivity.this, Str.t(R.string.post_deleted), Toast.LENGTH_SHORT).show();
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
        profileViewModel.observeProfileDetails(targetUserId);
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
                btnFollow.setText(Boolean.TRUE.equals(following) ? Str.t(R.string.common_following) : Str.t(R.string.common_follow_plus)));

        // Bấm vào số Followers / Following để xem danh sách người đó
        ((View) tvFollowersCount.getParent()).setOnClickListener(
                v -> FollowListDialog.show(this, targetUserId, true));
        ((View) tvFollowingCount.getParent()).setOnClickListener(
                v -> FollowListDialog.show(this, targetUserId, false));

        followViewModel.observeProfile(targetUserId);
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
}