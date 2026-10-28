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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.StampRepository;
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

    public static final String EXTRA_USER_ID = "user_id";

    private static final String STATE_FOLLOW_SHEET = "follow_sheet_mode";
    private static final int FOLLOW_SHEET_NONE = 0;
    private static final int FOLLOW_SHEET_FOLLOWERS = 1;
    private static final int FOLLOW_SHEET_FOLLOWING = 2;
    private int followSheetMode = FOLLOW_SHEET_NONE;
    private BottomSheetDialog followSheet;

    // Follow
    private FollowViewModel followViewModel;
    private ChatViewModel chatViewModel;
    private String targetUserId;
    private boolean isOwnProfile = true;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;
    private MaterialButton btnFollow;

    private UserProfileViewModel profileViewModel;
    private UserViewModel userViewModel;
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

    private final Map<String, User> userMap = new HashMap<>();
    private androidx.lifecycle.LiveData<Map<String, User>> userLive = null;
    private Observer<Map<String, User>> userObserver;
    private String watchedUserKey = "";

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

    private String currentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return "guest";
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private Observer<List<Genre>> genreObserver = new Observer<List<Genre>>() {
        @Override
        public void onChanged(List<Genre> genres) {
            allGenres = genres == null ? new ArrayList<>() : new ArrayList<>(genres);
            renderSelectedGenres();
        }
    };

    private Observer<List<Review>> reviewObserver = new Observer<List<Review>>() {
        @Override
        public void onChanged(List<Review> reviews) {
            reviewAdapter.setReviews(reviews);
            reviewAdapter.setUser(currentProfile);

            List<String> mangaIds = new ArrayList<>();
            if (reviews != null) {
                for (Review review : reviews) {
                    if (review != null && review.getComicId() != null && !mangaIds.contains(review.getComicId())) {
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
            if (user != null && user.getUserId() != null) {
                userMap.put(user.getUserId(), user);
                if (postAdapter != null) {
                    postAdapter.setUsers(userMap);
                }
            }
        }
    };

    private void renderSelectedGenres() {
        if (chipGenreProfile == null) return;

        chipGenreProfile.setSingleSelection(false);
        chipGenreProfile.setSelectionRequired(false);
        chipGenreProfile.removeAllViews();

        Set<String> selectedIds = new HashSet<>(selectedGenreIds);

        for (Genre genre : allGenres) {
            if (genre == null || genre.getGenreId() == null) continue;
            if (!selectedIds.contains(genre.getGenreId())) continue;

            Chip chip = new Chip(this);
            chip.setText(genre.getGenreName());
            chip.setTag(genre.getGenreId());
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
            if (userId == null || userId.trim().isEmpty()) return;
            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        }

        @Override
        public void onComicChipClick(String comicId) {
            Intent intent = new Intent(ProfileActivity.this, ComicDetailActivity.class);
            intent.putExtra("comic_id", comicId);
            intent.putExtra("search_option", "MangaDex");
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            followSheetMode = savedInstanceState.getInt(STATE_FOLLOW_SHEET, FOLLOW_SHEET_NONE);
        }

        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        if (btnBackHome != null) {
            btnBackHome.setVisibility(View.VISIBLE);
            btnBackHome.setOnClickListener(v -> finish());
        }

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileCover = findViewById(R.id.imgProfileCover);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditAvatarImage = findViewById(R.id.btnEditAvatarImage);
        btnEditCoverImage = findViewById(R.id.btnEditCoverImage);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        chipGenreProfile = findViewById(R.id.chipGroupProfile);

        layoutIntroductionDetails = findViewById(R.id.layoutIntroductionDetails);
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        genreViewModel = new ViewModelProvider(this).get(GenreViewModel.class);
        genreViewModel.getAllGenres().observe(this, genreObserver);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userObserver = new Observer<Map<String, User>>() {
            @Override
            public void onChanged(Map<String, User> users) {
                if (users != null) {
                    userMap.putAll(users);
                    if (postAdapter != null) {
                        postAdapter.setUsers(userMap);
                    }
                }
            }
        };

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

        followViewModel = new ViewModelProvider(this).get(FollowViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        FirebaseUser me = currentUser;
        String fromIntent = getIntent().getStringExtra(EXTRA_USER_ID);

        targetUserId = (fromIntent == null || fromIntent.trim().isEmpty())
                ? me.getUid()
                : fromIntent.trim();

        isOwnProfile = targetUserId.equals(me.getUid());
        currentProfileUserId = targetUserId;

        recyclerViewProfileComments = findViewById(R.id.rcvProfileReviews);
        reviewAdapter = new ComicReviewAdapter(reviewInteractionListener);
        reviewAdapter.setupInProfile();

        if (recyclerViewProfileComments != null) {
            recyclerViewProfileComments.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewProfileComments.setAdapter(reviewAdapter);
        }

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        comicViewModel = new ViewModelProvider(this).get(ComicViewModel.class);

        profileViewModel.getUserById(targetUserId).observe(this, profileObserver);
        reviewViewModel.getAllReviewsforUserId(targetUserId).observe(this, reviewObserver);

        tvProfilePostCount = findViewById(R.id.tvProfilePostCount);
        if (currentUser != null) {
            currentProfileUserId = currentUser.getUid();
            currentProfileUsername = currentUser.getDisplayName() == null ? "" : currentUser.getDisplayName();
        }

        if (tvProfilePostCount != null) {
            tvProfilePostCount.setText("0");
        }

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    if (tab == null) return;
                    int position = tab.getPosition();
                    if (layoutIntroductionDetails != null) {
                        layoutIntroductionDetails.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                    }
                    if (recyclerViewProfilePosts != null) {
                        recyclerViewProfilePosts.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                    }
                    if (recyclerViewProfileComments != null) {
                        recyclerViewProfileComments.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
                    }
                }
                @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
                @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
            tabLayout.selectTab(tabLayout.getTabAt(1));
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
        }

        if (imgProfileAvatar != null) {
            imgProfileAvatar.setOnClickListener(v -> showLargeImage(currentAvatarUrl, true));
        }

        if (imgProfileCover != null) {
            imgProfileCover.setOnClickListener(v -> showLargeImage(currentCoverUrl, false));
        }

        if (btnEditAvatarImage != null) {
            btnEditAvatarImage.setOnClickListener(v -> avatarPicker.launch("image/*"));
        }

        if (btnEditCoverImage != null) {
            btnEditCoverImage.setOnClickListener(v -> coverPicker.launch("image/*"));
        }

        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) return;
            selectedGenreIds = state.favGenres == null ? new ArrayList<>() : new ArrayList<>(state.favGenres);
            renderSelectedGenres();

            String username = state.username == null ? "" : state.username.trim();
            if (username.isEmpty()) username = Str.t(R.string.common_user);
            String email = state.email == null ? "" : state.email.trim();

            tvProfileName.setText(username);
            if (tvIntroUsername != null) tvIntroUsername.setText(username);
            if (tvIntroEmail != null) tvIntroEmail.setText(email.isEmpty() ? Str.t(R.string.common_not_updated) : email);
            if (tvIntroGender != null) tvIntroGender.setText(state.gender == null || state.gender.trim().isEmpty() ? Str.t(R.string.common_not_updated) : Str.gender(state.gender));
            if (tvIntroBirthDate != null) tvIntroBirthDate.setText(state.birthDate == null || state.birthDate.trim().isEmpty() ? Str.t(R.string.common_not_updated) : state.birthDate);

            tvProfileEmail.setText(email);
            tvProfileEmail.setVisibility(email.isEmpty() ? View.GONE : View.VISIBLE);

            if (state.avatarUrl != null && !state.avatarUrl.isEmpty()) {
                Glide.with(this).load(state.avatarUrl).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).circleCrop().into(imgProfileAvatar);
            } else {
                imgProfileAvatar.setImageResource(R.drawable.ic_launcher_background);
            }

            if (state.coverUrl != null && !state.coverUrl.isEmpty()) {
                Glide.with(this).load(state.coverUrl).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).centerCrop().into(imgProfileCover);
            } else {
                imgProfileCover.setImageResource(R.drawable.ic_launcher_background);
            }

            boolean isOwner = isOwnProfile && FirebaseAuth.getInstance().getCurrentUser() != null && state.loggedIn;
            if (btnEditProfile != null) btnEditProfile.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            if (btnEditAvatarImage != null) btnEditAvatarImage.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            if (btnEditCoverImage != null) btnEditCoverImage.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            if (isOwner) {
                profileActionButtons.setVisibility(View.GONE);
            } else {
                profileActionButtons.setVisibility(View.VISIBLE);
            }

            currentAvatarUrl = state.avatarUrl == null ? "" : state.avatarUrl;
            currentCoverUrl = state.coverUrl == null ? "" : state.coverUrl;
        });

        profileViewModel.getSaving().observe(this, saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);
            if (btnEditProfile != null) btnEditProfile.setEnabled(!isSaving);
            if (btnEditAvatarImage != null) btnEditAvatarImage.setEnabled(!isSaving);
            if (btnEditCoverImage != null) btnEditCoverImage.setEnabled(!isSaving);
        });

        profileViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewProfilePosts = findViewById(R.id.recyclerViewProfilePosts);
        if (recyclerViewProfilePosts != null) {
            recyclerViewProfilePosts.setLayoutManager(new LinearLayoutManager(this));
        }

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        if (fabCreatePost != null) {
            fabCreatePost.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, CreatePostActivity.class)));
        }

        // Khởi tạo PostAdapter
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

        StampRepository stampRepository = new StampRepository(getApplication());
        postViewModel.getAllPosts().observe(this, realPosts -> {
            if (realPosts == null) {
                if (tvProfilePostCount != null) tvProfilePostCount.setText("0");
                return;
            }

            List<Post> profilePosts = new ArrayList<>();
            int postCount = 0;
            TreeSet<String> userIdsToWatch = new TreeSet<>();

            for (Post post : realPosts) {
                if (post == null || post.getUserId() == null) continue;

                String authorId = post.getUserId().trim();

                // 👉 Đã sửa chuẩn xác: Chỉ lấy bài viết thuộc về chủ nhân trang profile hiện tại (targetUserId)
                boolean isTargetUserPost = authorId.equals(targetUserId);

                if (isTargetUserPost) {
                    postCount++;
                    profilePosts.add(post);
                }
                userIdsToWatch.add(authorId);
            }

            if (postAdapter != null) {
                postAdapter.setPosts(profilePosts);
            }

            if (tvProfilePostCount != null) {
                tvProfilePostCount.setText(String.valueOf(postCount));
            }

            if (!userIdsToWatch.isEmpty()) {
                String key = String.join(",", userIdsToWatch);
                if (!key.equals(watchedUserKey)) {
                    if (userLive != null) {
                        userLive.removeObserver(userObserver);
                    }
                    watchedUserKey = key;
                    userLive = userViewModel.watchUsersByIds(new ArrayList<>(userIdsToWatch));
                    userLive.observe(ProfileActivity.this, userObserver);
                }
            }

            Set<String> myStampedPostIds = new HashSet<>();
            for (Post post : profilePosts) {
                if (post.getPostId() != null) {
                    stampRepository.getStampsByPost(post.getPostId()).observe(ProfileActivity.this, stamps -> {
                        if (stamps != null) {
                            for (Stamp s : stamps) {
                                if (s.getUserId() != null && s.getUserId().equals(currentUid())) {
                                    myStampedPostIds.add(post.getPostId());
                                    break;
                                }
                            }
                            if (postAdapter != null) {
                                postAdapter.setStampedPostIds(myStampedPostIds);
                            }
                        }
                    });
                }
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

    private void setupFollow() {
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnFollow = findViewById(R.id.btnFollow);

        if (isOwnProfile) {
            btnFollow.setVisibility(View.GONE);
        } else {
            View fab = findViewById(R.id.fabCreatePost);
            if (fab != null) fab.setVisibility(View.GONE);

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

        followViewModel.getFollowerCount().observe(this, count ->
                tvFollowersCount.setText(String.valueOf(count == null ? 0 : count)));
        followViewModel.getFollowingCount().observe(this, count ->
                tvFollowingCount.setText(String.valueOf(count == null ? 0 : count)));
        followViewModel.getIsFollowing().observe(this, following ->
                btnFollow.setText(Boolean.TRUE.equals(following) ? Str.t(R.string.common_following) : Str.t(R.string.common_follow_plus)));

        // Bấm vào số Followers / Following để xem danh sách người đó
        ((View) tvFollowersCount.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFollowSheet(true);
            }
        });
        ((View) tvFollowingCount.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFollowSheet(false);
            }
        });

        followViewModel.observeProfile(targetUserId);

        // Vừa xoay màn hình: mở lại danh sách đang xem dở
        if (followSheetMode != FOLLOW_SHEET_NONE) {
            openFollowSheet(followSheetMode == FOLLOW_SHEET_FOLLOWERS);
        }
    }

    // Mở bottom sheet Followers / Following và nhớ lại để mở lại sau khi xoay màn hình
    private void openFollowSheet(boolean followers) {
        if (followSheet != null && followSheet.isShowing()) {
            followSheet.dismiss();
        }
        followSheetMode = followers ? FOLLOW_SHEET_FOLLOWERS : FOLLOW_SHEET_FOLLOWING;
        followSheet = FollowListDialog.show(this, targetUserId, followers, new Runnable() {
            @Override
            public void run() {
                // Xoay màn hình hoặc đóng Activity thì giữ nguyên để mở lại; người dùng tự đóng thì xoá
                if (!isChangingConfigurations() && !isFinishing()) {
                    followSheetMode = FOLLOW_SHEET_NONE;
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_FOLLOW_SHEET, followSheetMode);
    }

    @Override
    protected void onDestroy() {
        if (followSheet != null && followSheet.isShowing()) {
            followSheet.dismiss();
        }
        followSheet = null;
        super.onDestroy();
    }

    private void openChatWithTarget() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || targetUserId == null || targetUserId.isEmpty() || targetUserId.equals(me.getUid())) {
            return;
        }

        String myId = me.getUid();
        String chatRoomId = myId.compareTo(targetUserId) <= 0
                ? myId + "_" + targetUserId
                : targetUserId + "_" + myId;

        chatViewModel.createChatRoom(new ChatRoom(chatRoomId, myId, targetUserId));

        String title = tvProfileName != null ? tvProfileName.getText().toString() : "";

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
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(android.graphics.Color.BLACK);
        container.addView(imageView);

        dialog.setContentView(container);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
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
            loadUserLive();
            loadTargetProfile();
        }
    }

    private void loadUserLive() {
        if (!watchedUserKey.isEmpty()) {
            List<String> ids = new ArrayList<>();
            for (String id : watchedUserKey.split(",")) {
                if (!id.trim().isEmpty()) ids.add(id.trim());
            }
            if (!ids.isEmpty()) {
                if (userLive != null) userLive.removeObserver(userObserver);
                userLive = userViewModel.watchUsersByIds(ids);
                userLive.observe(this, userObserver);
            }
        }
    }
}