package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MangaDexRepository;

public class ComicDetailViewModel extends AndroidViewModel {

    private final MangaDexRepository repository;

    private LiveData<MangaData> manga;

    public ComicDetailViewModel(Application application) {
        super(application);
        repository = new MangaDexRepository();
    }

    public LiveData<MangaData> getMangaById(String mangaId) {
        if (manga == null) {
            manga = repository.getMangaById(mangaId);
        }

        return manga;
    }
}