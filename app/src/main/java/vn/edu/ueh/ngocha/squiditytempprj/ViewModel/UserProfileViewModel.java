package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import android.net.Uri;

import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserProfileRepository;

public class UserProfileViewModel extends ViewModel {

    private final MutableLiveData<Boolean> saving =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    private final UserProfileRepository repository =
            new UserProfileRepository();

    private final MutableLiveData<ProfileUiState> profile =
            new MutableLiveData<>(ProfileUiState.guest());

    public LiveData<ProfileUiState> getProfile() {
        return profile;
    }

    public void loadCurrentProfile() {
        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            profile.setValue(ProfileUiState.guest());
            return;
        }

        String email = user.getEmail() == null
                ? "" : user.getEmail();

        String username = user.getDisplayName();

        if (username == null || username.trim().isEmpty()) {
            username = email.contains("@")
                    ? email.substring(0, email.indexOf("@"))
                    : "Người dùng";
        }

        String avatarUrl = user.getPhotoUrl() == null
                ? "" : user.getPhotoUrl().toString();

        ProfileUiState fallback = new ProfileUiState(
                username, email, avatarUrl, "", true
        );

        // Hiển thị ngay dữ liệu Firebase Auth trước.
        profile.setValue(fallback);

        String uid = user.getUid();

        repository.getProfile(uid)
                .addOnSuccessListener(document -> {
                    FirebaseUser currentUser =
                            FirebaseAuth.getInstance().getCurrentUser();

                    // Không cho kết quả tài khoản cũ ghi đè tài khoản mới.
                    if (currentUser == null
                            || !uid.equals(currentUser.getUid())) {
                        return;
                    }

                    profile.setValue(
                            createState(document, fallback)
                    );
                })
                .addOnFailureListener(error ->
                        profile.setValue(fallback));
    }

    public void saveCurrentProfile() {
        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            profile.setValue(ProfileUiState.guest());
            return;
        }

        ProfileUiState current = profile.getValue();

        String username = current != null
                ? current.username
                : user.getDisplayName();

        repository.saveProfile(user, username)
                .addOnSuccessListener(unused ->
                        loadCurrentProfile());
    }

    public void updateCoverUrl(String coverUrl) {
        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null
                || coverUrl == null
                || coverUrl.trim().isEmpty()) {
            return;
        }

        repository.updateCoverUrl(user.getUid(), coverUrl)
                .addOnSuccessListener(unused ->
                        loadCurrentProfile());
    }

    private ProfileUiState createState(
            DocumentSnapshot document,
            ProfileUiState fallback
    ) {
        if (document == null || !document.exists()) {
            return fallback;
        }

        String username = document.getString("username");
        String email = document.getString("email");
        String avatarUrl = document.getString("avatarUrl");
        String coverUrl = document.getString("coverUrl");

        return new ProfileUiState(
                isBlank(username) ? fallback.username : username,
                isBlank(email) ? fallback.email : email,
                isBlank(avatarUrl) ? fallback.avatarUrl : avatarUrl,
                coverUrl == null ? "" : coverUrl,
                true
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class ProfileUiState {
        public final String username;
        public final String email;
        public final String avatarUrl;
        public final String coverUrl;
        public final boolean loggedIn;

        public ProfileUiState(
                String username,
                String email,
                String avatarUrl,
                String coverUrl,
                boolean loggedIn
        ) {
            this.username = username;
            this.email = email;
            this.avatarUrl = avatarUrl;
            this.coverUrl = coverUrl;
            this.loggedIn = loggedIn;
        }

        public static ProfileUiState guest() {
            return new ProfileUiState(
                    "Khách", "", "", "", false
            );
        }
    }

    public void updateUsername(String username) {
        String name = username == null ? "" : username.trim();

        if (name.isEmpty()) {
            message.setValue("Vui lòng nhập tên hiển thị.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            message.setValue("Vui lòng đăng nhập trước.");
            return;
        }

        saving.setValue(true);
        message.setValue(null);

        repository.updateUsername(user.getUid(), name)
                .addOnCompleteListener(task -> {
                    saving.setValue(false);

                    if (task.isSuccessful()) {
                        message.setValue("Đã cập nhật tên hồ sơ.");
                        loadCurrentProfile();
                    } else {
                        message.setValue(
                                "Không thể cập nhật tên: "
                                        + getErrorText(task.getException())
                        );
                    }
                });
    }

    public void updateAvatar(Uri imageUri) {
        uploadImage(imageUri, "avatars");
    }

    public void updateCover(Uri imageUri) {
        uploadImage(imageUri, "covers");
    }

    private void uploadImage(Uri imageUri, String folder) {
        if (imageUri == null) {
            message.setValue("Bạn chưa chọn ảnh.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            message.setValue("Vui lòng đăng nhập trước.");
            return;
        }

        saving.setValue(true);
        message.setValue(null);

        repository.uploadProfileImage(
                user.getUid(),
                imageUri,
                folder
        ).addOnSuccessListener(downloadUri -> {
            String url = downloadUri.toString();

            // Cập nhật URL tương ứng trong Firestore.
            com.google.android.gms.tasks.Task<Void> saveTask;

            if ("avatars".equals(folder)) {
                saveTask = repository.updateAvatarUrl(
                        user.getUid(), url
                );
            } else {
                saveTask = repository.updateCoverUrl(
                        user.getUid(), url
                );
            }

            saveTask.addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    saving.setValue(false);
                    message.setValue(
                            "Ảnh đã tải lên nhưng chưa lưu được hồ sơ. "
                                    + getErrorText(task.getException())
                    );
                    return;
                }

                if ("avatars".equals(folder)) {
                    com.google.firebase.auth.UserProfileChangeRequest request =
                            new com.google.firebase.auth.UserProfileChangeRequest
                                    .Builder()
                                    .setPhotoUri(downloadUri)
                                    .build();

                    user.updateProfile(request);
                }

                saving.setValue(false);
                message.setValue(
                        "covers".equals(folder)
                                ? "Đã cập nhật ảnh nền hồ sơ."
                                : "Đã cập nhật ảnh đại diện."
                );

                loadCurrentProfile();
            });
        }).addOnFailureListener(error -> {
            saving.setValue(false);
            message.setValue(
                    "Không thể tải ảnh lên: " + getErrorText(error)
            );
        });
    }

    private String getErrorText(Exception error) {
        if (error == null || error.getMessage() == null) {
            return "Vui lòng thử lại.";
        }

        return error.getMessage();
    }
}