package com.example.loginscreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        holder.textView.setText(notif.getMessage());

        if ("friend_request".equals(notif.getType())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> {
                acceptFriendRequest(notif);
            });
            holder.btnDecline.setOnClickListener(v -> declineFriendRequest(notif));
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


    private void acceptFriendRequest(NotificationItem notif) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Не сте логнати!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = auth.getCurrentUser().getUid();
        String friendUid = notif.getFromUid(); // UID на подателя

        if (friendUid == null || friendUid.isEmpty()) {
            Toast.makeText(context, "Липсва UID на подателя!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔄 Взимаме username и на двамата
        db.collection("users").document(friendUid).get().addOnSuccessListener(friendDoc -> {
            String friendUsername = friendDoc.getString("username");

            db.collection("users").document(currentUid).get().addOnSuccessListener(currentDoc -> {
                String currentUsername = currentDoc.getString("username");

                if (friendUsername == null || currentUsername == null) {
                    Toast.makeText(context, "Грешка: липсва username!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Данни за приятелите
                Map<String, Object> myData = new HashMap<>();
                myData.put("uid", currentUid);
                myData.put("username", currentUsername);
                myData.put("timestamp", System.currentTimeMillis());

                Map<String, Object> friendData = new HashMap<>();
                friendData.put("uid", friendUid);
                friendData.put("username", friendUsername);
                friendData.put("timestamp", System.currentTimeMillis());

                // 🔹 Записваме И ДВЕТЕ страни последователно и чакаме успешното приключване
                db.collection("users").document(currentUid).collection("friends").document(friendUid)
                        .set(friendData)
                        .addOnSuccessListener(aVoid1 -> {
                            db.collection("users").document(friendUid).collection("friends").document(currentUid)
                                    .set(myData)
                                    .addOnSuccessListener(aVoid2 -> {
                                        // 🗑 Изтриваме заявката
                                        db.collection("users").document(currentUid)
                                                .collection("requests").document(friendUid)
                                                .delete();

                                        // 🔔 Изпращаме нотификация
                                        NotificationItem confirm = new NotificationItem(
                                                "friend_accept",
                                                currentUsername,
                                                currentUid,
                                                currentUsername + " прие поканата ти за приятелство!"
                                        );

                                        db.collection("users").document(friendUid)
                                                .collection("notifications")
                                                .add(confirm);

                                        // ✅ Обновяваме интерфейса
                                        notifications.remove(notif);
                                        notifyDataSetChanged();

                                        Toast.makeText(context, "Приятелството е добавено!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "❌ Неуспех при запис към подателя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "❌ Неуспех при запис към себе си: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        });
    }



    private void declineFriendRequest(NotificationItem notif) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String friendUid = notif.getFromUid();

        FirebaseFirestore.getInstance()
                .collection("users").document(currentUid)
                .collection("requests").document(friendUid)
                .delete();

        notifications.remove(notif);
        notifyDataSetChanged();

        Toast.makeText(context, "Поканата е отказана.", Toast.LENGTH_SHORT).show();
    }
}
