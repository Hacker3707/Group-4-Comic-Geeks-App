package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.GenreRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MangaDexRepository;

public class GenreViewModel extends AndroidViewModel {

    private final GenreRepository genreRepository;
    private final MangaDexRepository mangaDexRepository;

    public final LiveData<List<Genre>> allGenres;

    private final LiveData<List<Genre>> mangaDexGenres;

    public GenreViewModel(@NonNull Application application) {
        super(application);

        this.genreRepository = new GenreRepository(application);
        this.mangaDexRepository = new MangaDexRepository();

        this.allGenres = genreRepository.getAllGenres();

        // Thể loại lấy từ MangaDex API
        this.mangaDexGenres = mangaDexRepository.getGenresLive();
    }

    // Thể loại lưu trong Room
    public LiveData<List<Genre>> getAllGenres_off() {
        return allGenres;
    }

    // Thể loại lấy từ MangaDex API
    public LiveData<List<Genre>> getAllGenres() {
        return mangaDexGenres;
    }

    public void insert(Genre genre) {
        genreRepository.insert(genre);
    }

    public void deleteAll() {
        genreRepository.deleteAll();
    }

    public LiveData<MangaData> getMangaById(String mangaId) {
        return mangaDexRepository.getMangaById(mangaId);
    }

    public LiveData<List<MangaData>> getMangaByGenreLive(String tagId) {
        return mangaDexRepository.getMangaByGenreLive(tagId);
    }

}