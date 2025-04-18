package com.example.loginscreen.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Dialogs.DetailsDialogFragment;
import com.example.loginscreen.ViewModel.BeenThereHelper;
import com.example.loginscreen.ViewModel.FirestoreHelper;
import com.example.loginscreen.R;
import com.example.loginscreen.Model.TouristObject;
import com.example.loginscreen.View.Adapters.TouristObjectsAdapter;
import com.example.loginscreen.View.LoginSignUp.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


public class BeenThere extends AppCompatActivity{
    private TextView starsTextView;
    private RecyclerView recyclerView;
    private List<TouristObject> visitedObjects;
    private int stars;
    private TouristObjectsAdapter adapter;
    private FirestoreHelper firestoreHelper;

    private BeenThereHelper beenThereHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beenthere);

        beenThereHelper = new BeenThereHelper();

        firestoreHelper = new FirestoreHelper();


        visitedObjects = new ArrayList<>();
        stars = 0;

        starsTextView = findViewById(R.id.starsTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TouristObjectsAdapter(this, visitedObjects, touristObject -> {
            showPlaceDetailsDialog(touristObject);
        });
        recyclerView.setAdapter(adapter);

        loadVisitedObjectsFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(this, Notifications.class));
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
            }
            return true;
        });
    }

    private void loadVisitedObjectsFromFirestore() {
        beenThereHelper.loadVisitedObjects(this, new FirestoreHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(List<TouristObject> touristObjects) {
                visitedObjects.clear();
                visitedObjects.addAll(touristObjects);
                stars = visitedObjects.size();
                adapter.notifyDataSetChanged();
                updateStars();
            }

            @Override
            public void onDataFailed(String errorMessage) {
                Toast.makeText(BeenThere.this, "Грешка при зареждане на данни: " + errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataLoaded(TouristObject touristObject) {
            }
        });
    }

    private void updateStars() {
        starsTextView.setText("Stars ⭐: " + stars);
        beenThereHelper.updateUserStars(this, stars, null);
    }

    public void showPlaceDetailsDialog(TouristObject touristObject) {
        firestoreHelper.loadTouristObjectDetails(touristObject.getName(), new FirestoreHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(TouristObject updatedTouristObject) {
                DetailsDialogFragment dialogFragment = DetailsDialogFragment.newInstance(updatedTouristObject);
                dialogFragment.show(getSupportFragmentManager(), "detailsDialog");

                addObjectToBeenThere(updatedTouristObject);
            }

            @Override
            public void onDataFailed(String errorMessage) {
                Toast.makeText(BeenThere.this, "Грешка при зареждане на данни: " + errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataLoaded(List<TouristObject> touristObjects) {
            }
        });
    }

    private void addObjectToBeenThere(TouristObject touristObject) {
        beenThereHelper.addToBeenThere(this, touristObject);
    }

    public void showDeleteDialog(TouristObject touristObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        builder.setView(dialogView);

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        titleTextView.setText("Искате ли да изтриете обекта от BeenThere?");

        AlertDialog alertDialog = builder.create();
        btnYes.setOnClickListener(v -> {
            beenThereHelper.deleteObjectFromBeenThere(
                    this,
                    touristObject,
                    visitedObjects,
                    adapter,
                    this::updateStars
            );
            alertDialog.dismiss();
        });



        btnNo.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

}