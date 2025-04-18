// Friends.java
package com.example.loginscreen.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.FriendInfo;
import com.example.loginscreen.ViewModel.FriendsHelper;
import com.example.loginscreen.R;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.example.loginscreen.View.Adapters.FriendsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Friends extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private List<FriendInfo> friendsList;
    private FriendsHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity);

        recyclerView = findViewById(R.id.friendsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsList = new ArrayList<>();
        adapter = new FriendsAdapter(friendsList);
        recyclerView.setAdapter(adapter);

        helper = new FriendsHelper();
        loadFriends();

        setupNavigation();
    }

    private void loadFriends() {
        helper.loadFriends(new FriendsHelper.OnFriendsLoaded() {
            @Override
            public void onSuccess(List<FriendInfo> friends) {
                friendsList.clear();
                friendsList.addAll(friends);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(Friends.this, "Грешка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
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
}
