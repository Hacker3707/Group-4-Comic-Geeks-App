package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ComicRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GenreRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MangaDexRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ReviewRepository;

public class ComicViewModel extends AndroidViewModel {
    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final ReviewRepository reviewRepository;
    MangaDexApi api = RetrofitClient.getMangaDexApi();
    private final MangaDexRepository mangaRepository;

    private final LiveData<List<Comic>> allComics;
    private final LiveData<List<Genre>> allGenres;
    private final LiveData<List<Review>> allReviews;
    private final MutableLiveData<String> selectedGenreId =
            new MutableLiveData<>();
    private MutableLiveData<List<MangaData>> mangaList =
            new MutableLiveData<List<MangaData>>();

    private int currentOffset = 0;
    private final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String currentQuery = "";
    private final LiveData<List<MangaData>> mangaByGenreLive;
    public void setMangaList(List<MangaData> mangaList) {
        this.mangaList.setValue(mangaList);
    }


    public ComicViewModel(@NonNull Application application) {
        super(application);
        this.comicRepository = new ComicRepository(application);
        this.genreRepository = new GenreRepository(application);
        this.reviewRepository = new ReviewRepository(application);
        this.mangaRepository = new MangaDexRepository();

        this.mangaByGenreLive = Transformations.<String, List<MangaData>>switchMap(
                selectedGenreId,
                new Function1<String, LiveData<List<MangaData>>>() {
                    @Override
                    public LiveData<List<MangaData>> invoke(String tagId) {
                        return mangaRepository.getMangaByGenreLive(tagId);
                    }
                }
        );

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

    public void loadNextPage() {
        mangaRepository.loadNextPage();
    }

    public LiveData<List<Tag>> getGenresLive() {
        return mangaRepository.getGenresLive();
    }

    public LiveData<List<MangaData>> getMangaByGenreResults() {
        return mangaByGenreLive;
    }

    public void selectGenre(String tagId) {
        selectedGenreId.setValue(tagId);
    }

    public LiveData<List<MangaData>> getMangaList() {
        return mangaRepository.getMangaList();
    }

    public LiveData<Boolean> getLoading() {
        return mangaRepository.getLoading();
    }

    public void searchComicswithMangaDex(String query) {
        mangaRepository.searchComics(query);
    }



}
