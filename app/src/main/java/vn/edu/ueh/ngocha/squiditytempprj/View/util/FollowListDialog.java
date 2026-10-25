package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ProfileActivity;

/**
 * Danh sách người theo dõi / đang theo dõi của một tài khoản. Bấm một người để mở trang cá nhân của họ.
 */
public final class FollowListDialog {

    private FollowListDialog() {}

    public static void show(final Activity activity, String userId, final boolean followers) {
        if (userId == null || userId.isEmpty()) return;
        final String title = Str.t(followers ? R.string.follow_list_followers : R.string.follow_list_following);

        final ListView listView = new ListView(activity);
        final List<String> names = new ArrayList<>();
        final List<String> ids = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(listView)
                .setNegativeButton(Str.t(R.string.common_close), null)
                .create();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(activity, ProfileActivity.class);
            i.putExtra(ProfileActivity.EXTRA_USER_ID, ids.get(position));
            dialog.dismiss();
            activity.startActivity(i);
        });
        names.add(Str.t(R.string.follow_list_loading));
        dialog.show();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String mine = followers ? "followingId" : "followerId";
        final String other = followers ? "followerId" : "followingId";
        db.collection("follows").whereEqualTo(mine, userId).get()
                .addOnSuccessListener(snap -> {
                    final List<String> userIds = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String uid = d.getString(other);
                        if (uid != null && !uid.isEmpty() && !userIds.contains(uid)) userIds.add(uid);
                    }
                    names.clear();
                    if (userIds.isEmpty()) {
                        names.add(Str.t(R.string.follow_list_empty));
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    final int[] pending = {(userIds.size() + 9) / 10};
                    for (int s = 0; s < userIds.size(); s += 10) {
                        List<String> chunk = userIds.subList(s, Math.min(s + 10, userIds.size()));
                        db.collection("users").whereIn(FieldPath.documentId(), chunk).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        for (DocumentSnapshot u : task.getResult().getDocuments()) {
                                            String name = u.getString("username");
                                            if (name == null || name.trim().isEmpty()) name = u.getString("email");
                                            if (name == null || name.trim().isEmpty()) name = Str.t(R.string.common_user);
                                            names.add(name);
                                            ids.add(u.getId());
                                        }
                                    }
                                    pending[0]--;
                                    if (pending[0] == 0) {
                                        if (names.isEmpty()) names.add(Str.t(R.string.follow_list_empty));
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    names.clear();
                    names.add(Str.t(R.string.follow_list_empty));
                    adapter.notifyDataSetChanged();
                });
    }
}
