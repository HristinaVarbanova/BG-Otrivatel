package com.example.loginscreen.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loginscreen.ViewModel.ProfileHelper;
import com.example.loginscreen.R;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Profile extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;

    private TextView profileNickname, profileNameTextView, profileEmailTextView, profileGenderTextView, profilePhoneTextView;
    private ImageView profileImageView;
    private FirebaseAuth auth;
    private ProfileHelper profileHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        profileHelper = new ProfileHelper(this);

        profileNickname = findViewById(R.id.textView6);
        profileNameTextView = findViewById(R.id.ProfileName);
        profileEmailTextView = findViewById(R.id.ProfileEmail);
        profileGenderTextView = findViewById(R.id.ProfileGender);
        profileImageView = findViewById(R.id.imageView5);
        profilePhoneTextView = findViewById(R.id.profilePhoneTextView);

        profileImageView.setOnClickListener(v -> openGallery());

        profileHelper.loadUserData(profileNickname, profileNameTextView, profileEmailTextView, profileGenderTextView, profilePhoneTextView, profileImageView);

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> startActivity(new Intent(this, EditProfile.class)));
        findViewById(R.id.btnGallery).setOnClickListener(v -> startActivity(new Intent(this, Gallery.class)));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(this, Notifications.class));
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
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
                profileHelper.uploadProfileImage(selectedImage, profileImageView);
            }
        }
    }
}