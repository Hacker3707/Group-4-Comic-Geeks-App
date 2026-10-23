package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

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
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;

/**
 * Màn tạo group mới: nhập tên, mô tả, chọn ảnh bìa và ảnh đại diện từ thư viện ảnh rồi bấm "Tạo nhóm".
 * Ảnh được tải lên Firebase Storage trước, có link rồi mới tạo group.
 * Người tạo tự động là ADMIN của group (xử lý trong GroupViewModel.createGroup).
 * Khi ViewModel tạo xong sẽ cập nhật LiveData, Observer ở đây nhận được thì đóng màn hình.
 */
public class CreateGroupActivity extends AppCompatActivity {

    private static final String STATE_COVER_URI = "cover_uri";
    private static final String STATE_AVATAR_URI = "avatar_uri";

    private GroupViewModel groupViewModel;

    private EditText edtGroupName;
    private EditText edtGroupDescription;
    private ImageView imgCoverPreview;
    private ImageView imgAvatarPreview;
    private TextView txtCoverHint;
    private TextView txtAvatarHint;
    private TextView btnRemoveCover;
    private TextView btnRemoveAvatar;
    private TextView btnCreateGroup;

    // Ảnh vừa chọn từ thư viện (null nếu chưa chọn)
    private Uri coverUri = null;
    private Uri avatarUri = null;

    private boolean submitting = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT)
        );
        setContentView(R.layout.create_group_activity);

        setupInsets();
        bindViews();
        setupListeners();

        // Xoay màn hình thì giữ lại ảnh đã chọn (tên, mô tả thì EditText tự giữ)
        if (savedInstanceState != null) {
            String savedCover = savedInstanceState.getString(STATE_COVER_URI);
            if (savedCover != null) {
                coverUri = Uri.parse(savedCover);
            }
            String savedAvatar = savedInstanceState.getString(STATE_AVATAR_URI);
            if (savedAvatar != null) {
                avatarUri = Uri.parse(savedAvatar);
            }
            refreshCoverPreview();
            refreshAvatarPreview();
        }

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        observeData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
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
                if (!submitting) {
                    pickCoverLauncher.launch(imageOnlyRequest());
                }
            }
        });

        findViewById(R.id.layoutAvatarPick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!submitting) {
                    pickAvatarLauncher.launch(imageOnlyRequest());
                }
            }
        });

        btnRemoveCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!submitting) {
                    coverUri = null;
                    refreshCoverPreview();
                }
            }
        });

        btnRemoveAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!submitting) {
                    avatarUri = null;
                    refreshAvatarPreview();
                }
            }
        });

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    // Chỉ cho chọn ảnh (không chọn video)
    private PickVisualMediaRequest imageOnlyRequest() {
        return new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
    }

    // Hiện ảnh bìa vừa chọn, chưa chọn thì hiện nền tím mặc định kèm chữ hướng dẫn
    private void refreshCoverPreview() {
        if (coverUri != null) {
            Glide.with(this).load(coverUri).centerCrop().into(imgCoverPreview);
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
        } else {
            Glide.with(this).clear(imgAvatarPreview);
            imgAvatarPreview.setImageResource(R.drawable.group_cover_placeholder);
            txtAvatarHint.setVisibility(View.VISIBLE);
            btnRemoveAvatar.setVisibility(View.GONE);
        }
    }

    // Observer chỉ nhận dữ liệu sau khi createGroup chạy xong (LiveData chưa có giá trị lúc đầu)
    private void observeData() {
        groupViewModel.getGroups().observe(this, new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> groups) {
                if (!submitting) {
                    return;
                }
                Toast.makeText(CreateGroupActivity.this,
                        R.string.create_group_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void submit() {
        if (submitting) {
            return;
        }
        final String name = edtGroupName.getText().toString().trim();
        final String description = edtGroupDescription.getText().toString().trim();

        if (name.isEmpty()) {
            edtGroupName.setError(getString(R.string.create_group_error_name));
            edtGroupName.requestFocus();
            return;
        }

        submitting = true;
        btnCreateGroup.setEnabled(false);
        btnCreateGroup.setText(R.string.group_image_uploading);

        // Tải ảnh lên trước (nếu có chọn), có link rồi mới tạo nhóm
        GroupImageUploader.uploadImages(this, coverUri, avatarUri, "", "",
                new GroupImageUploader.ResultListener() {
                    @Override
                    public void onDone(String coverUrl, String avatarUrl) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        groupViewModel.createGroup(name, description, coverUrl, avatarUrl,
                                GroupActivity.currentUserId());
                    }

                    @Override
                    public void onError(int messageResId) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        submitting = false;
                        btnCreateGroup.setEnabled(true);
                        btnCreateGroup.setText(R.string.create_group_submit);
                        Toast.makeText(CreateGroupActivity.this, messageResId,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
