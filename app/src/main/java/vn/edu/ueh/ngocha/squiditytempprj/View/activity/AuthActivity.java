package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
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
            tvAuthHeaderTitle.setText(Str.t(R.string.auth_header_sign_up));
            btnAuthSubmit.setText(Str.t(R.string.auth_register_btn));
            tvModeToggle.setText(Str.t(R.string.auth_toggle_to_login));

            usernameContainer.setVisibility(View.VISIBLE);
            confirmPasswordContainer.setVisibility(View.VISIBLE);
            btnGoogleSignIn.setText(Str.t(R.string.auth_google_sign_up));
        } else {
            tvAuthHeaderTitle.setText(Str.t(R.string.auth_header_sign_in));
            btnAuthSubmit.setText(Str.t(R.string.auth_sign_in));
            tvModeToggle.setText(Str.t(R.string.auth_toggle_to_register));

            usernameContainer.setVisibility(View.GONE);
            confirmPasswordContainer.setVisibility(View.GONE);
            btnGoogleSignIn.setText(Str.t(R.string.auth_google_sign_in));
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
            showError(Str.t(R.string.auth_err_name));
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            showError(Str.t(R.string.auth_err_email));
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(Str.t(R.string.auth_err_email_format));
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError(Str.t(R.string.auth_err_password));
            etPassword.requestFocus();
            return;
        }

        if (registerMode && password.length() < 8) {
            showError(Str.t(R.string.auth_err_password_short));
            etPassword.requestFocus();
            return;
        }

        if (registerMode && !password.equals(confirmPassword)) {
            showError(Str.t(R.string.auth_err_password_mismatch));
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
                            showError(Str.t(R.string.auth_err_google_invalid));
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
                            showError(Str.t(R.string.auth_err_google_read));
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("AuthActivity", "Credential Manager thất bại", e);
                        setLoading(false);
                        showError(Str.t(R.string.auth_google_failed_with, e.getMessage()));
                    }
                }
        );
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return Str.t(R.string.auth_err_generic);
        }

        String message = exception.getMessage();

        if (message == null) {
            return Str.t(R.string.auth_err_generic);
        }

        if (message.contains("email address is already in use")) {
            return Str.t(R.string.auth_err_email_taken);
        }

        if (message.contains("password is invalid")
                || message.contains("no user record")
                || message.contains("invalid-credential")) {
            return Str.t(R.string.auth_err_wrong_credentials);
        }

        if (message.contains("network")) {
            return Str.t(R.string.auth_err_network);
        }

        return message;
    }

    private void setLoading(boolean loading) {
        btnAuthSubmit.setEnabled(!loading);
        btnGoogleSignIn.setEnabled(!loading);

        if (loading) {
            tvAuthStatus.setText(Str.t(R.string.auth_processing));
            tvAuthStatus.setVisibility(View.VISIBLE);
        }
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
                showSuccess(Str.t(R.string.auth_register_success));
            }
        });
    }

    private void showError(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setVisibility(View.VISIBLE);
    }

    private void clearStatus() {
        tvAuthStatus.setText("");
        tvAuthStatus.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        tvAuthStatus.setText(message);
        tvAuthStatus.setTextColor(android.graphics.Color.rgb(46, 125, 50));
        tvAuthStatus.setVisibility(View.VISIBLE);
    }

}