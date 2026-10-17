package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicViewModel;

public class ComicListActivity extends AppCompatActivity {

    private ComicAdapter comicAdapter;
    private RecyclerView rcvcomic;
    private DrawerLayout drawerLayout;
    private EditText etSearch;
    private ImageView btnSearch;
    private TextView tvSearchResult;
    private Spinner spnSearchOpt;

    NavigationView navigationView;
    ImageView imgvAvatar;

    private ComicViewModel comicViewModel;

    private int currentOffset = 0;
    private final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private View.OnClickListener onProfileClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    };

    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String searchQuery = etSearch.getText().toString();
            tvSearchResult.setText("Searched for: " + searchQuery);
            loadComicwithMangaDex(searchQuery);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.comic_list_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rcvcomic = findViewById(R.id.rcvcomic);
        drawerLayout = findViewById(R.id.drawerLayout);
        etSearch = findViewById(R.id.etSearch);
        navigationView = findViewById(R.id.navigationView);
        imgvAvatar = findViewById(R.id.imgvAvatar);
        btnSearch = findViewById(R.id.btnSearch);
        tvSearchResult = findViewById(R.id.tvSearchResult);
        spnSearchOpt = findViewById(R.id.spn_searchopt);

        btnSearch.setOnClickListener(onSearchClickListener);
        imgvAvatar.setOnClickListener(onProfileClickListener);

        // 1. KHỞI TẠO ADAPTER VÀ SETUP CHO RECYCLERVIEW TRƯỚC TIÊN
        comicAdapter = new ComicAdapter(new ComicAdapter.OnComicClickListener() {
            @Override
            public void onComicClick(String comicId) {
                Log.d("DEBUG_CLICK", "Đã bấm vào truyện có ID: " + comicId);

                Intent intent = new Intent(ComicListActivity.this, ComicDetailActivity.class);
                intent.putExtra("comic_id", comicId);
                intent.putExtra("search_option", "MangaDex");
                startActivity(intent);
            }
        });

        rcvcomic.setAdapter(comicAdapter);
        rcvcomic.setLayoutManager(new GridLayoutManager(this, 2));

        // 2. KHỞI TẠO VIEWMODEL VÀ LẮNG NGHE LIVEDATA SAU CÙNG
        comicViewModel = new ViewModelProvider(this).get(ComicViewModel.class);

        comicViewModel.getMangaList().observe(this, new Observer<List<MangaData>>() {
            @Override
            public void onChanged(List<MangaData> mangaDataList) {
                if (mangaDataList != null) {
                    comicAdapter.setMangaList(mangaDataList);
                }
            }
        });

        // 3. Kiểm tra nếu có truyền từ khóa từ màn hình khác sang (ví dụ từ bài post)
        String searchQuery = getIntent().getStringExtra("SEARCH_QUERY");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            etSearch.setText(searchQuery);
            tvSearchResult.setText("Searched for: " + searchQuery);
            loadComicwithMangaDex(searchQuery);
        }
    }

    public void loadComicwithMangaDex(String searchQuery) {
        currentOffset = 0;
        isLoading = false;
        isLastPage = false;
        comicViewModel.searchComicswithMangaDex(searchQuery);
    }
}