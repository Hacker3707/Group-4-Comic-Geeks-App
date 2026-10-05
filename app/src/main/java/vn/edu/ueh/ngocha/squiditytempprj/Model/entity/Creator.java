package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import java.util.ArrayList;
import java.util.List;

public class Creator extends User {

    // =====================================================
    // ATTRIBUTES
    // =====================================================

    private String verifiedSource;

    private List<Comic> comicByCreator;

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

        this.verifiedSource = "";

        this.comicByCreator = new ArrayList<>();

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

        if (!comicByCreator.contains(comic)) {
            comicByCreator.add(comic);
        }
    }


    // =====================================================
    // REMOVE COMIC BY CREATOR
    // =====================================================

    public void removeComic(Comic comic) {

        if (comic == null) {
            return;
        }

        comicByCreator.remove(comic);
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

    public List<Comic> getComicByCreator() {
        return comicByCreator;
    }
}