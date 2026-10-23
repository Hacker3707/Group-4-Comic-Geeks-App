package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.R;

/**
 * Trang quản trị của cả app Squidity. Chỉ tài khoản có users/{uid}.role == "ADMIN" mới vào được.
 * - Tổng quan: số người dùng, tài khoản bị cấm, bài viết, nhóm, bình luận.
 * - Người dùng: cấm / bỏ cấm tài khoản, cấp / gỡ quyền admin.
 * - Bài viết: xem nội dung, xóa bài vi phạm (xóa luôn bình luận của bài).
 * - Nhóm: giải tán nhóm (xóa thành viên và bài viết của nhóm).
 * Dữ liệu đọc trực tiếp từ Firestore nên luôn mới nhất; máy khác tự đồng bộ lại phần Room.
 */
public class AdminActivity extends AppCompatActivity {

    public static final String ROLE_ADMIN = "ADMIN";

    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_USERS = 1;
    private static final int TAB_POSTS = 2;
    private static final int TAB_GROUPS = 3;

    private FirebaseFirestore db;
    private String myUid;

    private final List<DocumentSnapshot> users = new ArrayList<>();
    private final List<DocumentSnapshot> posts = new ArrayList<>();
    private final List<DocumentSnapshot> groups = new ArrayList<>();
    private int commentCount = 0;

    private ListenerRegistration regUsers;
    private ListenerRegistration regPosts;
    private ListenerRegistration regGroups;
    private ListenerRegistration regComments;

    private int selectedTab = TAB_OVERVIEW;
    private View overview;
    private RecyclerView rvAdmin;
    private TextView tvEmpty;
    private TextView tvStatUsers, tvStatBanned, tvStatPosts, tvStatGroups, tvStatComments;
    private RowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        myUid = me.getUid();

        overview = findViewById(R.id.adminOverview);
        rvAdmin = findViewById(R.id.rvAdmin);
        tvEmpty = findViewById(R.id.tvAdminEmpty);
        tvStatUsers = findViewById(R.id.tvStatUsers);
        tvStatBanned = findViewById(R.id.tvStatBanned);
        tvStatPosts = findViewById(R.id.tvStatPosts);
        tvStatGroups = findViewById(R.id.tvStatGroups);
        tvStatComments = findViewById(R.id.tvStatComments);

        findViewById(R.id.imgAdminBack).setOnClickListener(v -> finish());

