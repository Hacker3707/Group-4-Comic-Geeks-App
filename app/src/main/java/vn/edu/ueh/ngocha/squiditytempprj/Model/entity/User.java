package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "users")
public class User {

    // =====================================================
    // ATTRIBUTES
    // =====================================================

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "role")
    private String role;

    @ColumnInfo(name = "verified")
    private boolean verified;

    @ColumnInfo(name = "verified_source")
    private String verifiedSource;


    // =====================================================
    // RELATION / LIST
    // Room không lưu trực tiếp List<Entity> thành column
    // =====================================================

    @Ignore
    private List<User> followers;

    @Ignore
    private List<User> following;

    @Ignore
    private List<Genre> favoriteGenres;

    @Ignore
    private List<Comic> favoriteComics;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public User() {
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.favoriteGenres = new ArrayList<>();
        this.favoriteComics = new ArrayList<>();

        this.role = "USER";
        this.verified = false;
        this.verifiedSource = "";
    }

    @Ignore
    public User(String userId,
                String username,
                String email,
                String passwordHash,
                String avatarUrl) {

        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.role = "USER";
        this.verified = false;
        this.verifiedSource = "";

        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.favoriteGenres = new ArrayList<>();
        this.favoriteComics = new ArrayList<>();
    }


    // =====================================================
    // FOLLOW USER
    // =====================================================

    @Ignore
    public void follow(User user) {

        if (user == null) {
            return;
        }

        if (user == this) {
            return;
        }

        if (!following.contains(user)) {

            following.add(user);

            if (!user.followers.contains(this)) {
                user.followers.add(this);
            }
        }
    }


    // =====================================================
    // UNFOLLOW USER
    // =====================================================

    @Ignore
    public void unfollow(User user) {

        if (user == null) {
            return;
        }

        if (following.contains(user)) {

            following.remove(user);

            user.followers.remove(this);
        }
    }


    // =====================================================
    // EDIT PROFILE
    // =====================================================

    @Ignore
    public void editProfile(String username,
                            String avatarUrl) {

        if (username != null && !username.trim().isEmpty()) {
            this.username = username;
        }

        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }


    // =====================================================
    // EDIT PROFILE - FULL
    // =====================================================

    @Ignore
    public void editProfile(String username,
                            String email,
                            String avatarUrl) {

        if (username != null && !username.trim().isEmpty()) {
            this.username = username;
        }

        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }

        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }


    // =====================================================
    // GET FEED
    // =====================================================

    @Ignore
    public <T> List<T> getFeed() {

        return new ArrayList<>();
    }


    // =====================================================
    // REPORT ITEM
    // =====================================================

    @Ignore
    public void reportItem(Object item) {

        if (item == null) {
            return;
        }

        // Logic report sẽ được module tương ứng xử lý.
    }


    // =====================================================
    // CREATE POST
    // =====================================================

    @Ignore
    public void createPost(String content) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // Việc tạo Post thật sự sẽ do Post module xử lý.
    }


    // =====================================================
    // CREATE POST - CÓ THÊM THÔNG TIN
    // =====================================================

    @Ignore
    public void createPost(String content, Object... additionalData) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // additionalData có thể chứa:
        // image
        // comic
        // group
        // ...
    }


    // =====================================================
    // ADD FAVORITE GENRE
    // =====================================================

    @Ignore
    public void addGenre(Genre genre) {

        if (genre == null) {
            return;
        }

        if (!favoriteGenres.contains(genre)) {
            favoriteGenres.add(genre);
        }
    }


    // =====================================================
    // REMOVE FAVORITE GENRE
    // =====================================================

    @Ignore
    public void removeGenre(Genre genre) {

        if (genre == null) {
            return;
        }

        favoriteGenres.remove(genre);
    }


    // =====================================================
    // ADD COMIC TO FAVORITE
    // =====================================================

    @Ignore
    public void addComicToFavorite(Comic comic) {

        if (comic == null) {
            return;
        }

        if (!favoriteComics.contains(comic)) {
            favoriteComics.add(comic);
        }
    }


    // =====================================================
    // REMOVE COMIC FROM FAVORITE
    // =====================================================

    @Ignore
    public void removeComicFromFavorite(Comic comic) {

        if (comic == null) {
            return;
        }

        favoriteComics.remove(comic);
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public List<User> getFollowing() {
        return following;
    }

    public List<Genre> getFavoriteGenres() {
        return favoriteGenres;
    }

    public List<Comic> getFavoriteComics() {
        return favoriteComics;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerifiedSource() {
        return verifiedSource;
    }

    public void setVerifiedSource(String verifiedSource) {
        this.verifiedSource = verifiedSource;
    }
}