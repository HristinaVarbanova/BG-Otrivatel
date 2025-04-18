package com.example.loginscreen.ViewModel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.loginscreen.Model.TouristObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PlanHelper {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface PlanLoadCallback {
        void onObjectLoaded(TouristObject obj, Calendar visitCalendar);
        void onLoadFailed(String message);
    }

    public void addToPlan(Context context, TouristObject obj, Calendar visitDateCalendar, Runnable onSuccess) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference planRef = firestore.collection("users").document(userId).collection("plan");

        long visitTime = visitDateCalendar.getTimeInMillis();

        Map<String, Object> planData = new HashMap<>();
        planData.put("name", obj.getName());
        planData.put("image", obj.getImage());
        planData.put("visitTime", visitTime);

        String safeName = sanitizeForFirestoreId(obj.getName());
        Log.d("FirestoreDebug", "Добавям в Plan с ID: " + safeName);

        planRef.document(safeName)
                .set(planData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Обектът е добавен в плана.", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Не успяхме да добавим обекта в плана.", Toast.LENGTH_SHORT).show();
                });
    }

    public void deleteObjectFromPlan(Context context, String objectNameToDelete, Runnable onSuccess) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference planRef = firestore.collection("users").document(userId).collection("plan");

        planRef.document(sanitizeForFirestoreId(objectNameToDelete)).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Обектът беше изтрит от плана.", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Не успяхме да изтрием обекта.", Toast.LENGTH_SHORT).show();
                });
    }



    public void updateVisitTime(Context context, String objectName, long visitTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String safeName = sanitizeForFirestoreId(objectName);

        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .document(safeName)
                .update("visitTime", visitTime)
                .addOnSuccessListener(aVoid -> Log.d("Plan", "visitTime updated"))
                .addOnFailureListener(e -> Log.e("Plan", "Грешка при обновяване на visitTime: " + e.getMessage()));
    }

    private String sanitizeForFirestoreId(String name) {
        return name.replaceAll("[.#\\$\\[\\]/]", "_");
    }
}
