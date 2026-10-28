package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;

/**
 * Màn hình admin xem chi tiết một bài viết: đầy đủ nội dung, hình ảnh và bình luận
 * như người dùng thường thấy, sau đó quyết định xoá bài viết hay không.
 */
public class AdminPostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private FirebaseFirestore db;
    private String postId;

    private ImageView imgAuthor;
    private TextView tvAuthor;
    private TextView tvMeta;
    private TextView tvContent;
    private TextView tvTags;
    private LinearLayout imagesContainer;
    private TextView tvCommentsTitle;
    private LinearLayout commentsContainer;
    private View btnDelete;

    // Tên người dùng đã tải, tránh tải lại nhiều lần
    private final Map<String, String> nameCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_post_detail);

        db = FirebaseFirestore.getInstance();
        postId = getIntent().getStringExtra(EXTRA_POST_ID);

        imgAuthor = findViewById(R.id.imgPostAuthor);
        tvAuthor = findViewById(R.id.tvPostAuthor);
        tvMeta = findViewById(R.id.tvPostMeta);
        tvContent = findViewById(R.id.tvPostContent);
        tvTags = findViewById(R.id.tvPostTags);
        imagesContainer = findViewById(R.id.postImagesContainer);
        tvCommentsTitle = findViewById(R.id.tvPostCommentsTitle);
        commentsContainer = findViewById(R.id.postCommentsContainer);
        btnDelete = findViewById(R.id.btnAdminDeletePost);

        findViewById(R.id.imgPostDetailBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || postId == null || postId.isEmpty()) {
            finish();
            return;
        }
        checkAdminThenLoad(me.getUid());
    }

    // Chỉ tài khoản có role ADMIN mới xem được màn hình này
    private void checkAdminThenLoad(String myUid) {
        db.collection("users").document(myUid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc != null && AdminActivity.ROLE_ADMIN.equals(doc.getString("role"))) {
                            loadPost();
                        } else {
                            Toast.makeText(AdminPostDetailActivity.this,
                                    Str.t(R.string.admin_only_page), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminPostDetailActivity.this,
                                Str.t(R.string.admin_check_failed), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void loadPost() {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc == null || !doc.exists()) {
                            Toast.makeText(AdminPostDetailActivity.this,
                                    Str.t(R.string.admin_post_not_found), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        showPost(doc);
                        loadComments();
                        btnDelete.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminPostDetailActivity.this,
                                Str.t(R.string.admin_delete_failed, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showPost(DocumentSnapshot doc) {
        String content = doc.getString("content");
        if (content == null || content.trim().isEmpty()) {
            tvContent.setText(Str.t(R.string.admin_image_only_post));
        } else {
            tvContent.setText(content);
        }

        // Hashtag và truyện được gắn
        StringBuilder tags = new StringBuilder();
        String hashtag = doc.getString("hashtag");
        if (hashtag != null && !hashtag.trim().isEmpty()) {
            tags.append(hashtag.trim());
        }
        String comicTitle = doc.getString("comicTitle");
        if (comicTitle != null && !comicTitle.trim().isEmpty()) {
            if (tags.length() > 0) {
                tags.append("  •  ");
            }
            tags.append(comicTitle.trim());
        }
        if (tags.length() > 0) {
            tvTags.setText(tags.toString());
            tvTags.setVisibility(View.VISIBLE);
        } else {
            tvTags.setVisibility(View.GONE);
        }

        // Thời gian + nhóm (nếu bài đăng trong nhóm)
        final String time = formatTime(doc.getDate("timestamp"));
        tvMeta.setText(time);
        String groupId = doc.getString("groupId");
        if (groupId != null && !groupId.isEmpty()) {
            db.collection("groups").document(groupId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot g) {
                            String groupName = g == null ? null : g.getString("groupName");
                            if (groupName != null && !groupName.isEmpty()) {
                                tvMeta.setText(time + "  •  " + groupName);
                            }
                        }
                    });
        }

        showAuthor(doc.getString("userId"));
        showImages(doc.get("imageUris"));
    }

    private void showAuthor(String userId) {
        tvAuthor.setText(Str.t(R.string.common_user));
        if (userId == null || userId.isEmpty()) {
            return;
        }
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot u) {
                        if (u == null || !u.exists()) {
                            return;
                        }
                        String name = u.getString("username");
                        if (name != null && !name.trim().isEmpty()) {
                            tvAuthor.setText(name.trim());
                        }
                        String avatar = u.getString("avatarUrl");
                        if (avatar != null && !avatar.trim().isEmpty()) {
                            Glide.with(AdminPostDetailActivity.this)
                                    .load(avatar.trim())
                                    .centerCrop()
                                    .into(imgAuthor);
                        }
                    }
                });
    }

    // Hiện toàn bộ ảnh của bài viết, bấm vào để phóng to
    private void showImages(Object imageUris) {
        imagesContainer.removeAllViews();
        if (!(imageUris instanceof List)) {
            return;
        }
        List<?> list = (List<?>) imageUris;
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item == null) {
                continue;
            }
            final String url = item.toString().trim();
            if (url.isEmpty()) {
                continue;
            }
            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = (int) (8 * density);
            image.setLayoutParams(params);
            image.setAdjustViewBounds(true);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setBackgroundColor(0xFFEEEEEE);
            image.setMinimumHeight((int) (120 * density));
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminPostDetailActivity.this, ZoomImageActivity.class);
                    intent.putExtra("IMAGE_URL", url);
                    startActivity(intent);
                }
            });
            imagesContainer.addView(image);
            Glide.with(this).load(url).into(image);
        }
    }

    private void loadComments() {
        db.collection("comments").whereEqualTo("postId", postId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snap) {
                        List<DocumentSnapshot> comments = new ArrayList<>(snap.getDocuments());
                        Collections.sort(comments, new Comparator<DocumentSnapshot>() {
                            @Override
                            public int compare(DocumentSnapshot a, DocumentSnapshot b) {
                                Date da = a.getDate("timestamp");
                                Date dbb = b.getDate("timestamp");
                                if (da == null || dbb == null) {
                                    return 0;
                                }
                                return da.compareTo(dbb);
                            }
                        });
                        showComments(comments);
                    }
                });
    }

    private void showComments(List<DocumentSnapshot> comments) {
        commentsContainer.removeAllViews();
        tvCommentsTitle.setText(Str.t(R.string.admin_comments_title, comments.size()));
        if (comments.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(Str.t(R.string.admin_no_comments));
            empty.setTextColor(0xFF777777);
            commentsContainer.addView(empty);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (DocumentSnapshot c : comments) {
            View row = inflater.inflate(R.layout.item_admin_comment, commentsContainer, false);
            TextView tvUser = row.findViewById(R.id.tvAdminCommentUser);
            TextView tvText = row.findViewById(R.id.tvAdminCommentContent);
            tvText.setText(c.getString("content"));
            tvUser.setText(Str.t(R.string.common_user));
            bindUserName(c.getString("userId"), tvUser);
            commentsContainer.addView(row);
        }
    }

    private void bindUserName(final String userId, final TextView target) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        String cached = nameCache.get(userId);
        if (cached != null) {
            target.setText(cached);
            return;
        }
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot u) {
                        String name = u == null ? null : u.getString("username");
                        if (name != null && !name.trim().isEmpty()) {
                            nameCache.put(userId, name.trim());
                            target.setText(name.trim());
                        }
                    }
                });
    }

    private String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
    }

    // =================================
    // Xoá bài viết
    // =================================

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(Str.t(R.string.admin_delete_post_q))
                .setMessage(Str.t(R.string.admin_delete_post_msg))
                .setNegativeButton(Str.t(R.string.common_cancel), null)
                .setPositiveButton(Str.t(R.string.common_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost();
                    }
                })
                .show();
    }

    // Xoá bài viết cùng toàn bộ bình luận của bài
    private void deletePost() {
        btnDelete.setEnabled(false);
        db.collection("comments").whereEqualTo("postId", postId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snap) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot c : snap) {
                            batch.delete(c.getReference());
                        }
                        batch.delete(db.collection("posts").document(postId));
                        batch.commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(AdminPostDetailActivity.this,
                                                Str.t(R.string.admin_post_deleted),
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(deleteFailed);
                    }
                })
                .addOnFailureListener(deleteFailed);
    }

    private final OnFailureListener deleteFailed = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            btnDelete.setEnabled(true);
            Toast.makeText(AdminPostDetailActivity.this,
                    Str.t(R.string.admin_delete_failed, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    };
}
