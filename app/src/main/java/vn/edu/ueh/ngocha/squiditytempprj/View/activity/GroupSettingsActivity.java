package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupPermissionUtils;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;

/**
 * Trang cài đặt nhóm (mở từ nút "⋯" cạnh nút Joined ở GroupViewActivity).
 * Chỉ admin của app hoặc người tạo nhóm mới vào được.
 * - Đổi tên, mô tả, ảnh bìa, ảnh đại diện rồi bấm "Lưu thay đổi".
 * - "Giải tán nhóm": xoá nhóm và thành viên, nhóm biến mất khỏi danh sách của mọi người.
 * Dữ liệu lấy qua LiveData của GroupViewModel và lắng nghe bằng Observer.
 */
public class GroupSettingsActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";

    private GroupViewModel groupViewModel;
    private String groupId;
    private Group currentGroup;

    private EditText edtGroupName;
    private EditText edtGroupDescription;
    private EditText edtCoverUrl;
    private EditText edtAvatarUrl;
    private TextView btnSaveSettings;
    private TextView btnDissolveGroup;

    private boolean permissionChecked = false;
    private boolean fieldsFilled = false;
    private boolean saving = false;
    private boolean dissolving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT)
        );
        setContentView(R.layout.group_settings_activity);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            finish();
            return;
        }

        setupInsets();
        bindViews();
        setupListeners();

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        observeData();
        groupViewModel.loadGroup(groupId);
    }

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
        edtGroupName = findViewById(R.id.edtGroupName);
        edtGroupDescription = findViewById(R.id.edtGroupDescription);
        edtCoverUrl = findViewById(R.id.edtCoverUrl);
        edtAvatarUrl = findViewById(R.id.edtAvatarUrl);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnDissolveGroup = findViewById(R.id.btnDissolveGroup);
    }

    private void setupListeners() {
        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        btnDissolveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDissolve();
            }
        });
    }

    // =================================
    // Dữ liệu
    // =================================

    private void observeData() {
        // Thông tin nhóm: lần đầu để điền vào form, sau khi lưu xong thì đóng màn hình
        groupViewModel.getSelectedGroup().observe(this, new Observer<Group>() {
            @Override
            public void onChanged(Group group) {
                if (saving) {
                    Toast.makeText(GroupSettingsActivity.this,
                            R.string.group_settings_saved, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                if (group == null) {
                    finish();
                    return;
                }
                if (!permissionChecked) {
                    permissionChecked = true;
                    if (!GroupPermissionUtils.canManageGroup(GroupActivity.CURRENT_USER_ID, group)) {
                        Toast.makeText(GroupSettingsActivity.this,
                                R.string.group_settings_no_permission, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                currentGroup = group;
                if (!fieldsFilled) {
                    fieldsFilled = true;
                    fillFields(group);
                }
            }
        });

        // deleteGroup chạy xong sẽ cập nhật danh sách group, lúc đó mới đóng màn hình
        groupViewModel.getGroups().observe(this, new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> groups) {
                if (!dissolving) {
                    return;
                }
                Toast.makeText(GroupSettingsActivity.this,
                        R.string.group_dissolve_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void fillFields(Group group) {
        edtGroupName.setText(group.getGroupName());
        edtGroupDescription.setText(group.getDescription());
        edtCoverUrl.setText(group.getCoverImageUrl());
        edtAvatarUrl.setText(group.getAvatarUrl());
    }

    // =================================
    // Hành động
    // =================================

    private void saveSettings() {
        if (saving || dissolving || currentGroup == null) {
            return;
        }
        String name = edtGroupName.getText().toString().trim();
        String description = edtGroupDescription.getText().toString().trim();
        String coverUrl = edtCoverUrl.getText().toString().trim();
        String avatarUrl = edtAvatarUrl.getText().toString().trim();

        if (name.isEmpty()) {
            edtGroupName.setError(getString(R.string.create_group_error_name));
            edtGroupName.requestFocus();
            return;
        }

        saving = true;
        btnSaveSettings.setEnabled(false);
        groupViewModel.updateGroupInfo(currentGroup, name, description, coverUrl, avatarUrl);
    }

    private void confirmDissolve() {
        if (saving || dissolving || currentGroup == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_dissolve_confirm_title)
                .setMessage(R.string.group_dissolve_confirm_message)
                .setPositiveButton(R.string.group_dissolve, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dissolveGroup();
                    }
                })
                .setNegativeButton(R.string.group_cancel, null)
                .show();
    }

    private void dissolveGroup() {
        if (currentGroup == null) {
            return;
        }
        dissolving = true;
        btnDissolveGroup.setEnabled(false);
        btnSaveSettings.setEnabled(false);
        groupViewModel.deleteGroup(currentGroup);
    }
}
