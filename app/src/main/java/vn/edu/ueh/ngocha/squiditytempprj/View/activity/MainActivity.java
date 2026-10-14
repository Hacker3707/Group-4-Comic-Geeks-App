package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaAttributes;

import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AuthActivity;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView imgvAvatar;
    Button btnOpenAuth;
    private Button btnAuthAction;
    private SharedPreferences authPreferences;
    private TextView tvHeaderUsername;
    private TextView tvDrawerUsername;
    private TextView tvDrawerEmail;
    private ImageView drawerAvatar;

    private UserDao userDao;

    private final ExecutorService accountExecutor =
            Executors.newSingleThreadExecutor();
    private View.OnClickListener onComicClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ComicListActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener onProfileClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        imgvAvatar = findViewById(R.id.imgvAvatar);
        navigationView = findViewById(R.id.navigationView);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        tvHeaderUsername = findViewById(R.id.tvHeaderUsername);

        View drawerHeader = navigationView.getHeaderView(0);

        tvDrawerUsername =
                drawerHeader.findViewById(R.id.tvDrawerUsername);

        tvDrawerEmail =
                drawerHeader.findViewById(R.id.tvDrawerEmail);

        drawerAvatar =
                drawerHeader.findViewById(R.id.imageView4);

        userDao = AppDatabase.getDatabase(
                getApplicationContext()
        ).userDao();

        authPreferences = getSharedPreferences(
                "ComicGeeksAuth",
                MODE_PRIVATE
        );

        updateAuthButton();
        refreshAccountHeader();


        btnAuthAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.START);

                if (isLoggedIn()) {
                    logout();
                } else {
                    Intent intent = new Intent(
                            MainActivity.this,
                            AuthActivity.class
                    );
                    startActivity(intent);
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        int id = item.getItemId();

                        if (id == R.id.action_profile) {
                            // Mở màn hình Profile
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    ProfileActivity.class
                            );
                            startActivity(intent);

                        } else if (id == R.id.action_chat) {
                            // Xử lý mở Chat

                        } else if (id == R.id.action_group) {
                            // Xử lý mở Groups

                        } else if (id == R.id.action_setting) {
                            // Xử lý mở Settings
                        }

                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                }
        );

        imgvAvatar.setOnClickListener(onProfileClickListener);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (authPreferences != null) {
            updateAuthButton();
            refreshAccountHeader();
        }
    }


    private boolean isLoggedIn() {
        return authPreferences.getBoolean("isLoggedIn", false);
    }

    private void updateAuthButton() {
        if (btnAuthAction == null || authPreferences == null) {
            return;
        }

        if (isLoggedIn()) {
            btnAuthAction.setText("Đăng xuất");
        } else {
            btnAuthAction.setText("Đăng nhập / Đăng ký");
        }
    }

    private void logout() {
        authPreferences.edit()
                .putBoolean("isLoggedIn", false)
                .remove("email")
                .apply();

        updateAuthButton();
        refreshAccountHeader();

        Toast.makeText(
                MainActivity.this,
                "Bạn đã đăng xuất",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void refreshAccountHeader() {
        boolean loggedIn = isLoggedIn();
        String email = authPreferences.getString("email", "");

        if (!loggedIn || email == null || email.trim().isEmpty()) {
            tvHeaderUsername.setText("");
            tvHeaderUsername.setVisibility(View.GONE);

            tvDrawerUsername.setText("Khách");
            tvDrawerEmail.setText("");

            imgvAvatar.setImageResource(
                    android.R.drawable.sym_def_app_icon
            );
            drawerAvatar.setImageResource(
                    android.R.drawable.sym_def_app_icon
            );
            return;
        }

        final String accountEmail = email.trim();

        accountExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final User user = userDao.getByEmail(accountEmail);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        String displayName = accountEmail;

                        if (user != null
                                && user.getUsername() != null
                                && !user.getUsername().trim().isEmpty()) {
                            displayName = user.getUsername().trim();
                        } else {
                            int atIndex = accountEmail.indexOf("@");

                            if (atIndex > 0) {
                                displayName =
                                        accountEmail.substring(0, atIndex);
                            }
                        }

                        tvHeaderUsername.setText(displayName);
                        tvHeaderUsername.setVisibility(View.VISIBLE);

                        tvDrawerUsername.setText(displayName);
                        tvDrawerEmail.setText(accountEmail);

                        String avatarUrl = "";

                        if (user != null && user.getAvatarUrl() != null) {
                            avatarUrl = user.getAvatarUrl().trim();
                        }

                        if (!avatarUrl.isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(
                                            android.R.drawable.sym_def_app_icon
                                    )
                                    .error(
                                            android.R.drawable.sym_def_app_icon
                                    )
                                    .into(imgvAvatar);

                            Glide.with(MainActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(
                                            android.R.drawable.sym_def_app_icon
                                    )
                                    .error(
                                            android.R.drawable.sym_def_app_icon
                                    )
                                    .into(drawerAvatar);
                        } else {
                            imgvAvatar.setImageResource(
                                    android.R.drawable.sym_def_app_icon
                            );
                            drawerAvatar.setImageResource(
                                    android.R.drawable.sym_def_app_icon
                            );
                        }
                    }
                });
            }
        });
    }
}