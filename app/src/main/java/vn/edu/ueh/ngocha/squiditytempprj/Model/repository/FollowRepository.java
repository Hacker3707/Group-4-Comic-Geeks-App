package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;

import java.util.List;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.FollowDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Follow;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

public class FollowRepository {

    private final FollowDao followDao;

    public FollowRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        followDao = db.followDao();
    }

    /**
     * Follow một người.
     * @return true nếu follow thành công; false nếu tự follow chính mình hoặc đã follow rồi.
     */
    public boolean follow(String followerId, String followingId) {
        if (followerId == null || followingId == null || followerId.equals(followingId)) {
            return false;
        }
        if (followDao.isFollowing(followerId, followingId)) {
            return false;
        }
        Follow follow = new Follow(UUID.randomUUID().toString(), followerId, followingId);
        return followDao.insert(follow) != -1;
    }

    public void unfollow(String followerId, String followingId) {
        followDao.unfollow(followerId, followingId);
    }

    public boolean isFollowing(String followerId, String followingId) {
        return followDao.isFollowing(followerId, followingId);
    }

    public List<User> getFollowers(String userId) {
        return followDao.getFollowers(userId);
    }

    public List<User> getFollowing(String userId) {
        return followDao.getFollowing(userId);
    }

    public int countFollowers(String userId) {
        return followDao.countFollowers(userId);
    }

    public int countFollowing(String userId) {
        return followDao.countFollowing(userId);
    }

    public void deleteAll() {
        followDao.deleteAll();
    }
}