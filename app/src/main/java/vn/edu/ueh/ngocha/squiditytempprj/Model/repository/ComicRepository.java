package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ComicDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;

public class ComicRepository {
    private final ComicDao comicDao;
    private ExecutorService executorService;
    private final FirebaseFirestore firestore;
    private final LiveData<List<Comic>> allComics;


    public ComicRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        comicDao = db.comicDao();
        allComics = comicDao.getAllComics();
        executorService = Executors.newSingleThreadExecutor();
        firestore = FirebaseFirestore.getInstance();
    }

    public void insert(Comic comic) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                comicDao.insertComic(comic);
            }
        });
    }

    public LiveData<List<Comic>> getAllComics() {
        return allComics;
    }

    public void deleteAll() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                comicDao.deleteAll();
            }
        });
    }

    public void delete(Comic comic) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                comicDao.deleteComic(comic);
            }
        });
    }

    public LiveData<Comic> getComicbyName(String name) {
        return comicDao.getComicbyName(name);
    }
    public LiveData<Comic> getComicById(String comicId) {
        return comicDao.getComicById(comicId);
    }

}
