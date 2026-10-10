package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

public class UserRepository {

    private final UserDao userDao;

    private final LiveData<List<User>> allUsers;
    private final LiveData<List<User>> normalUsers;
    private final LiveData<List<User>> admins;
    private final LiveData<List<User>> creators;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public UserRepository(Application application) {

        AppDatabase database =
                AppDatabase.getDatabase(application);

        userDao = database.userDao();

        allUsers = userDao.getAll();
        normalUsers = userDao.getNormalUsers();
        admins = userDao.getAdmins();
        creators = userDao.getCreators();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<User>> getNormalUsersLive() {
        return normalUsers;
    }

    public LiveData<List<User>> getAdminsLive() {
        return admins;
    }

    public LiveData<List<User>> getCreatorsLive() {
        return creators;
    }

    public void insert(User user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void update(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public void delete(User user) {
        executor.execute(() -> userDao.delete(user));
    }

    public void deleteAll() {
        executor.execute(() -> userDao.deleteAll());
    }

    public void shutdown() {
        executor.shutdown();
    }
}