package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceFragmentCompat;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class AppSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment
            extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(
                Bundle savedInstanceState,
                String rootKey) {

            setPreferencesFromResource(
                    R.xml.preferences,
                    rootKey
            );
        }
    }

}