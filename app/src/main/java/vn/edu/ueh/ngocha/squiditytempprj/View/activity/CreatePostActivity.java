package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.PostRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private EditText edtPostContent, edtComicSearch;
    private MaterialButton btnPublish;
    private ImageButton btnCancelPost;
    private PostViewModel postViewModel;

    private View btnAddImage;
    private ImageView imgPreview;
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        edtPostContent = findViewById(R.id.edtPostContent);
        edtComicSearch = findViewById(R.id.edtComicSearch);
        btnPublish = findViewById(R.id.btnPublish);
        btnCancelPost = findViewById(R.id.btnCancelPost);
        btnAddImage = findViewById(R.id.btnAddImage);
        imgPreview = findViewById(R.id.imgPreview);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Cài đặt bộ chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        imgPreview.setImageURI(selectedImageUri);
                        imgPreview.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });


        btnCancelPost.setOnClickListener(v -> finish());

        btnPublish.setOnClickListener(v -> {
            try {
                String content = edtPostContent.getText().toString().trim();
                String comicNameInput = edtComicSearch.getText().toString().trim();

                // Bạn có thể dùng comicNameInput làm comicId hoặc kết nối id từ API nhóm trưởng tại đây
                String comicIdToSave = comicNameInput.isEmpty() ? "C01" : comicNameInput;

                if (content.isEmpty() && selectedImageUri == null) {
                    Toast.makeText(CreatePostActivity.this, "Vui lòng nhập nội dung hoặc chọn ảnh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String randomPostId = UUID.randomUUID().toString();
                ProgressDialog progressDialog = new ProgressDialog(CreatePostActivity.this);
                progressDialog.setCancelable(false);

                // TRƯỜNG HỢP 1: CÓ CHỌN ẢNH -> Tải ảnh lên Firebase Storage trước
                if (selectedImageUri != null) {
                    progressDialog.setMessage("Đang tải ảnh lên mây...");
                    progressDialog.show();

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                            .child("post_images/" + randomPostId + ".jpg");

                    storageRef.putFile(selectedImageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();

                                    Post newPost = new Post(randomPostId, "Meringoc_17", comicIdToSave, content, imageUrl);
                                    uploadPostToCloud(newPost, progressDialog);
                                });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                } else {
                    // TRƯỜNG HỢP 2: KHÔNG CÓ ẢNH (Chỉ có chữ & tên truyện)
                    progressDialog.setMessage("Đang đăng bài viết...");
                    progressDialog.show();

                    Post newPost = new Post(randomPostId, "Meringoc_17", comicIdToSave, content, null);
                    uploadPostToCloud(newPost, progressDialog);
                }

            } catch (Exception e) {
                Toast.makeText(CreatePostActivity.this, "Lỗi ngoại lệ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
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
}