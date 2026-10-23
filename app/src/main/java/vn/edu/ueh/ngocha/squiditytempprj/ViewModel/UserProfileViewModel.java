package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Collections;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.UserProfileRepository;

public class UserProfileViewModel extends ViewModel {

    private final UserProfileRepository repository =
            new UserProfileRepository();

    private final MutableLiveData<Boolean> saving =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    private final MutableLiveData<ProfileUiState> profile =
            new MutableLiveData<>(ProfileUiState.guest());

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<ProfileUiState> getProfile() {
        return profile;
    }

    // Danh sách tài khoản dùng cho màn tìm kiếm người dùng
    private final MutableLiveData<List<User>> searchableUsers = new MutableLiveData<List<User>>();

    public LiveData<List<User>> getSearchableUsers() {
        return searchableUsers;
    }

    public void loadAllUsers() {
        repository.getAllUsers()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<User> users = new ArrayList<User>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setUserId(doc.getId());
                                users.add(user);
                            }
                        }
                        searchableUsers.setValue(users);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        searchableUsers.setValue(new ArrayList<User>());
                    }
                });
    }

    // Tải hồ sơ của tài khoản đang đăng nhập.
    public void loadCurrentProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            profile.setValue(ProfileUiState.guest());
            return;
        }

        String email = user.getEmail() == null ? "" : user.getEmail();
        String username = user.getDisplayName();

        if (isBlank(username)) {
            username = email.contains("@")
                    ? email.substring(0, email.indexOf("@"))
                    : "Người dùng";
        }

        String avatarUrl = user.getPhotoUrl() == null
                ? "" : user.getPhotoUrl().toString();

        List<String> favGenres = null;

        ProfileUiState fallback = new ProfileUiState(
                username,
                email,
                avatarUrl,
                "",
                "",
                "",
                "",
                true,
                Collections.emptyList()
        );

        profile.setValue(fallback);

        String uid = user.getUid();

        repository.getProfile(uid)
                .addOnSuccessListener(document -> {
                    FirebaseUser currentUser =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (currentUser == null
                            || !uid.equals(currentUser.getUid())) {
                        return;
                    }

                    profile.setValue(createState(document, fallback));
                })
                .addOnFailureListener(error -> {
                    FirebaseUser currentUser =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (currentUser != null
                            && uid.equals(currentUser.getUid())) {
                        profile.setValue(fallback);
                        message.setValue(
                                "Không thể tải hồ sơ: "
                                        + getErrorText(error)
                        );
                    }
                });
    }

    // Xem profile của người khác (không đổi gì ở profile của chính mình)
    public void loadProfile(String uid) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();

        if (uid == null || uid.isEmpty()
                || (me != null && uid.equals(me.getUid()))) {
            loadCurrentProfile();
            return;
        }

        final ProfileUiState fallback = new ProfileUiState(
                "Người dùng", "", "", "", "", "", "", true, Collections.emptyList());
        profile.setValue(fallback);

        repository.getProfile(uid)
                .addOnSuccessListener(document -> {
                    ProfileUiState state = createState(document, fallback);
                    // Không hiện email của người khác
                    profile.setValue(new ProfileUiState(
                            state.username, "", state.avatarUrl,
                            state.coverUrl, state.bio, state.gender,
                            state.birthDate, true, state.favGenres));
                })
                .addOnFailureListener(error -> profile.setValue(fallback));
    }

    // Chuyển dữ liệu Firestore thành trạng thái hiển thị.
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
        String bio = document.getString("bio");
        String gender = document.getString("gender");
        String birthDate = document.getString("birthDate");
        List<String> favGenres = new ArrayList<>();

        Object rawGenres = document.get("favGenres");

        if (rawGenres instanceof List<?>) {
            for (Object item : (List<?>) rawGenres) {
                if (item instanceof String) {
                    favGenres.add((String) item);
                }
            }
        }

        return new ProfileUiState(
                isBlank(username) ? fallback.username : username,
                isBlank(email) ? fallback.email : email,
                isBlank(avatarUrl) ? fallback.avatarUrl : avatarUrl,
                coverUrl == null ? "" : coverUrl,
                bio == null ? "" : bio,
                gender == null ? "" : gender,
                birthDate == null ? "" : birthDate,
                true,
                favGenres
        );
    }

    // Lưu tên, giới thiệu, giới tính và ngày sinh.
    public void updateProfileInfo(
            String username,
            String bio,
            String gender,
            String birthDate
    ) {
        String name = username == null ? "" : username.trim();
        String introduction = bio == null ? "" : bio.trim();
        String selectedGender = gender == null ? "" : gender.trim();
        String dateOfBirth = birthDate == null ? "" : birthDate.trim();

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

        repository.updateProfileInfo(
                user.getUid(),
                name,
                introduction,
                selectedGender,
                dateOfBirth
        ).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                saving.setValue(false);
                message.setValue(
                        "Lưu hồ sơ thất bại: "
                                + getErrorText(task.getException())
                );
                return;
            }

            // Đồng bộ tên hiển thị với Firebase Authentication.
            UserProfileChangeRequest request =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

            user.updateProfile(request).addOnCompleteListener(authTask -> {
                saving.setValue(false);

                if (!authTask.isSuccessful()) {
                    message.setValue(
                            "Đã lưu thông tin nhưng chưa đồng bộ tên đăng nhập."
                    );
                } else {
                    message.setValue("Đã lưu hồ sơ.");
                }

                loadCurrentProfile();
            });
        });
    }

    // Cập nhật riêng tên hiển thị.
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
                    if (!task.isSuccessful()) {
                        saving.setValue(false);
                        message.setValue(
                                "Không thể cập nhật tên: "
                                        + getErrorText(task.getException())
                        );
                        return;
                    }

                    UserProfileChangeRequest request =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                    user.updateProfile(request)
                            .addOnCompleteListener(authTask -> {
                                saving.setValue(false);
                                message.setValue(
                                        authTask.isSuccessful()
                                                ? "Đã cập nhật tên hồ sơ."
                                                : "Đã lưu tên nhưng chưa đồng bộ Firebase Auth."
                                );
                                loadCurrentProfile();
                            });
                });
    }

    // Lưu hồ sơ hiện tại bằng Repository.
    public void saveCurrentProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            message.setValue("Vui lòng đăng nhập trước.");
            return;
        }

        ProfileUiState current = profile.getValue();
        String username = current == null
                ? user.getDisplayName()
                : current.username;

        if (isBlank(username)) {
            username = user.getEmail() == null
                    ? "Người dùng"
                    : user.getEmail().split("@")[0];
        }

        saving.setValue(true);

        repository.saveProfile(user, username)
                .addOnCompleteListener(task -> {
                    saving.setValue(false);

                    if (task.isSuccessful()) {
                        message.setValue("Đã lưu hồ sơ.");
                        loadCurrentProfile();
                    } else {
                        message.setValue(
                                "Không thể lưu hồ sơ: "
                                        + getErrorText(task.getException())
                        );
                    }
                });
    }

    // Cập nhật URL ảnh đại diện.
    public void updateAvatarUrl(String avatarUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || isBlank(avatarUrl)) {
            message.setValue("Không có tài khoản hoặc URL ảnh không hợp lệ.");
            return;
        }

        repository.updateAvatarUrl(user.getUid(), avatarUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadCurrentProfile();
                    } else {
                        message.setValue(
                                "Không thể lưu ảnh đại diện: "
                                        + getErrorText(task.getException())
                        );
                    }
                });
    }

    // Cập nhật URL ảnh bìa.
    public void updateCoverUrl(String coverUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || isBlank(coverUrl)) {
            message.setValue("Không có tài khoản hoặc URL ảnh không hợp lệ.");
            return;
        }

        repository.updateCoverUrl(user.getUid(), coverUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadCurrentProfile();
                    } else {
                        message.setValue(
                                "Không thể lưu ảnh bìa: "
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

    // Tải ảnh lên Storage rồi lưu URL vào Firestore.
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

        repository.uploadProfileImage(user.getUid(), imageUri, folder)
                .addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();

                    Task<Void> saveTask;

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
                                    "Tải ảnh thành công nhưng không lưu được URL: "
                                            + getErrorText(task.getException())
                            );
                            return;
                        }

                        if ("avatars".equals(folder)) {
                            UserProfileChangeRequest request =
                                    new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(downloadUri)
                                            .build();

                            user.updateProfile(request)
                                    .addOnCompleteListener(authTask -> {
                                        finishImageUpdate(folder);
                                    });
                        } else {
                            finishImageUpdate(folder);
                        }
                    });
                })
                .addOnFailureListener(error -> {
                    saving.setValue(false);
                    message.setValue(
                            "Không thể tải ảnh lên: " + getErrorText(error)
                    );
                });
    }

    private void finishImageUpdate(String folder) {
        saving.setValue(false);
        message.setValue(
                "covers".equals(folder)
                        ? "Đã cập nhật ảnh bìa."
                        : "Đã cập nhật ảnh đại diện."
        );
        loadCurrentProfile();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String getErrorText(Exception error) {
        if (error == null || error.getMessage() == null) {
            return "Vui lòng thử lại.";
        }
        return error.getMessage();
    }

    public static class ProfileUiState {
        public final String username;
        public final String email;
        public final String avatarUrl;
        public final String coverUrl;
        public final String bio;
        public final String gender;
        public final String birthDate;
        public final boolean loggedIn;
        public final List<String> favGenres;

        public ProfileUiState(
                String username,
                String email,
                String avatarUrl,
                String coverUrl,
                String bio,
                String gender,
                String birthDate,
                boolean loggedIn,
                List<String> favGenres
        ) {
            this.username = username;
            this.email = email;
            this.avatarUrl = avatarUrl;
            this.coverUrl = coverUrl;
            this.bio = bio;
            this.gender = gender;
            this.birthDate = birthDate;
            this.loggedIn = loggedIn;
            this.favGenres = favGenres;
        }

        public static ProfileUiState guest() {
            return new ProfileUiState(
                    "Khách",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    false,
                    Collections.emptyList()
            );
        }


    }
}