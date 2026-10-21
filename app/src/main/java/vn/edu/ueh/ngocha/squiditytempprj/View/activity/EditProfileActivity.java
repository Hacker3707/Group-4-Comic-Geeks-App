package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etBirthDate;
    private EditText etBio;
    private Spinner spGender;
    private EditText etIntroduction;
    private TextView tvEmail;
    private MaterialButton btnSave;

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
    }

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
}
