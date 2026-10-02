package vn.edu.ueh.ngocha.squiditytempprj.classes;

public class Admin extends User {

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Admin(String userId,
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
    }


    // =====================================================
    // BAN USER
    // =====================================================

    public void banUser(User user) {

        if (user == null) {
            return;
        }

        // Logic ban tài khoản sẽ được xử lý
        // bởi hệ thống quản lý User.
    }


    // =====================================================
    // REMOVE POST
    // =====================================================
    //
    // Dùng Object vì hiện tại project chưa có Post.java.
    //
    // Sau này:
    //
    // Post post = ...
    // admin.removePost(post);
    //
    // vẫn sử dụng được.
    // =====================================================

    public void removePost(Object post) {

        if (post == null) {
            return;
        }

        // Logic xóa Post sẽ được Post module xử lý.
    }


    // =====================================================
    // APPROVE CREATOR ACCOUNT
    // =====================================================

    public void approveCreatorAccount(Creator creator) {

        if (creator == null) {
            return;
        }

        creator.setVerified(true);
    }
}