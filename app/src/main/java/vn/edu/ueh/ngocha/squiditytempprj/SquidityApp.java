package vn.edu.ueh.ngocha.squiditytempprj;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;

public class SquidityApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Str.init(this);
        applySavedTheme();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(Str.getLanguage()));
    }

    // Giao diện sáng / tối / theo hệ thống, chọn trong Settings
    private void applySavedTheme() {
        String theme = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "system");
        AppCompatDelegate.setDefaultNightMode(themeToMode(theme));
    }

    public static int themeToMode(String theme) {
        if ("light".equals(theme)) return AppCompatDelegate.MODE_NIGHT_NO;
        if ("dark".equals(theme)) return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }
}
