package com.example.loginscreen.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.loginscreen.Dialogs.AddFriend;
import com.example.loginscreen.R;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import android.Manifest;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AddFriend.AddFriendListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "MainActivity стартира успешно!");

        Button btnWishlist = findViewById(R.id.wishlist);
        Button btnBeenThere = findViewById(R.id.beenthere);
        Button btnPlan = findViewById(R.id.plan);
        Button btnFriends = findViewById(R.id.friends);
        Button btnAddFriends = findViewById(R.id.addfriends);
        FloatingActionButton btnCamera = findViewById(R.id.floatingActionButton);

        btnWishlist.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Wishlist.class)));
        btnBeenThere.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BeenThere.class)));
        btnPlan.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Plan.class)));
        btnFriends.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Friends.class)));

        btnAddFriends.setOnClickListener(v -> {
            AddFriend addFriendDialog = new AddFriend();
            addFriendDialog.show(getSupportFragmentManager(), "AddFriendDialog");
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(MainActivity.this, Notifications.class));
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, Profile.class));
                    return true;
                }
                return false;
            });
        }

        btnCamera.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Нямате разрешение за използване на камерата", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                imageUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile
                );

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Грешка при създаване на файл", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String fileName = "IMG_" + System.currentTimeMillis();
        File storageDir = getCacheDir();
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("MAIN_CAMERA", "📷 URI: " + imageUri);
            Intent intent = new Intent(MainActivity.this, Gallery.class);
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);
        }
    }

    @Override
    public void onAddFriend(String email) {
        Toast.makeText(this, "Поканен приятел: " + email, Toast.LENGTH_SHORT).show();
    }
}