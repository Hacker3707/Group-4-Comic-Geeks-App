package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.BlockedUsersDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.util.Locale;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;

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
            setupLanguage();
            setupTheme();
            setupAccount();
            setupMisc();
            showCacheSize();
        }

        // Giao diện sáng / tối / theo hệ thống, áp dụng ngay
        private void setupTheme() {
            ListPreference theme = findPreference("theme");
            if (theme == null) return;
            theme.setOnPreferenceChangeListener((preference, newValue) -> {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        vn.edu.ueh.ngocha.squiditytempprj.SquidityApp.themeToMode(String.valueOf(newValue)));
                return true;
            });
        }

        private void onClick(String key, Preference.OnPreferenceClickListener l) {
            Preference p = findPreference(key);
            if (p != null) p.setOnPreferenceClickListener(l);
        }

        private void setupAccount() {
            onClick("reset_password", p -> { sendResetEmail(); return true; });
            onClick("logout", p -> { confirmLogout(); return true; });
            onClick("delete_account", p -> { confirmDeleteAccount(); return true; });
        }

        private void setupMisc() {
            onClick("clear_cache", p -> { clearCache(); return true; });
            onClick("blocked_users", p -> {
                BlockedUsersDialog.show(requireActivity());
                return true;
            });
            onClick("privacy_policy", p -> { info(R.string.pref_privacy_policy, R.string.settings_privacy_text); return true; });
            onClick("help", p -> { info(R.string.pref_help, R.string.settings_help_text); return true; });
            onClick("app_info", p -> { info(R.string.pref_app_info, R.string.settings_info_text); return true; });
            onClick("app_license", p -> { info(R.string.pref_app_license, R.string.settings_license_text); return true; });
            onClick("app_credits", p -> { info(R.string.pref_app_credits, R.string.settings_credits_text); return true; });
        }

        private void info(int titleRes, int textRes) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(Str.t(titleRes))
                    .setMessage(Str.t(textRes))
                    .setPositiveButton(Str.t(R.string.common_close), null)
                    .show();
        }

        private void toast(String text) {
            Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
        }

        private void sendResetEmail() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || user.getEmail() == null) {
                toast(Str.t(R.string.common_login_first));
                return;
            }
            final String email = user.getEmail();
            // Tài khoản đăng nhập bằng Google không có mật khẩu riêng trong app
            boolean hasPassword = false;
            for (com.google.firebase.auth.UserInfo info : user.getProviderData()) {
                if ("password".equals(info.getProviderId())) {
                    hasPassword = true;
                }
            }
            if (!hasPassword) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(Str.t(R.string.pref_reset_password))
                        .setMessage(Str.t(R.string.settings_reset_google, email))
                        .setPositiveButton(Str.t(R.string.common_close), null)
                        .show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(Str.t(R.string.pref_reset_password))
                    .setMessage(Str.t(R.string.settings_reset_confirm, email))
                    .setNegativeButton(Str.t(R.string.common_cancel), null)
                    .setPositiveButton(Str.t(R.string.settings_reset_send), (d, w) -> {
                        // Email gửi đúng ngôn ngữ đang dùng trong app
                        FirebaseAuth.getInstance().setLanguageCode(Str.getLanguage());
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener(v -> new AlertDialog.Builder(requireContext())
                                        .setTitle(Str.t(R.string.pref_reset_password))
                                        .setMessage(Str.t(R.string.settings_reset_sent, email))
                                        .setPositiveButton(Str.t(R.string.common_close), null)
                                        .show())
                                .addOnFailureListener(e -> toast(Str.t(R.string.settings_reset_failed, e.getMessage())));
                    })
                    .show();
        }

        private void goHome() {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        private void confirmLogout() {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                toast(Str.t(R.string.common_login_first));
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(Str.t(R.string.common_logout))
                    .setMessage(Str.t(R.string.logout_confirm))
                    .setNegativeButton(Str.t(R.string.common_cancel), null)
                    .setPositiveButton(Str.t(R.string.common_logout), (d, w) -> {
                        FirebaseAuth.getInstance().signOut();
                        toast(Str.t(R.string.logged_out));
                        goHome();
                    })
                    .show();
        }

        // Xóa cache ảnh (Glide) và các file tạm
        private void clearCache() {
            final Context app = requireContext().getApplicationContext();
            Glide.get(app).clearMemory();
            new Thread(() -> {
                Glide.get(app).clearDiskCache();
                requireActivity().runOnUiThread(() -> toast(Str.t(R.string.settings_cache_cleared)));
            }).start();
        }

        private void confirmDeleteAccount() {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                toast(Str.t(R.string.common_login_first));
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(Str.t(R.string.settings_delete_title))
                    .setMessage(Str.t(R.string.settings_delete_msg))
                    .setNegativeButton(Str.t(R.string.common_cancel), null)
                    .setPositiveButton(Str.t(R.string.common_delete), (d, w) -> deleteAccount(user))
                    .show();
        }

        private void deleteAccount(final FirebaseUser user) {
            final String uid = user.getUid();
            // Xóa tài khoản đăng nhập trước. Nếu Firebase đòi đăng nhập lại thì dừng, chưa xóa gì cả.
            user.delete().addOnSuccessListener(v -> {
                FirebaseFirestore.getInstance().collection("users").document(uid).delete();
                toast(Str.t(R.string.settings_delete_done));
                goHome();
            }).addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                    toast(Str.t(R.string.settings_delete_failed_relogin));
                } else {
                    toast(Str.t(R.string.settings_delete_failed, e.getMessage()));
                }
            });
        }

        // Đổi ngôn ngữ toàn app: English <-> Tiếng Việt
        private void setupLanguage() {
            ListPreference language = findPreference("language");
            if (language == null) return;
            language.setValue(Str.getLanguage());
            language.setOnPreferenceChangeListener((preference, newValue) -> {
                Str.setLanguage(String.valueOf(newValue));
                return true;
            });
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

        private long getDirectorySize(File directory) {
            if (directory == null || !directory.exists()) {
                return 0;
            }

            if (directory.isFile()) {
                return directory.length();
            }

            long size = 0;
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    size += getDirectorySize(file);
                }
            }

            return size;
        }

        private long getCacheSizeBytes() {
            long size = getDirectorySize(requireContext().getCacheDir());

            File externalCache = requireContext().getExternalCacheDir();

            if (externalCache != null) {
                size += getDirectorySize(externalCache);
            }

            return size;
        }

        private String formatCacheSize(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }

            if (bytes < 1024L * 1024) {
                return String.format(Locale.getDefault(),
                        "%.1f KB", bytes / 1024.0);
            }

            return String.format(Locale.getDefault(),
                    "%.1f MB", bytes / (1024.0 * 1024));
        }

        private void showCacheSize() {
            Preference cache = findPreference("clear_cache");

            if (cache == null) return;

            cache.setSummary("Calculating...");

            new Thread(() -> {
                long size = getCacheSizeBytes();
                String summary = "Current cache: " + formatCacheSize(size);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            cache.setSummary(summary);
                        }
                    });
                }
            }).start();
        }
    }


}
