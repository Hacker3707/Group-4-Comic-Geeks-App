package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.FollowRepository;

/**
 * ViewModel cho chức năng Follow.
 * Repository trả dữ liệu đồng bộ nên mọi thao tác chạy trên thread nền,
 * kết quả đẩy ra View qua LiveData (postValue).
 *
 * Cách dùng trong Activity/Fragment:
 *   FollowViewModel vm = new ViewModelProvider(this).get(FollowViewModel.class);
 *   vm.getFollowers().observe(this, list -> { ... });
 *   vm.loadFollowers(userId);
 */
public class FollowViewModel extends AndroidViewModel {

    private final FollowRepository followRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<User>> followers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> following = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>();
    private final MutableLiveData<Integer> followerCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>();

    public FollowViewModel(@NonNull Application application) {
        super(application);
        followRepository = new FollowRepository(application);
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

    public void loadFollowers(String userId) {
        executor.execute(() -> followers.postValue(followRepository.getFollowers(userId)));
    }

    public void loadFollowing(String userId) {
        executor.execute(() -> following.postValue(followRepository.getFollowing(userId)));
    }

    public void loadCounts(String userId) {
        executor.execute(() -> {
            followerCount.postValue(followRepository.countFollowers(userId));
            followingCount.postValue(followRepository.countFollowing(userId));
        });
    }

    // Kiểm tra currentUserId có đang follow targetUserId không (để đổi nút Follow/Unfollow)
    public void checkFollowing(String currentUserId, String targetUserId) {
        executor.execute(() ->
                isFollowing.postValue(followRepository.isFollowing(currentUserId, targetUserId)));
    }

    // =================================
    // Hành động
    // =================================

    public void follow(String currentUserId, String targetUserId) {
        executor.execute(() -> {
            followRepository.follow(currentUserId, targetUserId);
            refresh(currentUserId, targetUserId);
        });
    }

    public void unfollow(String currentUserId, String targetUserId) {
        executor.execute(() -> {
            followRepository.unfollow(currentUserId, targetUserId);
            refresh(currentUserId, targetUserId);
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