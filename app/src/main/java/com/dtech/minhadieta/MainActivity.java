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

import java.util.concurrent.atomic.AtomicReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layoutExerciseEntries;
    private float burnedCalories = 0;
    private TextView tvCaloriesConsumed, tvCaloriesRemaining;
    private float totalCaloriesGoal = 2000;

    private LinearLayout layoutBreakfast, layoutLunch, layoutDinner, layoutSnack;
    private AppDatabase db;
    private String selectedDate;
    private TextView tvWaterCount;
    private SwitchMaterial switchWaterReminder;
    private static final long REMINDER_INTERVAL = 2 * 60 * 60 * 1000; // 2 horas em milissegundos

    private final ActivityResultLauncher<Intent> foodSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String foodName = data.getStringExtra("FOOD_NAME");
                    int foodCalories = data.getIntExtra("FOOD_CALORIES", 0);
                    String mealType = data.getStringExtra("MEAL_TYPE");

                    MealEntryEntity newEntry = new MealEntryEntity(foodName, foodCalories, mealType, selectedDate);
                    saveMealEntry(newEntry);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        db = AppDatabase.getDatabase(this);

        initializeViews();
        setupListeners(); // A chamada principal para todos os listeners
        loadUserProfile();
        loadUIData(); // Renomeei para ficar mais claro
    }

    private void initializeViews() {
        layoutExerciseEntries = findViewById(R.id.layoutExerciseEntries);
        tvCaloriesConsumed = findViewById(R.id.tvCaloriesConsumed);
        tvCaloriesRemaining = findViewById(R.id.tvCaloriesRemaining);
        layoutBreakfast = findViewById(R.id.layoutBreakfastFoods);
        layoutLunch = findViewById(R.id.layoutLunchFoods);
        layoutDinner = findViewById(R.id.layoutDinnerFoods);
        layoutSnack = findViewById(R.id.layoutSnackFoods);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        switchWaterReminder = findViewById(R.id.switchWaterReminder);
    }

    private void loadMealsForDate(String date) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Busca todas as informações para a data selecionada
            List<MealEntryEntity> mealsForDate = db.foodDao().getMealsForDate(date);
            List<LoggedExerciseEntity> exercisesForDate = db.foodDao().getLoggedExercisesForDate(date);
            WaterIntakeEntity waterForDate = db.foodDao().getWaterForDate(date);

            runOnUiThread(() -> {
                // Limpa a tela
                clearAllMealLayouts();
                clearExerciseLayout();

                // Calcula calorias consumidas
                float currentConsumedCalories = 0;
                for (MealEntryEntity entry : mealsForDate) {
                    currentConsumedCalories += entry.calories;
                    addFoodToUI(entry);
                }

                // Calcula calorias queimadas e exibe os exercícios
                float currentBurnedCalories = 0;
                for (LoggedExerciseEntity entry : exercisesForDate) {
                    currentBurnedCalories += entry.caloriesBurned;
                    addExerciseToUI(entry);
                }

                // Atualiza os displays
                updateCalorieDisplay(currentConsumedCalories, currentBurnedCalories);
                updateWaterDisplay(waterForDate);
            });
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
            runOnUiThread(this::loadUIData);
        });
    }

    private void updateWaterDisplay(WaterIntakeEntity waterIntake) {
        if (waterIntake != null) {
            tvWaterCount.setText(String.valueOf(waterIntake.bottleCount));
        } else {
            tvWaterCount.setText("0");
        }
    }
    private void saveMealEntry(MealEntryEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().insertMealEntry(entry);
            runOnUiThread(this::loadUIData);
        });
    }
    private void deleteMealEntry(MealEntryEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().deleteMealEntryById(entry.id);
            runOnUiThread(this::loadUIData);
        });
    }

    private void addFoodToUI(MealEntryEntity entry) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View foodItemView = inflater.inflate(R.layout.list_item_meal_entry, null, false);
        TextView foodNameTextView = foodItemView.findViewById(R.id.tv_food_item_name);
        ImageButton deleteButton = foodItemView.findViewById(R.id.btn_delete_item);

        foodNameTextView.setText(String.format("• %s (%d kcal)", entry.foodName, entry.calories));
        deleteButton.setOnClickListener(v -> deleteMealEntry(entry));

        switch (entry.mealType) {
            case "breakfast": layoutBreakfast.addView(foodItemView); break;
            case "lunch": layoutLunch.addView(foodItemView); break;
            case "dinner": layoutDinner.addView(foodItemView); break;
            case "snack": layoutSnack.addView(foodItemView); break;
        }
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
                loadMealsForDate(selectedDate);
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
    // --- MÉTODOS DE APOIO ---

    private void setupAddButtons() {
        findViewById(R.id.btnAddBreakfast).setOnClickListener(v -> openFoodSearch("breakfast"));
        findViewById(R.id.btnAddLunch).setOnClickListener(v -> openFoodSearch("lunch"));
        findViewById(R.id.btnAddDinner).setOnClickListener(v -> openFoodSearch("dinner"));
        findViewById(R.id.btnAddSnack).setOnClickListener(v -> openFoodSearch("snack"));
        findViewById(R.id.btnAddWater).setOnClickListener(v -> updateWaterCount(1));
        findViewById(R.id.btnRemoveWater).setOnClickListener(v -> updateWaterCount(-1));

        setupWeekDaySelector();
    }

    private void openFoodSearch(String mealType) {
        Intent intent = new Intent(this, FoodSearchActivity.class);
        intent.putExtra("MEAL_TYPE", mealType);
        foodSearchLauncher.launch(intent);
    }

    private void clearAllMealLayouts() {
        layoutBreakfast.removeAllViews();
        layoutLunch.removeAllViews();
        layoutDinner.removeAllViews();
        layoutSnack.removeAllViews();
    }

    /**
     * Atualiza os contadores de calorias na tela.
     */
    // A nova versão aceita as calorias queimadas
    private void updateCalorieDisplay(float consumed, float burned) {
        tvCaloriesConsumed.setText(String.valueOf((int) consumed));

        // A nova fórmula: Meta - Consumidas + Queimadas
        float remaining = totalCaloriesGoal - consumed + burned;
        tvCaloriesRemaining.setText(String.valueOf((int) remaining));
    }


    /**
     * Compara um calendário com o dia de hoje.
     * Renomeado de isToday para isSameDay para evitar conflito.
     */
    private boolean isSameDay(Calendar cal) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        totalCaloriesGoal = prefs.getFloat("calorieGoal", 2000);
    }

    private void loadUIData() {
        // Carrega as refeições e água para a data atualmente selecionada
        loadMealsForDate(selectedDate);
    }

    private void setupListeners() {
        // Listeners dos botões de refeição
        findViewById(R.id.btnAddBreakfast).setOnClickListener(v -> openFoodSearch("breakfast"));
        findViewById(R.id.btnAddLunch).setOnClickListener(v -> openFoodSearch("lunch"));
        findViewById(R.id.btnAddDinner).setOnClickListener(v -> openFoodSearch("dinner"));
        findViewById(R.id.btnAddSnack).setOnClickListener(v -> openFoodSearch("snack"));

        // Listeners dos botões de água
        findViewById(R.id.btnAddWater).setOnClickListener(v -> updateWaterCount(1));
        findViewById(R.id.btnRemoveWater).setOnClickListener(v -> updateWaterCount(-1));

            //exercicio
        findViewById(R.id.btnAddExercise).setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseSearchActivity.class);
            exerciseSearchLauncher.launch(intent);
        });

        // Listener do Switch de Lembrete
        SharedPreferences prefs = getSharedPreferences("MinhaDietaPrefs", MODE_PRIVATE);
        switchWaterReminder.setChecked(prefs.getBoolean("water_reminder_on", false));
        switchWaterReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("water_reminder_on", isChecked).apply();
            if (isChecked) {
                scheduleWaterReminder();
            } else {
                cancelWaterReminder();
            }
        });

        // Configura os dias da semana
        setupWeekDaySelector();
    }

    private void scheduleWaterReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Agenda o alarme para disparar a cada 2 horas, começando daqui a 2 horas
        long triggerAtMillis = System.currentTimeMillis() + REMINDER_INTERVAL;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, REMINDER_INTERVAL, pendingIntent);

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

    private final ActivityResultLauncher<Intent> exerciseSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String name = data.getStringExtra("EXERCISE_NAME");
                    int duration = data.getIntExtra("EXERCISE_DURATION", 0);
                    int calories = data.getIntExtra("CALORIES_BURNED", 0);

                    LoggedExerciseEntity newEntry = new LoggedExerciseEntity(name, duration, calories, selectedDate);
                    saveLoggedExercise(newEntry);
                }
            });

    // Adicione estes 4 métodos na sua MainActivity

    private void saveLoggedExercise(LoggedExerciseEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().insertLoggedExercise(entry);
            // Recarrega todos os dados da UI para o dia selecionado
            runOnUiThread(this::loadUIData);
        });
    }

    private void deleteLoggedExercise(LoggedExerciseEntity entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.foodDao().deleteLoggedExerciseById(entry.id);
            runOnUiThread(this::loadUIData);
        });
    }

    private void addExerciseToUI(LoggedExerciseEntity entry) {
        LayoutInflater inflater = LayoutInflater.from(this);
        // Vamos reutilizar o mesmo layout de item, pois ele já tem o que precisamos
        View exerciseItemView = inflater.inflate(R.layout.list_item_meal_entry, null, false);

        TextView exerciseNameTextView = exerciseItemView.findViewById(R.id.tv_food_item_name);
        ImageButton deleteButton = exerciseItemView.findViewById(R.id.btn_delete_item);

        // Formata o texto para exibir o exercício, tempo e calorias queimadas
        String text = String.format("• %s (%d min) -%d kcal", entry.exerciseName, entry.durationInMinutes, entry.caloriesBurned);
        exerciseNameTextView.setText(text);

        // Configura o botão de deletar para este exercício
        deleteButton.setOnClickListener(v -> deleteLoggedExercise(entry));

        layoutExerciseEntries.addView(exerciseItemView);
    }

    // Limpa o layout de exercícios para evitar duplicatas
    private void clearExerciseLayout() {
        layoutExerciseEntries.removeAllViews();
    }
    }