package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<NotificationItem> notificationsList = new ArrayList<>();
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(this, notificationsList);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(Notifications.this, MainActivity.class));
                finish();
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Notifications.this, LoginActivity.class));
                finish();
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(Notifications.this, Profile.class));
            }
            return true;
        });
    }

    private void loadNotifications() {
        Log.d("NotificationCheck", "loadNotifications() стартира успешно");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Не сте логнати. Пренасочване...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String currentUid = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUid)
                .collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    notificationsList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        NotificationItem notif = doc.toObject(NotificationItem.class);

                        if (notif != null) {
                            notif.setId(doc.getId());

                            // 🐞 Лог за проверка
                            Log.d("NotificationLoad", "Заредено известие: "
                                    + notif.getMessage() + " | Тип: " + notif.getType());

                            notificationsList.add(notif);
                        } else {
                            Log.w("NotificationLoad", "Неуспешно парсване на известие: " + doc.getId());
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Notifications.this, "Грешка при зареждане на нотификации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
