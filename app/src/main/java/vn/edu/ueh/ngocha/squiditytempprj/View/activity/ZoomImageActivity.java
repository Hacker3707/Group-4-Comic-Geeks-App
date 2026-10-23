package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class ZoomImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        ImageView imgZoomed = findViewById(R.id.imgZoomed);
        ImageButton btnCloseZoom = findViewById(R.id.btnCloseZoom);

        String imageUrl = getIntent().getStringExtra("IMAGE_URL");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imgZoomed);
        }

        btnCloseZoom.setOnClickListener(v -> finish());
    }
}