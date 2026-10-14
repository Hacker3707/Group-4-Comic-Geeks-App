package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.R;

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

    Comic comic;

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

        txtName = findViewById(R.id.textView2);
        txtAuthor = findViewById(R.id.textView6);
        txtDescription = findViewById(R.id.textView8);

        txtStatus = findViewById(R.id.txtComicStatus);
        txtContentRating = findViewById(R.id.txtComicContentRating);
        txtLanguage = findViewById(R.id.txtComicLanguage);
        txtYear = findViewById(R.id.txtComicYear);
        txtLastVolume = findViewById(R.id.txtComicLastVolume);
        txtLastChapter = findViewById(R.id.txtComicLastChapter);

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

    private String valueOrDefault(String value) {
        return value != null && !value.trim().isEmpty()
                ? value
                : "Unknown";
    }
}