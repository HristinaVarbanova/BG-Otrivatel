package com.example.loginscreen;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import android.Manifest;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity implements AddFriend.AddFriendListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "MainActivity стартира успешно!");
        FirebaseFirestore.setLoggingEnabled(true);

        // Инициализиране на текущия потребител
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

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

        // Инициализация на bottomNavigationView
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
        } else {
            Log.e("MainActivity", "bottomNavigationView не е намерен в разметката!");
        }

        // Настройка на камерата
        btnCamera.setOnClickListener(v -> checkCameraPermission());

    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            // Ако разрешението не е дадено, поискайте разрешение
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

    }

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

    // Получаване на резултат от AddFriend диалога
    @Override
    public void onAddFriend(String email) {
        Toast.makeText(this, "Поканен приятел: " + email, Toast.LENGTH_SHORT).show();
        // Тук можеш да добавиш логика за запис в базата
    }

    protected void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Камерата не може да бъде отворена", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Тук можеш да добавиш какво да правиш с изображението (например да го покажеш в ImageView или да го запазиш)
            Toast.makeText(this, "Снимката е направена успешно!", Toast.LENGTH_SHORT).show();
        }
    }




    /*private void addToBeenThere(String name, String info, String location) {
        if (currentUserId == null) {
            Toast.makeText(this, "Потребителят не е логнат!", Toast.LENGTH_SHORT).show();
            return;
        }

        TouristObject place = new TouristObject(name, info, location);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("beenThere")
                .document(name)
                .set(place)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Добавено в Been There!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Грешка при добавяне: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }*/
}
