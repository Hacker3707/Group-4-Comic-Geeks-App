package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaAttributes;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MangaDexRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicDetailViewModel;

public class ComicDetailActivity extends AppCompatActivity {

    TextView txtName;
    TextView txtAuthor;
    TextView txtDescription;
    TextView txtStatus;
    TextView txtContentRating;
    TextView txtLanguage;
    TextView txtYear;
    TextView txtLastVolume;
    TextView txtLastChapter;
    ImageView imgCover;
    ImageButton btnBack;

    ComicDetailViewModel viewModel;

    View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comic_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String comicid = getIntent().getStringExtra("comic_id");
        String searchOption = getIntent().getStringExtra("search_option");
        viewModel = new ViewModelProvider(this).get(ComicDetailViewModel.class);

        txtName = findViewById(R.id.textView2);
        txtAuthor = findViewById(R.id.textView6);
        txtDescription = findViewById(R.id.textView8);
        imgCover = findViewById(R.id.imgcomic_cover);
        txtStatus = findViewById(R.id.txtComicStatus);
        txtContentRating = findViewById(R.id.txtComicContentRating);
        txtLanguage = findViewById(R.id.txtComicLanguage);
        txtYear = findViewById(R.id.txtComicYear);
        txtLastVolume = findViewById(R.id.txtComicLastVolume);
        txtLastChapter = findViewById(R.id.txtComicLastChapter);
        btnBack = findViewById(R.id.imgbtnBack);

        btnBack.setOnClickListener(onBackClickListener);

        if ("MangaDex".equals(searchOption) && comicid != null) {
            viewModel.getMangaById(comicid).observe(this,
                    new Observer<MangaData>() {
                        @Override
                        public void onChanged(MangaData mangaData) {
                            if (mangaData != null) {
                                displayMangaData(mangaData);
                            }
                        }
                    });
        }

    }

    private String getLocalizedText(Map<String, String> texts) {
        if (texts == null || texts.isEmpty()) {
            return "Unknown";
        }

        String englishText = texts.get("en");

        if (englishText != null && !englishText.trim().isEmpty()) {
            return englishText;
        }

        for (String text : texts.values()) {
            if (text != null && !text.trim().isEmpty()) {
                return text;
            }
        }

        return "Unknown";
    }

    private String valueOrDefault(String value) {
        return value != null && !value.trim().isEmpty()
                ? value
                : "Unknown";
    }

    private void displayMangaData(MangaData mangaData) {
        if (mangaData == null || mangaData.getAttributes() == null) {
            return;
        }

        MangaAttributes attributes = mangaData.getAttributes();

        txtName.setText(getLocalizedText(attributes.getTitle()));
        txtDescription.setText(
                getLocalizedText(attributes.getDescription())
        );


        txtStatus.setText(
                valueOrDefault(attributes.getStatus())
        );

        txtContentRating.setText(
                "Content rating: "
                        + valueOrDefault(attributes.getContentRating())
        );

        txtLanguage.setText(
                "Original language: "
                        + valueOrDefault(attributes.getOriginalLanguage())
        );

        txtYear.setText(
                "Release year: "
                        + (attributes.getYear() != null
                        ? attributes.getYear().toString()
                        : "Unknown")
        );

        txtLastVolume.setText(
                "Last volume: "
                        + valueOrDefault(attributes.getLastVolume())
        );

        txtLastChapter.setText(
                "Last chapter: "
                        + valueOrDefault(attributes.getLastChapter())
        );

        String coverUrl = null;

        if (mangaData.getRelationships() != null) {

            Boolean hasCover = false;
            Boolean hasAuthor = false;

            for (Relationship relationship : mangaData.getRelationships()) {

                if ("cover_art".equals(relationship.getType())
                        && relationship.getAttributes() != null
                        && relationship.getAttributes().getFileName() != null
                        && !hasCover) {

                    coverUrl = "https://uploads.mangadex.org/covers/"
                            + mangaData.getId()
                            + "/"
                            + relationship.getAttributes().getFileName();

                    hasCover = true;
                }

                if ("author".equals(relationship.getType())
                        && relationship.getAttributes() != null
                        && relationship.getAttributes().getName() != null
                        && !hasAuthor) {

                    txtAuthor.setText(
                            valueOrDefault(relationship.getAttributes().getName())
                    );

                    hasAuthor = true;
                }

            }
        }

        // Load ảnh vào ImageView của từng item
        Glide.with(imgCover.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.comic_background)
                .error(R.drawable.comic_background)
                .centerCrop()
                .into(imgCover);

    }

    public void displayMangaData(Comic comic)
    {
        // Giả sử comic là object Comic đã lấy được
        txtName.setText(comic.getComicName());
        txtAuthor.setText(comic.getComicAuthor());
        txtDescription.setText(comic.getComicDescription());

        txtStatus.setText(
                comic.getComicStatus() != null
                        ? comic.getComicStatus()
                        : "Unknown"
        );

        txtContentRating.setText(
                "Content rating: " + valueOrDefault(comic.getContentRating())
        );

        txtLanguage.setText(
                "Original language: " + valueOrDefault(comic.getOriginalLanguage())
        );

        txtYear.setText(
                "Release year: " +
                        (comic.getReleaseYear() != null
                                ? comic.getReleaseYear().toString()
                                : "Unknown")
        );

        txtLastVolume.setText(
                "Last volume: " + valueOrDefault(comic.getLastVolume())
        );

        txtLastChapter.setText(
                "Last chapter: " + valueOrDefault(comic.getLastChapter())
        );
    }
}