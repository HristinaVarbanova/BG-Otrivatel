package com.example.loginscreen.ModelView;

import com.example.loginscreen.Model.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsHelper {

    public interface OnNotificationsLoaded {
        void onSuccess(List<NotificationItem> notifications);
        void onFailure(String errorMessage);
    }

    public void loadNotifications(OnNotificationsLoaded callback) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUid)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<NotificationItem> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        NotificationItem notif = doc.toObject(NotificationItem.class);
                        if (notif != null) {
                            notif.setId(doc.getId());
                            notifications.add(notif);
                        }
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
