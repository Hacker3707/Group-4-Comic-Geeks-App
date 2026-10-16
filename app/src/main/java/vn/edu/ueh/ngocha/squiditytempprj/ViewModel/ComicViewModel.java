package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ComicRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GenreRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ReviewRepository;

public class ComicViewModel extends AndroidViewModel {
    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final ReviewRepository reviewRepository;
    MangaDexApi api = RetrofitClient.getMangaDexApi();

    private final LiveData<List<Comic>> allComics;
    private final LiveData<List<Genre>> allGenres;
    private final LiveData<List<Review>> allReviews;
    private MutableLiveData<List<MangaData>> mangaList =
            new MutableLiveData<List<MangaData>>();

    private int currentOffset = 0;
    private final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String currentQuery = "";

    public LiveData<List<MangaData>> getMangaList() {
        return mangaList;
    }
    public void setMangaList(List<MangaData> mangaList) {
        this.mangaList.setValue(mangaList);
    }

    public ComicViewModel(@NonNull Application application) {
        super(application);
        this.comicRepository = new ComicRepository(application);
        this.genreRepository = new GenreRepository(application);
        this.reviewRepository = new ReviewRepository(application);

        this.allComics = comicRepository.getAllComics();
        this.allGenres = genreRepository.getAllGenres();
        this.allReviews = reviewRepository.getAllReviews();

    }

    public LiveData<List<Comic>> getAllComics() {
        return allComics;
    }
    public LiveData<List<Genre>> getAllGenres() {
        return allGenres;
    }
    public LiveData<List<Review>> getAllReviews() {
        return allReviews;
    }

    public void insert(Comic comic) {
        comicRepository.insert(comic);
    }

    public void deleteAll() {
        comicRepository.deleteAll();
    }

    public void getComicbyName(String name) {
        comicRepository.getComicbyName(name);
    }

    public void searchComicswithMangaDex(String searchQuery) {
        currentQuery = searchQuery;
        currentOffset = 0;
        isLastPage = false;

        mangaList.setValue(new java.util.ArrayList<MangaData>());

        loadNextPage();
    }

    public void loadNextPage() {
        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

        Call<MangaResponse> call =
                api.searchManga(currentQuery, PAGE_SIZE, currentOffset, "cover_art");

        call.enqueue(new Callback<MangaResponse>() {
            @Override
            public void onResponse(
                    Call<MangaResponse> call,
                    Response<MangaResponse> response) {

                isLoading = false;

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("MANGADEX", "API error: " + response.code());
                    return;
                }

                MangaResponse result = response.body();
                List<MangaData> newItems = result.getData();

                if (newItems == null || newItems.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                List<MangaData> updatedList = mangaList.getValue();

                if (updatedList == null) {
                    updatedList = new java.util.ArrayList<MangaData>();
                } else {
                    updatedList = new java.util.ArrayList<MangaData>(updatedList);
                }

                updatedList.addAll(newItems);
                mangaList.setValue(updatedList);

                currentOffset += newItems.size();

                if (currentOffset >= result.getTotal()
                        || newItems.size() < PAGE_SIZE) {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<MangaResponse> call, Throwable t) {
                isLoading = false;
                Log.e("MANGADEX", "API ERROR", t);
            }
        });
    }

}
