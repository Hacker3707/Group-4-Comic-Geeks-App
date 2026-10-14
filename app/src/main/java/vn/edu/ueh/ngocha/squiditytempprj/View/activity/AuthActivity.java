package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import vn.edu.ueh.ngocha.squiditytempprj.R;

public class AuthActivity extends AppCompatActivity {

    private UserDao userDao;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private TextView tvAuthHeaderTitle;
    private TextView tvModeToggle;
    private TextView tvAuthStatus;

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private LinearLayout usernameContainer;
    private LinearLayout confirmPasswordContainer;

    private MaterialButton btnAuthSubmit;
    private MaterialButton btnGoogleSignIn;

    private boolean registerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        userDao = AppDatabase.getDatabase(getApplicationContext()).userDao();

        initializeViews();
        setRegisterMode(false);
        setupClickListeners();
    }

    private void initializeViews() {
        tvAuthHeaderTitle = findViewById(R.id.tvAuthHeaderTitle);
        tvModeToggle = findViewById(R.id.tvModeToggle);
        tvAuthStatus = findViewById(R.id.tvAuthStatus);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        usernameContainer = findViewById(R.id.usernameContainer);
        confirmPasswordContainer =
                findViewById(R.id.confirmPasswordContainer);

        btnAuthSubmit = findViewById(R.id.btnAuthSubmit);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
    }

    private void setupClickListeners() {
        setupFieldFocusValidation();
        tvModeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRegisterMode(!registerMode);
            }
        });

        btnAuthSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateForm();
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AuthActivity.this,
                        "Cần kết nối Google Sign-In để sử dụng chức năng này.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setupFieldFocusValidation() {
        View.OnFocusChangeListener focusListener =
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            return;
                        }

                        EditText field = (EditText) view;
                        String value = field.getText().toString().trim();

                        if (field.getId() == R.id.etUsername) {
                            if (registerMode && value.isEmpty()) {
                                showError("Vui lòng nhập họ và tên.");
                            }
                        } else if (field.getId() == R.id.etEmail) {
                            if (value.isEmpty()) {
                                showError("Vui lòng nhập email.");
                            } else if (!Patterns.EMAIL_ADDRESS
                                    .matcher(value).matches()) {
                                showError("Email không đúng định dạng.");
                            } else {
                                clearStatus();
                            }
                        } else if (field.getId() == R.id.etPassword) {
                            if (value.isEmpty()) {
                                showError("Vui lòng nhập mật khẩu.");
                            } else if (registerMode && value.length() < 8) {
                                showError("Mật khẩu phải có ít nhất 8 ký tự.");
                            } else {
                                clearStatus();
                            }
                        } else if (field.getId() == R.id.etConfirmPassword) {
                            String password =
                                    etPassword.getText().toString();
                            String confirmPassword =
                                    etConfirmPassword.getText().toString();

                            if (registerMode && confirmPassword.trim().isEmpty()) {
                                showError("Vui lòng xác nhận mật khẩu.");
                            } else if (registerMode
                                    && !password.equals(confirmPassword)) {
                                showError("Mật khẩu xác nhận không khớp.");
                            } else {
                                clearStatus();
                            }
                        }
                    }
                };

        etUsername.setOnFocusChangeListener(focusListener);
        etEmail.setOnFocusChangeListener(focusListener);
        etPassword.setOnFocusChangeListener(focusListener);
        etConfirmPassword.setOnFocusChangeListener(focusListener);
    }

    private void clearStatus() {
        tvAuthStatus.setText("");
        tvAuthStatus.setVisibility(View.GONE);
    }

    private void setRegisterMode(boolean isRegisterMode) {
        registerMode = isRegisterMode;

        if (registerMode) {
            tvAuthHeaderTitle.setText("SIGN UP");
            btnAuthSubmit.setText("ĐĂNG KÝ");
            tvModeToggle.setText("Đã có tài khoản? Đăng nhập");

            usernameContainer.setVisibility(View.VISIBLE);
            confirmPasswordContainer.setVisibility(View.VISIBLE);
            btnGoogleSignIn.setText("Đăng ký bằng Google");
        } else {
            tvAuthHeaderTitle.setText("SIGN IN");
            btnAuthSubmit.setText("ĐĂNG NHẬP");
            tvModeToggle.setText("Chưa có tài khoản? Đăng ký");

            usernameContainer.setVisibility(View.GONE);
            confirmPasswordContainer.setVisibility(View.GONE);
            btnGoogleSignIn.setText("Đăng nhập bằng Google");
        }

        tvAuthStatus.setText("");
        tvAuthStatus.setVisibility(View.GONE);
    }


    private void validateForm() {
        final String email = etEmail.getText().toString()
                .trim().toLowerCase(Locale.ROOT);
        final String password = etPassword.getText().toString();
        final String username = etUsername.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString();
        final boolean isRegister = registerMode;

        if (isRegister && TextUtils.isEmpty(username)) {
            showError("Vui lòng nhập họ và tên.");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            showError("Vui lòng nhập email.");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email không đúng định dạng.");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Vui lòng nhập mật khẩu.");
            etPassword.requestFocus();
            return;
        }

        if (isRegister && password.length() < 8) {
            showError("Mật khẩu phải có ít nhất 8 ký tự.");
            etPassword.requestFocus();
            return;
        }

        if (isRegister && !password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp.");
            etConfirmPassword.requestFocus();
            return;
        }

        tvAuthStatus.setText("Đang xử lý...");
        tvAuthStatus.setVisibility(View.VISIBLE);
        btnAuthSubmit.setEnabled(false);

        databaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isRegister) {
                        int count = userDao.countByEmail(email);

                        if (count > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnAuthSubmit.setEnabled(true);
                                    showError("Email này đã được đăng ký.");
                                }
                            });
                            return;
                        }

                        User user = new User(
                                UUID.randomUUID().toString(),
                                username,
                                email,
                                hashPassword(password),
                                ""
                        );

                        userDao.insert(user);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnAuthSubmit.setEnabled(true);
                                setRegisterMode(false);
                                etEmail.setText(email);
                                etPassword.setText("");
                                etConfirmPassword.setText("");

                                showError(
                                        "Đăng ký thành công! Hãy đăng nhập."
                                );
                            }
                        });

                    } else {
                        final User user = userDao.getByEmail(email);

                        if (user == null ||
                                !hashPassword(password).equals(
                                        user.getPasswordHash())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnAuthSubmit.setEnabled(true);
                                    showError("Email hoặc mật khẩu không đúng.");
                                }
                            });
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnAuthSubmit.setEnabled(true);

                                getSharedPreferences(
                                        "ComicGeeksAuth",
                                        MODE_PRIVATE
                                ).edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("email", user.getEmail())
                                        .apply();

                                Toast.makeText(
                                        AuthActivity.this,
                                        "Đăng nhập SQLite thành công!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnAuthSubmit.setEnabled(true);
                            showError(
                                    "Có lỗi khi truy cập database. "
                                            + "Vui lòng thử lại."
                            );
                        }
                    });
                }
            }
        });
    }

    private void showError(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setVisibility(View.VISIBLE);
    }


    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                    password.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    )
            );

            StringBuilder result = new StringBuilder();

            for (byte b : bytes) {
                result.append(String.format("%02x", b & 0xff));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Không hỗ trợ thuật toán SHA-256", e
            );
        }
    }
}