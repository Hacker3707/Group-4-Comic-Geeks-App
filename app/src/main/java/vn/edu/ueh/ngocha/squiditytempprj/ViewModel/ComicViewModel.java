package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ComicRepository;

public class ComicViewModel {
    private final ComicRepository comicRepository;

    public ComicViewModel(ComicRepository comicRepository) {
        this.comicRepository = comicRepository;
    }

}
