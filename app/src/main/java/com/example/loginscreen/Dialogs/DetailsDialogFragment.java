package com.example.loginscreen.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.loginscreen.Model.TouristObject;
import com.example.loginscreen.R;

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_details, null);

        TouristObject touristObject = (TouristObject) getArguments().getSerializable(ARG_TOURIST_OBJECT);

        TextView nameTextView = view.findViewById(R.id.nameTextView);
        TextView descriptionTextView = view.findViewById(R.id.descriptionTextView);
        TextView locationTextView = view.findViewById(R.id.locationTextView);

        if (touristObject != null) {
            nameTextView.setText(touristObject.getName());
            descriptionTextView.setText(touristObject.getInfo());
            locationTextView.setText(touristObject.getLocation());
        }


        view.setBackgroundResource(R.drawable.rounded_rectangle);
        view.setClipToOutline(true);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("OK", (dialog1, which) -> dialog1.dismiss())
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }
}
