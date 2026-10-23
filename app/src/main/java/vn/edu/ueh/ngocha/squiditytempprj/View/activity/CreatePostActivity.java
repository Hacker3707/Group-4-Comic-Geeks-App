package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ImagePreviewAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText edtPostContent;
    private EditText edtPostHashtag;
    private MaterialAutoCompleteTextView edtComicSearch;
    private MaterialButton btnPublish;
    private ImageButton btnCancelPost;
    private PostViewModel postViewModel;
    private String currentUserId = "";
    private String currentUsername = "Khách";
    private String currentAvatarUrl = "";

    private View btnAddImage;
    private RecyclerView rvImagePreview;
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<
            androidx.activity.result.PickVisualMediaRequest
            > imagePickerLauncher;

    private final String[] selectedComicInfo = {"Discussion", ""}; // [0]: comicId, [1]: comicTitle

    private boolean isEditMode = false;
    // Có giá trị khi đăng bài từ trang group (bài sẽ thuộc group này)
    private String groupId = null;
    private String editPostId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        loadCurrentUserProfile();

        edtPostContent = findViewById(R.id.edtPostContent);
        edtPostHashtag = findViewById(R.id.edtPostHashtag);
        edtComicSearch = findViewById(R.id.edtComicSearch);
        btnPublish = findViewById(R.id.btnPublish);
        btnCancelPost = findViewById(R.id.btnCancelPost);
        btnAddImage = findViewById(R.id.btnAddImage);
        rvImagePreview = findViewById(R.id.rvImagePreview);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        rvImagePreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imagePreviewAdapter.notifyDataSetChanged();
            if (selectedImageUris.isEmpty()) {
                rvImagePreview.setVisibility(View.GONE);
            }
        });
        rvImagePreview.setAdapter(imagePreviewAdapter);

        setupComicAutoComplete();

        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);
        groupId = getIntent().getStringExtra("group_id");
        if (groupId != null && !groupId.isEmpty() && !isEditMode) {
            edtPostHashtag.setHint("Chủ đề: #Anime, #Manga, #LightNovel, #Discussion, #Otaku");
        }
        editPostId = getIntent().getStringExtra("post_id");
        String oldContent = getIntent().getStringExtra("post_content");
        String oldComicId = getIntent().getStringExtra("post_comic_id");
        String oldComicTitle = getIntent().getStringExtra("post_comic_title");
        String oldHashtag = getIntent().getStringExtra("post_hashtag");
        ArrayList<String> oldImageUrls = getIntent().getStringArrayListExtra("post_image_urls");

        if (isEditMode) {
            edtPostContent.setText(oldContent);
            if (oldComicId != null) selectedComicInfo[0] = oldComicId;
            if (oldComicTitle != null) {
                selectedComicInfo[1] = oldComicTitle;
                edtComicSearch.setText(oldComicTitle);
            }
            if (oldHashtag != null) {
                edtPostHashtag.setText(oldHashtag);
            }
            btnPublish.setText("Cập nhật bài viết");

            if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
                for (String url : oldImageUrls) {
                    selectedImageUris.add(Uri.parse(url));
                }
                rvImagePreview.setVisibility(View.VISIBLE);
                imagePreviewAdapter.notifyDataSetChanged();
            }
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(10),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImageUris.addAll(uris);

                        rvImagePreview.setVisibility(View.VISIBLE);
                        imagePreviewAdapter.notifyDataSetChanged();

                        Toast.makeText(
                                this,
                                "Đã chọn " + selectedImageUris.size() + " ảnh!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        btnAddImage.setOnClickListener(v -> {
            imagePickerLauncher.launch(
                    new androidx.activity.result.PickVisualMediaRequest.Builder()
                            .setMediaType(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE
                            )
                            .build()
            );
        });

        btnCancelPost.setOnClickListener(v -> finish());

        btnPublish.setOnClickListener(v -> {
            try {
                String content = edtPostContent.getText().toString().trim();
                String hashtag = edtPostHashtag.getText().toString().trim();
                if (!hashtag.isEmpty() && !hashtag.startsWith("#")) {
                    hashtag = "#" + hashtag;
                }

                if (content.isEmpty() && selectedImageUris.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn ít nhất 1 ảnh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);

                String currentComicId = selectedComicInfo[0];
                String currentComicTitle = selectedComicInfo[1];
                String finalHashtag = hashtag;
                // Bài trong group lưu hashtag ngay trong nội dung để lọc theo chủ đề
                String finalContent = content;
                if (groupId != null && !groupId.isEmpty() && !isEditMode
                        && !hashtag.isEmpty() && !content.contains(hashtag)) {
                    finalContent = content.isEmpty() ? hashtag : content + " " + hashtag;
                }
                final String postContent = finalContent;

                if (isEditMode && editPostId != null) {
                    progressDialog.setMessage("Đang cập nhật bài viết...");
                    progressDialog.show();

                    List<String> finalImageUrls = new ArrayList<>();
                    List<Uri> newUrisToUpload = new ArrayList<>();

                    for (Uri uri : selectedImageUris) {
                        String uriStr = uri.toString();
                        if (uriStr.startsWith("http://") || uriStr.startsWith("https://")) {
                            finalImageUrls.add(uriStr);
                        } else {
                            newUrisToUpload.add(uri);
                        }
                    }

                    if (!newUrisToUpload.isEmpty()) {
                        selectedImageUris = newUrisToUpload;
                        uploadMultipleImagesToStorage(editPostId, uploadedNewUrls -> {
                            finalImageUrls.addAll(uploadedNewUrls);
                            updatePostOnCloud(editPostId, content, currentComicId, currentComicTitle, finalHashtag, finalImageUrls, progressDialog);
                        }, progressDialog);
                    } else {
                        updatePostOnCloud(editPostId, content, currentComicId, currentComicTitle, finalHashtag, finalImageUrls, progressDialog);
                    }
                } else {
                    String newPostId = UUID.randomUUID().toString();

                    if (!selectedImageUris.isEmpty()) {
                        progressDialog.setMessage("Đang tải ảnh lên mây...");
                        progressDialog.show();

                        uploadMultipleImagesToStorage(newPostId, imageUrls -> {
                            Post newPost = new Post(newPostId, currentUid(), currentComicId, postContent, imageUrls);
                            if (groupId != null && !groupId.isEmpty()) newPost.setGroupId(groupId);
                            newPost.setComicTitle(currentComicTitle);
                            newPost.setHashtag(finalHashtag);
                            uploadPostToCloud(newPost, progressDialog);
                        }, progressDialog);
                    } else {
                        progressDialog.setMessage("Đang đăng bài viết...");
                        progressDialog.show();

                        Post newPost = new Post(newPostId, currentUid(), currentComicId, postContent, null);
                        if (groupId != null && !groupId.isEmpty()) newPost.setGroupId(groupId);
                        newPost.setComicTitle(currentComicTitle);
                        newPost.setHashtag(finalHashtag);
                        uploadPostToCloud(newPost, progressDialog);
                    }
                }

            } catch (Exception e) {
                Toast.makeText(this, "Lỗi ngoại lệ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private void setupComicAutoComplete() {
        if (edtComicSearch == null) return;

        Map<String, String> titleToIdMap = new HashMap<>();

        edtComicSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchMangaFromApi(query, titleToIdMap);
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void searchMangaFromApi(String query, Map<String, String> titleToIdMap) {
        MangaDexApi api = RetrofitClient.getInstance().create(MangaDexApi.class);
        api.searchManga(query, 5, 0, "cover_art")
                .enqueue(new Callback<MangaResponse>() {
                    @Override
                    public void onResponse(Call<MangaResponse> call, Response<MangaResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<MangaData> mangaList = response.body().getData();
                            List<String> titles = new ArrayList<>();
                            titleToIdMap.clear();

                            for (MangaData manga : mangaList) {
                                if (manga.getAttributes() != null && manga.getAttributes().getTitle() != null) {
                                    String title = manga.getAttributes().getTitle().get("en");
                                    if (title == null && !manga.getAttributes().getTitle().isEmpty()) {
                                        title = manga.getAttributes().getTitle().values().iterator().next();
                                    }
                                    if (title != null) {
                                        titles.add(title);
                                        titleToIdMap.put(title, manga.getId());
                                    }
                                }
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(CreatePostActivity.this, android.R.layout.simple_dropdown_item_1line, titles);
                            edtComicSearch.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            edtComicSearch.showDropDown();

                            edtComicSearch.setOnItemClickListener((parent, view, position, id) -> {
                                String selectedTitle = adapter.getItem(position);
                                if (selectedTitle != null && titleToIdMap.containsKey(selectedTitle)) {
                                    selectedComicInfo[0] = titleToIdMap.get(selectedTitle); // Lưu ID chuẩn
                                    selectedComicInfo[1] = selectedTitle;               // Lưu Tên hiển thị
                                    Toast.makeText(CreatePostActivity.this, "Đã liên kết truyện: " + selectedTitle, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<MangaResponse> call, Throwable t) {}
                });
    }

    private interface OnMultiImageUploadListener {
        void onComplete(List<String> downloadUrls);
    }

    private void uploadMultipleImagesToStorage(String postId, OnMultiImageUploadListener listener, ProgressDialog progressDialog) {
        List<String> uploadedUrls = new ArrayList<>();
        int totalImages = selectedImageUris.size();
        final int[] uploadedCount = {0};

        for (int i = 0; i < totalImages; i++) {
            Uri uri = selectedImageUris.get(i);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("post_images/" + postId + "_" + System.currentTimeMillis() + "_" + i + ".jpg");

            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        uploadedUrls.add(downloadUri.toString());
                        uploadedCount[0]++;

                        if (uploadedCount[0] == totalImages) {
                            listener.onComplete(uploadedUrls);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadPostToCloud(Post post, ProgressDialog progressDialog) {
        if (postViewModel != null) {
            postViewModel.uploadPostToFirestore(post, new PostRepository.OnFirestoreUploadListener() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "Lỗi Firestore: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            progressDialog.dismiss();
        }
    }

    // Id của tài khoản đang đăng nhập, chưa đăng nhập thì dùng "guest"
    private String currentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return "guest";
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void updatePostOnCloud(String postId, String content, String comicId, String comicTitle, String hashtag, List<String> imageUrls, ProgressDialog progressDialog) {
        Post updatedPost = new Post(postId, currentUid(), comicId, content, imageUrls);
        updatedPost.setComicTitle(comicTitle);
        updatedPost.setHashtag(hashtag);
        if (groupId != null && !groupId.isEmpty()) updatedPost.setGroupId(groupId);

        if (postViewModel != null) {
            postViewModel.updatePost(updatedPost);
            progressDialog.dismiss();
            Toast.makeText(CreatePostActivity.this, "Cập nhật bài viết thành công!", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            progressDialog.dismiss();
            Toast.makeText(CreatePostActivity.this, "Lỗi: PostViewModel bị null!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentUserProfile() {
        ShapeableImageView imgAvatar =
                findViewById(R.id.imgvAvatar);
        TextView tvUsername =
                findViewById(R.id.tvMainUsername);

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            currentUserId = "";
            currentUsername = "Khách";

            if (tvUsername != null) {
                tvUsername.setText(currentUsername);
            }
            return;
        }

        currentUserId = user.getUid();

        // Hiển thị tên tạm từ Firebase Authentication
        String name = user.getDisplayName();

        if (name == null || name.trim().isEmpty()) {
            name = user.getEmail();
        }

        if (name != null && !name.trim().isEmpty()) {
            currentUsername = name.trim();
        }

        if (tvUsername != null) {
            tvUsername.setText(currentUsername);
        }

        // Hiển thị avatar từ Firebase Authentication
        if (imgAvatar != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgAvatar);
        }

        // Đồng bộ theo hồ sơ đã lưu trên Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .addSnapshotListener(this, (document, error) -> {
                    if (isFinishing() || isDestroyed()
                            || error != null
                            || document == null
                            || !document.exists()) {
                        return;
                    }

                    String username =
                            document.getString("username");

                    if (username == null || username.trim().isEmpty()) {
                        username = document.getString("displayName");
                    }

                    if (username != null && !username.trim().isEmpty()) {
                        currentUsername = username.trim();

                        if (tvUsername != null) {
                            tvUsername.setText(currentUsername);
                        }
                    }

                    String avatarUrl =
                            document.getString("avatarUrl");

                    if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                        avatarUrl = document.getString("photoUrl");
                    }

                    if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                        currentAvatarUrl = avatarUrl.trim();

                        if (imgAvatar != null) {
                            Glide.with(CreatePostActivity.this)
                                    .load(currentAvatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(imgAvatar);
                        }
                    }
                });
    }
}