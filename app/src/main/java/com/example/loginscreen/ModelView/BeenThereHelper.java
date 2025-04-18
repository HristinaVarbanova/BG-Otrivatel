package com.example.loginscreen.ModelView;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.TouristObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeenThereHelper {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void addToBeenThere(Context context, TouristObject object) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("beenThere")
                .document(object.getName())
                .set(object)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Добавено в BeenThere!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при добавяне в BeenThere", Toast.LENGTH_SHORT).show()
                );
    }

    public void deleteObjectFromBeenThere(
            Context context,
            TouristObject touristObject,
            List<TouristObject> visitedObjects,
            RecyclerView.Adapter<?> adapter,
            Runnable updateStarsCallback
    ) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String safeName = touristObject.getName().replaceAll("[.#\\$\\[\\]/]", "_");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("beenThere")
                .document(safeName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    visitedObjects.remove(touristObject);
                    adapter.notifyDataSetChanged();
                    updateStarsCallback.run(); // Извиква се метод от BeenThere
                    Toast.makeText(context, "Обектът е изтрит успешно.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при изтриването.", Toast.LENGTH_SHORT).show()
                );
    }

    public void updateUserStars(Context context, int stars, Runnable onComplete) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("users").document(userId)
                .update("stars", stars)
                .addOnSuccessListener(unused -> {
                    checkFriendsForMoreStars(context, userId, stars);
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при запис на звезди в Firestore", Toast.LENGTH_SHORT).show());
    }

    private void checkFriendsForMoreStars(Context context, String myUserId, int myStars) {
        firestore.collection("users").document(myUserId)
                .collection("friends")
                .get()
                .addOnSuccessListener(friendSnapshots -> {
                    for (DocumentSnapshot friendDoc : friendSnapshots) {
                        String friendId = friendDoc.getId();
                        firestore.collection("users").document(friendId).get()
                                .addOnSuccessListener(friendSnapshot -> {
                                    Long friendStarsLong = friendSnapshot.getLong("stars");
                                    String friendName = friendSnapshot.getString("name");

                                    if (friendStarsLong != null && friendStarsLong > myStars) {
                                        String friendNameSafe = (friendName != null) ? friendName : "Приятел";
                                        String expectedMessage = friendNameSafe + " ви надмина по звезди 😢!";

                                        firestore.collection("users").document(myUserId)
                                                .collection("notifications")
                                                .whereEqualTo("type", "star_rivalry")
                                                .whereEqualTo("message", expectedMessage)
                                                .get()
                                                .addOnSuccessListener(existingNotifs -> {
                                                    if (existingNotifs.isEmpty()) {
                                                        Map<String, Object> notification = new HashMap<>();
                                                        notification.put("message", expectedMessage);
                                                        notification.put("timestamp", FieldValue.serverTimestamp());
                                                        notification.put("type", "star_rivalry");

                                                        firestore.collection("users").document(myUserId)
                                                                .collection("notifications")
                                                                .add(notification)
                                                                .addOnSuccessListener(docRef ->
                                                                        Log.d("notif", "Добавено тестово известие"))
                                                                .addOnFailureListener(e ->
                                                                        Log.e("notif", "Грешка при добавяне", e));
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }
    public void loadVisitedObjects(Context context, FirestoreHelper.OnDataLoadedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("beenThere")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TouristObject> visitedList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        TouristObject obj = doc.toObject(TouristObject.class);
                        if (obj != null) {
                            obj.setName(doc.getId());
                            visitedList.add(obj);
                        }
                    }
                    listener.onDataLoaded(visitedList);
                })
                .addOnFailureListener(e -> listener.onDataFailed(e.getMessage()));
    }
}
