
package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final LiveData<List<User>> allUsers;
    private final LiveData<List<User>> normalUsers;
    private final LiveData<List<User>> admins;
    private final LiveData<List<User>> creators;

    public UserViewModel(@NonNull Application application) {
        super(application);

        userRepository = new UserRepository(application);

        allUsers = userRepository.getAllUsers();
        normalUsers = userRepository.getNormalUsersLive();
        admins = userRepository.getAdminsLive();
        creators = userRepository.getCreatorsLive();
    }

    // Danh sách tất cả tài khoản
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    // Danh sách User thường
    public LiveData<List<User>> getNormalUsers() {
        return normalUsers;
    }

    // Danh sách Admin
    public LiveData<List<User>> getAdmins() {
        return admins;
    }

    // Danh sách Creator
    public LiveData<List<User>> getCreators() {
        return creators;
    }

    // Thêm tài khoản
    public void insertUser(User user) {
        if (user == null) {
            return;
        }

        userRepository.insert(user);
    }

    // Cập nhật tài khoản
    public void updateUser(User user) {
        if (user == null) {
            return;
        }

        userRepository.update(user);
    }

    // Xóa một tài khoản
    public void deleteUser(User user) {
        if (user == null) {
            return;
        }

        userRepository.delete(user);
    }

    // Xóa toàn bộ tài khoản
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepository.shutdown();
    }
}