package com.example.loginscreen.ModelView;

import com.example.loginscreen.Model.TouristObject;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WishlistHelper {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface OnWishlistLoadedListener {
        void onSuccess(List<TouristObject> wishlist);
        void onFailure(Exception e);
    }

    public interface OnAddListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void loadWishlist(String userId, OnWishlistLoadedListener listener) {
        firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TouristObject> list = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            TouristObject obj = doc.toObject(TouristObject.class);
                            if (obj != null) {
                                obj.setName(doc.getId());
                                list.add(obj);
                            }
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void addToWishlist(String userId, TouristObject place, OnAddListener listener) {
        String safeName = sanitizeForFirestoreId(place.getName());

        firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .document(safeName)
                .set(place)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    private String sanitizeForFirestoreId(String name) {
        return name.replaceAll("[.#\\$\\[\\]/]", "_");
    }
}
