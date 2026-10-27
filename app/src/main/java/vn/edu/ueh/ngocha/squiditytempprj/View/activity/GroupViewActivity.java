package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.StampRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.GroupMemberAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupHashtagUtils;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupPermissionUtils;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.SearchTextUtils;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.UserDisplayUtils;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;

/**
 * Màn xem chi tiết một group (theo Figma "Manga Readers"):
 * thông tin group, nút Join/Joined, danh sách thành viên (kèm vai trò),
 * các bài viết trong group và ô đăng bài mới.
 *
 * Mở từ màn danh sách (GroupActivity):
 *   Intent intent = new Intent(context, GroupViewActivity.class);
 *   intent.putExtra(GroupViewActivity.EXTRA_GROUP_ID, groupId);
 *   startActivity(intent);
 */
public class GroupViewActivity extends BaseActivity {

    public static final String EXTRA_GROUP_ID = "group_id";

    // BaseActivity lo khung bên trái (menu cố định khi xoay ngang) và nạp layout này vào khung nội dung
    @Override
    protected int getContentLayoutId() {
        return R.layout.group_view_activity;
    }

    // TODO: user thử, dùng chung với GroupActivity. Thay bằng user đang đăng nhập khi có đăng nhập
    private final String CURRENT_USER_ID = GroupActivity.currentUserId();

    private final GroupSyncManager.OnSyncListener syncListener = new GroupSyncManager.OnSyncListener() {
        @Override
        public void onGroupsSynced() {
            if (groupId != null && groupViewModel != null) {
                groupViewModel.loadGroup(groupId);
                groupViewModel.loadMembers(groupId);
            }
        }
    };

    private static final int TAB_POSTS = 0;
    private static final int TAB_MEMBERS = 1;
    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final String STATE_SELECTED_TAG = "selected_tag";

    private GroupViewModel groupViewModel;
    private PostViewModel postViewModel;
    private UserViewModel userViewModel;

    private ImageView imgCover;
    private TextView txtTitle;
    private TextView txtGroupName;
    private TextView txtAvatarLetter;
    private ImageView imgAvatar;
    private View btnMoreGroup;
    private View imgMore;
    private TextView txtMembers;
    private TextView txtDescription;
    private TextView btnJoin;
    private TextView txtEmpty;
    private LinearLayout composerLayout;
    private EditText edtComposer;
    private View fabGroupPost;
    private boolean composerOpen = false;
    private EditText edtSearchMember;
    private RecyclerView rcvContent;
    private TabLayout tabLayout;
    private LinearLayout chipContainer;
    private LinearLayout tagSuggestionContainer;
    private View tagSuggestionBar;
    private TextView txtTagHelp;

    private PostAdapter postAdapter;
    private GroupMemberAdapter memberAdapter;

    private String groupId;
    private int selectedTab = TAB_POSTS;
    private boolean isMember = false;
    private String myMembershipId = null;
    private boolean isAdminUser = false;

    private String selectedTagKey = null;
    private int hashStart = -1;

    private final List<Post> allPosts = new ArrayList<>();
    private final List<Comment> allComments = new ArrayList<>();
    private final List<Post> groupPosts = new ArrayList<>();
    private final List<Post> visiblePosts = new ArrayList<>();
    private final List<GroupMember> groupMembers = new ArrayList<>();
    private final List<GroupMember> visibleMembers = new ArrayList<>();

