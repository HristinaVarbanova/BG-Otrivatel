package com.example.loginscreen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText editName, editUsername, editPassword, editEmail, editPhone;
    private Spinner spinnerGender;
    private Button btnSave;
    private ImageView btnBack;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        // Свързване с Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "Няма намерен потребител", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Свързване на UI елементите
        editName = findViewById(R.id.editName);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // ⚡️ Добавяне на адаптер за Spinner (пол: мъж/жена)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options, // трябва да го имаш в strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Зареждане на текущите данни на потребителя
        loadUserData();

        // Запазване на промените в Firebase
        btnSave.setOnClickListener(v -> saveUserData());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfile.this, Profile.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editName.setText(documentSnapshot.getString("name"));
                editUsername.setText(documentSnapshot.getString("username"));
                editEmail.setText(documentSnapshot.getString("email"));
                editPhone.setText(documentSnapshot.getString("phone"));

                String gender = documentSnapshot.getString("gender");

                if (gender != null) {
                    if (gender.equals("Male")) {
                        spinnerGender.setSelection(0);
                    } else if (gender.equals("Female")) {
                        spinnerGender.setSelection(1);
                    } else {
                        // Ако няма валидна стойност, избери индекс 0
                        spinnerGender.setSelection(0);
                    }
                } else {
                    // Ако няма стойност, избери индекс 0
                    spinnerGender.setSelection(0);
                }
            } else {
                Toast.makeText(this, "Потребителят не е намерен", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Грешка при зареждане!", Toast.LENGTH_SHORT).show());
    }
    /*private void saveUserData() {
        String newName = editName.getText().toString().trim();
        String newUsername = editUsername.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String newGender = spinnerGender.getSelectedItem().toString();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Попълнете всички полета!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Запазване в Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("username", newUsername);
        userData.put("email", newEmail);
        userData.put("phone", newPhone);
        userData.put("gender", newGender);

        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfile.this, "Данните са обновени!", Toast.LENGTH_SHORT).show();
                    checkIfRedirectNeeded(newPassword, newEmail);
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Грешка при запис!", Toast.LENGTH_SHORT).show());
    }

    private void checkIfRedirectNeeded(String newPassword, String newEmail) {
        boolean shouldRedirect = true;

        // Проверяваме дали трябва да се сменя парола
        if (!TextUtils.isEmpty(newPassword)) {
            shouldRedirect = false;
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfile.this, "Паролата е обновена!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfile.this, "Грешка при смяна на паролата!", Toast.LENGTH_SHORT).show();
                }
                redirectToProfile();
            });
        }

        // Проверяваме дали трябва да се сменя имейл
        if (!newEmail.equals(user.getEmail())) {
            shouldRedirect = false;
            user.updateEmail(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfile.this, "Имейлът е обновен!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfile.this, "Грешка при смяна на имейла!", Toast.LENGTH_SHORT).show();
                }
                redirectToProfile();
            });
        }

        if (shouldRedirect) {
            redirectToProfile();
        }
    }

  */
    private void saveUserData() {
        String newName = editName.getText().toString().trim();
        String newUsername = editUsername.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim(); // Новата парола
        String newEmail = editEmail.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String newGender = spinnerGender.getSelectedItem().toString();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Попълнете всички полета!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Записваме актуализираните данни в Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("username", newUsername);
        userData.put("email", newEmail);
        userData.put("phone", newPhone);
        userData.put("gender", newGender);

        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfile.this, "Данните са обновени!", Toast.LENGTH_SHORT).show();
                    checkIfRedirectNeeded(newPassword, newEmail); // Проверка дали да се смени парола и имейл
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Грешка при запис!", Toast.LENGTH_SHORT).show());
    }

    private void checkIfRedirectNeeded(String newPassword, String newEmail) {
        boolean shouldRedirect = true;

        // Проверяваме дали трябва да се сменя парола
        if (!TextUtils.isEmpty(newPassword)) {
            shouldRedirect = false;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Преавтентикация с старата парола (ако е нужно) преди да я сменим
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), "oldPassword"); // Старото парола трябва да бъде добавена тук
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(EditProfile.this, "Паролата е обновена!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditProfile.this, "Грешка при смяна на паролата!", Toast.LENGTH_SHORT).show();
                            }
                            redirectToProfile();
                        });
                    } else {
                        Toast.makeText(EditProfile.this, "Неуспешна преавтентикация!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Проверяваме дали трябва да се сменя имейл
        if (!newEmail.equals(user.getEmail())) {
            shouldRedirect = false;
            user.updateEmail(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfile.this, "Имейлът е обновен!", Toast.LENGTH_SHORT).show();

                    // След като имейлът е променен, потребителят трябва да влезе отново с новия имейл
                    FirebaseAuth.getInstance().signOut();  // Излизаме от акаунта
                    Intent loginIntent = new Intent(EditProfile.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                } else {
                    Toast.makeText(EditProfile.this, "Грешка при смяна на имейла!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (shouldRedirect) {
            redirectToProfile();
        }
    }

    private void redirectToProfile() {
        Intent intent = new Intent(EditProfile.this, Profile.class);
        startActivity(intent);
        finish(); // Затваряме EditProfile
    }


}






