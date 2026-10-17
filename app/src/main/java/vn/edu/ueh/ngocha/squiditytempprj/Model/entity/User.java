package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    // =====================================================
    // ATTRIBUTES
    // =====================================================

    @PrimaryKey
    @NonNull
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
    // CONSTRUCTORS
    // =====================================================

    public User() {
        this.userId = "";
        this.username = "";
        this.email = "";
        this.passwordHash = "";
        this.avatarUrl = "";
        this.role = "USER";
        this.verified = false;
        this.verifiedSource = "";
    }


    public User(
            @NonNull String userId,
            String username,
            String email,
            String passwordHash,
            String avatarUrl
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;

        this.role = "USER";
        this.verified = false;
        this.verifiedSource = "";
    }


    // =====================================================
    // EDIT PROFILE
    // =====================================================

    public void editProfile(
            String username,
            String avatarUrl
    ) {

        if (username != null && !username.trim().isEmpty()) {
            this.username = username;
        }

        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }


    public void editProfile(
            String username,
            String email,
            String avatarUrl
    ) {

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
    // GETTERS
    // =====================================================

    @NonNull
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

    public String getRole() {
        return role;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getVerifiedSource() {
        return verifiedSource;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setUserId(@NonNull String userId) {
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

    public void setRole(String role) {
        this.role = role;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setVerifiedSource(String verifiedSource) {
        this.verifiedSource = verifiedSource;
    }
}