package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.app.Activity;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.FollowRemoteRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ProfileActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.FollowUserAdapter;

/**
 * Danh sách người theo dõi / đang theo dõi của một tài khoản, hiện dạng bottom sheet
 * có layout giống khung bình luận. Mỗi dòng có nút Follow / Unfollow, bấm vào dòng để mở trang cá nhân.
 */
public final class FollowListDialog {

    private FollowListDialog() {
    }

    public static BottomSheetDialog show(Activity activity, String userId, boolean followers) {
        return show(activity, userId, followers, null);
    }

    /**
     * @param onDismissed chạy sau khi sheet đóng (có thể null)
     * @return sheet đang hiển thị, hoặc null nếu userId không hợp lệ
     */
    public static BottomSheetDialog show(final Activity activity, String userId,
                                         final boolean followers, final Runnable onDismissed) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }

        final BottomSheetDialog sheet = new BottomSheetDialog(activity);
        sheet.setContentView(R.layout.layout_follow_sheet);

        TextView tvTitle = sheet.findViewById(R.id.tvFollowSheetTitle);
        final TextView tvMessage = sheet.findViewById(R.id.tvFollowSheetMessage);
        final RecyclerView rcv = sheet.findViewById(R.id.rcvFollowUsers);
        View container = sheet.findViewById(R.id.followListContainer);

        if (tvTitle != null) {
            tvTitle.setText(Str.t(followers ? R.string.follow_list_followers : R.string.follow_list_following));
        }
        // Chiều cao danh sách theo màn hình để xoay ngang vẫn nhìn được hết
        if (container != null) {
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.5f);
            container.setLayoutParams(params);
        }

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        final String myId = me == null ? null : me.getUid();

        final FollowUserAdapter adapter = new FollowUserAdapter();
        adapter.setMyUserId(myId);
        final FollowRemoteRepository repository = new FollowRemoteRepository(activity.getApplication());
        adapter.setListener(new FollowUserAdapter.OnFollowActionListener() {
            @Override
            public void onToggleFollow(FollowUserAdapter.Item item, boolean currentlyFollowing) {
                if (myId == null) {
                    return;
                }
                if (currentlyFollowing) {
                    repository.unfollow(myId, item.userId);
                } else {
                    repository.follow(myId, item.userId);
                }
            }

            @Override
            public void onUserClick(FollowUserAdapter.Item item) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, item.userId);
                sheet.dismiss();
                activity.startActivity(intent);
            }
        });
        if (rcv != null) {
            rcv.setLayoutManager(new LinearLayoutManager(activity));
            rcv.setAdapter(adapter);
        }

        // Theo dõi realtime danh sách mình đang follow để nút đổi trạng thái ngay khi bấm
        final ListenerRegistration myFollowingReg;
        if (myId != null) {
            myFollowingReg = repository.listenFollowingIds(myId,
                    new FollowRemoteRepository.Callback<Set<String>>() {
                        @Override
                        public void onResult(Set<String> ids) {
                            adapter.setMyFollowingIds(ids);
                        }
                    });
        } else {
            myFollowingReg = null;
        }

        sheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (myFollowingReg != null) {
                    myFollowingReg.remove();
                }
                if (onDismissed != null) {
                    onDismissed.run();
                }
            }
        });

        // Mở rộng hết cỡ để xoay ngang không bị cắt
        BottomSheetBehavior<android.widget.FrameLayout> behavior = sheet.getBehavior();
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        showMessage(tvMessage, rcv, Str.t(R.string.follow_list_loading));
        sheet.show();
        loadUsers(userId, followers, adapter, tvMessage, rcv);
        return sheet;
    }

    // Hiện thông báo (đang tải / trống) thay cho danh sách
    private static void showMessage(TextView tvMessage, RecyclerView rcv, String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        if (rcv != null) {
            rcv.setVisibility(View.GONE);
        }
    }

    private static void showList(TextView tvMessage, RecyclerView rcv) {
        if (tvMessage != null) {
            tvMessage.setVisibility(View.GONE);
        }
        if (rcv != null) {
            rcv.setVisibility(View.VISIBLE);
        }
    }

    private static void loadUsers(String userId, boolean followers, final FollowUserAdapter adapter,
                                  final TextView tvMessage, final RecyclerView rcv) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String mine = followers ? "followingId" : "followerId";
        final String other = followers ? "followerId" : "followingId";

        db.collection("follows").whereEqualTo(mine, userId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snap) {
                        final List<String> userIds = new ArrayList<>();
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String uid = d.getString(other);
                            if (uid != null && !uid.isEmpty() && !userIds.contains(uid)) {
                                userIds.add(uid);
                            }
                        }
                        if (userIds.isEmpty()) {
                            showMessage(tvMessage, rcv, Str.t(R.string.follow_list_empty));
                            return;
                        }
                        loadUserDetails(db, userIds, adapter, tvMessage, rcv);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage(tvMessage, rcv, Str.t(R.string.follow_list_empty));
                    }
                });
    }

    // whereIn chỉ nhận tối đa 10 id mỗi lần nên chia nhóm
    private static void loadUserDetails(FirebaseFirestore db, List<String> userIds,
                                        final FollowUserAdapter adapter,
                                        final TextView tvMessage, final RecyclerView rcv) {
        final List<FollowUserAdapter.Item> result = new ArrayList<>();
        final int[] pending = {(userIds.size() + 9) / 10};

        for (int start = 0; start < userIds.size(); start += 10) {
            List<String> chunk = userIds.subList(start, Math.min(start + 10, userIds.size()));
            db.collection("users").whereIn(FieldPath.documentId(), chunk).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot u : task.getResult().getDocuments()) {
                                    String name = u.getString("username");
                                    if (name == null || name.trim().isEmpty()) {
                                        name = u.getString("email");
                                    }
                                    if (name == null || name.trim().isEmpty()) {
                                        name = Str.t(R.string.common_user);
                                    }
                                    result.add(new FollowUserAdapter.Item(
                                            u.getId(), name.trim(), u.getString("avatarUrl")));
                                }
                            }
                            pending[0]--;
                            if (pending[0] == 0) {
                                finishLoading(result, adapter, tvMessage, rcv);
                            }
                        }
                    });
        }
    }

    private static void finishLoading(List<FollowUserAdapter.Item> result, FollowUserAdapter adapter,
                                      TextView tvMessage, RecyclerView rcv) {
        if (result.isEmpty()) {
            showMessage(tvMessage, rcv, Str.t(R.string.follow_list_empty));
            return;
        }
        Collections.sort(result, new Comparator<FollowUserAdapter.Item>() {
            @Override
            public int compare(FollowUserAdapter.Item a, FollowUserAdapter.Item b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        adapter.setItems(result);
        showList(tvMessage, rcv);
    }
}
