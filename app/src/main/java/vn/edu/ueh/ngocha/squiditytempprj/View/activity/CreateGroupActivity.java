package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
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
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GroupViewModel;

/**
 * Màn tạo group mới: nhập tên, mô tả, link ảnh bìa rồi bấm "Tạo nhóm".
 * Người tạo tự động là ADMIN của group (xử lý trong GroupViewModel.createGroup).
 * Khi ViewModel tạo xong sẽ cập nhật LiveData, Observer ở đây nhận được thì đóng màn hình.
 */
public class CreateGroupActivity extends AppCompatActivity {

    private GroupViewModel groupViewModel;

    private EditText edtGroupName;
    private EditText edtGroupDescription;
    private EditText edtCoverUrl;
    private EditText edtAvatarUrl;
    private TextView btnCreateGroup;

    private boolean submitting = false;

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

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        observeData();
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
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
    }

    private void setupListeners() {
        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
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
        String name = edtGroupName.getText().toString().trim();
        String description = edtGroupDescription.getText().toString().trim();
        String coverUrl = edtCoverUrl.getText().toString().trim();
        String avatarUrl = edtAvatarUrl.getText().toString().trim();

        if (name.isEmpty()) {
            edtGroupName.setError(getString(R.string.create_group_error_name));
            edtGroupName.requestFocus();
            return;
        }

        submitting = true;
        btnCreateGroup.setEnabled(false);
        groupViewModel.createGroup(name, description, coverUrl, avatarUrl, GroupActivity.CURRENT_USER_ID);
    }
}
