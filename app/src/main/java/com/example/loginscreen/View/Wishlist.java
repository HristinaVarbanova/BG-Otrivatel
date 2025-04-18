package com.example.loginscreen.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.View.Adapters.TouristObjectsAdapter;
import com.example.loginscreen.Model.TouristObject;
import com.example.loginscreen.ViewModel.WishlistHelper;
import com.example.loginscreen.R;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
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

        firestore = FirebaseFirestore.getInstance();
        touristObjectsRef = firestore.collection("TouristObjects");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        recyclerView = findViewById(R.id.placesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        touristObjectsList = new ArrayList<>();
        adapter = new TouristObjectsAdapter(this, touristObjectsList, null);
        recyclerView.setAdapter(adapter);

        searchRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResults = new ArrayList<>();
        searchAdapter = new TouristObjectsAdapter(this, searchResults, place -> {
            WishlistHelper helper = new WishlistHelper();
            helper.addToWishlist(currentUserId, place, new WishlistHelper.OnAddListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(Wishlist.this, "Добавено в желани места!", Toast.LENGTH_SHORT).show();
                    helper.loadWishlist(currentUserId, new WishlistHelper.OnWishlistLoadedListener() {
                        @Override
                        public void onSuccess(List<TouristObject> wishlist) {
                            touristObjectsList.clear();
                            touristObjectsList.addAll(wishlist);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(Wishlist.this, "Грешка при презареждане", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(Wishlist.this, "Грешка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        searchRecyclerView.setAdapter(searchAdapter);

        WishlistHelper helper = new WishlistHelper();
        helper.loadWishlist(currentUserId, new WishlistHelper.OnWishlistLoadedListener() {
            @Override
            public void onSuccess(List<TouristObject> wishlist) {
                touristObjectsList.clear();
                touristObjectsList.addAll(wishlist);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Wishlist", "Грешка при зареждане", e);
                Toast.makeText(Wishlist.this, "Грешка при зареждане", Toast.LENGTH_SHORT).show();
            }
        });


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

    public void refreshUIFromHelper(List<TouristObject> objects) {
       adapter.updateList(objects);

    }




}