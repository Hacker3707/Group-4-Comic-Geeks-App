package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {
    @PrimaryKey
    @NonNull
    @ColumnInfo
    private String groupId;

    @ColumnInfo(name = "group_name")
    private String groupName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "cover_image_url")
    private String coverImageUrl;

    // Ảnh đại diện của nhóm (link ảnh)
    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    // userId của người tạo nhóm ("admin nhỏ"), dùng để kiểm tra quyền cài đặt nhóm
    @ColumnInfo(name = "creator_id")
    private String creatorId;

    public Group(@NonNull String groupId, String groupName, String description, String coverImageUrl) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
    }

    // =================================
    // Methods theo class diagram
    // =================================

    public void updateGroupInfo(String groupName, String description, String coverImageUrl) {
        this.groupName = groupName;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
    }

    // Bản có thêm ảnh đại diện
    public void updateGroupInfo(String groupName, String description, String coverImageUrl,
                                String avatarUrl) {
        updateGroupInfo(groupName, description, coverImageUrl);
        this.avatarUrl = avatarUrl;
    }

    public void deleteGroup() {
        // Việc xoá thật (group, các post có groupId này...) sẽ xử lý ở tầng dữ liệu
    }

    // =================================
    // Getter methods for each field
    // =================================

    @NonNull
    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCreatorId() {
        return creatorId;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
