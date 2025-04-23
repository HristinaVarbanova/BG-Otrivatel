    package com.example.loginscreen.View;

    import android.app.DatePickerDialog;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.os.Handler;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.AutoCompleteTextView;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AlertDialog;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;

    import com.example.loginscreen.ViewModel.PlanHelper;
    import com.example.loginscreen.R;
    import com.example.loginscreen.Model.TouristObject;
    import com.example.loginscreen.View.LoginSignUp.LoginActivity;
    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.CollectionReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Locale;

    public class Plan extends AppCompatActivity {

        private ImageView objectImage;
        private TextView objectName, countdownTimer;
        private Button visitDateButton;
        private FirebaseFirestore firestore;
        private CollectionReference touristObjectsRef;
        private Calendar visitDateCalendar;

        private AutoCompleteTextView searchAutoComplete;
        private ArrayAdapter<String> adapter;
        private List<TouristObject> allObjects = new ArrayList<>();
        private Handler countdownHandler = new Handler();
        private Runnable countdownRunnable;
        private final PlanHelper planHelper = new PlanHelper();


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_plan);

            firestore = FirebaseFirestore.getInstance();
            touristObjectsRef = firestore.collection("TouristObjects");

            if (visitDateCalendar == null) {
                visitDateCalendar = Calendar.getInstance(); 
            }

            searchAutoComplete = findViewById(R.id.searchAutoComplete);
            objectImage = findViewById(R.id.objectImage);
            objectName = findViewById(R.id.objectName);
            countdownTimer = findViewById(R.id.countdownTimer);
            visitDateButton = findViewById(R.id.visitDateButton);

            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>()) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    return view;
                }
            };

            searchAutoComplete.setAdapter(adapter);

            loadTouristObjects();
            loadPlanFromFirestore();


            searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = adapter.getItem(position);
                for (TouristObject obj : allObjects) {
                    if (obj.getName().equals(selectedName)) {
                        displayTouristObject(obj);
                        planHelper.addToPlan(this, obj, visitDateCalendar, () -> {
                            saveVisitTimeToPreferences(visitDateCalendar.getTimeInMillis());
                            saveLastPlannedObjectName(obj.getName());
                            planHelper.updateVisitTime(this, obj.getName(), visitDateCalendar.getTimeInMillis());
                            startCountdownTimer();
                        });

                        break;
                    }
                }
            });

            objectImage.setOnLongClickListener(v -> {
                showDeleteDialog();
                return true;
            });



            visitDateButton.setOnClickListener(v -> openDatePickerDialog());

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(Plan.this, MainActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(Plan.this, Notifications.class));
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Plan.this, LoginActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(Plan.this, Profile.class));
                    return true;
                }

                return false;
            });

            SharedPreferences sharedPreferences = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
            long savedVisitTime = sharedPreferences.getLong("visitTime", -1);

            if (savedVisitTime != -1) {
                visitDateCalendar.setTimeInMillis(savedVisitTime);  
                startCountdownTimer(); 
            }
        }


        private void showDeleteDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(Plan.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_delete, null);
            builder.setView(dialogView);
            Button btnYes = dialogView.findViewById(R.id.btnYes);
            Button btnNo = dialogView.findViewById(R.id.btnNo);
            TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
            titleTextView.setText("Искате ли да изтриете този обект от плана?");
            AlertDialog alertDialog = builder.create();
            btnYes.setOnClickListener(v -> {
                planHelper.deleteObjectFromPlan(this, objectName.getText().toString(),
                        () -> {
                            stopCountdownTimer();
                            clearPlanFromPreferences();
                            removeObjectFromLocalList(objectName.getText().toString());
                            resetObjectView();
                        });
                alertDialog.dismiss();
            });
            btnNo.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
            alertDialog.show();
        }

        private void removeObjectFromLocalList(String objectNameToDelete) {
            Iterator<TouristObject> iterator = allObjects.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getName().equals(objectNameToDelete)) {
                    iterator.remove();
                    break;
                }
            }
            List<String> names = new ArrayList<>();
            for (TouristObject obj : allObjects) {
                names.add(obj.getName());
            }
            adapter.clear();
            adapter.addAll(names);
            adapter.notifyDataSetChanged();
        }

        private void resetObjectView() {
            objectName.setText("");
            objectImage.setImageResource(R.drawable.default_image);
            countdownTimer.setText("");
            visitDateCalendar = null;
            Log.d("PlanDebug", "resetObjectView() извикан");
        }

        private void saveVisitTimeToPreferences(long visitTime) {
            SharedPreferences sharedPreferences = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("visitTime", visitTime);
            editor.apply();
        }


        private void startCountdownTimer() {
            if (visitDateCalendar == null) {
                countdownTimer.setText("Не е избрана дата за посещение");
                return; 
            }
            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);
            }
            countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    long visitTime = visitDateCalendar.getTimeInMillis(); 
                    long timeRemaining = visitTime - currentTime;
                    if (timeRemaining > 0) {
                        long days = timeRemaining / (1000 * 60 * 60 * 24);
                        long hours = (timeRemaining / (1000 * 60 * 60)) % 24;
                        long minutes = (timeRemaining / (1000 * 60)) % 60;
                        long seconds = (timeRemaining / 1000) % 60;
                        String time = String.format("%dд %02d:%02d:%02d", days, hours, minutes, seconds);
                        countdownTimer.setText(time);
                        countdownHandler.postDelayed(this, 1000);
                    } else {
                        countdownTimer.setText("Времето изтече!");
                    }
                }
            };
            countdownHandler.post(countdownRunnable);
        }

        private void loadTouristObjects() {
            touristObjectsRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> names = new ArrayList<>();
                    allObjects.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        TouristObject obj = document.toObject(TouristObject.class);
                        if (obj != null) {
                            allObjects.add(obj);
                            names.add(obj.getName());
                        }
                    }
                    if (allObjects.isEmpty()) {
                        Toast.makeText(Plan.this, "Няма налични туристически обекти.", Toast.LENGTH_SHORT).show();
                    }
                    adapter.clear();
                    adapter.addAll(names);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(Plan.this, "Не успяхме да заредим данни за обектите.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void displayTouristObject(TouristObject obj) {
            objectName.setText(obj.getName());
            if (obj.getImage() != null && !obj.getImage().isEmpty()) {
                int imageResId = getResources().getIdentifier(obj.getImage(), "drawable", getPackageName());
                if (imageResId != 0) {
                    objectImage.setImageResource(imageResId);
                } else {
                    objectImage.setImageResource(R.drawable.default_image);
                }
            } else {
                objectImage.setImageResource(R.drawable.default_image);
            }
            startCountdownTimer();
        }

        private void openDatePickerDialog() {
            if (visitDateCalendar == null) {
                visitDateCalendar = Calendar.getInstance();
            }
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    R.style.CustomDatePickerDialog,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        visitDateCalendar = Calendar.getInstance();
                        visitDateCalendar.set(year, monthOfYear, dayOfMonth);
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(visitDateCalendar.getTime());
                        visitDateButton.setText("Посещение на: " + formattedDate);
                        saveVisitTimeToPreferences(visitDateCalendar.getTimeInMillis());
                        planHelper.updateVisitTime(this, objectName.getText().toString(), visitDateCalendar.getTimeInMillis());
                        startCountdownTimer();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        private void saveLastPlannedObjectName(String name) {
            SharedPreferences prefs = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
            prefs.edit().putString("lastPlannedObject", name).apply();
        }


        private void loadPlanFromFirestore() {
            String targetName = getIntent().getStringExtra("objectName");
            long passedVisitTime = getIntent().getLongExtra("visitTime", -1);
            if (targetName == null) {
                SharedPreferences prefs = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
                targetName = prefs.getString("lastPlannedObject", null);
            }
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CollectionReference planRef = firestore.collection("users").document(userId).collection("plan");
            if (targetName != null) {
                String safeTargetName = sanitizeForFirestoreId(targetName);
                planRef.document(safeTargetName).get().addOnSuccessListener(document -> {
                    if (document.exists()) {
                        TouristObject obj = document.toObject(TouristObject.class);
                        if (obj != null) {
                            obj.setName(document.getId());
                            if (passedVisitTime > 0) {
                                visitDateCalendar = Calendar.getInstance();
                                visitDateCalendar.setTimeInMillis(passedVisitTime);
                                saveVisitTimeToPreferences(passedVisitTime);
                            } else if (obj.getVisitTime() > 0) {
                                visitDateCalendar = Calendar.getInstance();
                                visitDateCalendar.setTimeInMillis(obj.getVisitTime());
                                saveVisitTimeToPreferences(obj.getVisitTime());
                            }
                            displayTouristObject(obj);
                        }
                    } else {
                        Toast.makeText(this, "Обектът не е намерен в Plan.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка при зареждане на Plan обект.", Toast.LENGTH_SHORT).show();
                });
            } else {
                planRef.get().addOnSuccessListener(query -> {
                    for (DocumentSnapshot document : query) {
                        TouristObject obj = document.toObject(TouristObject.class);
                        if (obj != null) {
                            obj.setName(document.getId());

                            if (obj.getVisitTime() > 0) {
                                visitDateCalendar = Calendar.getInstance();
                                visitDateCalendar.setTimeInMillis(obj.getVisitTime());
                                saveVisitTimeToPreferences(obj.getVisitTime());
                            }
                            displayTouristObject(obj);
                            return;
                        }
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Грешка при зареждане на Plan.", Toast.LENGTH_SHORT).show()
                );
            }
        }



        @Override
        protected void onPause() {
            super.onPause();
            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (visitDateCalendar != null) {
                startCountdownTimer();
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);
                countdownRunnable = null;
            }
        }

        private void stopCountdownTimer() {
            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);
                countdownRunnable = null;
                countdownTimer.setText("");
            }
        }

        private String sanitizeForFirestoreId(String name) {
            return name.replaceAll("[.#\\$\\[\\]/]", "_");
        }

        private void clearPlanFromPreferences() {
            SharedPreferences prefs = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
            prefs.edit()
                    .remove("visitTime")
                    .remove("lastPlannedObject")
                    .apply();
        }

    }
