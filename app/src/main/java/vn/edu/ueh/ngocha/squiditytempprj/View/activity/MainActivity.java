package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
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
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.ueh.ngocha.squiditytempprj.View.activity.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
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
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter

import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NtfChannel;
import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NotificationPopup;
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

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;


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
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));

                    } else if (id == R.id.action_chat) {
                         startActivity(new Intent(MainActivity.this, ChatListActivity.class));
                    } else if (id == R.id.action_group) {

                    } else if (id == R.id.action_setting) {

                    } else if (id == R.id.action_comic) {
                        startActivity(new Intent(MainActivity.this, ComicListActivity.class));

                    } else if (item.getTitle().toString().toLowerCase().contains("đăng nhập")
                            || item.getTitle().toString().toLowerCase().contains("đăng ký")
                            || item.getTitle().toString().toLowerCase().contains("login")
                            || item.getTitle().toString().toLowerCase().contains("sign in")) {
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                    }

                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); NtfChannel.setup(this);
        NotificationPopup.attach(this, findViewById(R.id.btnNotification));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tvMainUsername = findViewById(R.id.tvMainUsername);
        updateMainUsername();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        View appBar = findViewById(R.id.appBar);
        tvMainUsername = appBar.findViewById(R.id.tvMainUsername);
        imgMainAvatar = appBar.findViewById(R.id.imgvAvatar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        rcvpostlist = findViewById(R.id.rcv_postlist);

        View header = navigationView.getHeaderView(0);
        tvDrawerUsername = header.findViewById(R.id.tvDrawerUsername);
        tvDrawerEmail = header.findViewById(R.id.tvDrawerEmail);
        imgDrawerAvatar = header.findViewById(R.id.imageView4);

        btnAuthAction = findViewById(R.id.btnAuthAction);

        btnAuthAction.setOnClickListener(v -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) {
                openAuthActivity();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            firebaseAuth.signOut();
                            updateUserInfo();
                            Toast.makeText(
                                    MainActivity.this,
                                    "Đã đăng xuất",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .show();
            }
        });

        updateUserInfo();

        drawerLayout = findViewById(R.id.drawerLayout);
        imgvAvatar = findViewById(R.id.imgvAvatar);
        navigationView = findViewById(R.id.navigationView);
        rcvpostlist = findViewById(R.id.rcv_postlist);

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
    private void updateMainUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            tvMainUsername.setText("Khách");
            return;
        }

        String username = user.getDisplayName();
        String email = user.getEmail();

        if (username == null || username.trim().isEmpty()) {
            if (email != null && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            } else {
                username = "Người dùng";
            }
        }

        tvMainUsername.setText(username);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseAuth != null
                && tvMainUsername != null
                && tvDrawerUsername != null) {
            updateUserInfo();
        }
    }
    private void updateUserInfo() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            tvMainUsername.setText("Khách");
            tvDrawerUsername.setText("Khách");
            tvDrawerEmail.setText("");

            btnAuthAction.setText("Đăng nhập / Đăng ký");

            imgMainAvatar.setImageResource(R.drawable.ic_launcher_background);
            imgDrawerAvatar.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        String email = user.getEmail() == null ? "" : user.getEmail();
        String username = user.getDisplayName();

        if (username == null || username.trim().isEmpty()) {
            username = email.contains("@")
                    ? email.substring(0, email.indexOf("@"))
                    : "Người dùng";
        }

        tvMainUsername.setText(username);
        tvDrawerUsername.setText(username);
        tvDrawerEmail.setText(email);
        btnAuthAction.setText("Đăng xuất");

        // Dùng ảnh hồ sơ Google/Firebase nếu có.
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(imgMainAvatar);

            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(imgDrawerAvatar);
        } else {
            imgMainAvatar.setImageResource(R.drawable.ic_launcher_background);
            imgDrawerAvatar.setImageResource(R.drawable.ic_launcher_background);
        }

        // Cập nhật tên/avatar tùy chỉnh đã lưu trong Firestore nếu có.
        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    // Không để kết quả cũ ghi đè giao diện sau khi đổi tài khoản.
                    if (currentUser == null
                            || !currentUser.getUid().equals(user.getUid())) {
                        return;
                    }

                    if (document.exists()) {
                        String savedName = document.getString("username");
                        String savedAvatar = document.getString("avatarUrl");

                        if (savedName != null && !savedName.trim().isEmpty()) {
                            tvMainUsername.setText(savedName);
                            tvDrawerUsername.setText(savedName);
                        }

                        if (savedAvatar != null && !savedAvatar.trim().isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(savedAvatar)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .circleCrop()
                                    .into(imgMainAvatar);

                            Glide.with(MainActivity.this)
                                    .load(savedAvatar)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .circleCrop()
                                    .into(imgDrawerAvatar);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("MainActivity", "Không tải được hồ sơ người dùng", e));
    }
}