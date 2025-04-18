package com.example.loginscreen.View.Firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.loginscreen.R;
import com.example.loginscreen.View.EditProfile;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.example.loginscreen.View.MainActivity;
import com.example.loginscreen.View.Notifications;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profile extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;  // Константа за отваряне на галерията

    private TextView profileNickname, profileNameTextView, profileEmailTextView, profileGenderTextView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private TextView profilePhoneTextView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileNickname = findViewById(R.id.textView6);
        profileNameTextView = findViewById(R.id.ProfileName);
        profileEmailTextView = findViewById(R.id.ProfileEmail);
        profileGenderTextView = findViewById(R.id.ProfileGender);
        profileImageView = findViewById(R.id.imageView5);
        profilePhoneTextView = findViewById(R.id.profilePhoneTextView);


        profileImageView.setOnClickListener(v -> {
            openGallery();
        });

        loadUserData();

        LinearLayout btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfile.class);
            startActivity(intent);
        });

        LinearLayout btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Gallery.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(Profile.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(Profile.this, Notifications.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    StorageReference storageRef = FirebaseStorage.getInstance()
                            .getReference("user-profile-photos/" + userId + "/profile.jpg");

                    storageRef.putFile(selectedImage)
                            .addOnSuccessListener(taskSnapshot -> {
                                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    saveProfileImageUrlToFirestore(downloadUrl);

                                    Glide.with(this)
                                            .load(downloadUrl)
                                            .circleCrop()
                                            .into(profileImageView);
                                });
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Грешка при качване на снимка: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                }
            }

            Glide.with(this)
                    .load(selectedImage)
                    .circleCrop()
                    .into(profileImageView);
        }
    }
    private void saveProfileImageUrlToFirestore(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            userRef.update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "Профилната снимка е обновена!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Грешка при обновяване на снимката: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String gender = documentSnapshot.getString("gender");
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    String phone = documentSnapshot.getString("phone");

                    if (username != null) profileNickname.setText(username);
                    if (name != null) profileNameTextView.setText(name);
                    if (email != null) profileEmailTextView.setText(email);
                    if (gender != null && !gender.isEmpty()) {
                        profileGenderTextView.setText(gender);
                    } else {
                        profileGenderTextView.setText("Все още няма въведен пол");
                    }

                    String phoneText = (phone != null && !phone.isEmpty()) ? phone : "Няма въведен телефонен номер";
                    profilePhoneTextView.setText(phoneText);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(profileImageView);
                    }
                } else {
                    Toast.makeText(this, "Данните не са намерени!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Грешка при зареждане: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Няма вписан потребител!", Toast.LENGTH_SHORT).show();
        }
    }


}
