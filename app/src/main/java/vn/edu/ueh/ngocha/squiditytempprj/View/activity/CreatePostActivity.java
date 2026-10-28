package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ImagePreviewAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.ComicViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText edtPostContent;
    private EditText edtPostHashtag;
    private MaterialAutoCompleteTextView edtComicSearch;
    private MaterialButton btnPublish;
    private ImageButton btnCancelPost;
    private ComicViewModel comicViewModel;
    private ArrayAdapter<String> mangaAdapter;

    private final Map<String, String> titleToIdMap = new HashMap<>();
    private final String[] selectedComicInfo = new String[2];
    private PostViewModel postViewModel;
    private String currentUserId = "";
    private String currentUsername = Str.t(R.string.common_guest);
    private String currentAvatarUrl = "";

    private View btnAddImage;
    private RecyclerView rvImagePreview;
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<
            androidx.activity.result.PickVisualMediaRequest
            > imagePickerLauncher;

    private boolean isEditMode = false;
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
        comicViewModel = new ViewModelProvider(this)
                .get(ComicViewModel.class);

        mangaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        edtComicSearch.setAdapter(mangaAdapter);

        comicViewModel.getSearchResults().observe(this, mangaList -> {
            if (mangaList == null) return;

            List<String> titles = new ArrayList<>();
            Map<String, String> newTitleToIdMap = new HashMap<>();

            for (MangaData manga : mangaList) {
                if (manga == null
                        || manga.getId() == null
                        || manga.getAttributes() == null
                        || manga.getAttributes().getTitle() == null) {
                    continue;
                }

                Map<String, String> titleMap =
                        manga.getAttributes().getTitle();

                String title = titleMap.get("en");

                if (title == null || title.trim().isEmpty()) {
                    if (!titleMap.isEmpty()) {
                        title = titleMap.values().iterator().next();
                    }
                }

                if (title != null && !title.trim().isEmpty()) {
                    titles.add(title);
                    newTitleToIdMap.put(title, manga.getId());
                }
            }

            titleToIdMap.clear();
            titleToIdMap.putAll(newTitleToIdMap);

            mangaAdapter.clear();
            mangaAdapter.addAll(titles);
            mangaAdapter.notifyDataSetChanged();

            if (!titles.isEmpty() && edtComicSearch.hasFocus()) {
                edtComicSearch.showDropDown();
            }
        });

        edtComicSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = mangaAdapter.getItem(position);

            if (selectedTitle != null
                    && titleToIdMap.containsKey(selectedTitle)) {
                selectedComicInfo[0] = titleToIdMap.get(selectedTitle);
                selectedComicInfo[1] = selectedTitle;

                Toast.makeText(
                        CreatePostActivity.this,
                        Str.t(R.string.post_comic_linked, selectedTitle),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

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
            edtPostHashtag.setHint(Str.t(R.string.post_group_hashtag_hint));
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
            btnPublish.setText(Str.t(R.string.post_update_btn));

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
                                Str.t(R.string.post_images_selected, selectedImageUris.size()),
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
                    Toast.makeText(this, Str.t(R.string.post_err_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);

                String currentComicId = selectedComicInfo[0];
                String currentComicTitle = selectedComicInfo[1];
                String finalHashtag = hashtag;
                String finalContent = content;
                if (groupId != null && !groupId.isEmpty() && !isEditMode
                        && !hashtag.isEmpty() && !content.contains(hashtag)) {
                    finalContent = content.isEmpty() ? hashtag : content + " " + hashtag;
                }
                final String postContent = finalContent;

                if (isEditMode && editPostId != null) {
                    progressDialog.setMessage(Str.t(R.string.post_updating));
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
                        progressDialog.setMessage(Str.t(R.string.post_uploading_images));
                        progressDialog.show();

                        uploadMultipleImagesToStorage(newPostId, imageUrls -> {
                            Post newPost = new Post(newPostId, currentUid(), currentComicId, postContent, imageUrls);
                            if (groupId != null && !groupId.isEmpty()) newPost.setGroupId(groupId);
                            newPost.setComicTitle(currentComicTitle);
                            newPost.setHashtag(finalHashtag);
                            uploadPostToCloud(newPost, progressDialog);
                        }, progressDialog);
                    } else {
                        progressDialog.setMessage(Str.t(R.string.post_publishing));
                        progressDialog.show();

                        Post newPost = new Post(newPostId, currentUid(), currentComicId, postContent, null);
                        if (groupId != null && !groupId.isEmpty()) newPost.setGroupId(groupId);
                        newPost.setComicTitle(currentComicTitle);
                        newPost.setHashtag(finalHashtag);
                        uploadPostToCloud(newPost, progressDialog);
                    }
                }

            } catch (Exception e) {
                Toast.makeText(this, Str.t(R.string.post_err_exception, e.getMessage()), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    // =========================================================================
    // 👉 BỔ SUNG: Lưu và khôi phục nội dung khi xoay ngang màn hình (Landscape)
    // =========================================================================
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (edtPostContent != null) {
            outState.putString("draft_content", edtPostContent.getText().toString());
        }
        if (edtPostHashtag != null) {
            outState.putString("draft_hashtag", edtPostHashtag.getText().toString());
        }
        if (edtComicSearch != null) {
            outState.putString("draft_comic", edtComicSearch.getText().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String draftContent = savedInstanceState.getString("draft_content");
        if (draftContent != null && edtPostContent != null) {
            edtPostContent.setText(draftContent);
        }
        String draftHashtag = savedInstanceState.getString("draft_hashtag");
        if (draftHashtag != null && edtPostHashtag != null) {
            edtPostHashtag.setText(draftHashtag);
        }
        String draftComic = savedInstanceState.getString("draft_comic");
        if (draftComic != null && edtComicSearch != null) {
            edtComicSearch.setText(draftComic);
        }
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

    private void searchMangaFromApi(
            String query,
            Map<String, String> titleToIdMap
    ) {
        List<String> includes = new ArrayList<>();
        includes.add("cover_art");
        if (query == null || query.trim().isEmpty()) {
            comicViewModel.searchManga("", 5, 0, includes);
            return;
        }

        comicViewModel.searchManga(
                query.trim(),
                5,
                0,
                includes
        );
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
                        Toast.makeText(this, Str.t(R.string.post_err_image, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadPostToCloud(Post post, ProgressDialog progressDialog) {
        if (postViewModel != null) {
            postViewModel.uploadPostToFirestore(post, new PostRepository.OnFirestoreUploadListener() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, Str.t(R.string.post_published), Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, Str.t(R.string.post_err_firestore, error), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            progressDialog.dismiss();
        }
    }

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
            Toast.makeText(CreatePostActivity.this, Str.t(R.string.post_updated), Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            progressDialog.dismiss();
            Toast.makeText(CreatePostActivity.this, Str.t(R.string.post_err_viewmodel), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentUserProfile() {
        ShapeableImageView imgAvatar =
                findViewById(R.id.imgMainAvatar);
        TextView tvUsername =
                findViewById(R.id.tvMainUsername);

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            currentUserId = "";
            currentUsername = Str.t(R.string.common_guest);

            if (tvUsername != null) {
                tvUsername.setText(currentUsername);
            }
            return;
        }

        currentUserId = user.getUid();

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

        if (imgAvatar != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgAvatar);
        }

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