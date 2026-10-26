package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import vn.edu.ueh.ngocha.squiditytempprj.R;
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
            throw new IllegalStateException(Str.t(R.string.auth_no_firebase_account));
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

    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            return null;
        }

        return user.getUid();
    }
}