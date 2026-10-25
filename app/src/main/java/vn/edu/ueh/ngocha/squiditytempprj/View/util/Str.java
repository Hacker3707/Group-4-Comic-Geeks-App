package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.R;

/**
 * Lấy chuỗi theo ngôn ngữ app đang chọn (mặc định English).
 * Dùng được ở mọi nơi (Activity, ViewModel, Repository) mà không cần Context.
 */
public final class Str {
    private static final String PREFS = "app_language_prefs";
    private static final String KEY = "language";
    private static final String[] GENDER_CANON = {"Chưa chọn", "Nam", "Nữ", "Khác", "Không muốn tiết lộ"};

    private static Context appContext;

    private Str() {}

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    /** "en" hoặc "vi" */
    public static String getLanguage() {
        if (appContext == null) return "en";
        SharedPreferences sp = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY, "en");
    }

    /** Lưu ngôn ngữ và áp dụng cho toàn app (Activity tự tạo lại). */
    public static void setLanguage(String lang) {
        if (appContext != null) {
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY, lang).apply();
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
    }

    private static Resources res() {
        Configuration conf = new Configuration(appContext.getResources().getConfiguration());
        conf.setLocale(new Locale(getLanguage()));
        return appContext.createConfigurationContext(conf).getResources();
    }

    public static String t(@StringRes int id, Object... args) {
        if (appContext == null) return "";
        String s = res().getString(id);
        return args == null || args.length == 0 ? s : String.format(res().getConfiguration().getLocales().get(0), s, args);
    }

    public static String[] array(@ArrayRes int id) {
        return res().getStringArray(id);
    }

    /** Giới tính lưu trong DB luôn là tiếng Việt, hàm này đổi sang ngôn ngữ đang chọn để hiển thị. */
    public static String gender(String canonical) {
        if (canonical == null) return "";
        String[] shown = array(R.array.profile_gender_options);
        for (int i = 0; i < GENDER_CANON.length && i < shown.length; i++) {
            if (GENDER_CANON[i].equalsIgnoreCase(canonical.trim())) return shown[i];
        }
        return canonical;
    }
}
