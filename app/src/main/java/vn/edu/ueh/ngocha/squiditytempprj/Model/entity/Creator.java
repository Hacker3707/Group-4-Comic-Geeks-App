package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

import java.util.ArrayList;
import java.util.List;

public class Creator extends User {

    // =====================================================
    // ATTRIBUTES
    // =====================================================

    private List<Comic> comicByCreator;


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

        // Xác định đây là tài khoản Creator
        setRole("CREATOR");

        // verified và verifiedSource
        // được kế thừa từ User
        setVerified(false);
        setVerifiedSource("");

        this.comicByCreator = new ArrayList<>();
    }


    // =====================================================
    // CREATE ANNOUNCEMENT POST
    // =====================================================

    public void createAnnouncementPost(String content) {

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // Logic tạo Announcement Post
        // sẽ được xử lý bởi Post module.
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
    // GET COMICS
    // =====================================================

    public List<Comic> getComicByCreator() {
        return comicByCreator;
    }
}