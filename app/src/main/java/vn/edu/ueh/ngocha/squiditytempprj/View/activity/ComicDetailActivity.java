package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaAttributes;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
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
    CardView cardRatings;

    ComicDetailViewModel viewModel;
    FlexboxLayout layoutComicGenres;

    String comicId;
    String comic_name;
    String comic_cover;

    View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    View.OnClickListener onReviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ComicDetailActivity.this, ComicReviewActivity.class);
            intent.putExtra("comic_id", comicId);
            intent.putExtra("comic_name", comic_name);
            intent.putExtra("comic_cover", comic_cover);
            startActivity(intent);
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

        comicId = getIntent().getStringExtra("comic_id");
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
        layoutComicGenres = findViewById(R.id.layoutComicGenres);
        cardRatings = findViewById(R.id.cardRatings);

        btnBack.setOnClickListener(onBackClickListener);
        cardRatings.setOnClickListener(onReviewClickListener);


        if ("MangaDex".equals(searchOption) && comicId != null) {
            viewModel.getMangaById(comicId).observe(this,
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


        List<String> genres = new ArrayList<>();

        if (attributes.getTags() != null) {
            for (Tag tag : attributes.getTags()) {
                if (tag.getAttributes() != null
                        && "genre".equals(tag.getAttributes().getGroup())) {

                    Map<String, String> names =
                            tag.getAttributes().getName();

                    if (names != null) {
                        String genreName = names.get("en");

                        if (genreName != null && !genreName.isEmpty()) {
                            genres.add(genreName);
                        }
                    }
                }
            }
        }

        displayGenres(genres);

        txtName.setText(getLocalizedText(attributes.getTitle()));

        comic_name = getLocalizedText(attributes.getTitle());

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

                    comic_cover = coverUrl;

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

    private void displayGenres(List<String> genres) {
        layoutComicGenres.removeAllViews();

        if (genres == null || genres.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String genre : genres) {
            TextView genreView = (TextView) inflater.inflate(
                    R.layout.comic_genre_item,
                    layoutComicGenres,
                    false
            );

            genreView.setText(genre);
            layoutComicGenres.addView(genreView);
        }
    }



}