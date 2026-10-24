package vn.edu.ueh.ngocha.squiditytempprj.View.menu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AppSettingActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AdminActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AuthActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ChatListActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ComicListActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.GroupActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.MainActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ProfileActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NotificationPopup;
import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NtfChannel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;
public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected TextView tvDrawerUsername;
    protected TextView tvDrawerEmail;
    protected TextView tvMainUsername;
    protected ShapeableImageView imgDrawerAvatar;
    protected ShapeableImageView imgMainAvatar;
    protected ImageView imgnavProfileCover;
    protected FrameLayout activityContent;
    protected FrameLayout baseOverlayContainer;
    protected int statusBarInset = 0;
    protected AuthViewModel authViewModel;
    protected UserProfileViewModel profileViewModel;
    private MaterialButton btnAuthAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_base_drawer);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        activityContent = findViewById(R.id.activityContent);
        baseOverlayContainer = findViewById(R.id.baseOverlayContainer);

        NtfChannel.setup(this);
        NotificationPopup.attach(this, findViewById(R.id.btnNotification));

        View mainContainer = findViewById(R.id.mainContainer);

        Log.d("BaseActivity", "drawerLayout = " + drawerLayout);
        Log.d("BaseActivity", "navigationView = " + navigationView);
        Log.d("BaseActivity", "mainContainer = " + mainContainer);

        if (drawerLayout == null || navigationView == null
                || mainContainer == null || activityContent == null || baseOverlayContainer == null) {
            throw new IllegalStateException(
                    "Không tìm thấy View trong activity_base_drawer.xml"
            );
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                drawerLayout,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    statusBarInset = systemBars.top;

                    mainContainer.setPadding(
                            mainContainer.getPaddingLeft(),
                            systemBars.top,
                            mainContainer.getPaddingRight(),
                            mainContainer.getPaddingBottom()
                    );

                    return insets;
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                navigationView,
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            v.getPaddingLeft(),
                            systemBars.top,
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(drawerLayout);

        activityContent = findViewById(R.id.activityContent);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        View header = navigationView.getHeaderView(0);
        tvDrawerUsername = header.findViewById(R.id.tvDrawerUsername);
        tvDrawerEmail = header.findViewById(R.id.tvDrawerEmail);
        imgDrawerAvatar = header.findViewById(R.id.imageView4);
        tvMainUsername = findViewById(R.id.tvMainUsername);
        imgnavProfileCover = header.findViewById(R.id.imgnavCover);
        imgMainAvatar = findViewById(R.id.imgMainAvatar);

        btnAuthAction.setOnClickListener(onAuthClickListener);
        if (imgMainAvatar != null) imgMainAvatar.setOnClickListener(onProfileClickListener);

        ImageButton btnBackHome =
                    findViewById(R.id.btnBackHome);

        if (btnBackHome != null) {
            if (shouldShowBackHomeButton()) {
                btnBackHome.setVisibility(View.VISIBLE);

                btnBackHome.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            BaseActivity.this,
                            MainActivity.class
                    );
                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );
                    startActivity(intent);
                    finish();
                });
            } else {
                btnBackHome.setVisibility(View.GONE);
            }
        }



        authViewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this)
                .get(UserProfileViewModel.class);

        profileViewModel.getProfile().observe(this, currentuserNav);
        profileViewModel.loadCurrentProfile();

        getLayoutInflater().inflate(
                getContentLayoutId(),
                activityContent,
                true
        );

        setupDrawer();

    }
    @Override
    protected void onResume() {
        super.onResume();

        if (profileViewModel != null) {
            profileViewModel.loadCurrentProfile();
        }
    }

    // Mỗi Activity con tự cung cấp layout nội dung
    protected abstract int getContentLayoutId();

    protected boolean shouldShowBackHomeButton() {
        return true;
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(
                            MenuItem item) {

                        int id = item.getItemId();

                        if (id == R.id.action_profile) {
                            if (authViewModel.getCurrentUser() == null) {

                                new AlertDialog.Builder(BaseActivity.this)
                                        .setTitle("Yêu cầu đăng nhập")
                                        .setMessage("Bạn cần đăng nhập để xem profile.")
                                        .setNegativeButton("Hủy", (dialog, which) -> {
                                            dialog.dismiss();
                                        })
                                        .setPositiveButton("Đăng nhập", (dialog, which) -> {
                                            Intent intent = new Intent(
                                                    BaseActivity.this,
                                                    AuthActivity.class
                                            );
                                            startActivity(intent);
                                        })
                                        .show();

                            } else {
                                Intent intent = new Intent(
                                        BaseActivity.this,
                                        ProfileActivity.class
                                );
                                startActivity(intent);
                            }

                        } else if (id == R.id.action_chat) {
                            startActivity(new Intent(BaseActivity.this, ChatListActivity.class));
                        } else if (id == R.id.action_group) {
                            // Mở màn hình Groups
                            Intent intent = new Intent(
                                    BaseActivity.this,
                                    GroupActivity.class
                            );
                            startActivity(intent);
                        }
                        else if (id == R.id.action_comic) {
                            Intent intent = new Intent(
                                    BaseActivity.this,
                                    ComicListActivity.class
                            );
                            startActivity(intent);
                        }
                        else if (id == R.id.action_admin) {
                            startActivity(new Intent(BaseActivity.this, AdminActivity.class));
                        }
                        else if (id == R.id.action_setting) {
                            startActivity(new Intent(BaseActivity.this, AppSettingActivity.class));
                        }

                        drawerLayout.closeDrawers();
                        return true;
                    }
                }
        );
    }

    private Observer<UserProfileViewModel.ProfileUiState> currentuserNav = new Observer<UserProfileViewModel.ProfileUiState>() {

        @Override
        public void onChanged(UserProfileViewModel.ProfileUiState state) {
            if (state == null) {
                return;
            }

            if (tvDrawerUsername != null) {
                tvDrawerUsername.setText(state.username);
            }

            if (tvDrawerEmail != null) {
                tvDrawerEmail.setText(state.email);
            }

            if (tvMainUsername != null) {
                tvMainUsername.setText(state.username);
            }

            if (btnAuthAction != null) {
                btnAuthAction.setText(
                        state.loggedIn ? "Đăng xuất" : "Đăng nhập / Đăng ký"
                );
            }

            int placeholder = R.drawable.ic_launcher_background;

            if (imgDrawerAvatar != null) {
                if (state.avatarUrl == null || state.avatarUrl.isEmpty()) {
                    imgDrawerAvatar.setImageResource(placeholder);
                } else {
                    Glide.with(BaseActivity.this)
                            .load(state.avatarUrl)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .circleCrop()
                            .into(imgDrawerAvatar);
                }
            }

            if (imgMainAvatar != null) {
                if (state.avatarUrl == null || state.avatarUrl.isEmpty()) {
                    imgMainAvatar.setImageResource(placeholder);
                } else {
                    Glide.with(BaseActivity.this)
                            .load(state.avatarUrl)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .circleCrop()
                            .into(imgMainAvatar);
                }
            }

            if (imgnavProfileCover != null) {
                if (state.coverUrl == null || state.coverUrl.isEmpty()) {
                    imgnavProfileCover.setImageResource(placeholder);
                } else {
                    Glide.with(BaseActivity.this)
                            .load(state.coverUrl)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .centerCrop()
                            .into(imgnavProfileCover);
                }
            }
        }
    };


    protected View.OnClickListener onProfileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (drawerLayout.getWidth() > 0
                    && drawerLayout.getHeight() > 0) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        }
    };

    private void openAuthActivity() {
        try {
            Intent intent = new Intent(BaseActivity.this, AuthActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("BaseActivity", "Không mở được AuthActivity", e);
            Toast.makeText(this, "Không mở được màn hình đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener onAuthClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user == null) {
                openAuthActivity();
            } else {
                new AlertDialog.Builder(BaseActivity.this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            authViewModel.logout();
                            profileViewModel.loadCurrentProfile();
                            Toast.makeText(
                                    BaseActivity.this,
                                    "Đã đăng xuất",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .show();
            }
        }
    };

}
