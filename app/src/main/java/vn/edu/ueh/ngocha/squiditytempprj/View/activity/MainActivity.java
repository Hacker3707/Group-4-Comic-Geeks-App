package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
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

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.HomeFeedViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.CommentUsersWatcher;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import java.util.Map;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.SearchViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;


import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NtfChannel;
import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NotificationPopup;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity {


    private MaterialAutoCompleteTextView etMainSearch;
    private LinearLayout layoutSearchBar;
    private ImageButton btnSearch;
    private LinearLayout searchOverlay;
    private boolean isSearchAnimating = false;

    private RecyclerView rcvpostlist;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel;
    private HomeFeedViewModel homeFeedViewModel;

    private View searchDimOverlay;

    private SearchViewModel searchViewModel;
    private ArrayAdapter<String> searchHistoryAdapter;
    private ImageButton btnDoSearch;

    private List<Post> allPosts = new ArrayList<>();
    private String currentSearchQuery = "";

    private boolean isSearchOpen = false;


    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_main_content;
    }

    @Override
    protected boolean shouldShowBackHomeButton() {
        return false;
    }


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

    // Hiện mục Admin trong menu nếu là admin; tài khoản bị cấm thì đăng xuất
    private void checkAdminAndBan() {
        if (navigationView == null) {
            return;
        }
        final android.view.MenuItem adminItem = navigationView.getMenu().findItem(R.id.action_admin);
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            if (adminItem != null) adminItem.setVisible(false);
            return;
        }
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(me.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        return;
                    }
                    if (Boolean.TRUE.equals(doc.getBoolean("banned"))) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this,
                                "Tài khoản của bạn đã bị khóa bởi admin", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                        return;
                    }
                    if (adminItem != null) {
                        adminItem.setVisible(AdminActivity.ROLE_ADMIN.equals(doc.getString("role")));
                    }
                });
    }

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
        // BaseActivity đã nạp AppBar, Drawer, profile và layout nội dung.

        rcvpostlist = findViewById(R.id.rcv_postlist);
        etMainSearch = findViewById(R.id.etMainSearch);
        layoutSearchBar = findViewById(R.id.layoutSearchBar);
        btnSearch = findViewById(R.id.btnSearch);
        searchOverlay = findViewById(R.id.searchOverlay);
        searchDimOverlay = findViewById(R.id.searchDimOverlay);
        btnDoSearch = findViewById(R.id.btnDoSearch);

        // Search overlay được đưa lên lớp phủ cấp Activity để không bị đẩy xuống dưới AppBar.
        ViewGroup oldSearchParent = (ViewGroup) searchOverlay.getParent();
        ViewGroup oldDimParent = (ViewGroup) searchDimOverlay.getParent();
        if (oldSearchParent != null) oldSearchParent.removeView(searchOverlay);
        if (oldDimParent != null) oldDimParent.removeView(searchDimOverlay);
        baseOverlayContainer.addView(searchDimOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        baseOverlayContainer.addView(searchOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.TOP));
        updateSearchOverlayPosition();

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

        // Nút tạo bài viết ở trang chủ
        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        if (fabCreatePost != null) {
            fabCreatePost.setOnClickListener(v -> {
                if (authViewModel.getCurrentUser() == null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Yêu cầu đăng nhập")
                            .setMessage("Bạn cần đăng nhập để đăng bài.")
                            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Đăng nhập", (dialog, which) ->
                                    startActivity(new Intent(MainActivity.this, AuthActivity.class)))
                            .show();
                } else {
                    startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
                }
            });
        }

        rcvpostlist.setLayoutManager(new LinearLayoutManager(this));

        postAdapter = new PostAdapter(this, new ArrayList<>(), new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onStampClick(Post post) {
                String randomStampId = UUID.randomUUID().toString();
                Stamp newStamp = new Stamp(randomStampId, currentUid(), StampType.LIKE);
                newStamp.setPostId(post.getPostId());
                postViewModel.addStamp(newStamp, post);
                Toast.makeText(MainActivity.this, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, currentUid(), post.getPostId(), commentText.trim());
                    postViewModel.addComment(newComment, post);
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
                // Hiện đúng tên + ảnh tài khoản của người bình luận
                CommentUsersWatcher usersWatcher = new CommentUsersWatcher(
                        MainActivity.this,
                        new ViewModelProvider(MainActivity.this).get(UserViewModel.class),
                        commentAdapter);
                bottomSheetDialog.setOnDismissListener(d -> usersWatcher.stop());

                if (post.getPostId() != null) {
                    postViewModel.getCommentsByPostId(post.getPostId()).observe(MainActivity.this, realComments -> {
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

        // Trang chủ chỉ hiện bài của người đã follow, bài trong group đã join và bài của mình
        homeFeedViewModel = new ViewModelProvider(this).get(HomeFeedViewModel.class);

        homeFeedViewModel.getGroups().observe(this, new Observer<Map<String, Group>>() {
            @Override
            public void onChanged(Map<String, Group> groups) {
                postAdapter.setGroups(groups);
            }
        });

        homeFeedViewModel.getUsers().observe(this, new Observer<Map<String, User>>() {
            @Override
            public void onChanged(Map<String, User> users) {
                postAdapter.setUsers(users);
            }
        });

        homeFeedViewModel.getFeed().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> feedPosts) {
                if (feedPosts != null) {
                    postAdapter.setPosts(feedPosts);
                }
            }
        });

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
        checkAdminAndBan();
        // Đăng nhập / đăng xuất / đổi tài khoản thì lọc lại feed
        if (homeFeedViewModel != null) {
            homeFeedViewModel.refreshUser();
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
        int topMargin = statusBarInset + (int) (125 * density);

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