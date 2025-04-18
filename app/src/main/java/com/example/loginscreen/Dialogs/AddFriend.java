package com.example.loginscreen.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;

import com.example.loginscreen.Model.NotificationItem;
import com.example.loginscreen.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddFriend extends DialogFragment {

    private SearchView searchView;
    private Button inviteButton;
    private Context context;

    /*public static AddFriend newInstance() {
        return new AddFriend();
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FirebaseFirestore.setLoggingEnabled(true);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_friends_activity, null);
        dialog.setContentView(view);

        context = requireContext();
        searchView = view.findViewById(R.id.search);
        inviteButton = view.findViewById(R.id.inviteButton);

        inviteButton.setOnClickListener(v -> {
            String username = searchView.getQuery().toString().trim();
            if (!username.isEmpty()) {
                sendFriendRequest(username);
                dismiss();
            } else {
                Toast.makeText(context, "Моля, въведете потребителско име.", Toast.LENGTH_SHORT).show();
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }
    private void sendFriendRequest(String username) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users").document(senderUid)
                .get()
                .addOnSuccessListener(senderDoc -> {
                    String senderUsername = senderDoc.getString("username");

                    firestore.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String receiverUid = document.getId();

                                        if (receiverUid.equals(senderUid)) {
                                            Toast.makeText(context, "Не можете да поканите себе си!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Map<String, Object> request = new HashMap<>();
                                        request.put("status", "pending");
                                        request.put("senderName", senderUsername);
                                        request.put("senderUid", senderUid);

                                        firestore.collection("users")
                                                .document(receiverUid)
                                                .collection("requests")
                                                .document(senderUid)
                                                .set(request)
                                                .addOnSuccessListener(aVoid -> {
                                                    NotificationItem notification = new NotificationItem(
                                                            "friend_request",
                                                            senderUsername,
                                                            senderUid,
                                                            senderUsername + " ти изпрати покана за приятелство"
                                                    );

                                                    firestore.collection("users")
                                                            .document(receiverUid)
                                                            .collection("notifications")
                                                            .add(notification)
                                                            .addOnSuccessListener(documentReference -> {
                                                                Toast.makeText(context, "Поканата е изпратена успешно.", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(context, "Грешка при запис на нотификация: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Грешка при запис на заявка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    Toast.makeText(context, "Потребител с това име не е намерен.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Грешка при търсене: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Грешка при извличане на потребителя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    public interface AddFriendListener {
        void onAddFriend(String username);
    }

}
