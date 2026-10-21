package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.FollowViewModel;

/**
 * Gắn chức năng Follow cho nút "+ Follow" trong mỗi bài viết.
 *
 * Cách dùng (trong PostAdapter.onBindViewHolder):
 *   FollowButtonBinder.bind(holder.btnFollow, post.getUserId());
 *
 * - Chưa đăng nhập hoặc là bài của chính mình: ẩn nút.
 * - Bấm nút: follow hoặc bỏ follow tác giả bài viết.
 * - Trạng thái nút ("+ Follow" / "Following") cập nhật thời gian thực qua LiveData của FollowViewModel.
 *
 * Lưu ý: post.getUserId() phải là uid Firebase của người đăng thì số Followers trên Profile
 * của người đó mới tăng.
 */
public final class FollowButtonBinder {

    // Các nút đang hiển thị và tác giả tương ứng (tự giải phóng khi nút không còn dùng)
    private static final Map<Button, String> BOUND_BUTTONS = new WeakHashMap<>();
    // Activity nào đã đăng ký Observer rồi (mỗi Activity chỉ đăng ký 1 lần)
    private static final Map<AppCompatActivity, Boolean> OBSERVING = new WeakHashMap<>();

    private FollowButtonBinder() {
    }

    public static void bind(final Button button, final String targetUserId) {
        if (button == null) {
            return;
        }
        final AppCompatActivity activity = findActivity(button.getContext());
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();

        boolean cannotFollow = activity == null
                || me == null
                || targetUserId == null
                || targetUserId.trim().isEmpty()
                || targetUserId.equals(me.getUid());
        if (cannotFollow) {
            BOUND_BUTTONS.remove(button);
            button.setVisibility(View.GONE);
            return;
        }

        final String myId = me.getUid();
        final FollowViewModel viewModel = new ViewModelProvider(activity).get(FollowViewModel.class);
        viewModel.startListeningFollowing(myId);

        BOUND_BUTTONS.put(button, targetUserId);
        button.setVisibility(View.VISIBLE);
        applyState(button, isFollowing(viewModel.getMyFollowingIds().getValue(), targetUserId));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.toggleFollow(myId, targetUserId);
            }
        });

        if (!OBSERVING.containsKey(activity)) {
            OBSERVING.put(activity, Boolean.TRUE);
            viewModel.getMyFollowingIds().observe(activity, new Observer<Set<String>>() {
                @Override
                public void onChanged(Set<String> ids) {
                    refreshButtons(activity, ids);
                }
            });
        }
    }

    // Cập nhật lại chữ trên tất cả nút Follow của Activity này khi danh sách đang follow thay đổi
    private static void refreshButtons(AppCompatActivity activity, Set<String> ids) {
        List<Map.Entry<Button, String>> entries = new ArrayList<>(BOUND_BUTTONS.entrySet());
        for (Map.Entry<Button, String> entry : entries) {
            Button button = entry.getKey();
            if (button == null || findActivity(button.getContext()) != activity) {
                continue;
            }
            applyState(button, isFollowing(ids, entry.getValue()));
        }
    }

    private static boolean isFollowing(Set<String> ids, String targetUserId) {
        return ids != null && ids.contains(targetUserId);
    }

    private static void applyState(Button button, boolean following) {
        if (following) {
            button.setText(R.string.follow_button_following);
            button.setAlpha(0.7f);
        } else {
            button.setText(R.string.follow_button_follow);
            button.setAlpha(1f);
        }
    }

    private static AppCompatActivity findActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof AppCompatActivity) {
                return (AppCompatActivity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        return null;
    }
}
