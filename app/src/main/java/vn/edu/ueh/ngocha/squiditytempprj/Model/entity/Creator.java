package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "creators")
public class Creator extends User {

    // =====================================================
    // ATTRIBUTES
    // =====================================================
    @ColumnInfo (name = "verified_source")
    private String verifiedSource;

    @ColumnInfo (name = "verified")
    private boolean verified;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Creator(String userId,
                   String username,
                   String email,
                   String passwordHash,
                   String avatarUrl) {

        super(
                userId,
                username,
                email,
                passwordHash,
                avatarUrl
        );

        setRole("CREATOR");

        setVerifiedSource("");

        setVerified(false);

        this.verifiedSource = "";
        this.verified = false;
    }


    // =====================================================
    // CREATE ANNOUNCEMENT POST
    // =====================================================

    public void createAnnouncementPost(String content) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // Tạo announcement post.
        //
        // Class Post sau này sẽ xử lý việc tạo
        // đối tượng Post thực tế.
    }


    // =====================================================
    // CREATE ANNOUNCEMENT POST
    // WITH ADDITIONAL DATA
    // =====================================================

    public void createAnnouncementPost(
            String content,
            Object... additionalData) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // additionalData có thể bao gồm:
        // Comic
        // Image
        // Group
        // ...
    }


    // =====================================================
    // ADD COMIC BY CREATOR
    // =====================================================

    public void addComic(Comic comic) {

        if (comic == null) {
            return;
        }


    }


    // =====================================================
    // REMOVE COMIC BY CREATOR
    // =====================================================

    public void removeComic(Comic comic) {

        if (comic == null) {
            return;
        }

    }


    // =====================================================
    // VERIFIED
    // =====================================================

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }


    // =====================================================
    // VERIFIED SOURCE
    // =====================================================

    public String getVerifiedSource() {
        return verifiedSource;
    }

    public void setVerifiedSource(String verifiedSource) {
        this.verifiedSource = verifiedSource;
    }


    // =====================================================
    // COMICS
    // =====================================================

}