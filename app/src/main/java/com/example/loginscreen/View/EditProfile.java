package com.example.loginscreen.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginscreen.ModelView.EditProfileHelper;
import com.example.loginscreen.R;
import com.example.loginscreen.View.Firebase.Profile;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText editName, editUsername, editPassword, editEmail, editPhone;
    private Spinner spinnerGender;
    private Button btnSave;
    private ImageView btnBack;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private EditProfileHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        helper = new EditProfileHelper(this);

        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "Няма намерен потребител", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserData();

        btnSave.setOnClickListener(v -> saveUserData());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfile.this, Profile.class);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        editName = findViewById(R.id.editName);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadUserData() {
        helper.loadUserData(userId, new EditProfileHelper.OnUserDataLoaded() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                editName.setText((String) userData.get("name"));
                editUsername.setText((String) userData.get("username"));
                editEmail.setText((String) userData.get("email"));
                editPhone.setText((String) userData.get("phone"));

                String gender = (String) userData.get("gender");
                if ("Male".equals(gender)) {
                    spinnerGender.setSelection(0);
                } else if ("Female".equals(gender)) {
                    spinnerGender.setSelection(1);
                } else {
                    spinnerGender.setSelection(0);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(EditProfile.this, "Грешка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        String name = editName.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Попълнете всички задължителни полета!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("username", username);
        updatedData.put("email", email);
        updatedData.put("phone", phone);
        updatedData.put("gender", gender);

        helper.saveUserData(userId, updatedData, new EditProfileHelper.OnUserDataSaved() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditProfile.this, "Данните са обновени!", Toast.LENGTH_SHORT).show();

                helper.updateAuthCredentials(
                        "",
                        password,
                        email,
                        new EditProfileHelper.OnAuthUpdate() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(EditProfile.this, "Профилът е актуализиран! Моля, влезте отново.", Toast.LENGTH_SHORT).show();
                                redirectToLogin();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(EditProfile.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(EditProfile.this, "Грешка при запис: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
