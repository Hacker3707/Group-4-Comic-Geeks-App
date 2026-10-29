package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.GenreViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.UserProfileViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etBirthDate;
    private EditText etBio;
    private Spinner spGender;
    private EditText etIntroduction;
    private TextView tvEmail;
    private MaterialButton btnSave;
    private ChipGroup chipGroupGenres;
    private List<String> selectedFavoriteGenreIds = new ArrayList<>();

    private GenreViewModel genreViewModel;
    private FirebaseUser currentUser;
    private UserProfileViewModel profileViewModel;

    private final String[] genders = {
            "Chưa chọn",
            "Nam",
            "Nữ",
            "Khác",
            "Không muốn tiết lộ"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editProfile), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this,
                    Str.t(R.string.common_login_first),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etUsername = findViewById(R.id.etEditUsername);
        etBirthDate = findViewById(R.id.etEditBirthDate);
        etBio = findViewById(R.id.etEditBio);
        spGender = findViewById(R.id.spEditGender);
        etIntroduction = findViewById(R.id.etEditIntroduction);
        tvEmail = findViewById(R.id.tvEditEmail);
        btnSave = findViewById(R.id.btnSaveProfile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvEmail.setText(
                currentUser.getEmail() == null
                        ? ""
                        : currentUser.getEmail()
        );

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveProfile());

        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        genreViewModel = new ViewModelProvider(this).get(GenreViewModel.class);
        genreViewModel.getAllGenres().observe(this, genresObserver);

        profileViewModel = new ViewModelProvider(this)
                .get(UserProfileViewModel.class);

        profileViewModel.getProfile().observe(this, state -> {
            if (state == null) return;

            etUsername.setText(state.username == null ? "" : state.username);
            etBio.setText(state.bio == null ? "" : state.bio);
            etBirthDate.setText(
                    state.birthDate == null ? "" : state.birthDate
            );

            selectGender(state.gender);

            selectedFavoriteGenreIds.clear();

            if (state.favGenres != null) {
                selectedFavoriteGenreIds.addAll(state.favGenres);
            }

            applySelectedGenres();
        });

        profileViewModel.getLiveIntroduction().observe(this, introduction -> {
            if (etIntroduction != null) {
                etIntroduction.setText(
                        introduction == null ? "" : introduction
                );
            }
        });

        profileViewModel.getSaving().observe(this, saving -> {
            btnSave.setEnabled(!Boolean.TRUE.equals(saving));
        });

        profileViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(
                        EditProfileActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        profileViewModel.loadCurrentProfile();
        profileViewModel.observeProfileDetails(currentUser.getUid());
    }

    private Observer<List<Genre>> genresObserver = new Observer<List<Genre>>() {
        @Override
        public void onChanged(List<Genre> genres) {
            if (genres == null) return;
            displayGenres(genres);
        }
    };

    private void selectGender(String gender) {
        int selectedPosition = 0;

        if (gender != null) {
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equalsIgnoreCase(gender)) {
                    selectedPosition = i;
                    break;
                }
            }
        }

        spGender.setSelection(selectedPosition);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentDate = etBirthDate.getText().toString().trim();

        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");
                calendar.set(
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[0])
                );
            } catch (Exception ignored) {
                // Nếu dữ liệu cũ không đúng định dạng, dùng ngày hiện tại.
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            day,
                            month + 1,
                            year
                    );
                    etBirthDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );

        dialog.show();
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String introduction = etIntroduction.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        int genderPos = spGender.getSelectedItemPosition();
        String gender = (genderPos <= 0 || genderPos >= genders.length) ? "" : genders[genderPos];

        if (username.isEmpty()) {
            etUsername.setError(Str.t(R.string.profile_err_name_required));
            etUsername.requestFocus();
            return;
        }

        if (!birthDate.isEmpty()
                && !birthDate.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            etBirthDate.setError(Str.t(R.string.profile_err_birth_format));
            return;
        }

        profileViewModel.updateCompleteProfile(
                username,
                bio,
                introduction,
                gender,
                birthDate,
                getSelectedGenres()
        );
    }

    private void styleGenreChip(Chip chip) {
        int purple = Color.parseColor("#493373");
        int white = Color.WHITE;
        // Màu chip chưa chọn đổi theo giao diện sáng / tối
        int idleBg = getColor(R.color.dm_bg_ffffff);
        int idleText = getColor(R.color.dm_fg_493373);
        int border = getColor(R.color.dm_stroke_d8cbea);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] {}
        };

        chip.setCheckable(true);
        chip.setEnsureMinTouchTargetSize(false);

        chip.setChipCornerRadius(dp(20));
        chip.setChipStrokeWidth(dp(1));
        chip.setChipMinHeight(dp(38));
        chip.setTextSize(14);
        chip.setPadding(dp(4), 0, dp(4), 0);

        chip.setChipStrokeColor(new ColorStateList(
                states,
                new int[] { purple, border }
        ));

        chip.setChipBackgroundColor(new ColorStateList(
                states,
                new int[] { purple, idleBg }
        ));

        chip.setTextColor(new ColorStateList(
                states,
                new int[] { white, idleText }
        ));

        chip.setCheckedIconVisible(false);
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void displayGenres(List<Genre> genres) {
        if (chipGroupGenres == null) {
            return;
        }

        chipGroupGenres.setSingleSelection(false);
        chipGroupGenres.setSelectionRequired(false);
        chipGroupGenres.removeAllViews();

        for (Genre genre : genres) {
            Chip chip = new Chip(this);

            chip.setId(View.generateViewId());
            chip.setText(genre.getGenreName());
            styleGenreChip(chip);
            chip.setTag(genre.getGenreId());

            chip.setChecked(
                    selectedFavoriteGenreIds.contains(genre.getGenreId())
            );

            chipGroupGenres.addView(chip);
        }
    }

    private void applySelectedGenres() {
        if (chipGroupGenres == null) {
            return;
        }

        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            View view = chipGroupGenres.getChildAt(i);

            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                Object tag = chip.getTag();

                if (tag != null) {
                    chip.setChecked(
                            selectedFavoriteGenreIds.contains(tag.toString())
                    );
                }
            }
        }
    }

    private List<String> getSelectedGenres() {
        List<String> selected = new ArrayList<>();

        for (int id : chipGroupGenres.getCheckedChipIds()) {
            Chip chip = chipGroupGenres.findViewById(id);

            if (chip != null) {
                selected.add(chip.getTag().toString());
            }
        }

        return selected;
    }
}
