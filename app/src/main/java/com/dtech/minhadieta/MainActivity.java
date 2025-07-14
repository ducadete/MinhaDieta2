package com.dtech.minhadieta;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // --- Variáveis de UI e Dados ---
    private TextView tvCaloriesConsumed, tvCaloriesRemaining, tvWaterCount;
    private float totalCaloriesGoal = 2000;
    private LinearLayout layoutBreakfast, layoutLunch, layoutDinner, layoutSnack, layoutExerciseEntries;
    private SwitchMaterial switchWaterReminder;
    private AppDatabase db;
    private String selectedDate;

    private static final String PREFS_NAME = "MinhaDietaPrefs";
    private static final String KEY_REMINDER_ON = "water_reminder_on";
    private static final long REMINDER_INTERVAL = 2 * 60 * 60 * 1000; // 2 horas

    // --- Launchers para Atividades ---
    private final ActivityResultLauncher<Intent> foodSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    MealEntryEntity newEntry = new MealEntryEntity(
                            data.getStringExtra("FOOD_NAME"),
                            data.getIntExtra("FOOD_CALORIES", 0),
                            data.getStringExtra("MEAL_TYPE"),
                            selectedDate
                    );
                    saveMealEntry(newEntry);
                }
            });

    private final ActivityResultLauncher<Intent> exerciseSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    LoggedExerciseEntity newEntry = new LoggedExerciseEntity(
                            data.getStringExtra("EXERCISE_NAME"),
                            data.getIntExtra("EXERCISE_DURATION", 0),
                            data.getIntExtra("CALORIES_BURNED", 0),
                            selectedDate
                    );
                    saveLoggedExercise(newEntry);
                }
            });


    // --- Ciclo de Vida da Activity ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        db = AppDatabase.getDatabase(this);

        initializeViews();
        setupListeners();
        loadUserProfile();
        loadDataForSelectedDate();
    }

    // --- Métodos de Configuração ---

    private void initializeViews() {
        tvCaloriesConsumed = findViewById(R.id.tvCaloriesConsumed);
        tvCaloriesRemaining = findViewById(R.id.tvCaloriesRemaining);
        layoutBreakfast = findViewById(R.id.layoutBreakfastFoods);
        layoutLunch = findViewById(R.id.layoutLunchFoods);
        layoutDinner = findViewById(R.id.layoutDinnerFoods);
        layoutSnack = findViewById(R.id.layoutSnackFoods);
        layoutExerciseEntries = findViewById(R.id.layoutExerciseEntries);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        switchWaterReminder = findViewById(R.id.switchWaterReminder);
    }

    private void setupListeners() {
        findViewById(R.id.btnAddBreakfast).setOnClickListener(v -> openFoodSearch("breakfast"));
        findViewById(R.id.btnAddLunch).setOnClickListener(v -> openFoodSearch("lunch"));
        findViewById(R.id.btnAddDinner).setOnClickListener(v -> openFoodSearch("dinner"));
        findViewById(R.id.btnAddSnack).setOnClickListener(v -> openFoodSearch("snack"));
        findViewById(R.id.btnAddExercise).setOnClickListener(v -> openExerciseSearch());
        findViewById(R.id.btnAddWater).setOnClickListener(v -> updateWaterCount(1));
        findViewById(R.id.btnRemoveWater).setOnClickListener(v -> updateWaterCount(-1));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchWaterReminder.setChecked(prefs.getBoolean(KEY_REMINDER_ON, false));
        switchWaterReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_REMINDER_ON, isChecked).apply();
            if (isChecked) {
                scheduleWaterReminder();
            } else {
                cancelWaterReminder();
            }
        });

        setupWeekDaySelector();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        totalCaloriesGoal = prefs.getFloat("calorieGoal", 2000);
    }

    // --- Lógica Principal de Dados ---

    private void loadDataForSelectedDate() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<MealEntryEntity> meals = db.foodDao().getMealsForDate(selectedDate);
            List<LoggedExerciseEntity> exercises = db.foodDao().getLoggedExercisesForDate(selectedDate);
            WaterIntakeEntity water = db.foodDao().getWaterForDate(selectedDate);

            runOnUiThread(() -> {
                clearAllLayouts();

                float totalConsumed = 0;
                for (MealEntryEntity entry : meals) {
                    totalConsumed += entry.calories;
                    addFoodToUI(entry);
                }

                float totalBurned = 0;
                for (LoggedExerciseEntity entry : exercises) {
                    totalBurned += entry.caloriesBurned;
                    addExerciseToUI(entry);
                }

                updateCalorieDisplay(totalConsumed, totalBurned);
                updateWaterDisplay(water);
            });
        });
    }

    private void saveMealEntry(MealEntryEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().insertMealEntry(entry);
            runOnUiThread(this::loadDataForSelectedDate);
        });
    }

    private void deleteMealEntry(MealEntryEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().deleteMealEntryById(entry.id);
            runOnUiThread(this::loadDataForSelectedDate);
        });
    }

    private void saveLoggedExercise(LoggedExerciseEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().insertLoggedExercise(entry);
            runOnUiThread(this::loadDataForSelectedDate);
        });
    }

    private void deleteLoggedExercise(LoggedExerciseEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().deleteLoggedExerciseById(entry.id);
            runOnUiThread(this::loadDataForSelectedDate);
        });
    }

    private void updateWaterCount(int change) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WaterIntakeEntity currentIntake = db.foodDao().getWaterForDate(selectedDate);
            if (currentIntake == null) {
                if (change > 0) {
                    db.foodDao().upsertWaterIntake(new WaterIntakeEntity(selectedDate, change));
                }
            } else {
                int newCount = currentIntake.bottleCount + change;
                if (newCount >= 0) {
                    currentIntake.bottleCount = newCount;
                    db.foodDao().upsertWaterIntake(currentIntake);
                }
            }
            runOnUiThread(this::loadDataForSelectedDate);
        });
    }

    // --- Lógica de UI ---

    private void addFoodToUI(MealEntryEntity entry) {
        View view = LayoutInflater.from(this).inflate(R.layout.list_item_meal_entry, null, false);
        ((TextView) view.findViewById(R.id.tv_food_item_name)).setText(String.format("• %s (%d kcal)", entry.foodName, entry.calories));
        view.findViewById(R.id.btn_delete_item).setOnClickListener(v -> deleteMealEntry(entry));
        switch (entry.mealType) {
            case "breakfast": layoutBreakfast.addView(view); break;
            case "lunch": layoutLunch.addView(view); break;
            case "dinner": layoutDinner.addView(view); break;
            case "snack": layoutSnack.addView(view); break;
        }
    }

    private void addExerciseToUI(LoggedExerciseEntity entry) {
        View view = LayoutInflater.from(this).inflate(R.layout.list_item_meal_entry, null, false);
        ((TextView) view.findViewById(R.id.tv_food_item_name)).setText(String.format("• %s (%d min) -%d kcal", entry.exerciseName, entry.durationInMinutes, entry.caloriesBurned));
        view.findViewById(R.id.btn_delete_item).setOnClickListener(v -> deleteLoggedExercise(entry));
        layoutExerciseEntries.addView(view);
    }

    private void updateWaterDisplay(WaterIntakeEntity waterIntake) {
        if (tvWaterCount != null) {
            tvWaterCount.setText(waterIntake != null ? String.valueOf(waterIntake.bottleCount) : "0");
        }
    }

    private void updateCalorieDisplay(float consumed, float burned) {
        tvCaloriesConsumed.setText(String.valueOf((int) consumed));
        float remaining = totalCaloriesGoal - consumed + burned;
        tvCaloriesRemaining.setText(String.valueOf((int) remaining));
    }

    private void clearAllLayouts() {
        layoutBreakfast.removeAllViews();
        layoutLunch.removeAllViews();
        layoutDinner.removeAllViews();
        layoutSnack.removeAllViews();
        layoutExerciseEntries.removeAllViews();
    }

    // --- Outros Métodos Auxiliares ---

    private void openFoodSearch(String mealType) {
        Intent intent = new Intent(this, FoodSearchActivity.class);
        intent.putExtra("MEAL_TYPE", mealType);
        foodSearchLauncher.launch(intent);
    }

    private void openExerciseSearch() {
        Intent intent = new Intent(this, ExerciseSearchActivity.class);
        exerciseSearchLauncher.launch(intent);
    }

    private void setupWeekDaySelector() {
        LinearLayout weekDaysContainer = findViewById(R.id.week_days_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] dayNames = {"SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM"};

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        }
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        weekDaysContainer.removeAllViews();

        for (int i = 0; i < 7; i++) {
            final String dateForDb = dbDateFormat.format(calendar.getTime());
            View dayView = inflater.inflate(R.layout.layout_day_of_week, weekDaysContainer, false);

            TextView tvDayName = dayView.findViewById(R.id.tv_day_name);
            TextView tvDayDate = dayView.findViewById(R.id.tv_day_date);
            tvDayName.setText(dayNames[i]);
            tvDayDate.setText(dateFormat.format(calendar.getTime()));

            dayView.setOnClickListener(v -> {
                for(int j = 0; j < weekDaysContainer.getChildCount(); j++) {
                    weekDaysContainer.getChildAt(j).setSelected(false);
                }
                v.setSelected(true);
                selectedDate = dateForDb;
                loadDataForSelectedDate();
            });

            if (isSameDay(calendar)) {
                dayView.setSelected(true);
                selectedDate = dateForDb;
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) params.setMarginStart(16);
            dayView.setLayoutParams(params);

            weekDaysContainer.addView(dayView);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar cal) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private void scheduleWaterReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + REMINDER_INTERVAL, REMINDER_INTERVAL, pendingIntent);
        Toast.makeText(this, "Lembrete de água ativado!", Toast.LENGTH_SHORT).show();
    }

    private void cancelWaterReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Lembrete de água desativado.", Toast.LENGTH_SHORT).show();
    }
}