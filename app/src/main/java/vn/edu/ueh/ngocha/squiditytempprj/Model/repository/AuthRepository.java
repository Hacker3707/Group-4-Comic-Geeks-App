package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthRepository {
    public Task<Void> updateDisplayName(String username) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            throw new IllegalStateException("Chưa có tài khoản Firebase.");
        }

        UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();

        return user.updateProfile(request);
    }

    private final FirebaseAuth firebaseAuth =
            FirebaseAuth.getInstance();

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Task<AuthResult> loginWithEmail(
            String email,
            String password
    ) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> registerWithEmail(
            String email,
            String password
    ) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginWithGoogle(AuthCredential credential) {
        return firebaseAuth.signInWithCredential(credential);
    }

    public void logout() {
        firebaseAuth.signOut();
    }
}