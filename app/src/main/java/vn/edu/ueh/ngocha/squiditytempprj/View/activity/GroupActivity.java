package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.GroupAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GroupSyncManager;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;

/**
 * Màn danh sách group (mở từ mục "Groups" trong thanh kéo của MainActivity).
 * - Bấm một group: mở GroupViewActivity để xem chi tiết.
 * - Bấm dấu +: mở CreateGroupActivity để tạo group mới.
 * - Gõ vào ô tìm kiếm: lọc group theo tên.
 * Dữ liệu lấy qua LiveData của GroupViewModel và được lắng nghe bằng Observer.
 */
public class GroupActivity extends BaseActivity {

    // BaseActivity sẽ inflate layout này vào khung nội dung (có sẵn thanh trên + menu kéo)
    @Override
    protected int getContentLayoutId() {
        return R.layout.group_activity;
    }

    // TODO: user thử, dùng chung cho 3 màn Group. Thay bằng user đang đăng nhập khi app có đăng nhập
    public static final String CURRENT_USER_ID = "demo_user_1";

    // Id người dùng thật (uid Firebase). Chưa đăng nhập thì dùng id thử ở trên.
    public static String currentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : CURRENT_USER_ID;
    }

    // Khi dữ liệu nhóm từ Firestore về Room thì tải lại danh sách
    private final GroupSyncManager.OnSyncListener syncListener = new GroupSyncManager.OnSyncListener() {
        @Override
        public void onGroupsSynced() {
            refreshList();
        }
    };

    private GroupViewModel groupViewModel;
    private GroupAdapter groupAdapter;

    private RecyclerView rcvGroups;
    private TextView txtEmpty;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BaseActivity đã lo setContentView, EdgeToEdge và insets nên không gọi lại ở đây
        super.onCreate(savedInstanceState);

        bindViews();
        setupRecycler();
        setupListeners();

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        observeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lắng nghe nhóm từ Firestore: nhóm do tài khoản/máy khác tạo sẽ hiện ra ở đây
        GroupSyncManager syncManager = GroupSyncManager.getInstance(getApplication());
        syncManager.addListener(syncListener);
        syncManager.start();
        // Quay lại từ màn tạo group hoặc xem chi tiết thì tải lại danh sách
        refreshList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GroupSyncManager.getInstance(getApplication()).removeListener(syncListener);
    }

    // =================================
    // Khởi tạo giao diện
    // =================================

    private void bindViews() {
        rcvGroups = findViewById(R.id.rcvGroups);
        txtEmpty = findViewById(R.id.txtEmpty);
        edtSearch = findViewById(R.id.edtSearch);
    }

    private void setupRecycler() {
        groupAdapter = new GroupAdapter();
        groupAdapter.setOnGroupClickListener(new GroupAdapter.OnGroupClickListener() {
            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(GroupActivity.this, GroupViewActivity.class);
                intent.putExtra(GroupViewActivity.EXTRA_GROUP_ID, group.getGroupId());
                startActivity(intent);
            }
        });
        rcvGroups.setLayoutManager(new LinearLayoutManager(this));
        rcvGroups.setAdapter(groupAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.imgAddGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
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
                refreshList();
            }
        });
    }

    // =================================
    // Dữ liệu
    // =================================

    private void observeData() {
        groupViewModel.getGroups().observe(this, new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> groups) {
                groupAdapter.setGroups(groups);
                boolean isEmpty = groups == null || groups.isEmpty();
                if (isEmpty) {
                    rcvGroups.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                } else {
                    rcvGroups.setVisibility(View.VISIBLE);
                    txtEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    // Ô tìm kiếm trống thì lấy tất cả group, có chữ thì tìm theo tên
    private void refreshList() {
        String keyword = edtSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            groupViewModel.loadAllGroups();
        } else {
            groupViewModel.searchGroups(keyword);
        }
    }
}