package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
import androidx.transition.TransitionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.SearchViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;


import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NtfChannel;
import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NotificationPopup;
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FrameLayout headerContainer;
    private NavigationView navigationView;
    private ImageView imgvAvatar;

    private MaterialAutoCompleteTextView etMainSearch;
    private LinearLayout layoutSearchBar;
    private ImageButton btnSearch;
    private LinearLayout searchOverlay;
    private boolean isSearchAnimating = false;
    private int statusBarInset = 0;

    private RecyclerView rcvpostlist;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel;

    private TextView tvMainUsername;
    private TextView tvDrawerUsername;
    private TextView tvDrawerEmail;

    private ShapeableImageView imgMainAvatar;
    private ShapeableImageView imgDrawerAvatar;

    private MaterialButton btnAuthAction;

    private AuthViewModel authViewModel;
    private UserProfileViewModel profileViewModel;

    private View searchDimOverlay;

    private SearchViewModel searchViewModel;
    private ArrayAdapter<String> searchHistoryAdapter;
    private ImageButton btnDoSearch;

    private List<Post> allPosts = new ArrayList<>();
    private String currentSearchQuery = "";

    private boolean isSearchOpen = false;


    private View.OnClickListener onProfileClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout != null) {
                        // Portrait: mở menu
                        drawerLayout.openDrawer(GravityCompat.START);
                    } else {
                        // Landscape: mở hồ sơ trực tiếp
                        Intent intent = new Intent(
                                MainActivity.this,
                                ProfileActivity.class
                        );
                        startActivity(intent);
                    }
                }
            };

    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btnSearch) {
                toggleSearchBar();
            }
            else {
                performHomeSearch();
            }
        }
    };

    private TextView.OnEditorActionListener onSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(
                TextView v, int actionId,
                android.view.KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null
                    && event.getKeyCode()
                    == android.view.KeyEvent.KEYCODE_ENTER
                    && event.getAction()
                    == android.view.KeyEvent.ACTION_DOWN)) {

                performHomeSearch();
                return true;
            }

            return false;
        }
    };

    private AdapterView.OnItemClickListener onSearchHistoryClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(
                AdapterView<?> parent,
                View view,
                int position,
                long id) {

            String query = parent
                    .getItemAtPosition(position)
                    .toString();

            etMainSearch.setText(query);
            etMainSearch.setSelection(query.length());

            performHomeSearch();
        }
    };

    private View.OnFocusChangeListener onSearchFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                etMainSearch.post(new Runnable() {
                    @Override
                    public void run() {
                        etMainSearch.showDropDown();
                    }
                });
            }
        }
    };

    private View.OnClickListener onSearchBGClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isSearchOpen) {
                toggleSearchBar();
            }
        }
    };

    private Observer<List<String>> historyObserver = new Observer<>() {
        @Override
        public void onChanged(List<String> historyList) {
            if (historyList != null) {
                searchHistoryAdapter.clear();
                searchHistoryAdapter.addAll(historyList);
                searchHistoryAdapter.notifyDataSetChanged();
            }
        }
    };

    private NavigationView.OnNavigationItemSelectedListener onAvatarsClickListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_profile) {
                        if (authViewModel.getCurrentUser() == null) {

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Yêu cầu đăng nhập")
                                    .setMessage("Bạn cần đăng nhập để xem profile.")
                                    .setNegativeButton("Hủy", (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .setPositiveButton("Đăng nhập", (dialog, which) -> {
                                        Intent intent = new Intent(
                                                MainActivity.this,
                                                AuthActivity.class
                                        );
                                        startActivity(intent);
                                    })
                                    .show();

                        } else {
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    ProfileActivity.class
                            );
                            startActivity(intent);
                        }

                    } else if (id == R.id.action_chat) {
                         startActivity(new Intent(MainActivity.this, ChatListActivity.class));
                    } else if (id == R.id.action_group) {
                        // Mở màn hình Groups
                        Intent intent = new Intent(
                                MainActivity.this,
                                GroupActivity.class
                        );
                        startActivity(intent);
                    }
                    else if (id == R.id.action_comic) {
                        Intent intent = new Intent(
                                MainActivity.this,
                                ComicListActivity.class
                        );
                        startActivity(intent);
                    }

                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(
                mainView,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    statusBarInset = systemBars.top;

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    if (searchOverlay != null) {
                        updateSearchOverlayPosition();
                    }

                    return insets;
                }
        );

        NtfChannel.setup(this);
        NotificationPopup.attach(this, findViewById(R.id.btnNotification));

        authViewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);

        profileViewModel = new ViewModelProvider(this)
                .get(UserProfileViewModel.class);

        tvMainUsername = findViewById(R.id.tvMainUsername);
        drawerLayout = findViewById(R.id.drawerLayout);
        imgvAvatar = findViewById(R.id.imgvAvatar);
        navigationView = findViewById(R.id.navigationView);


        ViewCompat.setOnApplyWindowInsetsListener(
                navigationView,
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            v.getPaddingLeft(),
                            systemBars.top,
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );

                    return insets;
                }
        );

        rcvpostlist = findViewById(R.id.rcv_postlist);
        View appBar = findViewById(R.id.appBar);
        tvMainUsername = appBar.findViewById(R.id.tvMainUsername);
        imgMainAvatar = appBar.findViewById(R.id.imgvAvatar);

        if (navigationView == null || navigationView.getHeaderCount() == 0) {
            Toast.makeText(this, "Không tìm thấy phần header của menu", Toast.LENGTH_SHORT).show();
            return;
        }

        View header = navigationView.getHeaderView(0);
        tvDrawerUsername = header.findViewById(R.id.tvDrawerUsername);
        tvDrawerEmail = header.findViewById(R.id.tvDrawerEmail);
        imgDrawerAvatar = header.findViewById(R.id.imageView4);
        etMainSearch = findViewById(R.id.etMainSearch);
        layoutSearchBar = findViewById(R.id.layoutSearchBar);
        btnSearch = findViewById(R.id.btnSearch);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        headerContainer = findViewById(R.id.headerContainer);
        searchOverlay = findViewById(R.id.searchOverlay);
        searchDimOverlay = findViewById(R.id.searchDimOverlay);
        btnDoSearch = findViewById(R.id.btnDoSearch);

        searchViewModel = new ViewModelProvider(this)
                .get(SearchViewModel.class);

        searchViewModel.loadhomeHistory();

        searchHistoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<String>()
        );

        etMainSearch.setAdapter(searchHistoryAdapter);
        etMainSearch.setThreshold(0);

        searchViewModel.gethomeHistory().observe(this, historyObserver);

        btnSearch.setOnClickListener(onSearchClickListener);
        searchDimOverlay.setOnClickListener(onSearchBGClickListener);
        etMainSearch.setOnEditorActionListener(onSearchEditorActionListener);
        etMainSearch.setOnItemClickListener(onSearchHistoryClickListener);
        etMainSearch.setOnFocusChangeListener(onSearchFocusChangeListener);
        btnDoSearch.setOnClickListener(onSearchClickListener);

        btnAuthAction.setOnClickListener(v -> {
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user == null) {
                openAuthActivity();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            authViewModel.logout();
                            profileViewModel.loadCurrentProfile();
                            Toast.makeText(
                                    MainActivity.this,
                                    "Đã đăng xuất",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .show();
            }
        });



        imgvAvatar.setOnClickListener(onProfileClickListener);
        navigationView.setNavigationItemSelectedListener(onAvatarsClickListener);

        rcvpostlist.setLayoutManager(new LinearLayoutManager(this));

        postAdapter = new PostAdapter(this, new ArrayList<>(), new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onStampClick(Post post) {
                String randomStampId = UUID.randomUUID().toString();
                Stamp newStamp = new Stamp(randomStampId, "Meringoc_17", StampType.LIKE);
                newStamp.setPostId(post.getPostId());
                postViewModel.addStamp(newStamp);
                Toast.makeText(MainActivity.this, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, "Meringoc_17", post.getPostId(), commentText.trim());
                    postViewModel.addComment(newComment);
                    Toast.makeText(MainActivity.this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onViewCommentsClick(Post post) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                bottomSheetDialog.setContentView(R.layout.layout_comment_sheet);

                RecyclerView rvComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);
                if (rvComments != null) {
                    rvComments.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }

                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
                if (rvComments != null) {
                    rvComments.setAdapter(commentAdapter);
                }

                if (post.getPostId() != null) {
                    postViewModel.getCommentsByPostId(post.getPostId()).observe(MainActivity.this, realComments -> {
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
                    Toast.makeText(MainActivity.this, "Đã xóa bài viết!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditPost(Post post) {
                Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("post_id", post.getPostId());
                intent.putExtra("post_content", post.getContent());
                intent.putExtra("post_comic_id", post.getComicId());
                intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                startActivity(intent);
            }
        });

        rcvpostlist.setAdapter(postAdapter);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> realPosts) {
                if (realPosts != null) {
                    postAdapter.setPosts(realPosts);
                }
            }
        });

        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) {
                return;
            }

            tvMainUsername.setText(state.username);
            tvDrawerUsername.setText(state.username);
            tvDrawerEmail.setText(state.email);

            btnAuthAction.setText(
                    state.loggedIn ? "Đăng xuất" : "Đăng nhập / Đăng ký"
            );

            if (state.avatarUrl == null || state.avatarUrl.isEmpty()) {
                imgMainAvatar.setImageResource(R.drawable.ic_launcher_background);
                imgDrawerAvatar.setImageResource(R.drawable.ic_launcher_background);
            } else {
                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgMainAvatar);

                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgDrawerAvatar);
            }
        });

        profileViewModel.loadCurrentProfile();
    }

    private void openAuthActivity() {
        try {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Không mở được AuthActivity", e);
            Toast.makeText(this, "Không mở được màn hình đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profileViewModel != null) {
            profileViewModel.loadCurrentProfile();
        }
    }

    private void toggleSearchBar() {
        if (isSearchAnimating) {
            return;
        }

        isSearchAnimating = true;

        if (!isSearchOpen) {
            btnSearch.setImageResource(R.drawable.ic_crossx);
            btnSearch.setContentDescription("Đóng tìm kiếm");
            // Mở Search bar
            layoutSearchBar.setVisibility(View.VISIBLE);
            searchDimOverlay.setVisibility(View.VISIBLE);
            searchDimOverlay.setAlpha(0f);
            btnSearch.setTranslationX(10);

            searchDimOverlay.animate()
                    .alpha(1f)
                    .setDuration(320)
                    .start();

            // Đo chiều cao thực tế sau khi layout hoàn tất
            searchOverlay.post(new Runnable() {
                @Override
                public void run() {
                    float distance = layoutSearchBar.getHeight();

                    searchOverlay.setTranslationY(-distance);
                    searchOverlay.setAlpha(0.7f);
                    searchOverlay.setBackground(null);
                    searchOverlay.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(320)
                            .setInterpolator(
                                    new android.view.animation.DecelerateInterpolator()
                            )
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    isSearchAnimating = false;
                                }
                            })
                            .start();

                    isSearchOpen = true;
                }
            });

        } else {
            // Đóng Search bar
            float distance = layoutSearchBar.getHeight();
            btnSearch.setImageResource(R.drawable.ic_search_white);
            btnSearch.setContentDescription("Mở tìm kiếm");
            btnSearch.setTranslationX(-10);

            searchOverlay.animate()
                    .translationY(-distance)
                    .alpha(0.7f)
                    .setDuration(250)
                    .setInterpolator(
                            new android.view.animation.AccelerateInterpolator()
                    )
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            layoutSearchBar.setVisibility(View.GONE);
                            searchOverlay.setTranslationY(0f);
                            searchOverlay.setAlpha(1f);
                            searchOverlay.setBackgroundColor(getColor(R.color.squidity_transparentwhite));
                            isSearchOpen = false;
                            isSearchAnimating = false;

                        }
                    })
                    .start();

            searchDimOverlay.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            searchDimOverlay.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void updateSearchOverlayPosition() {
        if (searchOverlay == null) {
            return;
        }

        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) searchOverlay.getLayoutParams();

        float density = getResources().getDisplayMetrics().density;
        int topMargin = statusBarInset + (int) (72 * density);

        params.topMargin = topMargin;
        searchOverlay.setLayoutParams(params);
    }

    private void performHomeSearch() {
        String query = etMainSearch.getText()
                .toString()
                .trim();

        currentSearchQuery = query;

        if (!query.isEmpty()) {
            // Lưu lịch sử tìm kiếm trang Home
            searchViewModel.savehomeSearch(query);
        }

        // Ẩn bàn phím
        InputMethodManager imm =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    etMainSearch.getWindowToken(), 0);
        }

        etMainSearch.clearFocus();
        toggleSearchBar();

        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra("SEARCH_QUERY", query);
        startActivity(intent);

    }


}