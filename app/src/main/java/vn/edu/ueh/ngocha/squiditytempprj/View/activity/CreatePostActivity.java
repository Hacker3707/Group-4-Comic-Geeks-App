package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private EditText edtPostContent;
    private Button btnPublish;
    private ImageView btnCancelPost;
    private PostViewModel postViewModel;

    // --- 1. KHAI BÁO BIẾN CHO PHẦN CHỌN ẢNH ---
    private ImageView btnAddImage;
    private ImageView imgPreview;
    private Uri selectedImageUri = null; // Biến lưu đường dẫn ảnh được chọn
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);


        edtPostContent = findViewById(R.id.edtPostContent);
        btnPublish = findViewById(R.id.btnPublish);
        btnCancelPost = findViewById(R.id.btnCancelPost);


        btnAddImage = findViewById(R.id.btnAddImage);
        imgPreview = findViewById(R.id.imgPreview);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);



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
                    }
                }
        );

        btnAddImage.setOnClickListener(v -> {
            // DÙNG
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });


        btnPublish.setOnClickListener(v -> {
            String content = edtPostContent.getText().toString().trim();

            if (content.isEmpty() && selectedImageUri == null) {
                Toast.makeText(CreatePostActivity.this, "Vui lòng nhập nội dung hoặc chọn ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            String randomPostId = UUID.randomUUID().toString();

            String imageUriString = null;
            if (selectedImageUri != null) {

                imageUriString = copyImageToAppCache(selectedImageUri);
            }


            Post newPost = new Post(randomPostId, "Meringoc_17", "C01", content, imageUriString);

            postViewModel.insertPost(newPost);

            Toast.makeText(CreatePostActivity.this, "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private String copyImageToAppCache(Uri originalUri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(originalUri);
            java.io.File cacheFile = new java.io.File(getCacheDir(), "post_image_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(cacheFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();


            return android.net.Uri.fromFile(cacheFile).toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}