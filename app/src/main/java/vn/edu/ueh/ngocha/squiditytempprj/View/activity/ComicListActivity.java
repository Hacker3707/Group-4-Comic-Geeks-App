package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicViewModel;

public class ComicListActivity extends AppCompatActivity {

    private ComicAdapter comicAdapter;
    private ActivityResultLauncher<Intent> detailLauncher;
    private RecyclerView rcvcomic;
    private DrawerLayout drawerLayout;
    private EditText etSearch;
    private ImageView btnSearch;
    private TextView tvSearchResult;
    private Spinner spnSearchOpt;
    private ChipGroup chipGroupGenres;

    NavigationView navigationView;
    HorizontalScrollView genreScrollView;
    ImageView imgvAvatar;
    View searchbar;

    private ComicViewModel comicViewModel;
    private boolean isGenreMode = false;



    // ------------------- Event Listener -------------------------
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(
                RecyclerView recyclerView,
                int dx, int dy) {

            super.onScrolled(recyclerView, dx, dy);

            // Chỉ phân trang khi đang tìm theo tên manga
            if (dy <= 0 || isGenreMode) {
                return;
            }

            RecyclerView.LayoutManager manager =
                    recyclerView.getLayoutManager();

            if (!(manager instanceof GridLayoutManager)) {
                return;
            }

            GridLayoutManager layoutManager =
                    (GridLayoutManager) manager;

            int totalItems = layoutManager.getItemCount();

            if (totalItems == 0) {
                return;
            }

            int lastVisible =
                    layoutManager.findLastVisibleItemPosition();

            if (lastVisible >= totalItems - 5) {
                comicViewModel.loadNextPage();
            }
        }
    };

    private View.OnClickListener onProfileClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    };

    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String searchQuery = etSearch.getText().toString();
            rcvcomic.scrollToPosition(0);
            loadComicwithMangaDex(searchQuery);
        }
    };

    private AdapterView.OnItemSelectedListener onSearchOptionSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(
                AdapterView<?> parent,
                View view,
                int position,
                long id) {

            Log.d("ComicListActivity", "Selected position: " + position);

            if (position == 0) {
                // Comic name
                tvSearchResult.setVisibility(View.VISIBLE);
                genreScrollView.setVisibility(View.GONE);
                searchbar.setVisibility(View.VISIBLE);
                comicAdapter.setMangaList(null);
                isGenreMode = false;

            } else {
                // Genre
                tvSearchResult.setVisibility(View.GONE);
                genreScrollView.setVisibility(View.VISIBLE);
                searchbar.setVisibility(View.GONE);
                comicAdapter.setMangaList(null);
                isGenreMode = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    // --------------------- Observer --------------------------

    Observer<List<Tag>> genreObserver = new Observer<List<Tag>>() {
        @Override
        public void onChanged(List<Tag> genres) {

            Log.d("GenreUI", "Observer received: "
                    + (genres == null ? "null" : genres.size()));

            chipGroupGenres.removeAllViews();


            for (Tag tag : genres) {
                String genreName =
                        tag.getAttributes()
                                .getName()
                                .get("en");

                Chip chip = new Chip(ComicListActivity.this);
                chip.setText(genreName);
                chip.setCheckable(true);
                chip.setTag(tag.getId());

                chip.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String tagId = (String) view.getTag();

                                comicViewModel.selectGenre(tagId);
                            }
                        }
                );

                chipGroupGenres.addView(chip);
            }
        }
    };

    private final Observer<List<MangaData>> mangaObserver = new Observer<List<MangaData>>() {
        @Override
        public void onChanged(List<MangaData> mangaDataList) {
            if (mangaDataList != null) {
                comicAdapter.setMangaList(mangaDataList);
                tvSearchResult.setText("Searched for " + etSearch.getText().toString() + " successfully!");
            }
        }
    };

    private final Observer<Boolean> loadingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean isLoadingnow) {
            if (isLoadingnow != null && isLoadingnow) {
                String searchQuery = etSearch.getText().toString();
                tvSearchResult.setText("Loading results for: " + searchQuery + "...");
            }
        }
    };

    private final Observer<List<MangaData>> genreMangaObserver = new Observer<List<MangaData>>() {
        @Override
        public void onChanged(List<MangaData> mangaList) {
            if (mangaList != null) {
                comicAdapter.setMangaList(mangaList);
                rcvcomic.scrollToPosition(0);
            }
        }
    };


    // --------------------- onCreate --------------------------
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
        genreScrollView = findViewById(R.id.genreScrollView);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        searchbar = findViewById(R.id.searchBar);

        spnSearchOpt.setOnItemSelectedListener(onSearchOptionSelectedListener);
        btnSearch.setOnClickListener(onSearchClickListener);
        imgvAvatar.setOnClickListener(onProfileClickListener);
        rcvcomic.addOnScrollListener(scrollListener);

        chipGroupGenres.setSingleSelection(true);
        chipGroupGenres.setSelectionRequired(false);

        detailLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == ComicDetailActivity.RESULT_OK
                                && result.getData() != null) {

                            String genreId = result.getData()
                                    .getStringExtra("genre_id");

                            if (genreId != null) {
                                spnSearchOpt.setSelection(1);
                                selectGenreChip(genreId);
                                comicViewModel.selectGenre(genreId);
                            }
                        }
                    }});


        comicViewModel = new ViewModelProvider(this)
                .get(ComicViewModel.class);

        comicViewModel.getMangaList().observe(this, mangaObserver);
        comicViewModel.getGenresLive().observe(this, genreObserver);
        comicViewModel.getMangaByGenreResults().observe(this, genreMangaObserver);
        comicViewModel.getLoading().observe(this, loadingObserver);

        comicAdapter = new ComicAdapter(
                new ComicAdapter.OnComicClickListener() {
                    @Override
                    public void onComicClick(String comicId) {
                        Intent intent = new Intent(
                                ComicListActivity.this,
                                ComicDetailActivity.class
                        );

                        intent.putExtra("comic_id", comicId);
                        intent.putExtra("search_option", "MangaDex");

                        detailLauncher.launch(intent);
                    }
                }
        );

        rcvcomic.setAdapter(comicAdapter);
        rcvcomic.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

    }

    public void loadComicwithMangaDex(String searchQuery) {
        comicViewModel.searchComicswithMangaDex(searchQuery);
    }

    public void loadMoreManga() {
        comicViewModel.loadNextPage();
    }

    private void selectGenreChip(String genreId) {
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            View view = chipGroupGenres.getChildAt(i);

            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                String tagId = (String) chip.getTag();

                if (genreId.equals(tagId)) {
                    chip.setChecked(true);
                    chipGroupGenres.setSingleSelection(true);
                    chipGroupGenres.setSelectionRequired(false);

                    // Cuộn đến chip được chọn nếu cần
                    chipGroupGenres.post(new Runnable() {
                        @Override
                        public void run() {
                            chipGroupGenres.requestChildFocus(chip, chip);
                        }
                    });

                    return;
                }
            }
        }
    }

}