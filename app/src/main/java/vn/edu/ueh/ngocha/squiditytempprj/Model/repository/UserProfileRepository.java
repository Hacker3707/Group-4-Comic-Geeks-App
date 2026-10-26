package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class UserProfileRepository {

    public ListenerRegistration listenProfileDetails(
            String uid,
            EventListener<DocumentSnapshot> listener
    ) {
        return firestore.collection("users")
                .document(uid)
                .addSnapshotListener(listener);
    }

    private final FirebaseStorage storage =
            FirebaseStorage.getInstance();

    private final FirebaseFirestore firestore =
            FirebaseFirestore.getInstance();

    // Lấy danh sách tài khoản để tìm kiếm theo tên (Firestore không hỗ trợ tìm "chứa" nên lọc ở máy)
    public Task<QuerySnapshot> getAllUsers() {
        return firestore.collection("users")
                .limit(300)
                .get();
    }

    public Task<DocumentSnapshot> getProfile(String uid) {
        return firestore.collection("users")
                .document(uid)
                .get();
    }

    public Task<Void> saveProfile(
            FirebaseUser user,
            String username
    ) {
        Map<String, Object> data = new HashMap<>();

        data.put("userId", user.getUid());
        data.put("username", username == null ? "" : username);
        data.put("email", user.getEmail() == null
                ? "" : user.getEmail());
        data.put("avatarUrl", user.getPhotoUrl() == null
                ? "" : user.getPhotoUrl().toString());
        data.put("role", "USER");
        data.put("verified", user.isEmailVerified());

        return firestore.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge());
    }

    public Task<Uri> uploadProfileImage(
            String uid,
            Uri imageUri,
            String folder
    ) {
        String fileName = java.util.UUID.randomUUID() + ".jpg";

        StorageReference imageRef = storage.getReference()
                .child("users")
                .child(uid)
                .child(folder)
                .child(fileName);

        return imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();

                        if (error != null) {
                            throw error;
                        }

                        throw new IllegalStateException(
                                Str.t(R.string.profile_err_storage_upload)
                        );
                    }

                    return imageRef.getDownloadUrl();
                });
    }

    public Task<Void> updateAvatarUrl(
            String uid,
            String avatarUrl
    ) {
        return firestore.collection("users")
                .document(uid)
                .set(
                        java.util.Collections.singletonMap(
                                "avatarUrl", avatarUrl
                        ),
                        SetOptions.merge()
                );
    }

    public Task<Void> updateCoverUrl(
            String uid,
            String coverUrl
    ) {
        return firestore.collection("users")
                .document(uid)
                .set(
                        java.util.Collections.singletonMap(
                                "coverUrl", coverUrl
                        ),
                        SetOptions.merge()
                );
    }

    public Task<Void> updateUsername(
            String uid,
            String username
    ) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || !uid.equals(user.getUid())) {
            throw new IllegalStateException(
                    Str.t(R.string.profile_err_no_account)
            );
        }

        UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();

        return user.updateProfile(request)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();

                        if (error != null) {
                            throw error;
                        }

                        throw new IllegalStateException(
                                Str.t(R.string.profile_err_update_name)
                        );
                    }

                    return firestore.collection("users")
                            .document(uid)
                            .set(
                                    java.util.Collections.singletonMap(
                                            "username", username
                                    ),
                                    SetOptions.merge()
                            );
                });
    }
    public Task<Void> updateProfileInfo(
            String uid,
            String username,
            String bio,
            String gender,
            String birthDate
    ) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || !uid.equals(user.getUid())) {
            throw new IllegalStateException(
                    Str.t(R.string.profile_err_no_account)
            );
        }

        UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();

        return user.updateProfile(request)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();
                        if (error != null) throw error;

                        throw new IllegalStateException(
                                Str.t(R.string.profile_err_update_name)
                        );
                    }

                    java.util.Map<String, Object> data =
                            new java.util.HashMap<>();

                    data.put("username", username);
                    data.put("bio", bio);
                    data.put("gender", gender);
                    data.put("birthDate", birthDate);
                    data.put("email",
                            user.getEmail() == null ? "" : user.getEmail());

                    return firestore.collection("users")
                            .document(uid)
                            .set(data, SetOptions.merge());
                });
    }


    /**
     * Lấy thông tin một người dùng theo UID Firebase.
     * Không thay đổi profile state đang được các màn hình khác sử dụng.
     */
    public LiveData<User> getUserById(String uid) {
        MutableLiveData<User> result = new MutableLiveData<>();

        if (uid == null || uid.trim().isEmpty()) {
            result.setValue(null);
            return result;
        }

        final String userId = uid.trim();

        getProfile(userId)
                .addOnSuccessListener(document -> {
                    if (document == null || !document.exists()) {
                        result.setValue(null);
                        return;
                    }

                    User user = document.toObject(User.class);

                    if (user != null) {
                        // Luôn lấy UID từ ID document Firestore.
                        user.setUserId(document.getId());

                        // Không để màn hình khác vô tình hiển thị email
                        // của người dùng được tra cứu.
                        user.setEmail(null);

                        result.setValue(user);
                    } else {
                        result.setValue(null);
                    }
                })
                .addOnFailureListener(error -> {
                    result.setValue(null);
                });

        return result;
    }

    public Task<Void> updateProfileDetails(
            String uid,
            String username,
            String bio,
            String introduction,
            String gender,
            String birthDate,
            List<String> favGenres
    ) {
        Map<String, Object> data = new HashMap<>();

        data.put("username", username);
        data.put("bio", bio);
        data.put("introduction", introduction);
        data.put("gender", gender);
        data.put("birthDate", birthDate);
        data.put("favGenres", favGenres);

        return firestore.collection("users")
                .document(uid)
                .set(data, SetOptions.merge());
    }
}