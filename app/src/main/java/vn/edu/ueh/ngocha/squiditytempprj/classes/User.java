package vn.edu.ueh.ngocha.squiditytempprj.classes;

import java.util.ArrayList;
import java.util.List;

public class User {

    // =====================================================
    // ATTRIBUTES
    // =====================================================

    private String userId;
    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;

    private List<User> followers;
    private List<User> following;

    private List<Genre> favoriteGenres;
    private List<Comic> favoriteComics;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

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

        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();

        this.favoriteGenres = new ArrayList<>();
        this.favoriteComics = new ArrayList<>();
    }


    // =====================================================
    // FOLLOW USER
    // =====================================================

    public void follow(User user) {

        if (user == null) {
            return;
        }

        // Không cho follow chính mình
        if (user == this) {
            return;
        }

        // Không follow trùng
        if (!following.contains(user)) {

            following.add(user);

            // Đồng thời thêm mình vào follower của user kia
            if (!user.followers.contains(this)) {
                user.followers.add(this);
            }
        }
    }


    // =====================================================
    // UNFOLLOW USER
    // =====================================================

    public void unfollow(User user) {

        if (user == null) {
            return;
        }

        if (following.contains(user)) {

            following.remove(user);

            // Xóa mình khỏi follower của user kia
            user.followers.remove(this);
        }
    }


    // =====================================================
    // EDIT PROFILE
    // =====================================================

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
    //
    // Dùng generic để hiện tại project chưa cần Post.java
    // vẫn compile được.
    //
    // Khi nhóm tạo Post.java:
    //
    // List<Post> posts = user.getFeed();
    //
    // vẫn có thể sử dụng.
    // =====================================================

    public <T> List<T> getFeed() {

        return new ArrayList<>();
    }


    // =====================================================
    // REPORT ITEM
    // =====================================================
    //
    // Hiện tại nhận Object để sau này có thể truyền:
    // Post
    // Comment
    // User
    // Comic
    // ...
    //
    // mà không phải sửa User.java.
    // =====================================================

    public void reportItem(Object item) {

        if (item == null) {
            return;
        }

        // Logic report sẽ được module tương ứng xử lý.
        // User chỉ gửi yêu cầu report.
    }


    // =====================================================
    // CREATE POST
    // =====================================================
    //
    // Theo UML của bạn User có createPost().
    //
    // Tuy nhiên Post.java hiện chưa tồn tại nên chưa
    // phụ thuộc trực tiếp vào class Post.
    // =====================================================

    public void createPost(String content) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // Việc tạo Post thật sự sẽ do Post module xử lý.
    }


    // =====================================================
    // CREATE POST - CÓ THÊM THÔNG TIN
    // =====================================================

    public void createPost(String content, Object... additionalData) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // additionalData có thể chứa:
        // image
        // comic
        // group
        // ...
        //
        // Khi các class khác được tạo,
        // có thể truyền dữ liệu vào đây.
    }


    // =====================================================
    // ADD FAVORITE GENRE
    // =====================================================

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

    public void removeGenre(Genre genre) {

        if (genre == null) {
            return;
        }

        favoriteGenres.remove(genre);
    }


    // =====================================================
    // ADD COMIC TO FAVORITE
    // =====================================================

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
}