        adapter = new RowAdapter();
        rvAdmin.setLayoutManager(new LinearLayoutManager(this));
        rvAdmin.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.adminTabs);
        tabs.addTab(tabs.newTab().setText("Tổng quan"));
        tabs.addTab(tabs.newTab().setText("Người dùng"));
        tabs.addTab(tabs.newTab().setText("Bài viết"));
        tabs.addTab(tabs.newTab().setText("Nhóm"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                render();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Chưa biết có phải admin hay không thì ẩn hết, kiểm tra xong mới hiện
        overview.setVisibility(View.GONE);
        db.collection("users").document(myUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && ROLE_ADMIN.equals(doc.getString("role"))) {
                        startListening();
                        render();
                    } else {
                        Toast.makeText(this, "Chỉ admin mới vào được trang này",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không kiểm tra được quyền admin", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void startListening() {
        regUsers = db.collection("users").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            users.clear();
            users.addAll(snap.getDocuments());
            render();
        });
        regPosts = db.collection("posts").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            posts.clear();
            posts.addAll(snap.getDocuments());
            render();
        });
        regGroups = db.collection("groups").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            groups.clear();
            groups.addAll(snap.getDocuments());
            render();
        });
        regComments = db.collection("comments").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            commentCount = snap.size();
            render();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (regUsers != null) regUsers.remove();
        if (regPosts != null) regPosts.remove();
        if (regGroups != null) regGroups.remove();
        if (regComments != null) regComments.remove();
    }

    // =================================
    // Hiển thị
    // =================================

    private void render() {
        if (overview == null || myUid == null || regUsers == null) {
            return;
        }
        if (selectedTab == TAB_OVERVIEW) {
            overview.setVisibility(View.VISIBLE);
            rvAdmin.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            int banned = 0;
            for (DocumentSnapshot u : users) {
                if (Boolean.TRUE.equals(u.getBoolean("banned"))) banned++;
            }
            tvStatUsers.setText("Người dùng: " + users.size());
            tvStatBanned.setText("Tài khoản bị cấm: " + banned);
            tvStatPosts.setText("Bài viết: " + posts.size());
            tvStatGroups.setText("Nhóm: " + groups.size());
            tvStatComments.setText("Bình luận: " + commentCount);
            return;
        }

        overview.setVisibility(View.GONE);
        List<Row> rows = new ArrayList<>();
        Map<String, String> names = userNames();
        if (selectedTab == TAB_USERS) {
            for (DocumentSnapshot u : users) {
                boolean banned = Boolean.TRUE.equals(u.getBoolean("banned"));
                boolean admin = ROLE_ADMIN.equals(u.getString("role"));
                Row r = new Row();
                r.id = u.getId();
                r.title = nonEmpty(u.getString("username"), "(chưa đặt tên)");
                r.subtitle = nonEmpty(u.getString("email"), u.getId());
                r.badge = banned ? "Bị cấm" : (admin ? "Admin" : "");
                r.badgeColor = banned ? Color.parseColor("#C62828") : Color.parseColor("#493373");
                rows.add(r);
            }
        } else if (selectedTab == TAB_POSTS) {
            for (DocumentSnapshot p : posts) {
                String author = names.containsKey(p.getString("userId"))
                        ? names.get(p.getString("userId")) : nonEmpty(p.getString("userId"), "?");
                Row r = new Row();
                r.id = p.getId();
                r.title = nonEmpty(p.getString("content"), "(bài chỉ có ảnh)");
                r.subtitle = "Bởi " + author;
                boolean inGroup = p.getString("groupId") != null && !p.getString("groupId").isEmpty();
                r.badge = inGroup ? "Trong nhóm" : "";
                r.badgeColor = Color.parseColor("#493373");
                rows.add(r);
            }
        } else {
            for (DocumentSnapshot g : groups) {
                String creator = names.containsKey(g.getString("creatorId"))
                        ? names.get(g.getString("creatorId")) : nonEmpty(g.getString("creatorId"), "?");
                Row r = new Row();
                r.id = g.getId();
                r.title = nonEmpty(g.getString("groupName"), "(nhóm không tên)");
                r.subtitle = "Tạo bởi " + creator;
                r.badge = "";
                rows.add(r);
            }
        }
        adapter.setRows(rows);
        rvAdmin.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private Map<String, String> userNames() {
        Map<String, String> map = new HashMap<>();
        for (DocumentSnapshot u : users) {
            String name = u.getString("username");
            if (name != null && !name.isEmpty()) map.put(u.getId(), name);
        }
        return map;
    }

    private static String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    // =================================
    // Thao tác
    // =================================

    private void onRowClick(Row row) {
        if (selectedTab == TAB_USERS) {
            showUserActions(row);
        } else if (selectedTab == TAB_POSTS) {
            showPostActions(row);
        } else if (selectedTab == TAB_GROUPS) {
            confirmDeleteGroup(row);
        }
    }

    private DocumentSnapshot find(List<DocumentSnapshot> list, String id) {
        for (DocumentSnapshot d : list) {
            if (d.getId().equals(id)) return d;
        }
        return null;
    }

    private void showUserActions(Row row) {
        if (row.id.equals(myUid)) {
            Toast.makeText(this, "Bạn không thể thao tác với chính mình", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentSnapshot u = find(users, row.id);
        if (u == null) return;
        final boolean banned = Boolean.TRUE.equals(u.getBoolean("banned"));
        final boolean admin = ROLE_ADMIN.equals(u.getString("role"));
        String[] options = {
                banned ? "Bỏ cấm tài khoản" : "Cấm tài khoản",
                admin ? "Gỡ quyền admin" : "Cấp quyền admin"
        };
        new AlertDialog.Builder(this)
                .setTitle(row.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        db.collection("users").document(row.id).update("banned", !banned);
                        Toast.makeText(this, banned ? "Đã bỏ cấm" : "Đã cấm tài khoản",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users").document(row.id)
                                .update("role", admin ? "USER" : ROLE_ADMIN);
                        Toast.makeText(this, admin ? "Đã gỡ quyền admin" : "Đã cấp quyền admin",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showPostActions(Row row) {
        new AlertDialog.Builder(this)
                .setTitle(row.subtitle)
                .setMessage(row.title)
                .setPositiveButton("Xóa bài viết", (d, w) -> confirmDeletePost(row))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void confirmDeletePost(Row row) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài viết?")
                .setMessage("Bài viết và bình luận của bài sẽ bị xóa khỏi toàn bộ app.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> deletePost(row.id))
                .show();
    }

    private void deletePost(String postId) {
        db.collection("comments").whereEqualTo("postId", postId).get()
                .addOnSuccessListener(snap -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot c : snap) {
                        batch.delete(c.getReference());
                    }
                    batch.delete(db.collection("posts").document(postId));
                    batch.commit().addOnSuccessListener(v ->
                            Toast.makeText(this, "Đã xóa bài viết", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không xóa được: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void confirmDeleteGroup(Row row) {
        new AlertDialog.Builder(this)
                .setTitle("Giải tán nhóm \"" + row.title + "\"?")
                .setMessage("Thành viên và toàn bộ bài viết trong nhóm sẽ bị xóa. Không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Giải tán", (d, w) -> deleteGroup(row.id))
                .show();
    }

    private void deleteGroup(String groupId) {
        db.collection("group_members").whereEqualTo("groupId", groupId).get()
                .addOnSuccessListener(members -> db.collection("posts")
                        .whereEqualTo("groupId", groupId).get()
                        .addOnSuccessListener(groupPosts -> {
                            WriteBatch batch = db.batch();
                            for (QueryDocumentSnapshot m : members) batch.delete(m.getReference());
                            for (QueryDocumentSnapshot p : groupPosts) batch.delete(p.getReference());
                            batch.delete(db.collection("groups").document(groupId));
                            batch.commit().addOnSuccessListener(v ->
                                    Toast.makeText(this, "Đã giải tán nhóm", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không xóa được: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // =================================
    // Danh sách
    // =================================

    private static class Row {
        String id;
        String title;
        String subtitle;
        String badge = "";
        int badgeColor = Color.parseColor("#493373");
    }

    private class RowAdapter extends RecyclerView.Adapter<RowAdapter.Holder> {
        private final List<Row> rows = new ArrayList<>();

        void setRows(List<Row> newRows) {
            rows.clear();
            rows.addAll(newRows);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Row row = rows.get(position);
            h.title.setText(row.title);
            h.subtitle.setText(row.subtitle);
            if (row.badge == null || row.badge.isEmpty()) {
                h.badge.setVisibility(View.GONE);
            } else {
                h.badge.setVisibility(View.VISIBLE);
                h.badge.setText(row.badge);
                h.badge.setTextColor(row.badgeColor);
            }
            h.itemView.setOnClickListener(v -> onRowClick(row));
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;
            final TextView badge;

            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvRowTitle);
                subtitle = itemView.findViewById(R.id.tvRowSubtitle);
                badge = itemView.findViewById(R.id.tvRowBadge);
            }
        }
    }
}
