package com.example.loginscreen.Dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.TouristObject;
import com.example.loginscreen.ViewModel.WishlistHelper;
import com.example.loginscreen.R;
import com.example.loginscreen.View.Plan;
import com.example.loginscreen.View.Wishlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Options extends DialogFragment {

    private TouristObject touristObject;
    private final WishlistHelper wishlistHelper = new WishlistHelper();



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
        btnRemove.setOnClickListener(v -> {
            Log.d("OptionsDialog", "btnRemove clicked");
            removeFromWishlist();
        });


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
                        Toast.makeText(requireContext(), touristObject.getName() + " добавено в посетени", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при добавяне в посетени", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void moveToPlan() {
        if (getActivity() == null || touristObject == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        long visitTime = System.currentTimeMillis();
        touristObject.setVisitTime(visitTime);
        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .document(sanitizeForFirestoreId(touristObject.getName()))
                .set(touristObject)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), touristObject.getName() + " добавено в план", Toast.LENGTH_SHORT).show();
                    }
                    removeFromWishlist();
                    Intent intent = new Intent(getActivity(), Plan.class);
                    intent.putExtra("objectName", touristObject.getName());
                    intent.putExtra("visitTime", visitTime);
                    startActivity(intent);
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при добавяне в план", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(requireContext(), touristObject.getName() + " премахнато от желани места", Toast.LENGTH_SHORT).show();
                    }
                    refreshWishlistUI();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при премахване от желани места", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshWishlistUI() {
        if (isAdded() && getActivity() instanceof Wishlist) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            WishlistHelper helper = new WishlistHelper();
            helper.loadWishlist(userId, new WishlistHelper.OnWishlistLoadedListener() {
                @Override
                public void onSuccess(List<TouristObject> objects) {
                    if (isAdded() && getActivity() instanceof Wishlist) {
                        ((Wishlist) getActivity()).refreshUIFromHelper(objects);
                    } else {
                        Log.e("Options", "Activity е null или не е Wishlist");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Грешка при зареждане: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private String sanitizeForFirestoreId(String name) {
        return name.replaceAll("[.#\\$\\[\\]/]", "_");
    }
}