package vn.edu.ueh.ngocha.squiditytempprj.classes;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String description;
    private String coverImageUrl;
    private List<Post> posts;

    public Group(String groupId, String groupName, String description, String coverImageUrl) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.posts = new ArrayList<>();
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
        this.posts.clear();
    }

    // --- XỬ LÝ POST TRONG GROUP ---
    public void addPost(Post post) {
        this.posts.add(post);
    }

    public void removePost(Post post) {
        this.posts.remove(post);
    }

    // =================================
    // Getter methods for each field
    // =================================

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

    public List<Post> getPosts() {
        return posts;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setGroupId(String groupId) {
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

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}