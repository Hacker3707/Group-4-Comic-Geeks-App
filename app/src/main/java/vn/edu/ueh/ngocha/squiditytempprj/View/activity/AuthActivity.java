package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.AuthViewModel;

import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;

import java.util.Locale;
import vn.edu.ueh.ngocha.squiditytempprj.R;

public class AuthActivity extends AppCompatActivity {

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
    private MaterialButton btnExitToLogin;
    private ImageButton btnAuthBack;
    private AuthViewModel authViewModel;

    private boolean registerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initializeViews();

        authViewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);

        setRegisterMode(false);
        setupClickListeners();
        observeAuthState();
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
        btnExitToLogin = findViewById(R.id.btnExitToLogin);
        btnAuthBack = findViewById(R.id.btnAuthBack);
    }

    private void setupClickListeners() {
        tvModeToggle.setOnClickListener(
                view -> setRegisterMode(!registerMode)
        );

        btnAuthSubmit.setOnClickListener(view -> validateForm());

        btnGoogleSignIn.setOnClickListener(view -> signInWithGoogle());

        if (btnAuthBack != null) {
            btnAuthBack.setOnClickListener(view -> finish());
        }

        if (btnExitToLogin != null) {
            btnExitToLogin.setOnClickListener(view -> exitToLoginScreen());
        }
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

        clearStatus();
    }

    private void validateForm() {
        String email = etEmail.getText().toString()
                .trim().toLowerCase(Locale.ROOT);
        String password = etPassword.getText().toString();
        String username = etUsername.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (registerMode && TextUtils.isEmpty(username)) {
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

        if (registerMode && password.length() < 8) {
            showError("Mật khẩu phải có ít nhất 8 ký tự.");
            etPassword.requestFocus();
            return;
        }

        if (registerMode && !password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp.");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        if (registerMode) {
            registerWithEmail(email, password, username);
        } else {
            loginWithEmail(email, password);
        }
    }

    private void registerWithEmail(
            String email,
            String password,
            String username
    ) {
        authViewModel.registerWithEmail(username, email, password);
    }

    private void loginWithEmail(String email, String password) {
        authViewModel.loginWithEmail(email, password);
    }

    private void signInWithGoogle() {
        setLoading(true);

        CredentialManager credentialManager = CredentialManager.create(this);

        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(
                        getString(R.string.default_web_client_id)
                ).build();

        GetCredentialRequest request =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(googleOption)
                        .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                getMainExecutor(),
                new androidx.credentials.CredentialManagerCallback<
                        GetCredentialResponse,
                        GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();

                        if (!(credential instanceof CustomCredential)
                                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                .equals(credential.getType())) {
                            setLoading(false);
                            showError("Không nhận được thông tin tài khoản Google hợp lệ.");
                            return;
                        }

                        try {
                            GoogleIdTokenCredential googleCredential =
                                    GoogleIdTokenCredential.createFrom(
                                            credential.getData()
                                    );

                            String idToken = googleCredential.getIdToken();

                            AuthCredential firebaseCredential =
                                    GoogleAuthProvider.getCredential(idToken, null);

                            authViewModel.loginWithGoogle(firebaseCredential);

                        } catch (Exception e) {
                            Log.e("AuthActivity", "Lỗi đọc Google ID token", e);
                            setLoading(false);
                            showError("Không thể đọc thông tin tài khoản Google.");
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("AuthActivity", "Credential Manager thất bại", e);
                        setLoading(false);
                        showError("Không thể đăng nhập Google: " + e.getMessage());
                    }
                }
        );
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Có lỗi xảy ra. Vui lòng thử lại.";
        }

        String message = exception.getMessage();

        if (message == null) {
            return "Có lỗi xảy ra. Vui lòng thử lại.";
        }

        if (message.contains("email address is already in use")) {
            return "Email này đã được đăng ký.";
        }

        if (message.contains("password is invalid")
                || message.contains("no user record")
                || message.contains("invalid-credential")) {
            return "Email hoặc mật khẩu không đúng.";
        }

        if (message.contains("network")) {
            return "Không có kết nối mạng. Vui lòng thử lại.";
        }

        return message;
    }

    private void setLoading(boolean loading) {
        btnAuthSubmit.setEnabled(!loading);
        btnGoogleSignIn.setEnabled(!loading);

        if (loading) {
            tvAuthStatus.setText("Đang xử lý...");
            tvAuthStatus.setTextColor(android.graphics.Color.rgb(100, 100, 100));
            tvAuthStatus.setVisibility(View.VISIBLE);
            if (btnExitToLogin != null) {
                btnExitToLogin.setVisibility(View.VISIBLE);
                btnExitToLogin.setEnabled(true);
            }
        }
    }

    private void exitToLoginScreen() {
        setLoading(false);
        setRegisterMode(false);
        clearStatus();
        if (btnExitToLogin != null) {
            btnExitToLogin.setVisibility(View.GONE);
        }
        Toast.makeText(this, "Đã quay lại màn hình đăng nhập", Toast.LENGTH_SHORT).show();
    }

    private void observeAuthState() {
        authViewModel.getLoading().observe(this, loading -> {
            if (loading != null) {
                setLoading(loading);
            }
        });

        authViewModel.getError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showError(message);
            }
        });

        authViewModel.getLoginResult().observe(this, user -> {
            if (user != null) {
                setResult(RESULT_OK);
                finish();
            }
        });

        authViewModel.getRegistered().observe(this, registered -> {
            if (Boolean.TRUE.equals(registered)) {
                setRegisterMode(false);
                etPassword.setText("");
                etConfirmPassword.setText("");
                showSuccess("Đăng ký thành công! Hãy đăng nhập.");
            }
        });
    }

    private void showError(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setTextColor(android.graphics.Color.rgb(198, 40, 40));
        tvAuthStatus.setVisibility(View.VISIBLE);
        if (btnExitToLogin != null) {
            btnExitToLogin.setVisibility(View.VISIBLE);
        }
    }

    private void clearStatus() {
        tvAuthStatus.setText("");
        tvAuthStatus.setVisibility(View.GONE);
        if (btnExitToLogin != null) {
            btnExitToLogin.setVisibility(View.GONE);
        }
    }

    private void showSuccess(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setTextColor(android.graphics.Color.rgb(46, 125, 50));
        tvAuthStatus.setVisibility(View.VISIBLE);
    }

}