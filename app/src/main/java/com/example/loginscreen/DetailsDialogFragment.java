package com.example.loginscreen;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class DetailsDialogFragment extends DialogFragment {

    private static final String ARG_TOURIST_OBJECT = "tourist_object";

    public static DetailsDialogFragment newInstance(TouristObject touristObject) {
        DetailsDialogFragment fragment = new DetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TOURIST_OBJECT, touristObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Създаване на диалогов прозорец
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_details, null);

        // Вземане на данните от TouristObject
        TouristObject touristObject = (TouristObject) getArguments().getSerializable(ARG_TOURIST_OBJECT);

        // Инициализация на UI елементи за показване на детайли
        TextView nameTextView = view.findViewById(R.id.nameTextView);
        TextView descriptionTextView = view.findViewById(R.id.descriptionTextView);
        TextView locationTextView = view.findViewById(R.id.locationTextView);

        // Показване на данни за туристическия обект
        if (touristObject != null) {
            nameTextView.setText(touristObject.getName());
            descriptionTextView.setText(touristObject.getInfo());
            locationTextView.setText(touristObject.getLocation());
        }

        // Уверете се, че фонът е правилен за заоблени ръбове
        view.setBackgroundResource(R.drawable.rounded_rectangle);  // Фон с заоблени ъгли
        view.setClipToOutline(true);  // Гарантира, че ъглите ще бъдат заоблени

        // Създаване на диалогов прозорец
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("OK", (dialog1, which) -> dialog1.dismiss())
                .create();

        // Премахваме фона на AlertDialog, за да се използва вашият персонализиран фон
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }
}
