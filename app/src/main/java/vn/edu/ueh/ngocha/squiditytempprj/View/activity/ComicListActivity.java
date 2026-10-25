package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ComicAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.SearchViewModel;

public class ComicListActivity extends BaseActivity {

    @Override
    protected int getContentLayoutId() {
        return R.layout.comic_list_activity;
    }

    private String pendingGenreId;

    private ComicAdapter comicAdapter;
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode()
                                    == ComicDetailActivity.RESULT_OK
                                    && result.getData() != null) {

                                String genreId = result.getData()
                                        .getStringExtra("genre_id");

                                if (genreId != null) {
                                    spnSearchOpt.setSelection(1);
                                    selectGenreChip(genreId);
                                    comicViewModel.selectGenre(genreId);
                                }
                            }
                        }
                    }
            );

    private RecyclerView rcvcomic;
    private ImageView btnSearch;
    private TextView tvSearchResult;
    private Spinner spnSearchOpt;
    private ChipGroup chipGroupGenres;
    private MaterialAutoCompleteTextView etSearch;
    private ArrayAdapter<String> searchAdapter;

    HorizontalScrollView genreScrollView;
    View searchbar;

    private static final String CURRENT_SEARCH_QUERY = "current_search_query";
    private String SEARCH_QUERY = "search_query";


    private ComicViewModel comicViewModel;
    private SearchViewModel searchViewModel;
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

    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String query = etSearch.getText().toString().trim();

            if (!query.isEmpty()) {
                SEARCH_QUERY = query;

                rcvcomic.scrollToPosition(0);
                searchViewModel.savecomicSearch(query);
                loadComicwithMangaDex(query);
            }
        }
    };

    private AdapterView.OnItemSelectedListener onSearchHistorySelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(
                AdapterView<?> parent,
                View view,
                int position,
                long id) {

            SEARCH_QUERY = parent.getItemAtPosition(position).toString();

            etSearch.setText(SEARCH_QUERY);
            etSearch.setSelection(SEARCH_QUERY.length());

            // Gọi hàm tìm kiếm hiện tại nếu muốn chọn lịch sử là tìm ngay
            loadComicwithMangaDex(SEARCH_QUERY);

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private View.OnClickListener onSearchBarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            etSearch.showDropDown();
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
                comicAdapter.setMangaList(new ArrayList<MangaData>());
                isGenreMode = false;

            } else {
                // Genre
                tvSearchResult.setVisibility(View.GONE);
                genreScrollView.setVisibility(View.VISIBLE);
                searchbar.setVisibility(View.GONE);
                comicAdapter.setMangaList(new ArrayList<MangaData>());
                isGenreMode = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };



    // --------------------- Observer --------------------------

    Observer<List<Genre>> genreObserver = new Observer<List<Genre>>() {
        @Override
        public void onChanged(List<Genre> genres) {

            if (genres == null) {
                return;
            }

            for (Genre genre : genres) {
                Chip chip = new Chip(ComicListActivity.this);
                chip.setText(genre.getGenreName());
                chip.setCheckable(true);
                chip.setTag(genre.getGenreId());

                chip.setOnClickListener(view -> {
                    String id = (String) view.getTag();
                    comicViewModel.selectGenre(id);
                });

                chipGroupGenres.addView(chip);
            }

            // Chỉ chọn sau khi các Chip đã được tạo.
            if (pendingGenreId != null) {
                selectGenreChip(pendingGenreId);
            }
        }
    };

    private final Observer<List<String>> historyObserver = new Observer<>() {
        @Override
        public void onChanged(List<String> historyList) {
            if (historyList == null) {
                return;
            }

            searchAdapter = new ArrayAdapter<>(
                    ComicListActivity.this,
                    android.R.layout.simple_dropdown_item_1line,
                    historyList
            );

            etSearch.setAdapter(searchAdapter);
        }

    };

    private final Observer<List<MangaData>> mangaObserver =
            new Observer<List<MangaData>>() {
                @Override
                public void onChanged(List<MangaData> mangaDataList) {
                    if (mangaDataList != null && !isGenreMode) {
                        comicAdapter.setMangaList(mangaDataList);

                        if (!SEARCH_QUERY.isEmpty() && mangaDataList.isEmpty()) {
                            tvSearchResult.setText(
                                    "Searched for "
                                            + SEARCH_QUERY
                                            + " successfully!"
                            );
                        }
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


    // =========================================================
    // --------------------- onCreate --------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rcvcomic = findViewById(R.id.rcvcomic);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvSearchResult = findViewById(R.id.tvSearchResult);
        spnSearchOpt = findViewById(R.id.spn_searchopt);
        genreScrollView = findViewById(R.id.genreScrollView);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        searchbar = findViewById(R.id.searchBar);
        spnSearchOpt.setOnItemSelectedListener(onSearchOptionSelectedListener);
        btnSearch.setOnClickListener(onSearchClickListener);
        rcvcomic.addOnScrollListener(scrollListener);

        chipGroupGenres.setSingleSelection(true);
        chipGroupGenres.setSelectionRequired(false);

        if (savedInstanceState != null) {
            SEARCH_QUERY = savedInstanceState.getString(
                    CURRENT_SEARCH_QUERY, ""
            );
        } else {
            SEARCH_QUERY = "";
        }

        etSearch.setText(SEARCH_QUERY);
        etSearch.setSelection(etSearch.getText().length());

        comicViewModel = new ViewModelProvider(this)
                .get(ComicViewModel.class);
        searchViewModel = new ViewModelProvider(this)
                .get(SearchViewModel.class);

        Intent intent = getIntent();
        pendingGenreId = intent.getStringExtra("genre_id");

        if (pendingGenreId != null && !pendingGenreId.trim().isEmpty()) {
            isGenreMode = true;
            spnSearchOpt.setSelection(1);
            comicViewModel.selectGenre(pendingGenreId);
        }

        comicViewModel.getMangaList().observe(this, mangaObserver);
        comicViewModel.getGenresLive().observe(this, genreObserver);
        comicViewModel.getMangaByGenreResults().observe(this, genreMangaObserver);
        comicViewModel.getLoading().observe(this, loadingObserver);

        searchViewModel.getcomicHistory().observe(this, historyObserver);

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CURRENT_SEARCH_QUERY, SEARCH_QUERY);
    }

    public void loadComicwithMangaDex(String searchQuery) {
        comicViewModel.searchComicswithMangaDex(searchQuery);
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