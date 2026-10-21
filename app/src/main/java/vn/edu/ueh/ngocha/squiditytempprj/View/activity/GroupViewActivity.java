package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.GroupMemberAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.GroupPostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupHashtagUtils;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupPermissionUtils;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

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
public class GroupViewActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";

    // TODO: user thử, dùng chung với GroupActivity. Thay bằng user đang đăng nhập khi có đăng nhập
    private final String CURRENT_USER_ID = GroupActivity.currentUserId();

    // Khi nhóm/thành viên thay đổi trên Firestore thì tải lại trang nhóm này
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

    private GroupViewModel groupViewModel;
    private PostViewModel postViewModel;

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
    private RecyclerView rcvContent;
    private TabLayout tabLayout;
    private LinearLayout chipContainer;
    private LinearLayout tagSuggestionContainer;
    private View tagSuggestionBar;
    private TextView txtTagHelp;

    private GroupPostAdapter postAdapter;
    private GroupMemberAdapter memberAdapter;

    private String groupId;
    private int selectedTab = TAB_POSTS;
    private boolean isMember = false;
    private String myMembershipId = null;
    private boolean isAdminUser = false;

    // Chủ đề đang được chọn ở hàng chip (null = hiện tất cả bài viết)
    private String selectedTagKey = null;
    // Vị trí dấu # mà người dùng đang gõ trong ô viết bài (-1 = không đang gõ hashtag)
    private int hashStart = -1;

    private final List<Post> allPosts = new ArrayList<>();
    private final List<Post> groupPosts = new ArrayList<>();
    private final List<Post> visiblePosts = new ArrayList<>();
    private final List<GroupMember> groupMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thanh tiêu đề màu tím đậm nên dùng biểu tượng sáng ở thanh trạng thái
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT)
        );
        setContentView(R.layout.group_view_activity);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            // Không biết xem group nào thì đóng màn hình
            finish();
            return;
        }

        setupInsets();
        bindViews();
        setupRecycler();
        setupListeners();
        setupChips();
        setupComposerHashtag();

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        observeData();
        loadGroupData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Quay lại từ trang cài đặt thì tải lại thông tin nhóm (đã sửa hoặc đã giải tán)
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

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                        int bottom = Math.max(systemBars.bottom, keyboard.bottom);
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
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
        rcvContent = findViewById(R.id.rcvGroupContent);
        tabLayout = findViewById(R.id.tabLayout);
        chipContainer = findViewById(R.id.chipContainer);
        tagSuggestionContainer = findViewById(R.id.tagSuggestionContainer);
        tagSuggestionBar = findViewById(R.id.tagSuggestionBar);
        txtTagHelp = findViewById(R.id.txtTagHelp);
    }

    private void setupRecycler() {
        postAdapter = new GroupPostAdapter();
        memberAdapter = new GroupMemberAdapter();
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
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }

    // Chip chủ đề: bấm để lọc các bài viết có hashtag tương ứng, bấm lần nữa để hiện tất cả.
    // Bài không có hashtag hợp lệ nằm trong chip "Khác".
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

    // =================================
    // Lấy dữ liệu từ ViewModel
    // =================================

    private void observeData() {
        groupViewModel.getSelectedGroup().observe(this, new Observer<Group>() {
            @Override
            public void onChanged(Group group) {
                if (group == null) {
                    // Nhóm đã bị giải tán hoặc không còn tồn tại
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
                groupViewModel.checkAdmin(CURRENT_USER_ID, groupId);
                if (selectedTab == TAB_MEMBERS) {
                    showSelectedTab();
                }
            }
        });

        // LiveData cho biết user hiện tại có phải admin của group không
        groupViewModel.getIsAdmin().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean admin) {
                isAdminUser = Boolean.TRUE.equals(admin);
            }
        });

        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                allPosts.clear();
                if (posts != null) {
                    allPosts.addAll(posts);
                }
                filterPostsOfGroup();
                if (selectedTab == TAB_POSTS) {
                    showSelectedTab();
                }
            }
        });
    }

    private void loadGroupData() {
        groupViewModel.loadGroup(groupId);
        groupViewModel.loadMembers(groupId);
        filterPostsOfGroup();
        showSelectedTab();
    }

    // Post chưa có hàm lấy theo group nên lọc ở đây dựa trên groupId
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

    // =================================
    // Hiển thị dữ liệu
    // =================================

    private void bindGroup(Group group) {
        if (group == null) {
            return;
        }
        txtTitle.setText(group.getGroupName());
        txtGroupName.setText(group.getGroupName());
        txtDescription.setText(group.getDescription());

        // Có link ảnh đại diện thì hiện ảnh, chưa có thì dùng chữ cái đầu của tên nhóm
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

        // Chỉ admin của app và người tạo nhóm mới thấy nút cài đặt nhóm
        boolean canManage = GroupPermissionUtils.canManageGroup(CURRENT_USER_ID, group);
        if (canManage) {
            btnMoreGroup.setVisibility(View.VISIBLE);
            imgMore.setVisibility(View.VISIBLE);
        } else {
            btnMoreGroup.setVisibility(View.GONE);
            imgMore.setVisibility(View.GONE);
        }

        // Có link ảnh bìa thì tải ảnh của nhóm, chưa có thì dùng nền tím mặc định
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

    // Kiểm tra user hiện tại đã tham gia group chưa để đổi nút Join/Joined
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
        if (selectedTab == TAB_POSTS) {
            composerLayout.setVisibility(View.VISIBLE);
            txtTagHelp.setVisibility(View.VISIBLE);
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
            rcvContent.setAdapter(memberAdapter);
            memberAdapter.setMembers(groupMembers);
            showContent(!groupMembers.isEmpty(), R.string.group_empty_members);
        } else {
            // Sự kiện, File, Giới thiệu: chưa có dữ liệu
            hideComposerExtras();
            showContent(false, R.string.group_empty_other);
        }
    }

    // groupPosts đã theo thứ tự mới nhất trước (PostDao sắp xếp theo timestamp giảm dần),
    // lọc theo chủ đề vẫn giữ nguyên thứ tự đó
    private void buildVisiblePosts() {
        visiblePosts.clear();
        for (Post post : groupPosts) {
            if (selectedTagKey == null
                    || GroupHashtagUtils.hasTag(post.getContent(), selectedTagKey)) {
                visiblePosts.add(post);
            }
        }
    }

    private void hideComposerExtras() {
        composerLayout.setVisibility(View.GONE);
        txtTagHelp.setVisibility(View.GONE);
        tagSuggestionBar.setVisibility(View.GONE);
    }

    // =================================
    // Hashtag khi soạn bài
    // =================================

    private void setupComposerHashtag() {
        edtComposer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần xử lý
            }

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

    // Khi người dùng đang gõ "#..." thì hiện các chủ đề hợp lệ để chọn
    private void updateTagSuggestions() {
        Editable text = edtComposer.getText();
        int cursor = edtComposer.getSelectionStart();
        if (cursor < 0 || cursor > text.length()) {
            cursor = text.length();
        }

        // Tìm dấu # gần nhất ở phía trước con trỏ (không bị ngắt bởi khoảng trắng)
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

        // Chỉ hiện chủ đề khớp phần đã gõ; không khớp cái nào thì hiện đủ để người dùng chọn
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

    // Thay phần "#..." đang gõ bằng chủ đề người dùng vừa chọn
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

    // =================================
    // Hành động của người dùng
    // =================================

    // Nút "⋯": mở trang cài đặt nhóm (chỉ hiện với admin của app và người tạo nhóm)
    private void openGroupSettings() {
        Intent intent = new Intent(GroupViewActivity.this, GroupSettingsActivity.class);
        intent.putExtra(GroupSettingsActivity.EXTRA_GROUP_ID, groupId);
        startActivity(intent);
    }

    // Admin bấm vào một thành viên để đổi vai trò hoặc xoá khỏi nhóm
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
                .setTitle(member.getUserId())
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
        //Post post = new Post(UUID.randomUUID().toString(), CURRENT_USER_ID, null, content);
        //post.setGroupId(groupId);
        //postViewModel.insertPost(post);
        edtComposer.setText("");
    }
}
