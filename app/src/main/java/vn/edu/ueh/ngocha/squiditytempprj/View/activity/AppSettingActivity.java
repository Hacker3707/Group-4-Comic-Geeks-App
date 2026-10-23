package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class AppSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thanh trạng thái trong suốt, icon sáng để nhìn rõ trên nền gradient tím
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_app_setting);

        View root = findViewById(R.id.main);
        View toolbar = findViewById(R.id.settingsToolbar);

        // Đẩy toolbar xuống dưới status bar, chừa chỗ cho navigation bar ở dưới
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    bars.top,
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
            );
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsContainer, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            showAppVersion();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Bỏ đường kẻ mặc định, mỗi mục đã là một thẻ bo góc riêng
            setDivider(null);
            setDividerHeight(0);

            float density = getResources().getDisplayMetrics().density;
            getListView().setClipToPadding(false);
            getListView().setPadding(
                    0,
                    (int) (8 * density),
                    0,
                    (int) (24 * density)
            );
        }

        // Hiện đúng versionName của app thay vì chuỗi cứng trong XML
        private void showAppVersion() {
            Preference version = findPreference("app_version");
            if (version == null) return;

            try {
                String name = requireContext()
                        .getPackageManager()
                        .getPackageInfo(requireContext().getPackageName(), 0)
                        .versionName;
                version.setSummary(name);
            } catch (PackageManager.NameNotFoundException ignored) {
                // Giữ nguyên summary mặc định trong preferences.xml
            }
        }
    }
}
