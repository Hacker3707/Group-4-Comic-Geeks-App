package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.FollowViewModel;

/**
 * Gắn chức năng Follow cho trang Profile:
 * - Followers: số người đang follow chủ trang. Following: số người chủ trang đang follow.
 *   Cả hai cập nhật thời gian thực, nên khi người khác follow thì số hiện lên ngay.
 * - Nút Follow: chỉ hiện khi xem Profile của người khác (truyền userId của họ). Xem Profile của chính
 *   mình thì ẩn nút.
 *
 * Cách dùng (cuối onCreate của ProfileActivity):
 *   FollowStatsBinder.bind(this, findViewById(R.id.tvFollowerCount), findViewById(R.id.tvFollowingCount),
 *           findViewById(R.id.btnFollow), getIntent().getStringExtra("profile_user_id"));
 */
public final class FollowStatsBinder {

    private FollowStatsBinder() {
    }

    // profileUserId = null hoặc rỗng nghĩa là xem Profile của chính mình
    public static void bind(final AppCompatActivity activity,
                            final TextView followerCountView,
                            final TextView followingCountView,
                            final Button followButton,
                            String profileUserId) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            return;
        }
        final String myId = me.getUid();
        final String shownUserId;
        if (profileUserId == null || profileUserId.trim().isEmpty()) {
            shownUserId = myId;
        } else {
            shownUserId = profileUserId;
        }

        final FollowViewModel viewModel = new ViewModelProvider(activity).get(FollowViewModel.class);

        // Hiện 0 trước khi có dữ liệu thật
        if (followerCountView != null) {
            followerCountView.setText("0");
        }
        if (followingCountView != null) {
            followingCountView.setText("0");
        }

        viewModel.getFollowerCount().observe(activity, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (followerCountView != null) {
                    followerCountView.setText(String.valueOf(count == null ? 0 : count));
                }
            }
        });
        viewModel.getFollowingCount().observe(activity, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (followingCountView != null) {
                    followingCountView.setText(String.valueOf(count == null ? 0 : count));
                }
            }
        });
        viewModel.startListeningCounts(shownUserId);

        if (followButton == null) {
            return;
        }
        if (shownUserId.equals(myId)) {
            // Không tự follow chính mình
            followButton.setVisibility(View.GONE);
            return;
        }

        followButton.setVisibility(View.VISIBLE);
        viewModel.startListeningFollowing(myId);
        viewModel.getMyFollowingIds().observe(activity, new Observer<Set<String>>() {
            @Override
            public void onChanged(Set<String> ids) {
                boolean following = ids != null && ids.contains(shownUserId);
                if (following) {
                    followButton.setText(R.string.follow_button_following);
                } else {
                    followButton.setText(R.string.follow_button_follow);
                }
            }
        });
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.toggleFollow(myId, shownUserId);
            }
        });
    }
}
