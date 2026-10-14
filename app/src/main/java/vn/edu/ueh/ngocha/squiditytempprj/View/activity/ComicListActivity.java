package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicAdapter;

public class ComicListActivity extends AppCompatActivity {

    private ComicAdapter comicAdapter;
    private RecyclerView rcvcomic;
    private DrawerLayout drawerLayout;
    private EditText etSearch;
    private ImageView btnSearch;
    private TextView tvSearchResult;

    NavigationView navigationView;
    ImageView imgvAvatar;

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

        btnSearch.setOnClickListener(onSearchClickListener);

        comicAdapter = new ComicAdapter(
                new ComicAdapter.OnComicClickListener() {
                    @Override
                    public void onComicClick(String comicId) {
                        Intent intent = new Intent(
                                ComicListActivity.this,
                                ComicDetailActivity.class
                        );

                        intent.putExtra("comic_id", comicId);

                        startActivity(intent);
                    }
                }
        );

        rcvcomic.setAdapter(comicAdapter);
        rcvcomic.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

    }

    public void loadComicwithMangaDex(String searchQuery) {
        MangaDexApi api = RetrofitClient.getMangaDexApi();

        Call<MangaResponse> call =
                api.searchManga(searchQuery, 5, 0, "cover_art");
        call.enqueue(new Callback<MangaResponse>() {

            @Override
            public void onResponse(
                    Call<MangaResponse> call,
                    Response<MangaResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    MangaResponse mangaResponse = response.body();

                    if (mangaResponse != null && mangaResponse.getData() != null) {
                        comicAdapter.setMangaList(mangaResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(
                    Call<MangaResponse> call,
                    Throwable t) {

                Log.e(
                        "MANGADEX",
                        "API ERROR",
                        t
                );
            }
        });
    }

}