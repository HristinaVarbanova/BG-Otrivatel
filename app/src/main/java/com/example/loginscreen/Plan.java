    package com.example.loginscreen;

    import android.app.DatePickerDialog;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.os.Handler;
    import android.provider.Settings;
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
    import androidx.work.OneTimeWorkRequest;
    import androidx.work.WorkManager;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;

    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.CollectionReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.HashMap;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;

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

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_plan);

            firestore = FirebaseFirestore.getInstance();
            touristObjectsRef = firestore.collection("TouristObjects");

            if (visitDateCalendar == null) {
                visitDateCalendar = Calendar.getInstance(); // Инициализация, ако е null
            }

            searchAutoComplete = findViewById(R.id.searchAutoComplete);
            objectImage = findViewById(R.id.objectImage);
            objectName = findViewById(R.id.objectName);
            countdownTimer = findViewById(R.id.countdownTimer);
            visitDateButton = findViewById(R.id.visitDateButton);

            // Custom adapter със зелен текст
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
                        displayTouristObject(obj);  // Показваме обекта
                        addToPlan(obj); // Добавяме обекта в план
                        break;
                    }
                }
            });

            objectImage.setOnLongClickListener(v -> {
                showDeleteDialog();
                return true;  // Връщаме true, за да сигнализираме, че събитието е обработено
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

            // Зареждаме времето за посещение от SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
            long savedVisitTime = sharedPreferences.getLong("visitTime", -1);

            if (savedVisitTime != -1) {
                visitDateCalendar.setTimeInMillis(savedVisitTime);  // Възстановяваме времето от SharedPreferences
                startCountdownTimer();  // Стартираме таймера с възстановеното време
            }
        }


        private void showDeleteDialog() {
            // Създаваме нов диалогов прозорец с персонализирания изглед
            AlertDialog.Builder builder = new AlertDialog.Builder(Plan.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_delete, null);
            builder.setView(dialogView);

            // Вземаме бутоните от диалога
            Button btnYes = dialogView.findViewById(R.id.btnYes);
            Button btnNo = dialogView.findViewById(R.id.btnNo);

            // Извеждаме заглавие на диалога
            TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
            titleTextView.setText("Искате ли да изтриете този обект от плана?");

            // Създаваме диалога
            AlertDialog alertDialog = builder.create();

            // Слушатели за бутоните
            btnYes.setOnClickListener(v -> {
                deleteTouristObjectFromPlan(); // Изтриваме обекта
                alertDialog.dismiss();  // Затваряме диалога
            });

            btnNo.setOnClickListener(v -> {
                alertDialog.dismiss();  // Просто затваряме диалога, без да правим нищо
            });

            // Показваме диалога
            alertDialog.show();
        }
        private void deleteTouristObjectFromPlan() {
            String objectNameToDelete = objectName.getText().toString();

            stopCountdownTimer();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CollectionReference planRef = firestore.collection("users").document(userId).collection("plan");

            planRef.document(sanitizeForFirestoreId(objectNameToDelete)).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Plan.this, "Обектът беше изтрит от плана.", Toast.LENGTH_SHORT).show();

                        // 🆕 Изчистваме данните от SharedPreferences
                        clearPlanFromPreferences();

                        removeObjectFromLocalList(objectNameToDelete);
                        resetObjectView();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Plan.this, "Не успяхме да изтрием обекта.", Toast.LENGTH_SHORT).show();
                    });
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
            // Премахваме всички данни от изгледа
            objectName.setText("");  // Изчистваме името на обекта
            objectImage.setImageResource(R.drawable.default_image); // Връщаме на стандартната картинка
            countdownTimer.setText(""); // Изчистваме таймера

            // Нулираме и времето на посещение
            visitDateCalendar = null;  // Нулираме календарния обект

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
                return; // Ако няма избрана дата, просто излизаме от метода
            }

            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);
            }

            countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    long visitTime = visitDateCalendar.getTimeInMillis(); // Вземаме инициализираното време
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
                    objectImage.setImageResource(R.drawable.default_image); // резервно изображение
                }
            } else {
                objectImage.setImageResource(R.drawable.default_image); // ако няма подадено изображение
            }


            startCountdownTimer();
        }

        private void openDatePickerDialog() {
            if (visitDateCalendar == null) {
                visitDateCalendar = Calendar.getInstance(); // Инициализация
            }
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    R.style.CustomDatePickerDialog, // Задай персонализирания стил тук
                    (view, year, monthOfYear, dayOfMonth) -> {
                        visitDateCalendar = Calendar.getInstance();
                        visitDateCalendar.set(year, monthOfYear, dayOfMonth);
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(visitDateCalendar.getTime());
                        visitDateButton.setText("Посещение на: " + formattedDate);

                        // Записваме времето за посещение в SharedPreferences
                        saveVisitTimeToPreferences(visitDateCalendar.getTimeInMillis());
                        updateVisitTimeInFirestore(objectName.getText().toString(), visitDateCalendar.getTimeInMillis());


                        startCountdownTimer();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        private void addToPlan(TouristObject obj) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CollectionReference planRef = firestore.collection("users").document(userId).collection("plan");

            // Вземаме текущото време за посещение
            if (visitDateCalendar == null) {
                visitDateCalendar = Calendar.getInstance();
            }

            long visitTime = visitDateCalendar.getTimeInMillis();

            // Създаваме обекта с данни
            Map<String, Object> planData = new HashMap<>();
            planData.put("name", obj.getName());
            planData.put("image", obj.getImage());
            planData.put("visitTime", visitTime);

            // 🔒 Безопасно име за Firestore ID
            String safeName = sanitizeForFirestoreId(obj.getName());
            Log.d("FirestoreDebug", "Добавям в Plan с ID: " + safeName); // по избор

            // Записваме обекта в Firestore
            planRef.document(safeName)
                    .set(planData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Plan.this, "Обектът е добавен в плана.", Toast.LENGTH_SHORT).show();
                        saveVisitTimeToPreferences(visitTime);
                        saveLastPlannedObjectName(obj.getName()); // 🆕// ⏱️ запазваме времето
                        startCountdownTimer(); // стартираме таймер
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Plan.this, "Не успяхме да добавим обекта в плана.", Toast.LENGTH_SHORT).show();
                    });
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
                String safeTargetName = sanitizeForFirestoreId(targetName); // ✅ добавено!

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
                countdownHandler.removeCallbacks(countdownRunnable);  // Спиране на таймера при напускане на екрана
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            // Продължаваме таймера, ако има зададена дата на посещение
            if (visitDateCalendar != null) {
                startCountdownTimer();  // Стартираме отново таймера
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            // Спираме таймера, ако активността се унищожава
            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);  // Спираме таймера
                countdownRunnable = null;  // Изчистваме runnable
            }
        }

        private void stopCountdownTimer() {
            if (countdownRunnable != null) {
                countdownHandler.removeCallbacks(countdownRunnable);  // Премахваме текущия Runnable
                countdownRunnable = null;  // Изчистваме runnable
                countdownTimer.setText(""); // Изчистваме таймера от екрана
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

        private void updateVisitTimeInFirestore(String objectName, long visitTime) {
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










    }
