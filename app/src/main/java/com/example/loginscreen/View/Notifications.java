package com.example.loginscreen.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.NotificationItem;
import com.example.loginscreen.View.Adapters.NotificationsAdapter;
import com.example.loginscreen.R;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import com.example.loginscreen.ViewModel.NotificationsHelper;

public class Notifications extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<NotificationItem> notificationsList = new ArrayList<>();
    private NotificationsAdapter adapter;
    private NotificationsHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(this, notificationsList);
        recyclerView.setAdapter(adapter);

        helper = new NotificationsHelper();

        loadNotifications();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
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

    private void loadNotifications() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Не сте логнати. Пренасочване...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        helper.loadNotifications(new NotificationsHelper.OnNotificationsLoaded() {
            @Override
            public void onSuccess(List<NotificationItem> notifications) {
                notificationsList.clear();
                notificationsList.addAll(notifications);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(Notifications.this, "Грешка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}