package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
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
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicDetailViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ReviewViewModel;

public class ComicDetailActivity extends BaseActivity {

    TextView txtName;
    TextView txtAuthor;
    TextView txtDescription;
    TextView txtStatus;
    TextView txtContentRating;
    TextView txtLanguage;
    TextView txtYear;
    TextView txtLastVolume;
    TextView txtLastChapter;
    TextView txticRatings;
    TextView txtplacehdRatings;
    TextView txtRatings;
    ImageView imgCover;
    ImageButton btnBack;
    CardView cardRatings;
    RatingBar ratingBar;

    ComicDetailViewModel viewModel;
    ComicViewModel comicViewModel;

    FlexboxLayout layoutComicGenres;

    String comicId;
    String comic_name;
    String comic_cover;

    ReviewViewModel reviewViewModel;



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

    View.OnClickListener onGenreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tagId = (String) view.getTag();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("genre_id", tagId);

            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        comicId = getIntent().getStringExtra("comic_id");
        String searchOption = getIntent().getStringExtra("search_option");
        viewModel = new ViewModelProvider(this).get(ComicDetailViewModel.class);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);


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
        ratingBar = findViewById(R.id.ratingBar);
        txticRatings = findViewById(R.id.txticRatings);
        txtplacehdRatings = findViewById(R.id.txtComicRating);
        txtRatings = findViewById(R.id.txtRatings);

        btnBack.setOnClickListener(onBackClickListener);
        cardRatings.setOnClickListener(onReviewClickListener);

        final Observer<Float> reviewsratingObserver = new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating(aFloat);
                txtRatings.setVisibility(View.VISIBLE);
                txtRatings.setText(String.valueOf(aFloat) + " / 5");
                txticRatings.setVisibility(View.GONE);
                txtplacehdRatings.setVisibility(View.GONE);
            }
        };
        reviewViewModel.getAverageRating(comicId).observe(ComicDetailActivity.this, reviewsratingObserver);


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

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_comic_detail;
    }

    @Override
    protected boolean shouldShowBackHomeButton() {
        return false;
    }

    private String getLocalizedText(Map<String, String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Str.t(R.string.common_unknown);
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

        return Str.t(R.string.common_unknown);
    }

    private String valueOrDefault(String value) {
        return value != null && !value.trim().isEmpty()
                ? value
                : Str.t(R.string.common_unknown);
    }

    private void displayMangaData(MangaData mangaData) {
        if (mangaData == null || mangaData.getAttributes() == null) {
            return;
        }

        MangaAttributes attributes = mangaData.getAttributes();


        List<Tag> genres = new ArrayList<>();

        if (attributes.getTags() != null) {
            for (Tag tag : attributes.getTags()) {
                if (tag != null
                        && tag.getId() != null
                        && tag.getAttributes() != null
                        && "genre".equals(tag.getAttributes().getGroup())) {

                    Map<String, String> names =
                            tag.getAttributes().getName();

                    if (names != null) {
                        String genreName = names.get("en");

                        if (genreName != null && !genreName.isEmpty()) {
                            genres.add(tag);
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
                Str.t(R.string.comic_content_rating, valueOrDefault(attributes.getContentRating()))
        );

        txtLanguage.setText(
                Str.t(R.string.comic_original_language, valueOrDefault(attributes.getOriginalLanguage()))
        );

        txtYear.setText(
                Str.t(R.string.comic_release_year, (attributes.getYear() != null
                        ? attributes.getYear().toString()
                        : Str.t(R.string.common_unknown)))
        );

        txtLastVolume.setText(
                Str.t(R.string.comic_last_volume, valueOrDefault(attributes.getLastVolume()))
        );

        txtLastChapter.setText(
                Str.t(R.string.comic_last_chapter, valueOrDefault(attributes.getLastChapter()))
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
                        : Str.t(R.string.common_unknown)
        );

        txtContentRating.setText(
                Str.t(R.string.comic_content_rating, valueOrDefault(comic.getContentRating()))
        );

        txtLanguage.setText(
                Str.t(R.string.comic_original_language, valueOrDefault(comic.getOriginalLanguage()))
        );

        txtYear.setText(
                Str.t(R.string.comic_release_year, (comic.getReleaseYear() != null
                                ? comic.getReleaseYear().toString()
                                : Str.t(R.string.common_unknown)))
        );

        txtLastVolume.setText(
                Str.t(R.string.comic_last_volume, valueOrDefault(comic.getLastVolume()))
        );

        txtLastChapter.setText(
                Str.t(R.string.comic_last_chapter, valueOrDefault(comic.getLastChapter()))
        );
    }

    private void displayGenres(List<Tag> genres) {
        layoutComicGenres.removeAllViews();

        if (genres == null || genres.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Tag tag : genres) {
            TextView genreView = (TextView) inflater.inflate(
                    R.layout.comic_genre_item,
                    layoutComicGenres,
                    false
            );

            String genreName = getLocalizedText(
                    tag.getAttributes().getName()
            );

            genreView.setText(genreName);

            // Lưu ID thật của genre vào View
            genreView.setTag(tag.getId());

            // Khi bấm genre, gửi ID về ComicListActivity
            genreView.setOnClickListener(onGenreClickListener);

            layoutComicGenres.addView(genreView);
        }
    }

}