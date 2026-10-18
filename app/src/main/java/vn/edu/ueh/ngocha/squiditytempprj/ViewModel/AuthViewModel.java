package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.AuthRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserProfileRepository;

public class AuthViewModel extends ViewModel {

    private final UserProfileRepository profileRepository =
            new UserProfileRepository();

    private final MutableLiveData<Boolean> registered =
            new MutableLiveData<>(false);

    public LiveData<Boolean> getRegistered() {
        return registered;
    }

    private final AuthRepository repository;

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<FirebaseUser> loginResult =
            new MutableLiveData<>();

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> logoutResult =
            new MutableLiveData<>(false);

    public AuthViewModel() {
        repository = new AuthRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<FirebaseUser> getLoginResult() {
        return loginResult;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLogoutResult() {
        return logoutResult;
    }

    public FirebaseUser getCurrentUser() {
        return repository.getCurrentUser();
    }

    public void loginWithEmail(String email, String password) {
        loading.setValue(true);
        error.setValue(null);

        repository.loginWithEmail(email, password)
                .addOnCompleteListener(task -> {
                    loading.setValue(false);

                    if (task.isSuccessful()) {
                        loginResult.setValue(
                                repository.getCurrentUser()
                        );
                    } else {
                        error.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Đăng nhập thất bại."
                        );
                    }
                });
    }

    public void registerWithEmail(
            String username,
            String email,
            String password
    ) {
        loading.setValue(true);
        error.setValue(null);

        repository.registerWithEmail(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        loading.setValue(false);
                        error.setValue(task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng ký thất bại.");
                        return;
                    }

                    FirebaseUser user = repository.getCurrentUser();

                    if (user == null) {
                        loading.setValue(false);
                        error.setValue("Không lấy được tài khoản vừa tạo.");
                        return;
                    }

                    repository.updateDisplayName(username)
                            .addOnCompleteListener(nameTask -> {
                                if (!nameTask.isSuccessful()) {
                                    loading.setValue(false);
                                    error.setValue("Không cập nhật được tên hiển thị.");
                                    return;
                                }

                                profileRepository.saveProfile(user, username)
                                        .addOnCompleteListener(saveTask -> {
                                            loading.setValue(false);

                                            if (!saveTask.isSuccessful()) {
                                                error.setValue(
                                                        "Đã tạo tài khoản nhưng chưa lưu được hồ sơ."
                                                );
                                                return;
                                            }

                                            repository.logout();
                                            registered.setValue(true);
                                        });
                            });
                });
    }

    public void loginWithGoogle(AuthCredential credential) {
        loading.setValue(true);
        error.setValue(null);

        repository.loginWithGoogle(credential)
                .addOnCompleteListener(task -> {
                    loading.setValue(false);

                    if (task.isSuccessful()) {
                        loginResult.setValue(
                                repository.getCurrentUser()
                        );
                    } else {
                        error.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Đăng nhập Google thất bại."
                        );
                    }
                });
    }

    public void logout() {
        repository.logout();
        logoutResult.setValue(true);
    }

    public String getCurrentUserId() {
        String userId = repository.getCurrentUserId();

        if (userId != null) {
            return repository.getCurrentUserId();
        }
        return null;
    }
}