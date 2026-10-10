package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GenreDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;

public class GenreRepository {
    public final GenreDao genreDao;
    public final LiveData<List<Genre>> allGenres;

    public GenreRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.genreDao = db.genreDao();
        allGenres = genreDao.getAllGenres();
    }

    public void insert(Genre genre) {
        this.genreDao.insertGenre(genre);
    }

    public LiveData<List<Genre>> getAllGenres() {
        return genreDao.getAllGenres();
    }

    public void deleteAll() {
        this.genreDao.deleteAll();
    }


}
