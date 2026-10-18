package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.ImagePreviewAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private EditText edtPostContent, edtComicSearch;
    private MaterialButton btnPublish;
    private ImageButton btnCancelPost;
    private PostViewModel postViewModel;

    private View btnAddImage;
    private RecyclerView rvImagePreview;
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Biến phục vụ chế độ chỉnh sửa (Edit Mode)
    private boolean isEditMode = false;
    private String editPostId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        edtPostContent = findViewById(R.id.edtPostContent);
        edtComicSearch = findViewById(R.id.edtComicSearch);
        btnPublish = findViewById(R.id.btnPublish);
        btnCancelPost = findViewById(R.id.btnCancelPost);
        btnAddImage = findViewById(R.id.btnAddImage);
        rvImagePreview = findViewById(R.id.rvImagePreview);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Cài đặt RecyclerView hiển thị danh sách ảnh ngang
        rvImagePreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imagePreviewAdapter.notifyDataSetChanged();
            if (selectedImageUris.isEmpty()) {
                rvImagePreview.setVisibility(View.GONE);
            }
        });
        rvImagePreview.setAdapter(imagePreviewAdapter);

        // Kiểm tra chế độ Sửa bài viết (Edit Mode)
        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);
        editPostId = getIntent().getStringExtra("post_id");
        String oldContent = getIntent().getStringExtra("post_content");

        // Đọc thông tin hashtag (comicId) và danh sách ảnh cũ được truyền sang từ Intent
        String oldComicId = getIntent().getStringExtra("post_comic_id");
        ArrayList<String> oldImageUrls = getIntent().getStringArrayListExtra("post_image_urls");

        if (isEditMode) {
            edtPostContent.setText(oldContent);
            if (oldComicId != null && !oldComicId.isEmpty()) {
                edtComicSearch.setText(oldComicId);
            }
            btnPublish.setText("Cập nhật bài viết");

            // Nạp lại danh sách ảnh cũ lên thanh xem trước
            if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
                for (String url : oldImageUrls) {
                    selectedImageUris.add(Uri.parse(url));
                }
                rvImagePreview.setVisibility(View.VISIBLE);
                imagePreviewAdapter.notifyDataSetChanged();
            }
        }

        // Cài đặt bộ chọn NHIỀU ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                try {
                                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                } catch (Exception ignored) {}
                                selectedImageUris.add(imageUri);
                            }
                        } else if (result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {
                                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {}
                            selectedImageUris.add(imageUri);
                        }

                        if (!selectedImageUris.isEmpty()) {
                            rvImagePreview.setVisibility(View.VISIBLE);
                            imagePreviewAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Đã chọn " + selectedImageUris.size() + " ảnh!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều ảnh cùng lúc
            imagePickerLauncher.launch(intent);
        });

        btnCancelPost.setOnClickListener(v -> finish());

        // Sự kiện Đăng / Cập nhật bài viết
        btnPublish.setOnClickListener(v -> {
            try {
                String content = edtPostContent.getText().toString().trim();
                String comicNameInput = edtComicSearch.getText().toString().trim();
                String comicIdToSave = comicNameInput.isEmpty() ? "C01" : comicNameInput;

                if (content.isEmpty() && selectedImageUris.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn ít nhất 1 ảnh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);

                // --- TRƯỜNG HỢP 1: ĐANG Ở CHẾ ĐỘ CHỈNH SỬA ---
                if (isEditMode && editPostId != null) {
                    progressDialog.setMessage("Đang cập nhật bài viết...");
                    progressDialog.show();

                    // Tách riêng ảnh cũ (http) và ảnh mới chọn từ thiết bị
                    List<String> finalImageUrls = new ArrayList<>();
                    List<Uri> newUrisToUpload = new ArrayList<>();

                    for (Uri uri : selectedImageUris) {
                        String uriStr = uri.toString();
                        if (uriStr.startsWith("http://") || uriStr.startsWith("https://")) {
                            finalImageUrls.add(uriStr); // Giữ lại ảnh cũ
                        } else {
                            newUrisToUpload.add(uri); // Ảnh mới cần upload
                        }
                    }

                    // Nếu người dùng có chọn thêm ảnh mới
                    if (!newUrisToUpload.isEmpty()) {
                        List<Uri> tempUris = selectedImageUris;
                        selectedImageUris = newUrisToUpload;

                        uploadMultipleImagesToStorage(editPostId, uploadedNewUrls -> {
                            finalImageUrls.addAll(uploadedNewUrls); // Gộp ảnh cũ và ảnh mới upload xong
                            updatePostOnCloud(editPostId, content, comicIdToSave, finalImageUrls, progressDialog);
                        }, progressDialog);
                    } else {
                        // Không chọn ảnh mới, chỉ cập nhật nội dung, hashtag và danh sách ảnh cũ đang có
                        updatePostOnCloud(editPostId, content, comicIdToSave, finalImageUrls, progressDialog);
                    }
                }
                // --- TRƯỜNG HỢP 2: TẠO MỚI BÀI VIẾT ---
                else {
                    String newPostId = UUID.randomUUID().toString();

                    if (!selectedImageUris.isEmpty()) {
                        progressDialog.setMessage("Đang tải ảnh lên mây...");
                        progressDialog.show();

                        uploadMultipleImagesToStorage(newPostId, imageUrls -> {
                            Post newPost = new Post(newPostId, "Meringoc_17", comicIdToSave, content, imageUrls);
                            uploadPostToCloud(newPost, progressDialog);
                        }, progressDialog);
                    } else {
                        progressDialog.setMessage("Đang đăng bài viết...");
                        progressDialog.show();

                        Post newPost = new Post(newPostId, "Meringoc_17", comicIdToSave, content, null);
                        uploadPostToCloud(newPost, progressDialog);
                    }
                }

            } catch (Exception e) {
                Toast.makeText(this, "Lỗi ngoại lệ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    // Hàm vòng lặp bất đồng bộ upload nhiều ảnh lên Firebase Storage
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

    private void updatePostOnCloud(String postId, String content, String comicId, List<String> imageUrls, ProgressDialog progressDialog) {
        // 👉 Tạo một đối tượng Post mới chứa đầy đủ thông tin vừa sửa
        Post updatedPost = new Post(postId, "Meringoc_17", comicId, content, imageUrls);

        // 👉 Gọi thông qua PostViewModel để nó cập nhật cả Firebase lẫn Room Database cục bộ
        if (postViewModel != null) {
            postViewModel.updatePost(updatedPost);

            // Đóng dialog và kết thúc Activity ngay sau khi gửi lệnh cập nhật
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
}