    private final Map<String, User> userMap = new HashMap<>();
    private LiveData<Map<String, User>> userLive = null;
    private Observer<Map<String, User>> userObserver;
    private String watchedUserKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BaseActivity đã setContentView (khung menu bên trái + nội dung group)
        super.onCreate(savedInstanceState);
        setupBaseFrame();

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            finish();
            return;
        }

        setupInsets();
        bindViews();
        setupRecycler();
        setupListeners();
        setupChips();
        setupComposerHashtag();
        restoreSelectedChip(savedInstanceState);

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userObserver = new Observer<Map<String, User>>() {
            @Override
            public void onChanged(Map<String, User> users) {
                userMap.clear();
                if (users != null) {
                    userMap.putAll(users);
                }
                memberAdapter.setUsers(userMap);
                postAdapter.setUsers(userMap);
                showSelectedTab();
            }
        };
        observeData();
        loadGroupData();

        if (savedInstanceState != null) {
            int savedTab = savedInstanceState.getInt(STATE_SELECTED_TAB, TAB_POSTS);
            TabLayout.Tab tab = tabLayout.getTabAt(savedTab);
            if (tab != null) {
                tab.select();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        // Giữ chủ đề đang lọc khi xoay màn hình
        outState.putString(STATE_SELECTED_TAG, selectedTagKey);
    }

    // Xoay màn hình thì chọn lại chip chủ đề đang lọc (Activity được tạo lại nên chip bị mất trạng thái)
    private void restoreSelectedChip(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        selectedTagKey = savedInstanceState.getString(STATE_SELECTED_TAG);
        if (selectedTagKey == null) {
            return;
        }
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            View chip = chipContainer.getChildAt(i);
            chip.setSelected(selectedTagKey.equals(chip.getTag()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (groupId != null && groupViewModel != null) {
            groupViewModel.loadGroup(groupId);
        }
        GroupSyncManager syncManager = GroupSyncManager.getInstance(getApplication());
        syncManager.addListener(syncListener);
        syncManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GroupSyncManager.getInstance(getApplication()).removeListener(syncListener);
    }

    // =================================
    // Khởi tạo giao diện
    // =================================

    // Màn group có thanh tiêu đề riêng nên ẩn app bar của BaseActivity,
    // bỏ lề của khung nội dung và tô nền tím cho vùng status bar
    private void setupBaseFrame() {
        View appBar = findViewById(R.id.appBar);
        if (appBar != null) {
            appBar.setVisibility(View.GONE);
        }
        activityContent.setPadding(0, 0, 0, 0);

        int headerColor = getResources().getColor(R.color.group_header);
        View mainContainer = findViewById(R.id.mainContainer);
        if (mainContainer != null) {
            mainContainer.setBackgroundColor(headerColor);
        }
        View landscapeContent = findViewById(R.id.landscapeMainContent);
        if (landscapeContent != null) {
            landscapeContent.setBackgroundColor(headerColor);
        }

        boolean landscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        if (!landscape) {
            // Dọc: thanh tiêu đề tím đậm nên dùng biểu tượng sáng ở thanh trạng thái
            EdgeToEdge.enable(
                    this,
                    SystemBarStyle.dark(Color.TRANSPARENT),
                    SystemBarStyle.dark(Color.TRANSPARENT)
            );
        }
    }

    // Chỉ xử lý phần đáy (thanh điều hướng + bàn phím); phần trên đã do BaseActivity lo
    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                        int bottom = Math.max(systemBars.bottom, keyboard.bottom);
                        v.setPadding(0, 0, 0, bottom);
                        return insets;
                    }
                });
    }

    private void bindViews() {
        imgCover = findViewById(R.id.imgCover);
        txtTitle = findViewById(R.id.txtTitle);
        txtGroupName = findViewById(R.id.txtGroupName);
        txtAvatarLetter = findViewById(R.id.txtAvatarLetter);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnMoreGroup = findViewById(R.id.btnMoreGroup);
        imgMore = findViewById(R.id.imgMore);
        txtMembers = findViewById(R.id.txtMembers);
        txtDescription = findViewById(R.id.txtDescription);
        btnJoin = findViewById(R.id.btnJoin);
        txtEmpty = findViewById(R.id.txtEmpty);
        composerLayout = findViewById(R.id.composerLayout);
        edtComposer = findViewById(R.id.edtComposer);
        fabGroupPost = findViewById(R.id.fabGroupPost);
        fabGroupPost.setOnClickListener(v -> {
            if (groupId == null) {
                return;
            }
            Intent intent = new Intent(GroupViewActivity.this, CreatePostActivity.class);
            intent.putExtra("group_id", groupId);
            startActivity(intent);
        });
        edtSearchMember = findViewById(R.id.edtSearchMember);
        rcvContent = findViewById(R.id.rcvGroupContent);
        tabLayout = findViewById(R.id.tabLayout);
        chipContainer = findViewById(R.id.chipContainer);
        tagSuggestionContainer = findViewById(R.id.tagSuggestionContainer);
        tagSuggestionBar = findViewById(R.id.tagSuggestionBar);
        txtTagHelp = findViewById(R.id.txtTagHelp);
    }

    private void setupRecycler() {
        postAdapter = PostAdapter.forGroupPage(this,
                new vn.edu.ueh.ngocha.squiditytempprj.View.util.SharedPostInteraction(this,
                        new ViewModelProvider(this).get(PostViewModel.class)));
        memberAdapter = new GroupMemberAdapter();
        memberAdapter.setCurrentUserId(CURRENT_USER_ID);
        memberAdapter.setOnMemberClickListener(new GroupMemberAdapter.OnMemberClickListener() {
            @Override
            public void onMemberClick(GroupMember member) {
                onMemberClicked(member);
            }
        });
        rcvContent.setLayoutManager(new LinearLayoutManager(this));
        rcvContent.setAdapter(postAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.imgSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabLayout.Tab membersTab = tabLayout.getTabAt(TAB_MEMBERS);
                if (membersTab != null) {
                    membersTab.select();
                }
                edtSearchMember.requestFocus();
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(edtSearchMember, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        edtSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (selectedTab == TAB_MEMBERS) {
                    showSelectedTab();
                }
            }
        });

        View.OnClickListener moreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGroupSettings();
            }
        };
        imgMore.setOnClickListener(moreClickListener);
        btnMoreGroup.setOnClickListener(moreClickListener);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinClicked();
            }
        });

        findViewById(R.id.btnSendPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPost();
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                showSelectedTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupChips() {
        View.OnClickListener chipClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View clickedChip) {
                boolean wasSelected = clickedChip.isSelected();
                for (int i = 0; i < chipContainer.getChildCount(); i++) {
                    chipContainer.getChildAt(i).setSelected(false);
                }
                clickedChip.setSelected(!wasSelected);
                if (wasSelected) {
                    selectedTagKey = null;
                } else {
                    selectedTagKey = (String) clickedChip.getTag();
                }
                if (selectedTab == TAB_POSTS) {
                    showSelectedTab();
                }
            }
        };
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            chipContainer.getChildAt(i).setOnClickListener(chipClickListener);
        }
    }

    private void observeData() {
        groupViewModel.getSelectedGroup().observe(this, new Observer<Group>() {
            @Override
            public void onChanged(Group group) {
                if (group == null) {
                    finish();
                    return;
                }
                bindGroup(group);
            }
        });

        groupViewModel.getMemberCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                int total = 0;
                if (count != null) {
                    total = count;
                }
                txtMembers.setText(getString(R.string.group_members_count, total));
            }
        });

        groupViewModel.getMembers().observe(this, new Observer<List<GroupMember>>() {
            @Override
            public void onChanged(List<GroupMember> members) {
                groupMembers.clear();
                if (members != null) {
                    groupMembers.addAll(members);
                }
                updateMembership();
                watchUsers();
                groupViewModel.checkAdmin(CURRENT_USER_ID, groupId);
                if (selectedTab == TAB_MEMBERS) {
                    showSelectedTab();
                }
            }
        });

        groupViewModel.getIsAdmin().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean admin) {
                isAdminUser = Boolean.TRUE.equals(admin);
            }
        });

        // 👉 Tích hợp đồng bộ trạng thái Stamp cho GroupViewActivity
        StampRepository stampRepository = new StampRepository(getApplication());

        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                allPosts.clear();
                if (posts != null) {
                    allPosts.addAll(posts);
                }
                filterPostsOfGroup();
                watchUsers();

                if (posts != null && !posts.isEmpty()) {
                    Set<String> myStampedPostIds = new HashSet<>();
                    for (Post post : posts) {
                        if (post.getPostId() != null) {
                            stampRepository.getStampsByPost(post.getPostId()).observe(GroupViewActivity.this, stamps -> {
                                if (stamps != null) {
                                    for (Stamp s : stamps) {
                                        if (s.getUserId() != null && s.getUserId().equals(CURRENT_USER_ID)) {
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

                if (selectedTab == TAB_POSTS) {
                    showSelectedTab();
                }
            }
        });

        postViewModel.getAllComments().observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(List<Comment> comments) {
                allComments.clear();
                if (comments != null) {
                    allComments.addAll(comments);
                }
                postAdapter.setComments(groupCommentsByPost());
                watchUsers();
            }
        });
    }

    private Map<String, List<Comment>> groupCommentsByPost() {
        Map<String, List<Comment>> result = new HashMap<>();
        for (Comment comment : allComments) {
            List<Comment> list = result.get(comment.getPostId());
            if (list == null) {
                list = new ArrayList<>();
                result.put(comment.getPostId(), list);
            }
            list.add(comment);
        }
        for (List<Comment> list : result.values()) {
            Collections.sort(list, new Comparator<Comment>() {
                @Override
                public int compare(Comment a, Comment b) {
                    if (a.getTimestamp() == null || b.getTimestamp() == null) {
                        return 0;
                    }
                    return a.getTimestamp().compareTo(b.getTimestamp());
                }
            });
        }
        return result;
    }

    private void watchUsers() {
        TreeSet<String> ids = new TreeSet<>();
        for (GroupMember member : groupMembers) {
            if (member.getUserId() != null && !member.getUserId().isEmpty()) {
                ids.add(member.getUserId());
            }
        }
        List<String> groupPostIds = new ArrayList<>();
        for (Post post : groupPosts) {
            groupPostIds.add(post.getPostId());
            if (post.getUserId() != null && !post.getUserId().isEmpty()) {
                ids.add(post.getUserId());
            }
        }
        for (Comment comment : allComments) {
            if (groupPostIds.contains(comment.getPostId())
                    && comment.getUserId() != null && !comment.getUserId().isEmpty()) {
                ids.add(comment.getUserId());
            }
        }

        String key = String.join(",", ids);
        if (userLive != null && key.equals(watchedUserKey)) {
            return;
        }
        if (userLive != null) {
            userLive.removeObserver(userObserver);
        }
        watchedUserKey = key;
        userLive = userViewModel.watchUsersByIds(new ArrayList<String>(ids));
        userLive.observe(this, userObserver);
    }

    private String displayNameOf(String userId) {
        return UserDisplayUtils.displayName(
                userMap.get(userId), getString(R.string.group_member_unknown_name));
    }

    private void loadGroupData() {
        groupViewModel.loadGroup(groupId);
        groupViewModel.loadMembers(groupId);
        filterPostsOfGroup();
        showSelectedTab();
    }

    private void filterPostsOfGroup() {
        groupPosts.clear();
        if (groupId == null) {
            return;
        }
        for (Post post : allPosts) {
            if (groupId.equals(post.getGroupId())) {
                groupPosts.add(post);
            }
        }
    }

    private void bindGroup(Group group) {
        if (group == null) {
            return;
        }
        txtTitle.setText(group.getGroupName());
        txtGroupName.setText(group.getGroupName());
        txtDescription.setText(group.getDescription());

        String name = group.getGroupName();
        if (name == null || name.trim().isEmpty()) {
            txtAvatarLetter.setText("?");
        } else {
            txtAvatarLetter.setText(name.trim().substring(0, 1).toUpperCase());
        }
        String avatarUrl = group.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            imgAvatar.setVisibility(View.VISIBLE);
            txtAvatarLetter.setVisibility(View.GONE);
            Glide.with(GroupViewActivity.this)
                    .load(avatarUrl.trim())
                    .centerCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setVisibility(View.GONE);
            txtAvatarLetter.setVisibility(View.VISIBLE);
        }

        boolean canManage = GroupPermissionUtils.canManageGroup(CURRENT_USER_ID, group);
        if (canManage) {
            btnMoreGroup.setVisibility(View.VISIBLE);
            imgMore.setVisibility(View.VISIBLE);
        } else {
            btnMoreGroup.setVisibility(View.GONE);
            imgMore.setVisibility(View.GONE);
        }

        String coverUrl = group.getCoverImageUrl();
        if (coverUrl != null && !coverUrl.trim().isEmpty()) {
            Glide.with(GroupViewActivity.this)
                    .load(coverUrl.trim())
                    .placeholder(R.drawable.group_cover_placeholder)
                    .error(R.drawable.group_cover_placeholder)
                    .centerCrop()
                    .into(imgCover);
        } else {
            imgCover.setImageResource(R.drawable.group_cover_placeholder);
        }
    }

    private void updateMembership() {
        isMember = false;
        myMembershipId = null;
        for (GroupMember member : groupMembers) {
            if (CURRENT_USER_ID.equals(member.getUserId())) {
                isMember = true;
                myMembershipId = member.getMembershipId();
                break;
            }
        }
        if (isMember) {
            btnJoin.setText(R.string.group_joined);
        } else {
            btnJoin.setText(R.string.group_join);
        }
    }

    private void showSelectedTab() {
        if (selectedTab != TAB_MEMBERS) {
            edtSearchMember.setVisibility(View.GONE);
            if (edtSearchMember.length() > 0) {
                edtSearchMember.setText("");
            }
        }
        if (selectedTab == TAB_POSTS) {
            fabGroupPost.setVisibility(View.VISIBLE);
            updateComposerVisibility();
            buildVisiblePosts();
            rcvContent.setAdapter(postAdapter);
            postAdapter.setPosts(visiblePosts);
            if (selectedTagKey == null) {
                showContent(!visiblePosts.isEmpty(), R.string.group_empty_posts);
            } else {
                showContent(!visiblePosts.isEmpty(), R.string.group_empty_tag);
            }
        } else if (selectedTab == TAB_MEMBERS) {
            hideComposerExtras();
            edtSearchMember.setVisibility(View.VISIBLE);
            buildVisibleMembers();
            rcvContent.setAdapter(memberAdapter);
            memberAdapter.setMembers(visibleMembers);
            if (groupMembers.isEmpty()) {
                showContent(false, R.string.group_empty_members);
            } else {
                showContent(!visibleMembers.isEmpty(), R.string.group_empty_member_search);
            }
        } else {
            hideComposerExtras();
            showContent(false, R.string.group_empty_other);
        }
    }

    private void buildVisiblePosts() {
        visiblePosts.clear();
        for (Post post : groupPosts) {
            if (selectedTagKey == null
                    || GroupHashtagUtils.hasTag(post.getContent(), selectedTagKey)) {
                visiblePosts.add(post);
            }
        }
    }

    private void buildVisibleMembers() {
        visibleMembers.clear();
        String query = SearchTextUtils.normalize(edtSearchMember.getText().toString());
        for (GroupMember member : groupMembers) {
            if (query.isEmpty() || SearchTextUtils.contains(displayNameOf(member.getUserId()), query)) {
                visibleMembers.add(member);
            }
        }
    }

    private void updateComposerVisibility() {
        composerLayout.setVisibility(View.GONE);
        txtTagHelp.setVisibility(View.GONE);
        tagSuggestionBar.setVisibility(View.GONE);
    }

    private void hideComposerExtras() {
        fabGroupPost.setVisibility(View.GONE);
        composerLayout.setVisibility(View.GONE);
        txtTagHelp.setVisibility(View.GONE);
        tagSuggestionBar.setVisibility(View.GONE);
    }

    private void setupComposerHashtag() {
        edtComposer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateTagSuggestions();
            }
        });

        View.OnClickListener suggestionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View suggestion) {
                insertHashtag((String) suggestion.getTag());
            }
        };
        for (int i = 0; i < tagSuggestionContainer.getChildCount(); i++) {
            tagSuggestionContainer.getChildAt(i).setOnClickListener(suggestionClickListener);
        }
    }

    private void updateTagSuggestions() {
        Editable text = edtComposer.getText();
        int cursor = edtComposer.getSelectionStart();
        if (cursor < 0 || cursor > text.length()) {
            cursor = text.length();
        }

        hashStart = -1;
        int i = cursor - 1;
        while (i >= 0) {
            char c = text.charAt(i);
            if (c == '#') {
                hashStart = i;
                break;
            }
            if (Character.isWhitespace(c)) {
                break;
            }
            i--;
        }

        if (hashStart < 0 || selectedTab != TAB_POSTS) {
            tagSuggestionBar.setVisibility(View.GONE);
            return;
        }

        String typed = text.subSequence(hashStart + 1, cursor).toString();
        int matchCount = 0;
        for (int k = 0; k < tagSuggestionContainer.getChildCount(); k++) {
            String tag = (String) tagSuggestionContainer.getChildAt(k).getTag();
            if (GroupHashtagUtils.tagStartsWith(tag, typed)) {
                matchCount++;
            }
        }
        for (int k = 0; k < tagSuggestionContainer.getChildCount(); k++) {
            View chip = tagSuggestionContainer.getChildAt(k);
            String tag = (String) chip.getTag();
            if (matchCount == 0 || GroupHashtagUtils.tagStartsWith(tag, typed)) {
                chip.setVisibility(View.VISIBLE);
            } else {
                chip.setVisibility(View.GONE);
            }
        }
        tagSuggestionBar.setVisibility(View.VISIBLE);
    }

    private void insertHashtag(String tag) {
        Editable text = edtComposer.getText();
        int cursor = edtComposer.getSelectionStart();
        if (cursor < 0 || cursor > text.length()) {
            cursor = text.length();
        }
        if (hashStart < 0 || hashStart > cursor) {
            return;
        }
        text.replace(hashStart, cursor, tag + " ");
        int newCursor = Math.min(hashStart + tag.length() + 1, edtComposer.getText().length());
        edtComposer.setSelection(newCursor);
    }

    private void showContent(boolean hasContent, int emptyMessageResId) {
        if (hasContent) {
            rcvContent.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);
        } else {
            rcvContent.setVisibility(View.GONE);
            txtEmpty.setText(emptyMessageResId);
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void openGroupSettings() {
        Intent intent = new Intent(GroupViewActivity.this, GroupSettingsActivity.class);
        intent.putExtra(GroupSettingsActivity.EXTRA_GROUP_ID, groupId);
        startActivity(intent);
    }

    private void onMemberClicked(GroupMember member) {
        if (!isAdminUser) {
            return;
        }
        if (CURRENT_USER_ID.equals(member.getUserId())) {
            Toast.makeText(this, R.string.group_member_self, Toast.LENGTH_SHORT).show();
            return;
        }

        final String membershipId = member.getMembershipId();
        final GroupRole newRole;
        String roleOption;
        if (member.getRole() == GroupRole.ADMIN) {
            newRole = GroupRole.MEMBER;
            roleOption = getString(R.string.group_member_make_member);
        } else {
            newRole = GroupRole.ADMIN;
            roleOption = getString(R.string.group_member_make_admin);
        }
        String[] options = new String[]{roleOption, getString(R.string.group_member_remove)};

        new AlertDialog.Builder(this)
                .setTitle(displayNameOf(member.getUserId()))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            groupViewModel.changeRole(membershipId, newRole, groupId);
                        } else {
                            groupViewModel.removeMember(membershipId, groupId);
                        }
                    }
                })
                .show();
    }

    private void onJoinClicked() {
        if (groupId == null) {
            return;
        }
        if (isMember && myMembershipId != null) {
            groupViewModel.removeMember(myMembershipId, groupId);
        } else {
            groupViewModel.joinGroup(CURRENT_USER_ID, groupId);
        }
    }

    private void sendPost() {
        if (groupId == null) {
            return;
        }
        String content = edtComposer.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        final View btnSend = findViewById(R.id.btnSendPost);
        btnSend.setEnabled(false);

        Post post = new Post(UUID.randomUUID().toString(), CURRENT_USER_ID, null, content, null);
        post.setGroupId(groupId);

        postViewModel.uploadPostToFirestore(post, new PostRepository.OnFirestoreUploadListener() {
            @Override
            public void onSuccess() {
                btnSend.setEnabled(true);
                edtComposer.setText("");
                composerOpen = false;
                updateComposerVisibility();
                Toast.makeText(GroupViewActivity.this, Str.t(R.string.group_post_published), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                btnSend.setEnabled(true);
                Toast.makeText(GroupViewActivity.this, Str.t(R.string.group_post_failed, error), Toast.LENGTH_LONG).show();
            }
        });
    }
}