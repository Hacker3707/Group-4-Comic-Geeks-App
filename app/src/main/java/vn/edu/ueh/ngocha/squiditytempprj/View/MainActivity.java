package vn.edu.ueh.ngocha.squiditytempprj.View;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaAttributes;


public class MainActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);


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

                    Log.d("MANGADEX", "Total: "
                            + mangaResponse.getTotal());

                    for (MangaData manga : mangaResponse.getData()) {

                        String id = manga.getId();

                        MangaAttributes attributes =
                                manga.getAttributes();

                        String title = "";

                        if (attributes.getTitle() != null
                                && attributes.getTitle().get("en") != null) {

                            title = attributes.getTitle().get("en");

                        } else if (attributes.getTitle() != null
                                && attributes.getTitle().get("ja-ro") != null) {

                            title = attributes.getTitle().get("ja-ro");
                        }

                        String coverFileName = "";

                        if (manga.getRelationships() != null) {

                            for (Relationship relationship : manga.getRelationships()) {

                                Log.d(
                                        "MANGADEX",
                                        "Relationship type: " + relationship.getType()
                                                + " | ID: " + relationship.getId()
                                                + " | Attributes: " + relationship.getAttributes()

                                );

                                if ("cover_art".equals(relationship.getType())
                                        && relationship.getAttributes() != null) {

                                    coverFileName =
                                            relationship.getAttributes().getFileName();

                                    String coverUrl =
                                            "https://uploads.mangadex.org/covers/"
                                                    + manga.getId()
                                                    + "/"
                                                    + coverFileName;

                                    Glide.with(MainActivity.this).load(coverUrl).into(imageView);

                                    Log.d("MANGADEX", "Cover URL: " + coverUrl);

                                    break;
                                }


                            }
                        }

                        Log.d(
                                "MANGADEX",
                                "ID: " + id + "\n"
                                        + "Title: " + title + "\n"
                                        + "Status: " + attributes.getStatus()
                                        + "\nCover_art: " + coverFileName
                        );
                    }

                } else {

                    Log.e(
                            "MANGADEX",
                            "Request failed: " + response.code()
                    );
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