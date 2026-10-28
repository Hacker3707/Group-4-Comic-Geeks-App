package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseUser;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.StampRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;

import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.HomeFeedViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.CommentUsersWatcher;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import java.util.Map;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.SearchViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity {

    private MaterialAutoCompleteTextView etMainSearch;
    private LinearLayout layoutSearchBar;
    private ImageButton btnSearch;
    private LinearLayout searchOverlay;
    private boolean isSearchAnimating = false;

    private RecyclerView rcvpostlist;
    public static final String EXTRA_POST_ID = "extra_post_id";
    private String pendingPostId;
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

    @Override
    protected String getActivityName() { return "MainActivity"; }

    @Override
    protected boolean isHome() {
        return true;
    }

    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btnSearch) {
                toggleSearchBar();
            } else {
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
                                Str.t(R.string.account_banned_by_admin), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                        return;
                    }
                    if (adminItem != null) {
                        adminItem.setVisible(AdminActivity.ROLE_ADMIN.equals(doc.getString("role")));
                    }
                });
    }

    private String currentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return "guest";
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rcvpostlist = findViewById(R.id.rcv_postlist);
        etMainSearch = findViewById(R.id.etMainSearch);
        layoutSearchBar = findViewById(R.id.layoutSearchBar);
        btnSearch = findViewById(R.id.btnSearch);
        searchOverlay = findViewById(R.id.searchOverlay);
        searchDimOverlay = findViewById(R.id.searchDimOverlay);
        btnDoSearch = findViewById(R.id.btnDoSearch);

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

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        if (fabCreatePost != null) {
            fabCreatePost.setOnClickListener(v -> {
                if (authViewModel.getCurrentUser() == null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(Str.t(R.string.common_login_required_title))
                            .setMessage(Str.t(R.string.post_login_required_msg))
                            .setNegativeButton(Str.t(R.string.common_cancel), (dialog, which) -> dialog.dismiss())
                            .setPositiveButton(Str.t(R.string.common_login), (dialog, which) ->
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
                Toast.makeText(MainActivity.this, Str.t(R.string.post_stamped), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, currentUid(), post.getPostId(), commentText.trim());
                    postViewModel.addComment(newComment, post);
                    Toast.makeText(MainActivity.this, Str.t(R.string.post_comment_sent), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, Str.t(R.string.post_comment_empty), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onViewCommentsClick(Post post) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                bottomSheetDialog.setContentView(R.layout.layout_comment_sheet);
                if (bottomSheetDialog.getWindow() != null) {
                    bottomSheetDialog.getWindow().setSoftInputMode(
                            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    );
                }

                View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheetInternal != null) {
                    com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                            com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheetInternal);
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                }

                RecyclerView rvComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);
                EditText edtCommentInput = bottomSheetDialog.findViewById(R.id.edtCommentInput);
                ImageButton btnSend = bottomSheetDialog.findViewById(R.id.btnSendCommentSheet);

                if (rvComments != null) {
                    rvComments.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }

                final String[] activeParentId = {null};
                final String[] activeReplyUsername = {null};

                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>(), commentClicked -> {
                    // Gán chính xác ID của comment được chọn làm cha
                    activeParentId[0] = commentClicked.getCommentId();

                    // Lấy tên người được trả lời
                    String targetName = commentClicked.getReplyToUsername();
                    if (targetName == null || targetName.trim().isEmpty()) {
                        targetName = commentClicked.getUserId();
                    }
                    activeReplyUsername[0] = targetName;

                    if (edtCommentInput != null) {
                        edtCommentInput.setHint("Đang trả lời @" + targetName + "...");
                        edtCommentInput.requestFocus();

                        if (bottomSheetDialog.getWindow() != null) {
                            bottomSheetDialog.getWindow().setSoftInputMode(
                                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            );
                        }

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(edtCommentInput, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });

                if (rvComments != null) {
                    rvComments.setAdapter(commentAdapter);
                }

                CommentUsersWatcher usersWatcher = new CommentUsersWatcher(
                        MainActivity.this,
                        new ViewModelProvider(MainActivity.this).get(UserViewModel.class),
                        commentAdapter);
                bottomSheetDialog.setOnDismissListener(d -> usersWatcher.stop());

                if (post.getPostId() != null) {
                    postViewModel.getCommentsByPostId(post.getPostId()).observe(MainActivity.this, realComments -> {
                        if (realComments != null) {
                            // Sắp xếp phân cấp để comment con hiển thị ngay dưới comment cha
                            List<Comment> organizedComments = organizeComments(realComments);

                            commentAdapter.setComments(organizedComments);
                            usersWatcher.update(organizedComments);
                            if (!organizedComments.isEmpty() && rvComments != null) {
                                rvComments.scrollToPosition(organizedComments.size() - 1);
                            }
                        }
                    });
                }

                if (btnSend != null && edtCommentInput != null) {
                    btnSend.setOnClickListener(v -> {
                        String commentText = edtCommentInput.getText().toString().trim();
                        if (!commentText.isEmpty()) {
                            String randomCommentId = UUID.randomUUID().toString();
                            Comment newComment = new Comment(randomCommentId, currentUid(), post.getPostId(), commentText);

                            if (activeParentId[0] != null) {
                                newComment.setParentId(activeParentId[0]);
                                newComment.setReplyToUsername(activeReplyUsername[0]);
                            }

                            postViewModel.addComment(newComment, post);

                            edtCommentInput.setText("");
                            edtCommentInput.setHint("Viết bình luận...");
                            activeParentId[0] = null;
                            activeReplyUsername[0] = null;

                            Toast.makeText(MainActivity.this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                bottomSheetDialog.show();
            }

            @Override
            public void onDeletePost(Post post) {
                if (post != null) {
                    postViewModel.deletePost(post);
                    Toast.makeText(MainActivity.this, Str.t(R.string.post_deleted), Toast.LENGTH_SHORT).show();
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

        StampRepository stampRepository = new StampRepository(getApplication());
        homeFeedViewModel.getFeed().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> feedPosts) {
                if (feedPosts != null) {
                    postAdapter.setPosts(feedPosts);
                    scrollToPendingPost(feedPosts);

                    Set<String> myStampedPostIds = new HashSet<>();
                    for (Post post : feedPosts) {
                        if (post.getPostId() != null) {
                            stampRepository.getStampsByPost(post.getPostId()).observe(MainActivity.this, stamps -> {
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
                }
            }
        });

        pendingPostId = getIntent().getStringExtra(EXTRA_POST_ID);
    }

    // 📌 Hàm sắp xếp phân cấp bình luận (Parent -> Children)
    private List<Comment> organizeComments(List<Comment> rawComments) {
        List<Comment> organized = new ArrayList<>();
        if (rawComments == null) return organized;

        Map<String, List<Comment>> childrenMap = new HashMap<>();
        List<Comment> roots = new ArrayList<>();

        for (Comment c : rawComments) {
            String parentId = c.getParentId();
            if (parentId == null || parentId.trim().isEmpty()) {
                roots.add(c);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
            }
        }

        for (Comment root : roots) {
            organized.add(root);
            addChildrenRecursive(root.getCommentId(), childrenMap, organized);
        }

        for (Comment c : rawComments) {
            if (!organized.contains(c)) {
                organized.add(c);
            }
        }

        return organized;
    }

    private void addChildrenRecursive(String parentId, Map<String, List<Comment>> childrenMap, List<Comment> organized) {
        List<Comment> children = childrenMap.get(parentId);
        if (children != null) {
            for (Comment child : children) {
                if (!organized.contains(child)) {
                    organized.add(child);
                    addChildrenRecursive(child.getCommentId(), childrenMap, organized);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        pendingPostId = intent.getStringExtra(EXTRA_POST_ID);
        if (pendingPostId != null && homeFeedViewModel.getFeed().getValue() != null) {
            scrollToPendingPost(homeFeedViewModel.getFeed().getValue());
        }
    }

    private void scrollToPendingPost(List<Post> posts) {
        if (pendingPostId == null) return;
        for (int i = 0; i < posts.size(); i++) {
            if (pendingPostId.equals(posts.get(i).getPostId())) {
                rcvpostlist.scrollToPosition(i);
                break;
            }
        }
        pendingPostId = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profileViewModel != null) {
            profileViewModel.loadCurrentProfile();
        }
        checkAdminAndBan();
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
            btnSearch.setContentDescription(Str.t(R.string.search_close_cd));
            layoutSearchBar.setVisibility(View.VISIBLE);
            searchDimOverlay.setVisibility(View.VISIBLE);
            searchDimOverlay.setAlpha(0f);
            btnSearch.setTranslationX(10);

            searchDimOverlay.animate()
                    .alpha(1f)
                    .setDuration(320)
                    .start();

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
            float distance = layoutSearchBar.getHeight();
            btnSearch.setImageResource(R.drawable.ic_search_white);
            btnSearch.setContentDescription(Str.t(R.string.search_open_cd));
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
            searchViewModel.savehomeSearch(query);
        }

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