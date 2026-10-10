package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ComicDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;

public class ComicRepository {
    private final ComicDao comicDao;
    private final LiveData<List<Comic>> allComics;

    public ComicRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        comicDao = db.comicDao();
        allComics = comicDao.getAllComics();
    }

    public void insert(Comic comic) {
        comicDao.insertComic(comic);
    }

    public LiveData<List<Comic>> getAllComics() {
        return comicDao.getAllComics();
    }

    public void deleteAll() {
        comicDao.deleteAll();
    }

    public void delete(Comic comic) { comicDao.deleteComic(comic); }

    public Comic getComicbyName(String name) {
        return comicDao.getComicbyName(name);
    }
}
