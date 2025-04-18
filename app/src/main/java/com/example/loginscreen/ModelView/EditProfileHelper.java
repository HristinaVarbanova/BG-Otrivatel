package com.example.loginscreen.ModelView;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileHelper {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;

    public EditProfileHelper(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    public interface OnUserDataLoaded {
        void onSuccess(Map<String, Object> userData);
        void onFailure(String errorMessage);
    }

    public void loadUserData(String userId, OnUserDataLoaded callback) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                callback.onSuccess(snapshot.getData());
            } else {
                callback.onFailure("Потребителят не е намерен");
            }
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface OnUserDataSaved {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void saveUserData(String userId, Map<String, Object> updatedData, OnUserDataSaved callback) {
        db.collection("users").document(userId)
                .update(updatedData)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface OnAuthUpdate {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void updateAuthCredentials(String oldPassword, String newPassword, String newEmail, OnAuthUpdate callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Няма текущ потребител.");
            return;
        }

        if (!newPassword.isEmpty()) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailure("Грешка при смяна на паролата: " + task.getException().getMessage());

                }
            });
        }

        if (!newEmail.equals(user.getEmail())) {
            user.updateEmail(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Грешка при смяна на имейл: " + task.getException().getMessage());
                }
            });
        } else {
            callback.onSuccess();
        }
    }
}
