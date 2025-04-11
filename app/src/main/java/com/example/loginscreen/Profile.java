package com.example.loginscreen;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;  // Константа за отваряне на галерията

    private TextView profileNickname, profileNameTextView, profileEmailTextView, profileGenderTextView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView profileImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Инициализация на UI елементите
        profileNickname = findViewById(R.id.textView6);
        profileNameTextView = findViewById(R.id.ProfileName);
        profileEmailTextView = findViewById(R.id.ProfileEmail);
        profileGenderTextView = findViewById(R.id.ProfileGender);
        profileImageView = findViewById(R.id.imageView5);

        profileImageView.setOnClickListener(v -> {
            // Извикваме метод за отваряне на галерията
            openGallery();
        });

        FloatingActionButton btnCamera = findViewById(R.id.floatingActionButton);

        loadUserData();  // Зареждаме данните от Firestore

        // Навигация за бутоните
        LinearLayout btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfile.class);
            startActivity(intent);
        });

        LinearLayout btnFriends = findViewById(R.id.btnfRiends);
        btnFriends.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Friends.class);
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

    // Метод за отваряне на галерията
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // Метод за обработка на резултат от избора на изображение от галерията
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String imageUrl = selectedImage.toString(); // Получаваме URL на изображението

            // Извикваме метода за запис в Firestore
            saveProfileImageUrlToFirestore(imageUrl);

            // Зареждаме изображението в ImageView с помощта на Glide
            Glide.with(this)
                    .load(selectedImage)
                    .circleCrop()  // Можем да използваме circleCrop(), за да направим изображението кръгло
                    .into(profileImageView);  // Зареждаме снимката в ImageView
        }
    }

    // Метод за запазване на URL на снимката в Firestore
    private void saveProfileImageUrlToFirestore(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            // Използваме .update(), за да актуализираме само снимката
            userRef.update("profileImageUrl", imageUrl)  // Обновяваме само URL на снимката
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
                    String gender = documentSnapshot.getString("gender"); // Взимаме стойността за пола
                    String imageUrl = documentSnapshot.getString("profileImageUrl"); // Вземаме URL на снимката от Firestore
                    String phone = documentSnapshot.getString("phone"); // Вземаме телефонния номер от Firestore

                    // Зареждаме информацията в UI
                    if (username != null) profileNickname.setText(username);
                    if (name != null) profileNameTextView.setText(name);
                    if (email != null) profileEmailTextView.setText(email);
                    if (gender != null && !gender.isEmpty()) {
                        profileGenderTextView.setText(gender);
                    } else {
                        profileGenderTextView.setText("Все още няма въведен пол");
                    }

                    // Зареждаме телефонния номер
                    if (phone != null && !phone.isEmpty()) {
                        profilePhoneTextView.setText(phone); // Зареждаме телефона в TextView
                    } else {
                        profilePhoneTextView.setText("Няма въведен телефонен номер");
                    }

                    // Ако има снимка в Firestore, я зареждаме
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()  // Поставяме снимката в кръг
                                .into(profileImageView);  // Зареждаме снимката в ImageView
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
