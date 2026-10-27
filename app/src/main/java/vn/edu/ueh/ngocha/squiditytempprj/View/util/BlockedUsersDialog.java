package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.BlockList;
import vn.edu.ueh.ngocha.squiditytempprj.R;

/** Danh sách người đã chặn. Bấm một người để bỏ chặn. */
public final class BlockedUsersDialog {

    private BlockedUsersDialog() {}

    public static void show(final Activity activity) {
        final List<String> ids = new ArrayList<>(BlockList.all(activity));
        if (ids.isEmpty()) {
            new AlertDialog.Builder(activity)
                    .setTitle(Str.t(R.string.pref_blocked))
                    .setMessage(Str.t(R.string.settings_blocked_empty))
                    .setPositiveButton(Str.t(R.string.common_close), null)
                    .show();
            return;
        }
        final List<String> names = new ArrayList<>(ids);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity, android.R.layout.simple_list_item_1, names);
        ListView listView = new ListView(activity);
        listView.setAdapter(adapter);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(Str.t(R.string.pref_blocked))
                .setView(listView)
                .setNegativeButton(Str.t(R.string.common_close), null)
                .create();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final String userId = ids.get(position);
            final String name = names.get(position);
            new AlertDialog.Builder(activity)
                    .setTitle(Str.t(R.string.settings_unblock_q, name))
                    .setNegativeButton(Str.t(R.string.common_cancel), null)
                    .setPositiveButton(Str.t(R.string.settings_unblock), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            BlockList.remove(activity, userId);
                            Toast.makeText(activity, Str.t(R.string.chat_unblocked), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .show();
        });
        dialog.show();
        // Thay id bằng tên hiển thị khi tải xong
        for (int i = 0; i < ids.size(); i++) {
            final int index = i;
            FirebaseFirestore.getInstance().collection("users").document(ids.get(i)).get()
                    .addOnSuccessListener(doc -> {
                        String n = doc.getString("username");
                        if (n != null && !n.trim().isEmpty()) {
                            names.set(index, n);
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}
