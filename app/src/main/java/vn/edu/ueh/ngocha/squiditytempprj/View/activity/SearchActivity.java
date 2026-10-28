package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.GroupAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.UserSearchAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.SearchTextUtils;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;

public class SearchActivity extends BaseActivity {

    private static final int TYPE_POST = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_GROUP = 2;

    private EditText etGlobalSearch;
    private ImageButton btnBackSearch;
    private TabLayout searchTabLayout;
    private RecyclerView recyclerViewSearchResults;
    private TextView tvSearchEmpty;

    private PostViewModel postViewModel;
    private GroupViewModel groupViewModel;
    private UserProfileViewModel userViewModel;

    private PostAdapter postAdapter;
    private GroupAdapter groupAdapter;
    private UserSearchAdapter userAdapter;

    // Dữ liệu gốc để lọc theo từ khoá
    private List<Post> allPosts = new ArrayList<Post>();
    private List<Group> allGroups = new ArrayList<Group>();
    private List<User> allUsers = new ArrayList<User>();
    private boolean usersLoaded = false;
    private boolean usersRequested = false;

    private int currentSearchType = TYPE_POST;

    // Nhóm từ tài khoản/máy khác về Room xong thì tải lại danh sách nhóm
    private final GroupSyncManager.OnSyncListener syncListener = new GroupSyncManager.OnSyncListener() {
        @Override
        public void onGroupsSynced() {
            groupViewModel.loadAllGroups();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etGlobalSearch = findViewById(R.id.etGlobalSearch);
        btnBackSearch = findViewById(R.id.btnBackSearch);
        searchTabLayout = findViewById(R.id.searchTabLayout);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        tvSearchEmpty = findViewById(R.id.tvSearchEmpty);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        setupAdapters();
        showAdapterForType(currentSearchType);
        observeData();
        usersRequested = true;
        userViewModel.loadAllUsers();

        String initialQuery = getIntent().getStringExtra("SEARCH_QUERY");
        if (initialQuery != null && !initialQuery.isEmpty()) {
            etGlobalSearch.setText(initialQuery);
            etGlobalSearch.setSelection(initialQuery.length());
        }

        btnBackSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentSearchType = tab.getPosition();
                showAdapterForType(currentSearchType);
                performSearch();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Gõ tới đâu tìm tới đó
        etGlobalSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                performSearch();
            }
        });

        etGlobalSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        performSearch();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void onResume() {
        super.onResume();
        GroupSyncManager syncManager = GroupSyncManager.getInstance(getApplication());
        syncManager.addListener(syncListener);
        syncManager.start();
        groupViewModel.loadAllGroups();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GroupSyncManager.getInstance(getApplication()).removeListener(syncListener);
    }

    // =================================
    // Adapter và dữ liệu
    // =================================

    private void setupAdapters() {
        postAdapter = new PostAdapter(this, new ArrayList<Post>(), null);

        groupAdapter = new GroupAdapter();
        groupAdapter.setOnGroupClickListener(new GroupAdapter.OnGroupClickListener() {
            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(SearchActivity.this, GroupViewActivity.class);
                intent.putExtra(GroupViewActivity.EXTRA_GROUP_ID, group.getGroupId());
                startActivity(intent);
            }
        });

        userAdapter = new UserSearchAdapter();
        userAdapter.setOnUserClickListener(new UserSearchAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, user.getUserId());
                startActivity(intent);
            }
        });
    }

    private void showAdapterForType(int type) {
        if (type == TYPE_USER) {
            recyclerViewSearchResults.setAdapter(userAdapter);
        } else if (type == TYPE_GROUP) {
            recyclerViewSearchResults.setAdapter(groupAdapter);
        } else {
            recyclerViewSearchResults.setAdapter(postAdapter);
        }
    }

    private void observeData() {
        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                allPosts = posts != null ? posts : new ArrayList<Post>();
                if (currentSearchType == TYPE_POST) {
                    performSearch();
                }
            }
        });

        groupViewModel.getGroups().observe(this, new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> groups) {
                allGroups = groups != null ? groups : new ArrayList<Group>();
                if (currentSearchType == TYPE_GROUP) {
                    performSearch();
                }
            }
        });

        userViewModel.getSearchableUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                allUsers = users != null
                        ? users
                        : new ArrayList<User>();

                // Tạo danh sách tài khoản để PostAdapter tìm người đăng bài.
                Map<String, User> userMap = new HashMap<>();

                for (User user : allUsers) {
                    if (user == null) {
                        continue;
                    }

                    String userId = user.getUserId();

                    if (userId != null && !userId.trim().isEmpty()) {
                        userMap.put(userId.trim(), user);
                    }

                    // Hỗ trợ cả trường hợp post đang lưu username thay vì UID.
                    String username = user.getUsername();

                    if (username != null && !username.trim().isEmpty()) {
                        userMap.put(username.trim(), user);
                    }
                }

                // Quan trọng: truyền dữ liệu tài khoản vào adapter bài viết.
                postAdapter.setUsers(userMap);

                usersLoaded = true;

                if (currentSearchType == TYPE_USER) {
                    performSearch();
                }
            }
        });
    }

    // =================================
    // Tìm kiếm
    // =================================

    private void performSearch() {
        String rawQuery = etGlobalSearch.getText().toString().trim();
        String query = SearchTextUtils.normalize(rawQuery);

        if (query.isEmpty()) {
            clearResults();
            showEmpty(Str.t(R.string.search_enter_keyword));
            return;
        }

        if (currentSearchType == TYPE_POST) {
            searchPosts(query);
        } else if (currentSearchType == TYPE_USER) {
            searchUsers(query);
        } else {
            searchGroups(query);
        }
    }

    private void searchPosts(String query) {
        List<Post> result = new ArrayList<Post>();
        for (Post post : allPosts) {
            if (SearchTextUtils.contains(post.getContent(), query)
                    || SearchTextUtils.contains(post.getHashtag(), query)
                    || SearchTextUtils.contains(post.getComicTitle(), query)) {
                result.add(post);
            }
        }
        postAdapter.setPosts(result);
        updateEmptyState(result.isEmpty(), Str.t(R.string.search_no_posts));
    }

    private void searchUsers(String query) {
        if (!usersLoaded) {
            // Lần đầu mở tab Người dùng: tải danh sách tài khoản từ Firestore rồi tìm
            if (!usersRequested) {
                usersRequested = true;
                userViewModel.loadAllUsers();
            }
            userAdapter.setUsers(new ArrayList<User>());
            showEmpty(Str.t(R.string.search_loading_users));
            return;
        }
        List<User> result = new ArrayList<User>();
        for (User user : allUsers) {
            if (SearchTextUtils.contains(user.getUsername(), query)) {
                result.add(user);
            }
        }
        userAdapter.setUsers(result);
        updateEmptyState(result.isEmpty(), Str.t(R.string.search_no_users));
    }

    private void searchGroups(String query) {
        List<Group> result = new ArrayList<Group>();
        for (Group group : allGroups) {
            if (SearchTextUtils.contains(group.getGroupName(), query)
                    || SearchTextUtils.contains(group.getDescription(), query)) {
                result.add(group);
            }
        }
        groupAdapter.setGroups(result);
        updateEmptyState(result.isEmpty(), Str.t(R.string.search_no_groups));
    }

    // =================================
    // Hiển thị trạng thái rỗng
    // =================================

    private void clearResults() {
        postAdapter.setPosts(new ArrayList<Post>());
        userAdapter.setUsers(new ArrayList<User>());
        groupAdapter.setGroups(new ArrayList<Group>());
    }

    private void updateEmptyState(boolean empty, String message) {
        if (empty) {
            showEmpty(message);
        } else {
            tvSearchEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(String message) {
        tvSearchEmpty.setText(message);
        tvSearchEmpty.setVisibility(View.VISIBLE);
    }
}
