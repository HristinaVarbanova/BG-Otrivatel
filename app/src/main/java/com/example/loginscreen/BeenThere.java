package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeenThere extends AppCompatActivity{
    private TextView starsTextView;
    private RecyclerView recyclerView;
    private List<TouristObject> visitedObjects; // Списък с обектите, които съм посетила
    private int stars; // Броя на звездите
    private TouristObjectsAdapter adapter; // Адаптер за RecyclerView
    private FirestoreHelper firestoreHelper; // За връзка с Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beenthere);

        // Инициализация на FirestoreHelper
        firestoreHelper = new FirestoreHelper();

        // Инициализация на списъка с посетени обекти
        visitedObjects = new ArrayList<>();
        stars = 0;

        // Инициализация на UI елементите
        starsTextView = findViewById(R.id.starsTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация на адаптера за RecyclerView
        adapter = new TouristObjectsAdapter(this, visitedObjects, touristObject -> {
            // Метод, който ще се извика при клик върху елемент от списъка
            showPlaceDetailsDialog(touristObject);
        });
        recyclerView.setAdapter(adapter);

        // Зареждаме обектите от Firestore
        loadVisitedObjectsFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(this, Notifications.class));
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
            }
            return true;
        });
    }

    // Зареждане на обектите от Firestore
    private void loadVisitedObjectsFromFirestore() {
        firestoreHelper.loadVisitedObjects(this, new FirestoreHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(List<TouristObject> touristObjects) {
                // Обновяваме списъка с туристическите обекти
                visitedObjects.clear();
                visitedObjects.addAll(touristObjects);
                stars = visitedObjects.size(); // Броят на звездите е равен на броя на обектите
                adapter.notifyDataSetChanged(); // Обновяване на адаптера
                updateStars(); // Обновяване на текстовото поле със звездите
            }

            @Override
            public void onDataFailed(String errorMessage) {
                // Ако има грешка при зареждане на данни
                Toast.makeText(BeenThere.this, "Грешка при зареждане на данни: " + errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataLoaded(TouristObject touristObject) {
                // Празен метод, защото не го използваме в този случай
            }
        });
    }

    private void updateStars() {
        String starsText = "Stars ⭐: " + stars;
        starsTextView.setText(starsText);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("stars", stars)
                .addOnSuccessListener(unused -> {
                    checkFriendsForMoreStars(userId, stars); // 👈 ново
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка при запис на звезди в Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkFriendsForMoreStars(String myUserId, int myStars) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(myUserId)
                .collection("friends")
                .get()
                .addOnSuccessListener(friendSnapshots -> {
                    for (var friendDoc : friendSnapshots) {
                        String friendId = friendDoc.getId(); // UID на приятеля

                        db.collection("users").document(friendId).get().addOnSuccessListener(friendSnapshot -> {
                            Long friendStarsLong = friendSnapshot.getLong("stars");
                            String friendName = friendSnapshot.getString("name");

                            if (friendStarsLong != null && friendStarsLong > myStars) {
                                String friendNameSafe = (friendName != null) ? friendName : "Приятел";
                                String expectedMessage = friendNameSafe + " ви надмина по звезди 😢!";

                                // 🔁 Проверка дали вече има такова известие
                                db.collection("users")
                                        .document(myUserId)
                                        .collection("notifications")
                                        .whereEqualTo("type", "star_rivalry")
                                        .whereEqualTo("message", expectedMessage)
                                        .get()
                                        .addOnSuccessListener(existingNotifs -> {
                                            if (existingNotifs.isEmpty()) {
                                                // ✅ Няма такова – добавяме го
                                                Map<String, Object> notification = new HashMap<>();
                                                notification.put("message", expectedMessage);
                                                notification.put("timestamp", FieldValue.serverTimestamp());
                                                notification.put("type", "star_rivalry");

                                                db.collection("users")
                                                        .document(myUserId)
                                                        .collection("notifications")
                                                        .add(notification)
                                                        .addOnSuccessListener(docRef -> Log.d("notif", "Добавено тестово известие"))
                                                        .addOnFailureListener(e -> Log.e("notif", "Грешка при добавяне", e));
                                            }
                                        });
                            }
                        });
                    }
                });
    }



    public void showPlaceDetailsDialog(TouristObject touristObject) {
        firestoreHelper.loadTouristObjectDetails(touristObject.getName(), new FirestoreHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(TouristObject updatedTouristObject) {
                DetailsDialogFragment dialogFragment = DetailsDialogFragment.newInstance(updatedTouristObject);
                dialogFragment.show(getSupportFragmentManager(), "detailsDialog");

                addObjectToBeenThere(updatedTouristObject);
            }

            @Override
            public void onDataFailed(String errorMessage) {
                Toast.makeText(BeenThere.this, "Грешка при зареждане на данни: " + errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataLoaded(List<TouristObject> touristObjects) {
            }
        });
    }

    private void addObjectToBeenThere(TouristObject touristObject) {
        firestoreHelper.addToBeenThere(this, touristObject);
    }

    public void showDeleteDialog(TouristObject touristObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        builder.setView(dialogView);

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        titleTextView.setText("Искате ли да изтриете обекта от BeenThere?");

        AlertDialog alertDialog = builder.create();

        btnYes.setOnClickListener(v -> {
            deleteObjectFromBeenThere(touristObject);
            alertDialog.dismiss();
        });

        btnNo.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void deleteObjectFromBeenThere(TouristObject touristObject) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String safeName = touristObject.getName().replaceAll("[.#\\$\\[\\]/]", "_");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("beenThere")
                .document(safeName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    visitedObjects.remove(touristObject);
                    adapter.notifyDataSetChanged();
                    stars = visitedObjects.size();
                    updateStars();
                    Toast.makeText(this, "Обектът е изтрит успешно.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Грешка при изтриването.", Toast.LENGTH_SHORT).show()
                );
    }


}