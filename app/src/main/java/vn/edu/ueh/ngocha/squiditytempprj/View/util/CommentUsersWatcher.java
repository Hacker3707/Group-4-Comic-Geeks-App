package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.CommentAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserViewModel;

/**
 * Theo dõi tên + ảnh của những người đã bình luận để CommentAdapter hiện đúng tên tài khoản
 * (thay vì userId). Chỉ đăng ký lại khi danh sách người bình luận thay đổi.
 */
public class CommentUsersWatcher {

    private final LifecycleOwner owner;
    private final UserViewModel userViewModel;
    private final CommentAdapter adapter;

    private LiveData<Map<String, User>> live = null;
    private String watchedKey = null;

    private final Observer<Map<String, User>> observer = new Observer<Map<String, User>>() {
        @Override
        public void onChanged(Map<String, User> users) {
            adapter.setUsers(users);
        }
    };

    public CommentUsersWatcher(LifecycleOwner owner, UserViewModel userViewModel,
                               CommentAdapter adapter) {
        this.owner = owner;
        this.userViewModel = userViewModel;
        this.adapter = adapter;
    }

    // Gọi mỗi khi danh sách bình luận thay đổi
    public void update(List<Comment> comments) {
        TreeSet<String> ids = new TreeSet<>();
        if (comments != null) {
            for (Comment comment : comments) {
                if (comment.getUserId() != null && !comment.getUserId().isEmpty()) {
                    ids.add(comment.getUserId());
                }
            }
        }
        String key = String.join(",", ids);
        if (live != null && key.equals(watchedKey)) {
            return;
        }
        stop();
        watchedKey = key;
        live = userViewModel.watchUsersByIds(new ArrayList<>(ids));
        live.observe(owner, observer);
    }

    // Gọi khi đóng bảng bình luận
    public void stop() {
        if (live != null) {
            live.removeObserver(observer);
            live = null;
        }
    }
}
