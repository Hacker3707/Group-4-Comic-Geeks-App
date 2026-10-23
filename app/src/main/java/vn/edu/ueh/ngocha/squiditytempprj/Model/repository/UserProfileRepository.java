package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UserProfileRepository {

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
                                "Không thể tải ảnh lên Firebase Storage."
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
                    "Không tìm thấy tài khoản đang đăng nhập."
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
                                "Không thể cập nhật tên tài khoản."
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
                    "Không tìm thấy tài khoản đang đăng nhập."
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
                                "Không thể cập nhật tên tài khoản."
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
}