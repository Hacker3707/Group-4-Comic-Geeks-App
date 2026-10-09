package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ComicRepository;

public class ComicViewModel {
    private final ComicRepository comicRepository;
    private final LiveData<List<Comic>> allComics;

    public ComicViewModel(Application application) {
        //super(application);
        this.comicRepository = new ComicRepository(application);
        this.allComics = comicRepository.getAllComics();
    }

    public LiveData<List<Comic>> getAllComics() {
        return allComics;
    }
    public void insert(Comic comic) {
        comicRepository.insert(comic);
    }
    public void deleteAll() {
        comicRepository.deleteAll();
    }
}
