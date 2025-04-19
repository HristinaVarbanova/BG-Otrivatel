package com.example.loginscreen.View.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.NotificationItem;
import com.example.loginscreen.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private Context context;
    private List<NotificationItem> notifications;

    public NotificationsAdapter(Context context, List<NotificationItem> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem notif = notifications.get(position);
        if (notif == null) {
            holder.textView.setText("Грешка при зареждане на известие.");
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
            return;
        }
        holder.textView.setText(notif.getMessage());
        Log.d("NotifDebug", "TYPE: " + notif.getType() + ", STATUS: " + notif.getStatus());
        if ("friend_request".equals(notif.getType())) {
            String status = notif.getStatus();
            if ("accepted".equals(status) || "declined".equals(status)) {
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnDecline.setVisibility(View.GONE);
            } else {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnDecline.setVisibility(View.VISIBLE);

                holder.btnAccept.setOnClickListener(v -> acceptFriendRequest(notif, position));
                holder.btnDecline.setOnClickListener(v -> declineFriendRequest(notif, position));
            }
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button btnAccept, btnDecline;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.notification_text);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }

    private void acceptFriendRequest(NotificationItem notif, int position) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Не сте логнати!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = auth.getCurrentUser().getUid();
        String friendUid = notif.getFromUid();

        db.collection("users").document(friendUid).get().addOnSuccessListener(friendDoc -> {
            String friendUsername = friendDoc.getString("username");

            db.collection("users").document(currentUid).get().addOnSuccessListener(currentDoc -> {
                String currentUsername = currentDoc.getString("username");

                Map<String, Object> myData = new HashMap<>();
                myData.put("uid", currentUid);
                myData.put("username", currentUsername);
                myData.put("timestamp", System.currentTimeMillis());

                Map<String, Object> friendData = new HashMap<>();
                friendData.put("uid", friendUid);
                friendData.put("username", friendUsername);
                friendData.put("timestamp", System.currentTimeMillis());

                db.collection("users").document(currentUid).collection("friends").document(friendUid)
                        .set(friendData)
                        .addOnSuccessListener(aVoid -> {
                            db.collection("users").document(friendUid).collection("friends").document(currentUid)
                                    .set(myData)
                                    .addOnSuccessListener(aVoid2 -> {
                                        db.collection("users").document(currentUid)
                                                .collection("requests").document(friendUid)
                                                .delete();
                                        String message = "Вие приехте поканата на " + friendUsername;
                                        notif.setMessage(message);
                                        notif.setStatus("accepted");

                                        if (notif.getId() != null) {
                                            db.collection("users").document(currentUid)
                                                    .collection("notifications")
                                                    .document(notif.getId())
                                                    .set(new HashMap<String, Object>() {{
                                                        put("message", message);
                                                        put("status", "accepted");
                                                    }}, SetOptions.merge());
                                        }

                                        NotificationItem confirm = new NotificationItem(
                                                "friend_accept",
                                                currentUsername,
                                                currentUid,
                                                currentUsername + " прие поканата ти за приятелство!"
                                        );

                                        db.collection("users").document(friendUid)
                                                .collection("notifications")
                                                .add(confirm)
                                                .addOnSuccessListener(docRef -> Log.d("notif", "Успешно добавена нотификация"))
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Грешка при запис: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                });

                                        notifyItemChanged(position);
                                        Toast.makeText(context, "Приятелството е добавено!", Toast.LENGTH_SHORT).show();
                                    });
                        });
            });
        });
    }

    private void declineFriendRequest(NotificationItem notif, int position) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String currentUid = auth.getCurrentUser().getUid();
        String friendUid = notif.getFromUid();

        db.collection("users").document(currentUid)
                .collection("requests").document(friendUid)
                .delete();

        String message = "Вие отказахте поканата на " + notif.getFrom();
        notif.setMessage(message);
        notif.setStatus("declined");

        if (notif.getId() != null) {
            db.collection("users").document(currentUid)
                    .collection("notifications")
                    .document(notif.getId())
                    .set(new HashMap<String, Object>() {{
                        put("message", message);
                        put("status", "declined");
                    }}, SetOptions.merge());
        }

        notifyItemChanged(position);
        Toast.makeText(context, "Поканата е отказана.", Toast.LENGTH_SHORT).show();
    }
}
