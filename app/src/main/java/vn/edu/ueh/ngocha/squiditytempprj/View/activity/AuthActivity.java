package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private boolean registerMode = false;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                            setLoading(false);
                            showError("Bạn đã hủy đăng nhập Google.");
                            return;
                        }

                        try {
                            GoogleSignInAccount account =
                                    GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                            .getResult(ApiException.class);

                            if (account == null || account.getIdToken() == null) {
                                setLoading(false);
                                showError("Không lấy được Google ID Token. Hãy kiểm tra cấu hình OAuth.");
                                return;
                            }

                            AuthCredential credential =
                                    GoogleAuthProvider.getCredential(account.getIdToken(), null);

                            firebaseAuth.signInWithCredential(credential)
                                    .addOnCompleteListener(this, task -> {
                                        if (!task.isSuccessful()) {
                                            setLoading(false);
                                            showError(getErrorMessage(task.getException()));
                                            return;
                                        }

                                        FirebaseUser user = firebaseAuth.getCurrentUser();

                                        if (user == null) {
                                            setLoading(false);
                                            showError("Không lấy được tài khoản Firebase.");
                                            return;
                                        }

                                        completeLogin(user);
                                    });

                        } catch (ApiException e) {
                            setLoading(false);
                            showError("Đăng nhập Google thất bại: " + e.getStatusCode());
                            Log.e("AuthActivity", "Google Sign-In failed", e);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

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
        tvModeToggle.setOnClickListener(
                view -> setRegisterMode(!registerMode)
        );

        btnAuthSubmit.setOnClickListener(view -> validateForm());

        btnGoogleSignIn.setOnClickListener(view -> signInWithGoogle());
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

    private void registerWithEmail(String email, String password, String username) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        showError(getErrorMessage(task.getException()));
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        setLoading(false);
                        showError("Không lấy được thông tin tài khoản.");
                        return;
                    }

                    com.google.firebase.auth.UserProfileChangeRequest request =
                            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                    user.updateProfile(request).addOnCompleteListener(profileTask -> {
                        // Đăng ký không phụ thuộc vào việc Firestore có phản hồi hay không.
                        firebaseAuth.signOut();
                        setLoading(false);

                        etPassword.setText("");
                        etConfirmPassword.setText("");

                        setRegisterMode(false);
                        showSuccess("Đăng ký thành công! Hãy đăng nhập bằng tài khoản vừa tạo.");
                    });
                });
    }

    private void loginWithEmail(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        showError(getErrorMessage(task.getException()));
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        setLoading(false);
                        showError("Không lấy được thông tin tài khoản.");
                        return;
                    }

                    completeLogin(user);
                });
    }

    private void signInWithGoogle() {
        setLoading(true);

        String webClientId = getString(R.string.default_web_client_id);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, options);

        googleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    googleSignInLauncher.launch(signInIntent);
                });
    }

    private void saveUserProfile(
            FirebaseUser user,
            String username,
            String email
    ) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUid());
        profile.put("username", username == null ? "" : username);
        profile.put("email", email == null ? "" : email);
        profile.put("avatarUrl", user.getPhotoUrl() == null
                ? "" : user.getPhotoUrl().toString());
        profile.put("role", "USER");
        profile.put("verified", user.isEmailVerified());
        profile.put("verifiedSource",
                user.isEmailVerified() ? "FIREBASE" : "");

        // Lưu hồ sơ bất đồng bộ; không giữ AuthActivity chờ Firestore.
        firestore.collection("users")
                .document(user.getUid())
                .set(profile, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener(e ->
                        android.util.Log.e(
                                "AuthActivity",
                                "Không lưu được hồ sơ Firestore",
                                e
                        ));
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
            tvAuthStatus.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setVisibility(View.VISIBLE);
    }

    private void clearStatus() {
        tvAuthStatus.setText("");
        tvAuthStatus.setVisibility(View.GONE);
    }
    private void completeLogin(FirebaseUser user) {
        String email = user.getEmail() == null ? "" : user.getEmail();
        String username = user.getDisplayName();

        if (username == null || username.trim().isEmpty()) {
            username = email.contains("@")
                    ? email.substring(0, email.indexOf("@"))
                    : "Người dùng";
        }

        saveUserProfile(user, username, email);

        setLoading(false);
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showSuccess(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setTextColor(android.graphics.Color.rgb(46, 125, 50));
        tvAuthStatus.setVisibility(View.VISIBLE);
    }

}