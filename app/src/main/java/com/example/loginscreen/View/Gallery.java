package com.example.loginscreen.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.ImageData;
import com.example.loginscreen.R;
import com.example.loginscreen.View.Adapters.GalleryAdapter;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.example.loginscreen.ViewModel.GalleryHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Gallery extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private ArrayList<ImageData> imageDataList;
    private String userId;
    private GalleryHelper galleryHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.galleryRecyclerView);
        imageDataList = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(imageDataList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(galleryAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "Грешка: потребителят не е намерен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        galleryHelper = new GalleryHelper(userId, this, imageDataList, galleryAdapter);
        galleryHelper.loadUserPhotosFromFirestore();

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);
            galleryHelper.processImageFromUri(imageUri);
        }

        setupBottomNavigation();
        galleryAdapter.setOnImageLongClickListener((position, imageUrl) -> showDeleteDialog(position, imageUrl));
    }

    private void showDeleteDialog(int position, String imageUrl) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete1, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnYes).setOnClickListener(v -> {
            galleryHelper.deleteImage(position, imageUrl);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupBottomNavigation() {
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
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, Profile.class));
                    return true;
                }
                return false;
            });
        }
    }
}