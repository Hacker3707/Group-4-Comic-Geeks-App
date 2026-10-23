package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

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
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

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
                    "Vui lòng đăng nhập trước.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();

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

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());

        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        genreViewModel = new ViewModelProvider(this).get(GenreViewModel.class);
        genreViewModel.getAllGenres().observe(this, genresObserver);

    }

    private Observer<List<Genre>> genresObserver = new Observer<List<Genre>>() {
        @Override
        public void onChanged(List<Genre> genres) {
            if (genres == null) return;
            displayGenres(genres);
        }
    };

    private void loadProfile() {
        String uid = currentUser.getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (isFinishing() || isDestroyed()) return;

                    String username = document.getString("username");
                    String bio = document.getString("bio");
                    String gender = document.getString("gender");
                    String birthDate = document.getString("birthDate");
                    String introduction = document.getString("introduction");
                    selectedFavoriteGenreIds.clear();

                    Object rawGenres = document.get("favGenres");

                    if (rawGenres instanceof List<?>) {
                        for (Object item : (List<?>) rawGenres) {
                            if (item instanceof String) {
                                selectedFavoriteGenreIds.add((String) item);
                            }
                        }
                    }

                    applySelectedGenres();

                    if (username == null || username.trim().isEmpty()) {
                        username = currentUser.getDisplayName();
                    }

                    etUsername.setText(
                            username == null ? "" : username
                    );

                    etBio.setText(bio == null ? "" : bio);
                    if (etIntroduction != null) {
                        etIntroduction.setText(
                                introduction == null ? "" : introduction
                        );
                    }

                    etBirthDate.setText(
                            birthDate == null ? "" : birthDate
                    );

                    selectGender(gender);
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this,
                            "Không tải được hồ sơ: "
                                    + error.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

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
        String birthDate = etBirthDate.getText().toString().trim();
        String gender = spGender.getSelectedItem().toString();
        String introduction = etIntroduction == null
                ? ""
                : etIntroduction.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên hiển thị.");
            etUsername.requestFocus();
            return;
        }

        if (!birthDate.isEmpty() && !birthDate.matches(
                "^\\d{2}/\\d{2}/\\d{4}$")) {
            etBirthDate.setError("Ngày sinh không đúng định dạng.");
            return;
        }

        btnSave.setEnabled(false);

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("bio", bio);
        data.put("introduction", introduction);
        data.put("gender", gender.equals("Chưa chọn") ? "" : gender);
        data.put("birthDate", birthDate);
        data.put("email",
                currentUser.getEmail() == null
                        ? ""
                        : currentUser.getEmail());
        data.put("favGenres", getSelectedGenres());

        firestore.collection("users")
                .document(currentUser.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    UserProfileChangeRequest request =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();



                    currentUser.updateProfile(request)
                            .addOnCompleteListener(task -> {
                                btnSave.setEnabled(true);

                                Toast.makeText(
                                        EditProfileActivity.this,
                                        task.isSuccessful()
                                                ? "Đã lưu thông tin cá nhân."
                                                : "Đã lưu Firestore nhưng chưa đồng bộ tên Firebase Auth.",
                                        Toast.LENGTH_LONG
                                ).show();

                                if (task.isSuccessful()) {
                                    finish();
                                }
                            });
                })
                .addOnFailureListener(error -> {
                    btnSave.setEnabled(true);

                    Toast.makeText(this,
                            "Lưu thất bại: "
                                    + error.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                });

    }

    private void styleGenreChip(Chip chip) {
        int purple = Color.parseColor("#493373");
        int white = Color.WHITE;
        int border = Color.parseColor("#D8CBEA");

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
                new int[] { purple, white }
        ));

        chip.setTextColor(new ColorStateList(
                states,
                new int[] { white, purple }
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
