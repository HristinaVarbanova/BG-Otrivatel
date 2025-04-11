package com.example.loginscreen;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreHelper {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void addTestData() {
        // Създаване на тестови туристически обекти
        TouristObject object1 = new TouristObject("Античен град 'Хераклея Синтика'", "Античният град Хераклея Синтика се намира близо до Петрич...", "БЛАГОЕВГРАДСКА ОБЛАСТ/Петрич", "antichengrad");
        TouristObject object2 = new TouristObject("Музей Рилски манастир", "Рилският манастир е основан през X век от св. Иван Рилски и е най-големият духовен център в България. През XIV век феодалът Хрельо Драговол го укрепява, а Хрельовата кула става емблематична. Главната църква „Рождество Богородично“ е изографисана от Захари Зограф и други майстори. Манастирът притежава богата библиотека и музей с 35 000 исторически експоната. Уникални реликви са чудотворната икона на Света Богородица Одигитрия и изящно резбованият кръст на монах Рафаил, който ослепява след 12 години работа върху него. ", "КЮСТЕНДИЛСКА ОБЛАСТ", "rilski");
        TouristObject object3 = new TouristObject("Поморийско езеро","Поморийско езеро е свръхсолена лагуна на българското Черноморско крайбрежие, близо до гр. Поморие. То е дом на редки соленолюбиви растения и животни, а също така е част от миграционния път Via Pontica. В езерото се наблюдават над 270 вида птици през различните сезони.Зоната е защитена по българското и международното природозащитно законодателство, включена в Рамсарската конвенция и Натура 2000.Посетителският център на Поморийско езеро предлага екскурзии, наблюдение на птици и изложби, с цел популяризиране на биоразнообразието и природозащитни нагласи. От терасата се открива гледка към езерото и гнездовата колония на гривестите рибарки.", "БУРГАСКА ОБЛАСТ/Поморие", "pomoriiskoezero");

        // Добавяне на обектите в колекцията "TouristObjects"
        firestore.collection("TouristObjects").document(object1.getName()).set(object1);
        firestore.collection("TouristObjects").document(object2.getName()).set(object2);
        firestore.collection("TouristObjects").document(object3.getName()).set(object3);

    }

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


    public void loadVisitedObjects(Context context, OnDataLoadedListener listener) {
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
                            obj.setName(doc.getId()); // ако използваш името за ID
                            visitedList.add(obj);
                        }
                    }
                    listener.onDataLoaded(visitedList);
                })
                .addOnFailureListener(e -> listener.onDataFailed(e.getMessage()));
    }


    public void loadTouristObjectDetails(String objectName, final OnDataLoadedListener listener) {
        firestore.collection("TouristObjects")
                .document(objectName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        TouristObject touristObject = task.getResult().toObject(TouristObject.class);
                        listener.onDataLoaded(touristObject);
                    } else {
                        listener.onDataFailed("Няма намерени данни за обект " + objectName);
                    }
                });
    }

    public void removeObjectFromBeenThere(Context context, TouristObject object, final OnDataLoadedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("beenThere")
                .document(object.getName())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Обектът е премахнат от BeenThere!", Toast.LENGTH_SHORT).show();
                    listener.onDataLoaded(new ArrayList<>());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Грешка при премахване от BeenThere!", Toast.LENGTH_SHORT).show();
                    listener.onDataFailed("Грешка при премахване на обект от BeenThere");
                });

    }

    public void addToWishlist(Context context, TouristObject object) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .document(object.getName())
                .set(object)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Добавено в Wishlist!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при добавяне в Wishlist!", Toast.LENGTH_SHORT).show()
                );
    }

    public void addToPlan(Context context, TouristObject object) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .document(object.getName())
                .set(object)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Добавено в Plan!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при добавяне в Plan!", Toast.LENGTH_SHORT).show()
                );
    }

    public void loadPlanObjects(Context context, OnDataLoadedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TouristObject> planList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        TouristObject obj = doc.toObject(TouristObject.class);
                        if (obj != null) {
                            obj.setName(doc.getId());
                            planList.add(obj);
                        }
                    }
                    listener.onDataLoaded(planList);
                })
                .addOnFailureListener(e -> listener.onDataFailed(e.getMessage()));
    }



    public interface OnDataLoadedListener {
        void onDataLoaded(List<TouristObject> touristObjects);
        void onDataFailed(String errorMessage);
        void onDataLoaded(TouristObject touristObject);
    }
}
