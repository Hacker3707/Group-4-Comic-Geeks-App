package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProfilePosts;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tabLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Ánh xạ RecyclerView và cài đặt LayoutManager
        recyclerViewProfilePosts = findViewById(R.id.recyclerViewProfilePosts);
        recyclerViewProfilePosts.setLayoutManager(new LinearLayoutManager(this));

        // 2. Tạo một danh sách rỗng để chứa dữ liệu giả
        List<Post> dummyPosts = new ArrayList<>();


        Post baiSo1 = new Post("P01", "Meilin Zhang", "C01", "Trời ơi truyện này hay xỉu luôn! Nam chính ngầu bá cháy.");
        Post baiSo2 = new Post("P02", "Meilin Zhang", "C02", "Hóng chap mới quá đi mất 😭");


        dummyPosts.add(baiSo1);
        dummyPosts.add(baiSo2);

        postAdapter = new PostAdapter(this, dummyPosts);
        recyclerViewProfilePosts.setAdapter(postAdapter);
    }
}