package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;

/**
 * Kiểm tra ai được cài đặt / giải tán nhóm:
 * - Admin lớn: admin của cả app Squidity.
 * - Admin nhỏ: người tạo ra nhóm đó (Group.creatorId).
 */
public final class GroupPermissionUtils {

    // TODO: app chưa có đăng nhập nên tạm cố định một tài khoản admin của app.
    // Khi có đăng nhập: thay bằng kiểm tra "ADMIN".equals(user.getRole()) của user đang đăng nhập.
    public static final String APP_ADMIN_USER_ID = "squidity_admin";

    private GroupPermissionUtils() {
    }

    public static boolean isAppAdmin(String userId) {
        return userId != null && APP_ADMIN_USER_ID.equals(userId);
    }

    public static boolean isGroupCreator(String userId, Group group) {
        return userId != null && group != null && userId.equals(group.getCreatorId());
    }

    public static boolean canManageGroup(String userId, Group group) {
        return isAppAdmin(userId) || isGroupCreator(userId, group);
    }
}
