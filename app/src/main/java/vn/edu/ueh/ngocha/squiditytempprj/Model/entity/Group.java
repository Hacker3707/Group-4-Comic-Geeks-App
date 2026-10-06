package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {
    @PrimaryKey
    @NonNull
    private String groupId;

    @ColumnInfo(name = "group_name")
    private String groupName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "cover_image_url")
    private String coverImageUrl;

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
}