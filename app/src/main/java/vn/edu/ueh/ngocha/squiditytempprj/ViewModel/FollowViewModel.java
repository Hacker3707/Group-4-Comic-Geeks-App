package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.FollowRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.NotificationRepository;

/**
 * ViewModel cho chức năng Follow.
 * Repository trả dữ liệu đồng bộ nên mọi thao tác chạy trên thread nền,
 * kết quả đẩy ra View qua LiveData (postValue).
 *
 * Cách dùng trong Activity/Fragment:
 *   FollowViewModel viewModel = new ViewModelProvider(this).get(FollowViewModel.class);
 *   viewModel.getFollowers().observe(this, new Observer<List<User>>() {
 *       public void onChanged(List<User> users) { ... }
 *   });
 *   viewModel.loadFollowers(userId);
 */
public class FollowViewModel extends AndroidViewModel {

    private final FollowRepository followRepository;
    private final NotificationRepository notificationRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<User>> followers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> following = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>();
    private final MutableLiveData<Integer> followerCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>();

    public FollowViewModel(@NonNull Application application) {
        super(application);
        followRepository = new FollowRepository(application);
        notificationRepository = new NotificationRepository(application);
    }

    // =================================
    // LiveData cho View observe
    // =================================

    public LiveData<List<User>> getFollowers() {
        return followers;
    }

    public LiveData<List<User>> getFollowing() {
        return following;
    }

    public LiveData<Boolean> getIsFollowing() {
        return isFollowing;
    }

    public LiveData<Integer> getFollowerCount() {
        return followerCount;
    }

    public LiveData<Integer> getFollowingCount() {
        return followingCount;
    }

    // =================================
    // Load dữ liệu
    // =================================

    public void loadFollowers(final String userId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<User> result = followRepository.getFollowers(userId);
                followers.postValue(result);
            }
        });
    }

    public void loadFollowing(final String userId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<User> result = followRepository.getFollowing(userId);
                following.postValue(result);
            }
        });
    }

    public void loadCounts(final String userId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int totalFollowers = followRepository.countFollowers(userId);
                int totalFollowing = followRepository.countFollowing(userId);
                followerCount.postValue(totalFollowers);
                followingCount.postValue(totalFollowing);
            }
        });
    }

    // Kiểm tra currentUserId có đang follow targetUserId không (để đổi nút Follow/Unfollow)
    public void checkFollowing(final String currentUserId, final String targetUserId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean result = followRepository.isFollowing(currentUserId, targetUserId);
                isFollowing.postValue(result);
            }
        });
    }

    // =================================
    // Hành động
    // =================================

    public void follow(final String currentUserId, final String targetUserId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean followed = followRepository.follow(currentUserId, targetUserId);
                if (followed) {
                    // Follow mới thì báo cho người được follow
                    String actorName = notificationRepository.currentActorName(currentUserId);
                    notificationRepository.sendNotification(targetUserId, currentUserId, actorName,
                            NotificationType.FOLLOW, null, actorName + " đã theo dõi bạn");
                }
                refresh(currentUserId, targetUserId);
            }
        });
    }

    public void unfollow(final String currentUserId, final String targetUserId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                followRepository.unfollow(currentUserId, targetUserId);
                refresh(currentUserId, targetUserId);
            }
        });
    }

    // Cập nhật lại trạng thái nút và số đếm sau khi follow/unfollow (gọi trên thread nền)
    private void refresh(String currentUserId, String targetUserId) {
        isFollowing.postValue(followRepository.isFollowing(currentUserId, targetUserId));
        followerCount.postValue(followRepository.countFollowers(targetUserId));
        followingCount.postValue(followRepository.countFollowing(currentUserId));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}