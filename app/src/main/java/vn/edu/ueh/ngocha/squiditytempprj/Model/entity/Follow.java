package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import vn.edu.ueh.ngocha.squiditytempprj.Model.Converters;

/**
 * Bảng "follows": 1 dòng = 1 lượt follow (followerId theo dõi followingId).
 * Thay cho 2 List<User> followers/following trong class User (Room không lưu List vào cột được).
 * unique index (follower_id, following_id) để không follow trùng.
 */
@Entity(
        tableName = "follows",
        indices = {@Index(value = {"follower_id", "following_id"}, unique = true)}
)
public class Follow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "follow_id")
    private String followId;

    // id của người đi follow
    @ColumnInfo(name = "follower_id")
    private String followerId;

    // id của người được follow
    @ColumnInfo(name = "following_id")
    private String followingId;

    @ColumnInfo(name = "timestamp")
    @TypeConverters(Converters.class)
    private Date timestamp;

    public Follow(@NonNull String followId, String followerId, String followingId) {
        this.followId = followId;
        this.followerId = followerId;
        this.followingId = followingId;
        this.timestamp = new Date();
    }

    // =================================
    // Getter methods
    // =================================

    @NonNull
    public String getFollowId() {
        return followId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public String getFollowingId() {
        return followingId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // =================================
    // Setter methods
    // =================================

    public void setFollowId(@NonNull String followId) {
        this.followId = followId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}