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

    private final MutableLiveData<List<String>> history =
            new MutableLiveData<>();

    public SearchViewModel(@NonNull Application application) {
        super(application);

        repository = new SearchHistoryRepository(application);

        loadHistory();
    }

    public LiveData<List<String>> getHistory() {
        return history;
    }

    public void loadHistory() {
        history.setValue(repository.getHistory());
    }

    public void saveSearch(String query) {
        repository.saveSearch(query);
        loadHistory();
    }

    public void clearHistory() {
        repository.clearHistory();
        loadHistory();
    }
}