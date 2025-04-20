package com.example.loginscreen.ViewModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.ImageData;
import com.example.loginscreen.View.Adapters.GalleryAdapter;
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

public class GalleryHelper {

    private final String userId;
    private final Context context;
    private final ArrayList<ImageData> imageDataList;
    private final GalleryAdapter galleryAdapter;
    private final FirebaseFirestore db;

    public GalleryHelper(String userId, Context context, ArrayList<ImageData> imageDataList, GalleryAdapter galleryAdapter) {
        this.userId = userId;
        this.context = context;
        this.imageDataList = imageDataList;
        this.galleryAdapter = galleryAdapter;
        this.db = FirebaseFirestore.getInstance();
    }

    public void processImageFromUri(Uri imageUri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                }

                File imageFile = convertBitmapToFile(bitmap, "temp_" + System.currentTimeMillis() + ".jpg");
                String fileName = "photo_" + System.currentTimeMillis() + ".jpg";

                if (imageFile != null) {
                    uploadToFirebaseStorage(imageFile, fileName);
                }

            } catch (IOException e) {
                Toast.makeText(context, "Грешка при зареждане на изображението", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private File convertBitmapToFile(Bitmap bitmap, String fileName) {
        File file = new File(context.getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            return file;
        } catch (IOException e) {
            Log.e("GalleryHelper", "Грешка при запис на файл: " + e.getMessage());
            return null;
        }
    }

    private void uploadToFirebaseStorage(File imageFile, String fileName) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("user-photos")
                .child(userId)
                .child(fileName);
        Uri fileUri = Uri.fromFile(imageFile);
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            Log.d("UPLOAD", "Качено: " + imageUrl);
                            Map<String, Object> photoData = new HashMap<>();
                            photoData.put("uid", userId);
                            photoData.put("url", imageUrl);
                            photoData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                            db.collection("users")
                                    .document(userId)
                                    .collection("photos")
                                    .add(photoData)
                                    .addOnSuccessListener(ref -> {
                                        Log.d("FIRESTORE", "Записано в Firestore");
                                        loadUserPhotosFromFirestore();
                                    });
                        }))
                .addOnFailureListener(e -> Log.e("UPLOAD", "Грешка при качване", e));
    }

    public void loadUserPhotosFromFirestore() {
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
                    Log.e("GALLERY", "Грешка при зареждане на снимките: " + e.getMessage());
                    Toast.makeText(context, "Грешка при зареждане на снимките", Toast.LENGTH_SHORT).show();
                });
    }

    public void deleteImage(int position, String imageUrl) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        photoRef.delete()
                .addOnSuccessListener(aVoid -> db.collection("users")
                        .document(userId)
                        .collection("photos")
                        .whereEqualTo("url", imageUrl)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete();
                            }

                            imageDataList.remove(position);
                            galleryAdapter.notifyItemRemoved(position);
                            Toast.makeText(context, "Снимката е изтрита", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Неуспешно изтриване", Toast.LENGTH_SHORT).show());
    }
}
