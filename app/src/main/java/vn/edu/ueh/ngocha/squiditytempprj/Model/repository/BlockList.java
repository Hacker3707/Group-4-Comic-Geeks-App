package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Danh sách người dùng đã chặn (lưu trên máy). Tin nhắn và thông báo từ họ bị bỏ qua. */
public final class BlockList {
    private static final String PREFS = "block_prefs";
    private static final String KEY = "blocked_ids";

    private BlockList() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static Set<String> all(Context c) {
        return new HashSet<>(prefs(c).getStringSet(KEY, new HashSet<String>()));
    }

    public static boolean isBlocked(Context c, String userId) {
        return userId != null && all(c).contains(userId);
    }

    public static void add(Context c, String userId) {
        Set<String> s = all(c);
        s.add(userId);
        prefs(c).edit().putStringSet(KEY, s).apply();
    }

    public static void remove(Context c, String userId) {
        Set<String> s = all(c);
        s.remove(userId);
        prefs(c).edit().putStringSet(KEY, s).apply();
    }
}
