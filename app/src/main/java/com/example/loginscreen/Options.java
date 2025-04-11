package com.example.loginscreen;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Options extends DialogFragment {

    private TouristObject touristObject;

    public static Options newInstance(TouristObject touristObject) {
        Options fragment = new Options();
        Bundle args = new Bundle();
        args.putSerializable("touristObject", touristObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            touristObject = (TouristObject) getArguments().getSerializable("touristObject");
        }

        if (touristObject == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Няма обект за обработка.", Toast.LENGTH_SHORT).show();
            }
            return super.onCreateDialog(savedInstanceState);
        }

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_options, null);
        view.setBackgroundResource(R.drawable.dialog_bkg);
        view.setClipToOutline(true);

        Button btnBeenThere = view.findViewById(R.id.btnBeenThere);
        Button btnPlan = view.findViewById(R.id.btnPlan);
        Button btnRemove = view.findViewById(R.id.btnRemove);

        btnBeenThere.setOnClickListener(v -> moveToBeenThere());
        btnPlan.setOnClickListener(v -> moveToPlan());
        btnRemove.setOnClickListener(v -> removeFromWishlist());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void moveToBeenThere() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(userId)
                .collection("beenThere")
                .document(sanitizeForFirestoreId(touristObject.getName()))
                .set(touristObject)
                .addOnSuccessListener(aVoid -> {
                    removeFromWishlist();
                    if (isAdded()) {
                        Toast.makeText(requireContext(), touristObject.getName() + " добавено в BeenThere", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при добавяне в BeenThere", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void moveToPlan() {
        if (getActivity() == null || touristObject == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // 1️⃣ Добавяме време на посещение
        long visitTime = System.currentTimeMillis();
        touristObject.setVisitTime(visitTime);

        // 2️⃣ Запис в "plan"
        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .document(sanitizeForFirestoreId(touristObject.getName()))
                .set(touristObject)
                .addOnSuccessListener(aVoid -> {
                    // 3️⃣ Показваме потвърждение
                    if (isAdded()) {
                        Toast.makeText(requireContext(), touristObject.getName() + " добавено в Plan", Toast.LENGTH_SHORT).show();
                    }

                    // 4️⃣ Премахваме от wishlist
                    removeFromWishlist();

                    // 5️⃣ Стартираме Plan activity и подаваме името и времето
                    Intent intent = new Intent(getActivity(), Plan.class);
                    intent.putExtra("objectName", touristObject.getName());
                    intent.putExtra("visitTime", visitTime);
                    startActivity(intent);

                    // 6️⃣ Затваряме диалога
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при добавяне в Plan", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void removeFromWishlist() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .document(sanitizeForFirestoreId(touristObject.getName()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), touristObject.getName() + " премахнато от Wishlist", Toast.LENGTH_SHORT).show();
                    }
                    refreshWishlistUI();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при премахване от Wishlist", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshWishlistUI() {
        if (getActivity() instanceof Wishlist) {
            ((Wishlist) getActivity()).loadWishlist();
        }
    }

    private String sanitizeForFirestoreId(String name) {
        return name.replaceAll("[.#\\$\\[\\]/]", "_");
    }
}
