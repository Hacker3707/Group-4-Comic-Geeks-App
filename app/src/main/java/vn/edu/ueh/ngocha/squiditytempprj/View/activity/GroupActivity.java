package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.GroupAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;

/**
 * Màn danh sách group (mở từ mục "Groups" trong thanh kéo của MainActivity).
 * - Bấm một group: mở GroupViewActivity để xem chi tiết.
 * - Bấm dấu +: mở CreateGroupActivity để tạo group mới.
 * - Gõ vào ô tìm kiếm: lọc group theo tên.
 * Dữ liệu lấy qua LiveData của GroupViewModel và được lắng nghe bằng Observer.
 */
public class GroupActivity extends AppCompatActivity {

    // TODO: user thử, dùng chung cho 3 màn Group. Thay bằng user đang đăng nhập khi app có đăng nhập
    public static final String CURRENT_USER_ID = "demo_user_1";

    private GroupViewModel groupViewModel;
    private GroupAdapter groupAdapter;

    private RecyclerView rcvGroups;
    private TextView txtEmpty;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT)
        );
        setContentView(R.layout.group_activity);

        setupInsets();
        bindViews();
        setupRecycler();
        setupListeners();

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        observeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Quay lại từ màn tạo group hoặc xem chi tiết thì tải lại danh sách
        refreshList();
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
        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
