
package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserFavoriteGenreDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.UserFavoriteGenre;

public class UserFavoriteGenreRepository {

    private final UserFavoriteGenreDao dao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public UserFavoriteGenreRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dao = db.userFavoriteGenreDao();
    }

    public LiveData<List<String>> getFavoriteGenreIds(String userId) {
        return dao.getFavoriteGenreIds(userId);
    }

    public void saveFavoriteGenreIds(
            String userId,
            List<String> genreIds
    ) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        List<String> safeIds = genreIds == null
                ? new ArrayList<>()
                : new ArrayList<>(genreIds);

        executor.execute(() -> {
            // Chỉ xóa lựa chọn của chính user này.
            dao.deleteByUserId(userId);

            List<UserFavoriteGenre> rows = new ArrayList<>();

            for (String genreId : safeIds) {
                if (genreId != null && !genreId.trim().isEmpty()) {
                    rows.add(new UserFavoriteGenre(userId, genreId));
                }
            }

            if (!rows.isEmpty()) {
                dao.insertAll(rows);
            }
        });
    }

    public void clearFavoriteGenres(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        executor.execute(() -> dao.deleteByUserId(userId));
    }
}
