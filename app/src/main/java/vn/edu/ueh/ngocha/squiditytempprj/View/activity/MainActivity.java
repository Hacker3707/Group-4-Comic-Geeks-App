package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AuthActivity;
import android.widget.Toast;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaAttributes;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;

import androidx.lifecycle.ViewModelProvider;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;


public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView imgvAvatar;
    RecyclerView rcvpostlist;
    PostAdapter postAdapter;
    private TextView tvMainUsername;
    private TextView tvDrawerUsername;
    private TextView tvDrawerEmail;

    private ShapeableImageView imgMainAvatar;
    private ShapeableImageView imgDrawerAvatar;

    private MaterialButton btnAuthAction;

    private AuthViewModel authViewModel;
    private UserProfileViewModel profileViewModel;


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

    private NavigationView.OnNavigationItemSelectedListener onAvatarsClickListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_profile) {
                        if (authViewModel.getCurrentUser() == null) {

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Yêu cầu đăng nhập")
                                    .setMessage("Bạn cần đăng nhập để xem profile.")
                                    .setNegativeButton("Hủy", (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .setPositiveButton("Đăng nhập", (dialog, which) -> {
                                        Intent intent = new Intent(
                                                MainActivity.this,
                                                AuthActivity.class
                                        );
                                        startActivity(intent);
                                    })
                                    .show();

                        } else {
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    ProfileActivity.class
                            );
                            startActivity(intent);
                        }
                    }

                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);

        profileViewModel = new ViewModelProvider(this)
                .get(UserProfileViewModel.class);

        tvMainUsername = findViewById(R.id.tvMainUsername);

        drawerLayout = findViewById(R.id.drawerLayout);
        imgvAvatar = findViewById(R.id.imgvAvatar);
        navigationView = findViewById(R.id.navigationView);
        rcvpostlist = findViewById(R.id.rcv_postlist);

        View appBar = findViewById(R.id.appBar);
        tvMainUsername = appBar.findViewById(R.id.tvMainUsername);
        imgMainAvatar = appBar.findViewById(R.id.imgvAvatar);

        if (navigationView == null || navigationView.getHeaderCount() == 0) {
            Toast.makeText(
                    this,
                    "Không tìm thấy phần header của menu",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        View header = navigationView.getHeaderView(0);

        tvDrawerUsername = header.findViewById(R.id.tvDrawerUsername);
        tvDrawerEmail = header.findViewById(R.id.tvDrawerEmail);
        imgDrawerAvatar = header.findViewById(R.id.imageView4);

        btnAuthAction = findViewById(R.id.btnAuthAction);

        btnAuthAction.setOnClickListener(v -> {
            FirebaseUser user = authViewModel.getCurrentUser();

            if (user == null) {
                openAuthActivity();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            authViewModel.logout();
                            profileViewModel.loadCurrentProfile();
                            Toast.makeText(
                                    MainActivity.this,
                                    "Đã đăng xuất",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .show();
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


        imgvAvatar.setOnClickListener(onProfileClickListener);
        navigationView.setNavigationItemSelectedListener(onAvatarsClickListener);

        List<Post> dummyPosts = new ArrayList<>();


        Post baiSo1 = new Post("P01", "Meilin Zhang", "C01", "Trời ơi truyện này hay xỉu luôn! Nam chính ngầu bá cháy.");
        Post baiSo2 = new Post("P02", "Meilin Zhang", "C02", "Hóng chap mới quá đi mất 😭");


        dummyPosts.add(baiSo1);
        dummyPosts.add(baiSo2);

        postAdapter = new PostAdapter(this, dummyPosts);
        rcvpostlist.setAdapter(postAdapter);
        rcvpostlist.setLayoutManager(new LinearLayoutManager(this));


        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) {
                return;
            }

            tvMainUsername.setText(state.username);
            tvDrawerUsername.setText(state.username);
            tvDrawerEmail.setText(state.email);

            btnAuthAction.setText(
                    state.loggedIn ? "Đăng xuất" : "Đăng nhập / Đăng ký"
            );

            if (state.avatarUrl == null || state.avatarUrl.isEmpty()) {
                imgMainAvatar.setImageResource(R.drawable.ic_launcher_background);
                imgDrawerAvatar.setImageResource(R.drawable.ic_launcher_background);
            } else {
                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgMainAvatar);

                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgDrawerAvatar);
            }
        });

        profileViewModel.loadCurrentProfile();


    }
    private void openAuthActivity() {
        try {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Không mở được AuthActivity", e);
            Toast.makeText(this, "Không mở được màn hình đăng nhập",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (profileViewModel != null) {
            profileViewModel.loadCurrentProfile();
        }
    }
}