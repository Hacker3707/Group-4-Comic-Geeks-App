package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.SearchHistoryRepository;

public class SearchViewModel extends AndroidViewModel {

    private final SearchHistoryRepository repository;

    private final MutableLiveData<List<String>> comicsearchHistory =
            new MutableLiveData<>();

    private final MutableLiveData<List<String>> homesearchHistory =
            new MutableLiveData<>();

    public SearchViewModel(@NonNull Application application) {
        super(application);

        repository = new SearchHistoryRepository(application);

        loadcomicHistory();
    }

    public LiveData<List<String>> getcomicHistory() {
        return comicsearchHistory;
    }

    public LiveData<List<String>> gethomeHistory() {
        return homesearchHistory;
    }

    public void loadcomicHistory() {
        comicsearchHistory.setValue(repository.getcomicHistory());
    }
    public void loadhomeHistory() {
        homesearchHistory.setValue(repository.gethomeHistory());
    }

    public void savecomicSearch(String query) {
        repository.savecomicSearch(query);
        loadhomeHistory();
    }

    public void savehomeSearch(String query) {
        repository.savehomeSearch(query);
        loadhomeHistory();
    }

    public void clearcomicHistory() {
        repository.clearcomicHistory();
        loadcomicHistory();
    }

    public void clearhomeHistory() {
        repository.clearhomeHistory();
        loadhomeHistory();
    }
}