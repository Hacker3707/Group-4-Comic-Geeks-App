package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MangaDexRepository;

public class ComicDetailViewModel extends AndroidViewModel {

    private final MangaDexRepository repository;

    public ComicDetailViewModel(Application application) {
        super(application);
        repository = new MangaDexRepository();
    }

    public LiveData<MangaData> getMangaById(String mangaId) {
        return repository.getMangaById(mangaId);
    }
}