package com.example.loginscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.loginscreen.FriendInfo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Friends extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private List<FriendInfo> friendsList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity);

        recyclerView = findViewById(R.id.friendsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsList = new ArrayList<>();
        adapter = new FriendsAdapter(friendsList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        loadFriendsFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
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

    private void loadFriendsFromFirestore() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference friendsRef = firestore.collection("users").document(currentUid).collection("friends");

        friendsRef.addSnapshotListener((friendDocs, error) -> {
            if (error != null) {
                Toast.makeText(this, "Грешка при зареждане: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("FirestoreCheck", "Намерени приятели: " + friendDocs.size());


            friendsList.clear();

            if (friendDocs != null && !friendDocs.isEmpty()) {
                // Брояч за асинхронните извиквания
                final int totalFriends = friendDocs.size();
                final int[] loadedCount = {0};

                for (QueryDocumentSnapshot doc : friendDocs) {
                    final String friendUid = doc.getId();
                    final String username = doc.getString("username") != null ? doc.getString("username") : "Непознат";

                    firestore.collection("users").document(friendUid)
                            .collection("beenThere")
                            .get()
                            .addOnSuccessListener(beenThere -> {
                                int stars = beenThere.size();
                                friendsList.add(new FriendInfo(username, stars));

                                loadedCount[0]++;
                                if (loadedCount[0] == totalFriends) {
                                    // Всички асинхронни заявки са приключили
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> {
                                loadedCount[0]++;
                                if (loadedCount[0] == totalFriends) {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                }
            } else {
                adapter.notifyDataSetChanged(); // Празен списък
            }
        });
    }




    public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

        private final List<FriendInfo> friends;

        public FriendsAdapter(List<FriendInfo> friends) {
            this.friends = friends;
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(Friends.this).inflate(R.layout.friend_item, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            FriendInfo friend = friends.get(position);
            holder.username.setText(friend.getUsername());
            holder.stars.setText("\u2B50 " + friend.getStars());
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        class FriendViewHolder extends RecyclerView.ViewHolder {
            TextView username, stars;

            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.friendUsername);
                stars = itemView.findViewById(R.id.friendStars);
            }
        }
    }

}
