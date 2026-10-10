package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

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

    private final LiveData<List<Comic>> allComics;
    private final LiveData<List<Genre>> allGenres;
    private final LiveData<List<Review>> allReviews;


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

}
