package com.example.loginscreen.ViewModel;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.loginscreen.Model.HelperClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileHelper {

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public ProfileHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void uploadProfileImage(Uri imageUri, ImageView profileImageView) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("user-profile-photos/" + userId + "/profile.jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveProfileImageUrlToFirestore(imageUrl);
                                Glide.with(context).load(imageUrl).circleCrop().into(profileImageView);
                            }))
                    .addOnFailureListener(e -> Toast.makeText(context, "Грешка при качване: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public void saveProfileImageUrlToFirestore(String imageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId)
                    .update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Профилната снимка е обновена!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Грешка при запис: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
   public void loadUserData(TextView nickname, TextView name, TextView email, TextView gender, TextView phone, ImageView profileImageView) {
       FirebaseUser user = auth.getCurrentUser();
       if (user != null) {
           String userId = user.getUid();
           db.collection("users").document(userId).get()
                   .addOnSuccessListener(document -> {
                       if (document.exists()) {
                           HelperClass userData = document.toObject(HelperClass.class);

                           if (userData != null) {
                               nickname.setText(userData.getUsername());
                               name.setText(userData.getName());
                               email.setText(userData.getEmail());
                               gender.setText(userData.getGender() != null ? userData.getGender() : "Няма въведен пол");
                               phone.setText(userData.getPhone() != null ? userData.getPhone() : "Няма въведен телефонен номер");

                               if (userData.getProfileImageUrl() != null && !userData.getProfileImageUrl().isEmpty()) {
                                   Glide.with(context).load(userData.getProfileImageUrl()).circleCrop().into(profileImageView);
                               }
                           }
                       } else {
                           Toast.makeText(context, "Данните не са намерени!", Toast.LENGTH_SHORT).show();
                       }
                   })
                   .addOnFailureListener(e -> Toast.makeText(context, "Грешка при зареждане: " + e.getMessage(), Toast.LENGTH_SHORT).show());
       }
   }

}