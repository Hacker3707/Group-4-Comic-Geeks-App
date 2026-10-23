package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

/**
 * Lấy tên hiển thị của một tài khoản:
 * Thứ tự ưu tiên: username, rồi phần trước dấu @ của email, cuối cùng là chữ dự phòng
 * (khi chưa tải được tài khoản).
 */
public final class UserDisplayUtils {

    private UserDisplayUtils() {
    }

    public static String displayName(User user, String fallback) {
        if (user != null) {
            String username = user.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                return username.trim();
            }
            String email = user.getEmail();
            if (email != null) {
                int at = email.indexOf("@");
                if (at > 0) {
                    return email.substring(0, at);
                }
            }
        }
        return fallback;
    }
}
