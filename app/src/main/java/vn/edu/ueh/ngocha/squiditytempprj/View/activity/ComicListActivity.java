package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        comicAdapter = new ComicAdapter();
        rcvcomic.setAdapter(comicAdapter);
        rcvcomic.setLayoutManager(
                new GridLayoutManager(this, 2)
        );


        MangaDexApi api = RetrofitClient.getMangaDexApi();

        Call<MangaResponse> call =
                api.searchManga("Blue Exorcist", 5, 0, "cover_art");
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