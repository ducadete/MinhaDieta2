package com.dtech.minhadieta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView tvCaloriesConsumed, tvCaloriesRemaining;
    private float totalCaloriesGoal = 2000;
    private float consumedCalories = 0;

    private LinearLayout layoutBreakfast, layoutLunch, layoutDinner, layoutSnack;

    // Launcher para receber o resultado da FoodSearchActivity
    private final ActivityResultLauncher<Intent> foodSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String foodName = data.getStringExtra("FOOD_NAME");
                    int foodCalories = data.getIntExtra("FOOD_CALORIES", 0);
                    String mealType = data.getStringExtra("MEAL_TYPE");

                    addFoodToMeal(foodName, foodCalories, mealType);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadUserProfile();
        setupWeekDaySelector();
        setupAddButtons();
        updateCalorieDisplay();
    }

    private void initializeViews() {
        tvCaloriesConsumed = findViewById(R.id.tvCaloriesConsumed);
        tvCaloriesRemaining = findViewById(R.id.tvCaloriesRemaining);
        layoutBreakfast = findViewById(R.id.layoutBreakfastFoods);
        layoutLunch = findViewById(R.id.layoutLunchFoods);
        layoutDinner = findViewById(R.id.layoutDinnerFoods);
        layoutSnack = findViewById(R.id.layoutSnackFoods);
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        totalCaloriesGoal = prefs.getFloat("calorieGoal", 2000);
    }

    private void setupWeekDaySelector() {
        // ... (código da resposta anterior)
        ChipGroup chipGroup = findViewById(R.id.chipGroupWeekDays);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int chipIndex = today - 2;
        if (chipIndex < 0) { chipIndex = 6; }
        if (chipGroup.getChildCount() > chipIndex) {
            ((Chip) chipGroup.getChildAt(chipIndex)).setChecked(true);
        }
    }

    private void setupAddButtons() {
        ImageButton btnAddBreakfast = findViewById(R.id.btnAddBreakfast);
        ImageButton btnAddLunch = findViewById(R.id.btnAddLunch);
        ImageButton btnAddDinner = findViewById(R.id.btnAddDinner);
        ImageButton btnAddSnack = findViewById(R.id.btnAddSnack);

        btnAddBreakfast.setOnClickListener(v -> openFoodSearch("breakfast"));
        btnAddLunch.setOnClickListener(v -> openFoodSearch("lunch"));
        btnAddDinner.setOnClickListener(v -> openFoodSearch("dinner"));
        btnAddSnack.setOnClickListener(v -> openFoodSearch("snack"));
    }

    private void openFoodSearch(String mealType) {
        Intent intent = new Intent(this, FoodSearchActivity.class);
        intent.putExtra("MEAL_TYPE", mealType);
        foodSearchLauncher.launch(intent);
    }

    private void addFoodToMeal(String name, int calories, String mealType) {
        consumedCalories += calories;
        updateCalorieDisplay();

        TextView foodView = new TextView(this);
        foodView.setText(String.format("• %s (%d kcal)", name, calories));
        foodView.setTextSize(16);
        foodView.setPadding(0, 4, 0, 4);

        switch (mealType) {
            case "breakfast":
                layoutBreakfast.addView(foodView);
                break;
            case "lunch":
                layoutLunch.addView(foodView);
                break;

            case "dinner":
                layoutDinner.addView(foodView);
                break;
            case "snack":
                layoutSnack.addView(foodView);
                break;
        }
    }

    private void updateCalorieDisplay() {
        tvCaloriesConsumed.setText(String.valueOf((int) consumedCalories));
        float remaining = totalCaloriesGoal - consumedCalories;
        tvCaloriesRemaining.setText(String.valueOf((int) remaining));
    }
}
