package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import android.widget.Toast;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import vn.edu.ueh.ngocha.squiditytempprj.R;

import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;


import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NtfChannel;
import vn.edu.ueh.ngocha.squiditytempprj.View.notification.NotificationPopup;
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView imgvAvatar;
    private RecyclerView rcvpostlist;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel;

    private TextView tvMainUsername;
    private TextView tvDrawerUsername;
    private TextView tvDrawerEmail;

    private ShapeableImageView imgMainAvatar;
    private ShapeableImageView imgDrawerAvatar;

    private MaterialButton btnAuthAction;

    private AuthViewModel authViewModel;
    private UserProfileViewModel profileViewModel;


    private View.OnClickListener onComicClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ComicListActivity.class);
            startActivity(intent);
        }
    };


    private View.OnClickListener onProfileClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout != null) {
                        // Portrait: mở menu
                        drawerLayout.openDrawer(GravityCompat.START);
                    } else {
                        // Landscape: mở hồ sơ trực tiếp
                        Intent intent = new Intent(
                                MainActivity.this,
                                ProfileActivity.class
                        );
                        startActivity(intent);
                    }
                }
            };


    private NavigationView.OnNavigationItemSelectedListener onAvatarsClickListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_profile) {
                        if (authViewModel.getCurrentUser() == null) {

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Yêu cầu đăng nhập")
                                    .setMessage("Bạn cần đăng nhập để xem profile.")
                                    .setNegativeButton("Hủy", (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .setPositiveButton("Đăng nhập", (dialog, which) -> {
                                        Intent intent = new Intent(
                                                MainActivity.this,
                                                AuthActivity.class
                                        );
                                        startActivity(intent);
                                    })
                                    .show();

                        } else {
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    ProfileActivity.class
                            );
                            startActivity(intent);
                        }

                    } else if (id == R.id.action_chat) {
                         startActivity(new Intent(MainActivity.this, ChatListActivity.class));
                    } else if (id == R.id.action_group) {
                        // Mở màn hình Groups
                        Intent intent = new Intent(
                                MainActivity.this,
                                GroupActivity.class
                        );
                        startActivity(intent);
                    }
                    else if (id == R.id.action_comic) {
                        Intent intent = new Intent(
                                MainActivity.this,
                                ComicListActivity.class
                        );
                        startActivity(intent);
                    }

                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        NtfChannel.setup(this);
        NotificationPopup.attach(this, findViewById(R.id.btnNotification));

        authViewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);

        profileViewModel = new ViewModelProvider(this)
                .get(UserProfileViewModel.class);

        tvMainUsername = findViewById(R.id.tvMainUsername);
        drawerLayout = findViewById(R.id.drawerLayout);
        imgvAvatar = findViewById(R.id.imgvAvatar);
        navigationView = findViewById(R.id.navigationView);

        ViewCompat.setOnApplyWindowInsetsListener(
                navigationView,
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            v.getPaddingLeft(),
                            systemBars.top,
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );

                    return insets;
                }
        );

        rcvpostlist = findViewById(R.id.rcv_postlist);

        View appBar = findViewById(R.id.appBar);
        tvMainUsername = appBar.findViewById(R.id.tvMainUsername);
        imgMainAvatar = appBar.findViewById(R.id.imgvAvatar);

        ImageView btnSearchIcon = appBar.findViewById(R.id.btnSearch); // Thay ID này đúng với nút kính lúp trong layout_app_bar của nhóm
        if (btnSearchIcon != null) {
            btnSearchIcon.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        if (navigationView == null || navigationView.getHeaderCount() == 0) {
            Toast.makeText(this, "Không tìm thấy phần header của menu", Toast.LENGTH_SHORT).show();
            return;
        }

        View header = navigationView.getHeaderView(0);
        tvDrawerUsername = header.findViewById(R.id.tvDrawerUsername);
        tvDrawerEmail = header.findViewById(R.id.tvDrawerEmail);
        imgDrawerAvatar = header.findViewById(R.id.imageView4);

        btnAuthAction = findViewById(R.id.btnAuthAction);

        btnAuthAction.setOnClickListener(v -> {
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user == null) {
                openAuthActivity();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            authViewModel.logout();
                            profileViewModel.loadCurrentProfile();
                            Toast.makeText(
                                    MainActivity.this,
                                    "Đã đăng xuất",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .show();
            }
        });



        imgvAvatar.setOnClickListener(onProfileClickListener);
        navigationView.setNavigationItemSelectedListener(onAvatarsClickListener);

        rcvpostlist.setLayoutManager(new LinearLayoutManager(this));

        postAdapter = new PostAdapter(this, new ArrayList<>(), new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onStampClick(Post post) {
                String randomStampId = UUID.randomUUID().toString();
                Stamp newStamp = new Stamp(randomStampId, "Meringoc_17", StampType.LIKE);
                newStamp.setPostId(post.getPostId());
                postViewModel.addStamp(newStamp);
                Toast.makeText(MainActivity.this, "Đã thả Stamp!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentSend(Post post, String commentText) {
                if (commentText != null && !commentText.trim().isEmpty()) {
                    String randomCommentId = UUID.randomUUID().toString();
                    Comment newComment = new Comment(randomCommentId, "Meringoc_17", post.getPostId(), commentText.trim());
                    postViewModel.addComment(newComment);
                    Toast.makeText(MainActivity.this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onViewCommentsClick(Post post) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                bottomSheetDialog.setContentView(R.layout.layout_comment_sheet);

                RecyclerView rvComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);
                if (rvComments != null) {
                    rvComments.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }

                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
                if (rvComments != null) {
                    rvComments.setAdapter(commentAdapter);
                }

                if (post.getPostId() != null) {
                    postViewModel.getCommentsByPostId(post.getPostId()).observe(MainActivity.this, realComments -> {
                        if (realComments != null) {
                            commentAdapter.setComments(realComments);
                            if (!realComments.isEmpty() && rvComments != null) {
                                rvComments.scrollToPosition(realComments.size() - 1);
                            }
                        }
                    });
                }
                bottomSheetDialog.show();
            }

            @Override
            public void onDeletePost(Post post) {
                if (post != null) {
                    postViewModel.deletePost(post);
                    Toast.makeText(MainActivity.this, "Đã xóa bài viết!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditPost(Post post) {
                Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("post_id", post.getPostId());
                intent.putExtra("post_content", post.getContent());
                intent.putExtra("post_comic_id", post.getComicId());
                intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                startActivity(intent);
            }
        });

        rcvpostlist.setAdapter(postAdapter);

        postViewModel.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> realPosts) {
                if (realPosts != null) {
                    postAdapter.setPosts(realPosts);
                }
            }
        });

        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) {
                return;
            }

            tvMainUsername.setText(state.username);
            tvDrawerUsername.setText(state.username);
            tvDrawerEmail.setText(state.email);

            btnAuthAction.setText(
                    state.loggedIn ? "Đăng xuất" : "Đăng nhập / Đăng ký"
            );

            if (state.avatarUrl == null || state.avatarUrl.isEmpty()) {
                imgMainAvatar.setImageResource(R.drawable.ic_launcher_background);
                imgDrawerAvatar.setImageResource(R.drawable.ic_launcher_background);
            } else {
                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgMainAvatar);

                Glide.with(this)
                        .load(state.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgDrawerAvatar);
            }
        });

        profileViewModel.loadCurrentProfile();
    }

    private void openAuthActivity() {
        try {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Không mở được AuthActivity", e);
            Toast.makeText(this, "Không mở được màn hình đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profileViewModel != null) {
            profileViewModel.loadCurrentProfile();
        }
    }
}