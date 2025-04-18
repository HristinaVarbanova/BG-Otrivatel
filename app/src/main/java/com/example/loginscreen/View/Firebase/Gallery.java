package com.example.loginscreen.View.Firebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.View.Adapters.GalleryAdapter;
import com.example.loginscreen.Model.ImageData;
import com.example.loginscreen.R;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.example.loginscreen.View.MainActivity;
import com.example.loginscreen.View.Notifications;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gallery extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private ArrayList<ImageData> imageDataList;
    private String userId;

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

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);
            processImageFromUri(imageUri);
        }

        loadUserPhotosFromFirestore();

        setupBottomNavigation();
        galleryAdapter.setOnImageLongClickListener((position, imageUrl) -> showDeleteDialog(position, imageUrl));

    }

    private void processImageFromUri(Uri imageUri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                }

                File imageFile = convertBitmapToFile(bitmap, "temp_" + System.currentTimeMillis() + ".jpg");
                String fileName = "photo_" + System.currentTimeMillis() + ".jpg";

                if (imageFile != null) {
                    uploadToFirebaseStorage(imageFile, fileName);
                }

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Грешка при зареждане на изображението", Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            }
        });
    }

    private void uploadToFirebaseStorage(File imageFile, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("user-photos")
                .child(userId)
                .child(fileName);

        Uri fileUri = Uri.fromFile(imageFile);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            Log.d("UPLOAD", "Качено в Firebase Storage: " + imageUrl);

                            Map<String, Object> photoData = new HashMap<>();
                            photoData.put("uid", userId);
                            photoData.put("url", imageUrl);
                            photoData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(userId)
                                    .collection("photos")
                                    .add(photoData)
                                    .addOnSuccessListener(ref -> {
                                        Log.d("FIRESTORE", "Записано в Firestore");
                                        loadUserPhotosFromFirestore();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIRESTORE", "Firestore грешка", e));
                        }))
                .addOnFailureListener(e -> Log.e("UPLOAD", " Firebase Storage грешка", e));
    }

    private void loadUserPhotosFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("photos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    imageDataList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String url = document.getString("url");
                        if (url != null) {
                            imageDataList.add(new ImageData(url));
                        }
                    }
                    galleryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("GALLERY", "❌ Грешка при зареждане на снимките: " + e.getMessage());
                    Toast.makeText(this, "Грешка при зареждане на снимките", Toast.LENGTH_SHORT).show();
                });
    }

    private File convertBitmapToFile(Bitmap bitmap, String fileName) {
        File file = new File(getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            return file;
        } catch (IOException e) {
            Log.e("GALLERY", "Грешка при запис на файл: " + e.getMessage());
            return null;
        }
    }

    private void showDeleteDialog(int position, String imageUrl) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete1, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnYes).setOnClickListener(v -> {
            deleteImage(position, imageUrl);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteImage(int position, String imageUrl) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        photoRef.delete()
                .addOnSuccessListener(aVoid -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document(userId)
                            .collection("photos")
                            .whereEqualTo("url", imageUrl)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    doc.getReference().delete();
                                }

                                // Махни от списъка и обнови адаптера
                                imageDataList.remove(position);
                                galleryAdapter.notifyItemRemoved(position);

                                Toast.makeText(this, "Снимката е изтрита", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Неуспешно изтриване", Toast.LENGTH_SHORT).show();
                });
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
