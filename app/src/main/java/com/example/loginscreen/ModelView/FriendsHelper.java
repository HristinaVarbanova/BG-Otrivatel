package com.example.loginscreen.ModelView;

import com.example.loginscreen.Model.FriendInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsHelper {

    private final FirebaseFirestore firestore;

    public interface OnFriendsLoaded {
        void onSuccess(List<FriendInfo> friends);
        void onFailure(String error);
    }

    public FriendsHelper() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void loadFriends(OnFriendsLoaded callback) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference friendsRef = firestore.collection("users").document(currentUid).collection("friends");

        friendsRef.get().addOnSuccessListener(friendDocs -> {
            List<FriendInfo> friendsList = new ArrayList<>();
            if (!friendDocs.isEmpty()) {
                final int totalFriends = friendDocs.size();
                final int[] loadedCount = {0};

                for (QueryDocumentSnapshot doc : friendDocs) {
                    String friendUid = doc.getId();
                    String username = doc.getString("username") != null ? doc.getString("username") : "Непознат";

                    firestore.collection("users").document(friendUid)
                            .collection("beenThere")
                            .get()
                            .addOnSuccessListener(beenThere -> {
                                int stars = beenThere.size();
                                friendsList.add(new FriendInfo(username, stars));

                                loadedCount[0]++;
                                if (loadedCount[0] == totalFriends) {
                                    callback.onSuccess(friendsList);
                                }
                            })
                            .addOnFailureListener(e -> {
                                loadedCount[0]++;
                                if (loadedCount[0] == totalFriends) {
                                    callback.onSuccess(friendsList);
                                }
                            });
                }
            } else {
                callback.onSuccess(friendsList);
            }
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
