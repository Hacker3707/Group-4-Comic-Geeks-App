package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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


public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView imgvAvatar;
    RecyclerView rcvpostlist;
    PostAdapter postAdapter;


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

    private NavigationView.OnNavigationItemSelectedListener onAvatarsClickListener = new NavigationView.OnNavigationItemSelectedListener() {
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
                startActivity(new Intent(MainActivity.this, ChatListActivity.class));

                // Xử lý mở Chat

            } else if (id == R.id.action_group) {
                // Xử lý mở Groups

            } else if (id == R.id.action_setting) {
                // Xử lý mở Settings
            } else if (id == R.id.action_comic) {
                // Mở màn hình Comic
                Intent intent = new Intent(
                        MainActivity.this,
                        ComicListActivity.class
                );
                startActivity(intent);
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
}