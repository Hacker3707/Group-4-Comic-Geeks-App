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
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(Str.getLanguage()));
    }
}
