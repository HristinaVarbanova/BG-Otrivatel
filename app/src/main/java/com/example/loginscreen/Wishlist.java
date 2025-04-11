package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Wishlist extends AppCompatActivity {
    private RecyclerView recyclerView, searchRecyclerView;
    private TouristObjectsAdapter adapter, searchAdapter;
    private List<TouristObject> touristObjectsList, searchResults;
    private FirebaseFirestore firestore;
    private CollectionReference touristObjectsRef;
    private SearchView searchView;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // Firebase
        firestore = FirebaseFirestore.getInstance();
        touristObjectsRef = firestore.collection("TouristObjects");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // SearchView
        searchView = findViewById(R.id.search);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlace(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    searchRecyclerView.setVisibility(View.VISIBLE);
                    searchPlace(newText);
                } else {
                    searchRecyclerView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        // Wishlist RecyclerView
        recyclerView = findViewById(R.id.placesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        touristObjectsList = new ArrayList<>();
        adapter = new TouristObjectsAdapter(this, touristObjectsList, null);
        recyclerView.setAdapter(adapter);

        // Search RecyclerView
        searchRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResults = new ArrayList<>();
        searchAdapter = new TouristObjectsAdapter(this, searchResults, this::addToWishlist);
        searchRecyclerView.setAdapter(searchAdapter);

        // Зареди данните от Firestore
        loadWishlist();

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(Wishlist.this, MainActivity.class));
                finish();
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(Wishlist.this, Notifications.class));
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Wishlist.this, LoginActivity.class));
                finish();
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(Wishlist.this, Profile.class));
            }
            return true;
        });
    }

    // Зареди обектите от wishlist
    public void loadWishlist() {
        firestore.collection("users")
                .document(currentUserId)
                .collection("wishlist")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        touristObjectsList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            TouristObject touristObject = document.toObject(TouristObject.class);
                            if (touristObject != null) {
                                touristObject.setName(document.getId());
                                touristObjectsList.add(touristObject);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Wishlist", "Грешка при зареждане: ", task.getException());
                    }
                });
    }

    // Търси обекти по име
    private void searchPlace(String query) {
        touristObjectsRef
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        searchResults.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            TouristObject place = document.toObject(TouristObject.class);
                            if (place != null) {
                                place.setName(document.getId());
                                searchResults.add(place);
                            }
                        }
                        searchAdapter.notifyDataSetChanged();
                    }
                });
    }

    // Добави към wishlist с име като ID
    private void addToWishlist(TouristObject place) {
        String safeName = sanitizeForFirestoreId(place.getName());



        firestore.collection("users")
                .document(currentUserId)
                .collection("wishlist")
                .document(safeName)
                .set(place)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Добавено в Wishlist!", Toast.LENGTH_SHORT).show();
                    loadWishlist();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Премахване от wishlist
    private void removeFromWishlist(String placeName) {
        String safeName = sanitizeForFirestoreId(placeName);

        firestore.collection("users")
                .document(currentUserId)
                .collection("wishlist")
                .document(safeName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Премахнато от Wishlist", Toast.LENGTH_SHORT).show();
                    loadWishlist();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Премести от wishlist към visited
    private void moveToVisited(TouristObject place) {
        String safeName = sanitizeForFirestoreId(place.getName());

        firestore.collection("users")
                .document(currentUserId)
                .collection("visited")
                .document(safeName)
                .set(place)
                .addOnSuccessListener(aVoid -> removeFromWishlist(place.getName()))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка при преместване: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void moveToPlan(TouristObject place) {
        String safeName = sanitizeForFirestoreId(place.getName());

        // Добавяме в "plan"
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("plan")
                .document(safeName)
                .set(place)
                .addOnSuccessListener(aVoid -> {
                    // След успешното добавяне – трием от wishlist
                    removeFromWishlist(place.getName());
                    Toast.makeText(this, "Преместено в Plan!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка при преместване в Plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void removeItemFromAdapter(TouristObject objToRemove) {
        for (int i = 0; i < touristObjectsList.size(); i++) {
            TouristObject current = touristObjectsList.get(i);
            if (current.getName().equals(objToRemove.getName())) {
                touristObjectsList.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }



    // Помощна функция за безопасен ID
    private String sanitizeForFirestoreId(String name) {
        return name.replaceAll("[.#\\$\\[\\]/]", "_");
    }
}
