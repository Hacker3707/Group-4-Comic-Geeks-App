package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupImageUploader;
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

    private static final String STATE_COVER_URI = "cover_uri";
    private static final String STATE_AVATAR_URI = "avatar_uri";
    private static final String STATE_COVER_URL = "cover_url";
    private static final String STATE_AVATAR_URL = "avatar_url";
    private static final String STATE_FIELDS_FILLED = "fields_filled";

    private GroupViewModel groupViewModel;
    private String groupId;
    private Group currentGroup;

    private EditText edtGroupName;
    private EditText edtGroupDescription;
    private ImageView imgCoverPreview;
    private ImageView imgAvatarPreview;
    private TextView txtCoverHint;
    private TextView txtAvatarHint;
    private TextView btnRemoveCover;
    private TextView btnRemoveAvatar;
    private TextView btnSaveSettings;
    private TextView btnDissolveGroup;

    private boolean permissionChecked = false;
    private boolean fieldsFilled = false;
    private boolean saving = false;
    private boolean uploading = false;

    // Link ảnh đang có của nhóm (rỗng nếu chưa có hoặc người dùng bấm "Xóa ảnh")
    private String currentCoverUrl = "";
    private String currentAvatarUrl = "";
    // Ảnh mới vừa chọn từ thư viện (null nếu không đổi)
    private Uri coverUri = null;
    private Uri avatarUri = null;

    // Photo Picker của Android: chọn ảnh mà không cần xin quyền đọc bộ nhớ
    private final ActivityResultLauncher<PickVisualMediaRequest> pickCoverLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                coverUri = uri;
                                refreshCoverPreview();
                            }
                        }
                    });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                avatarUri = uri;
                                refreshAvatarPreview();
                            }
                        }
                    });
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

        // Xoay màn hình thì giữ lại ảnh đã chọn/đã xóa và chữ đang sửa (không điền lại từ dữ liệu cũ)
        if (savedInstanceState != null) {
            fieldsFilled = savedInstanceState.getBoolean(STATE_FIELDS_FILLED, false);
            currentCoverUrl = nonNull(savedInstanceState.getString(STATE_COVER_URL));
            currentAvatarUrl = nonNull(savedInstanceState.getString(STATE_AVATAR_URL));
            String savedCover = savedInstanceState.getString(STATE_COVER_URI);
            if (savedCover != null) {
                coverUri = Uri.parse(savedCover);
            }
            String savedAvatar = savedInstanceState.getString(STATE_AVATAR_URI);
            if (savedAvatar != null) {
                avatarUri = Uri.parse(savedAvatar);
            }
            if (fieldsFilled) {
                refreshCoverPreview();
                refreshAvatarPreview();
            }
        }

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        observeData();
        groupViewModel.loadGroup(groupId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FIELDS_FILLED, fieldsFilled);
        outState.putString(STATE_COVER_URL, currentCoverUrl);
        outState.putString(STATE_AVATAR_URL, currentAvatarUrl);
        if (coverUri != null) {
            outState.putString(STATE_COVER_URI, coverUri.toString());
        }
        if (avatarUri != null) {
            outState.putString(STATE_AVATAR_URI, avatarUri.toString());
        }
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
        imgCoverPreview = findViewById(R.id.imgCoverPreview);
        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);
        txtCoverHint = findViewById(R.id.txtCoverHint);
        txtAvatarHint = findViewById(R.id.txtAvatarHint);
        btnRemoveCover = findViewById(R.id.btnRemoveCover);
        btnRemoveAvatar = findViewById(R.id.btnRemoveAvatar);
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

        findViewById(R.id.layoutCoverPick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!saving && !uploading) {
                    pickCoverLauncher.launch(imageOnlyRequest());
                }
            }
        });

        findViewById(R.id.layoutAvatarPick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!saving && !uploading) {
                    pickAvatarLauncher.launch(imageOnlyRequest());
                }
            }
        });

        btnRemoveCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!saving && !uploading) {
                    coverUri = null;
                    currentCoverUrl = "";
                    refreshCoverPreview();
                }
            }
        });

        btnRemoveAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!saving && !uploading) {
                    avatarUri = null;
                    currentAvatarUrl = "";
                    refreshAvatarPreview();
                }
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
                    if (!GroupPermissionUtils.canManageGroup(GroupActivity.currentUserId(), group)) {
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
        currentCoverUrl = nonNull(group.getCoverImageUrl());
        currentAvatarUrl = nonNull(group.getAvatarUrl());
        refreshCoverPreview();
        refreshAvatarPreview();
    }

    private String nonNull(String value) {
        return value == null ? "" : value.trim();
    }

    // Chỉ cho chọn ảnh (không chọn video)
    private PickVisualMediaRequest imageOnlyRequest() {
        return new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
    }

    // Ưu tiên ảnh vừa chọn, sau đó đến ảnh hiện có của nhóm, không có thì hiện nền tím mặc định
    private void refreshCoverPreview() {
        if (coverUri != null) {
            Glide.with(this).load(coverUri).centerCrop().into(imgCoverPreview);
            txtCoverHint.setVisibility(View.GONE);
            btnRemoveCover.setVisibility(View.VISIBLE);
        } else if (!currentCoverUrl.isEmpty()) {
            Glide.with(this).load(currentCoverUrl)
                    .placeholder(R.drawable.group_cover_placeholder)
                    .error(R.drawable.group_cover_placeholder)
                    .centerCrop()
                    .into(imgCoverPreview);
            txtCoverHint.setVisibility(View.GONE);
            btnRemoveCover.setVisibility(View.VISIBLE);
        } else {
            Glide.with(this).clear(imgCoverPreview);
            imgCoverPreview.setImageResource(R.drawable.group_cover_placeholder);
            txtCoverHint.setVisibility(View.VISIBLE);
            btnRemoveCover.setVisibility(View.GONE);
        }
    }

    private void refreshAvatarPreview() {
        if (avatarUri != null) {
            Glide.with(this).load(avatarUri).centerCrop().into(imgAvatarPreview);
            txtAvatarHint.setVisibility(View.GONE);
            btnRemoveAvatar.setVisibility(View.VISIBLE);
        } else if (!currentAvatarUrl.isEmpty()) {
            Glide.with(this).load(currentAvatarUrl)
                    .placeholder(R.drawable.group_cover_placeholder)
                    .error(R.drawable.group_cover_placeholder)
                    .centerCrop()
                    .into(imgAvatarPreview);
            txtAvatarHint.setVisibility(View.GONE);
            btnRemoveAvatar.setVisibility(View.VISIBLE);
        } else {
            Glide.with(this).clear(imgAvatarPreview);
            imgAvatarPreview.setImageResource(R.drawable.group_cover_placeholder);
            txtAvatarHint.setVisibility(View.VISIBLE);
            btnRemoveAvatar.setVisibility(View.GONE);
        }
    }

    // =================================
    // Hành động
    // =================================

    private void saveSettings() {
        if (saving || uploading || dissolving || currentGroup == null) {
            return;
        }
        final String name = edtGroupName.getText().toString().trim();
        final String description = edtGroupDescription.getText().toString().trim();

        if (name.isEmpty()) {
            edtGroupName.setError(getString(R.string.create_group_error_name));
            edtGroupName.requestFocus();
            return;
        }

        // Tải ảnh mới lên trước (nếu có chọn), có link rồi mới lưu nhóm
        uploading = true;
        btnSaveSettings.setEnabled(false);
        btnSaveSettings.setText(R.string.group_image_uploading);
        GroupImageUploader.uploadImages(this, coverUri, avatarUri, currentCoverUrl, currentAvatarUrl,
                new GroupImageUploader.ResultListener() {
                    @Override
                    public void onDone(String coverUrl, String avatarUrl) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        uploading = false;
                        saving = true;
                        groupViewModel.updateGroupInfo(
                                currentGroup, name, description, coverUrl, avatarUrl);
                    }

                    @Override
                    public void onError(int messageResId) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        uploading = false;
                        btnSaveSettings.setEnabled(true);
                        btnSaveSettings.setText(R.string.group_settings_save);
                        Toast.makeText(GroupSettingsActivity.this, messageResId,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmDissolve() {
        if (saving || uploading || dissolving || currentGroup == null) {
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
