package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Utils.NetworkUtils;
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
                                    selectedGenreId = genreId;
                                    pendingGenreId = genreId;
                                    isGenreMode = true;

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
    private static final String CURRENT_SEARCH_OPTION = "current_search_option";
    private static final String CURRENT_GENRE_ID = "current_genre_id";

    private String SEARCH_QUERY = "";
    private String selectedGenreId = null;
    private boolean isGenreMode = false;
    private boolean restoringState = false;


    private ComicViewModel comicViewModel;
    private SearchViewModel searchViewModel;
    private boolean isOffline = false;
    private List<Comic> cachedComics = new ArrayList<>();



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
                comicViewModel.loadNextPage(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE);
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
                if (isOffline) {
                    showRoomResults(query);
                } else {
                    loadComicwithMangaDex(query);
                }
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

    private final AdapterView.OnItemSelectedListener onSearchOptionSelectedListener =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(
                        AdapterView<?> parent,
                        View view,
                        int position,
                        long id) {

                    boolean newGenreMode = position == 1;

                    tvSearchResult.setVisibility(newGenreMode ? View.GONE : View.VISIBLE);
                    genreScrollView.setVisibility(newGenreMode ? View.VISIBLE : View.GONE);
                    searchbar.setVisibility(newGenreMode ? View.GONE : View.VISIBLE);

                    if (restoringState) {
                        return;
                    }

                    if (isGenreMode == newGenreMode) {
                        return;
                    }

                    isGenreMode = newGenreMode;

                    if (isOffline) {
                        showRoomResults(isGenreMode ? "" : SEARCH_QUERY);
                        return;
                    }

                    if (isGenreMode) {
                        if (selectedGenreId != null) {
                            comicViewModel.selectGenre(selectedGenreId);
                        } else {
                            comicAdapter.setMangaList(new ArrayList<>());
                        }
                    } else {
                        if (!SEARCH_QUERY.trim().isEmpty()) {
                            loadComicwithMangaDex(SEARCH_QUERY);
                        } else {
                            comicAdapter.setMangaList(new ArrayList<>());
                        }
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

            chipGroupGenres.removeAllViews();
            if (genres == null) {
                return;
            }

            for (Genre genre : genres) {
                Chip chip = new Chip(ComicListActivity.this);
                chip.setText(genre.getGenreName());
                chip.setCheckable(true);
                chip.setTag(genre.getGenreId());

                chip.setOnClickListener(onChipClickListener);

                chipGroupGenres.addView(chip);
            }

            // Chỉ chọn sau khi các Chip đã được tạo.
            if (pendingGenreId != null) {
                selectGenreChip(pendingGenreId);
            }
        }
    };

    private View.OnClickListener onChipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = (String) view.getTag();

            selectedGenreId = id;
            pendingGenreId = id;

            comicViewModel.selectGenre(id);
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

    private final Observer<List<Comic>> roomComicObserver = new Observer<List<Comic>>() {
        @Override
        public void onChanged(List<Comic> comics) {
            cachedComics = comics == null ? new ArrayList<>() : new ArrayList<>(comics);
            if (isOffline) {
                showRoomResults(SEARCH_QUERY);
            }
        }
    };

    private void showRoomResults(String query) {
        List<Comic> filtered = new ArrayList<>();
        String normalized = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        for (Comic comic : cachedComics) {
            if (comic == null) continue;
            String title = comic.getComicName() == null ? "" : comic.getComicName();
            if (normalized.isEmpty() || title.toLowerCase(java.util.Locale.ROOT).contains(normalized)) {
                filtered.add(comic);
            }
        }
        comicAdapter.setComicList(filtered);
        if (tvSearchResult != null) {
            tvSearchResult.setText(filtered.isEmpty()
                    ? (normalized.isEmpty()
                       ? "Chưa có truyện được lưu để xem offline."
                       : "Không tìm thấy truyện đã lưu khớp với: " + query)
                    : "Đang hiển thị " + filtered.size() + " truyện đã lưu trên thiết bị.");
        }
    }

    /** Called by BaseActivity's shared NetworkMonitor. */
    @Override
    protected void onNetworkStatusChanged(boolean connected) {
        isOffline = !connected;
        if (isOffline && comicAdapter != null) {
            showRoomResults(SEARCH_QUERY);
        } else if (connected && !isGenreMode && SEARCH_QUERY != null
                && !SEARCH_QUERY.trim().isEmpty() && comicViewModel != null) {
            loadComicwithMangaDex(SEARCH_QUERY);
        }
    }

    private final Observer<List<MangaData>> mangaObserver =
            new Observer<List<MangaData>>() {
                @Override
                public void onChanged(List<MangaData> mangaDataList) {
                    if (mangaDataList != null && !isGenreMode && !isOffline) {
                        comicAdapter.setMangaList(mangaDataList);

                        if (!SEARCH_QUERY.isEmpty() && mangaDataList.isEmpty()) {
                            tvSearchResult.setText(
                                    Str.t(R.string.comic_searched_for, SEARCH_QUERY)
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
                tvSearchResult.setText(Str.t(R.string.comic_loading_results, searchQuery));
            }
        }
    };

    private final Observer<List<MangaData>> genreMangaObserver = new Observer<List<MangaData>>() {
        @Override
        public void onChanged(List<MangaData> mangaList) {
            if (mangaList != null && !isOffline && isGenreMode) {
                comicAdapter.setMangaList(mangaList);
                rcvcomic.scrollToPosition(0);
            }
        }
    };

    @Override
    public boolean hideAppBarinLandscape() {
        return true;
    }

    @Override
    protected String getActivityName() {
        return "ComicListActivity";
    }


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


        etSearch.setOnItemSelectedListener(onSearchHistorySelectedListener);
        btnSearch.setOnClickListener(onSearchClickListener);
        rcvcomic.addOnScrollListener(scrollListener);

        chipGroupGenres.setSingleSelection(true);
        chipGroupGenres.setSelectionRequired(false);

        restoringState = true;

        comicViewModel = new ViewModelProvider(this)
                .get(ComicViewModel.class);

        searchViewModel = new ViewModelProvider(this)
                .get(SearchViewModel.class);

        if (savedInstanceState != null) {
            SEARCH_QUERY = savedInstanceState.getString(
                    CURRENT_SEARCH_QUERY, "");

            isGenreMode = savedInstanceState.getBoolean(
                    CURRENT_SEARCH_OPTION, false);

            selectedGenreId = savedInstanceState.getString(
                    CURRENT_GENRE_ID);

        } else {
            SEARCH_QUERY = "";

            Intent intent = getIntent();
            selectedGenreId = intent.getStringExtra("genre_id");
            isGenreMode = selectedGenreId != null
                    && !selectedGenreId.trim().isEmpty();
        }

        pendingGenreId = selectedGenreId;

        etSearch.setText(SEARCH_QUERY);
        etSearch.setSelection(etSearch.length());

        spnSearchOpt.setSelection(isGenreMode ? 1 : 0, false);

        tvSearchResult.setVisibility(isGenreMode ? View.GONE : View.VISIBLE);
        genreScrollView.setVisibility(isGenreMode ? View.VISIBLE : View.GONE);
        searchbar.setVisibility(isGenreMode ? View.GONE : View.VISIBLE);

        comicAdapter = new ComicAdapter(
                new ComicAdapter.OnComicClickListener() {
                    @Override
                    public void onComicClick(String comicId, MangaData mangaData) {
                        // Chỉ lưu truyện từ API khi người dùng thật sự mở chi tiết.
                        if (mangaData != null) {
                            comicViewModel.saveMangaToRoom(mangaData);
                        }
                        Intent intent = new Intent(
                                ComicListActivity.this,
                                ComicDetailActivity.class
                        );

                        intent.putExtra("comic_id", comicId);
                        intent.putExtra("search_option", "MangaDex");
                        intent.putExtra("from_activity", "ComicListActivity");

                        detailLauncher.launch(intent);
                    }
                }
        );


        rcvcomic.setAdapter(comicAdapter);
        rcvcomic.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        // Đăng ký observer sau khi adapter đã được khởi tạo.
        comicViewModel.getAllComics().observe(this, roomComicObserver);
        comicViewModel.getMangaList().observe(this, mangaObserver);
        comicViewModel.getGenresLive().observe(this, genreObserver);
        comicViewModel.getMangaByGenreResults().observe(this, genreMangaObserver);
        comicViewModel.getLoading().observe(this, loadingObserver);
        searchViewModel.getcomicHistory().observe(this, historyObserver);

        isOffline = !NetworkUtils.isInternetAvailable(this);
        if (isOffline) {
            showRoomResults(SEARCH_QUERY);
        }

        spnSearchOpt.post(new Runnable() {
            @Override
            public void run() {
                restoringState = false;
                spnSearchOpt.setOnItemSelectedListener(onSearchOptionSelectedListener);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_SEARCH_QUERY, SEARCH_QUERY);
        outState.putBoolean(CURRENT_SEARCH_OPTION, isGenreMode);
        outState.putString(CURRENT_GENRE_ID, selectedGenreId);

        super.onSaveInstanceState(outState);
    }

    public void loadComicwithMangaDex(String searchQuery) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            comicViewModel.searchComicswithMangaDex(searchQuery, true);
        }
        else {
            comicViewModel.searchComicswithMangaDex(searchQuery, false);
        }
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