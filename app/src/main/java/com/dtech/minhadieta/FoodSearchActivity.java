package com.dtech.minhadieta;

import android.widget.ImageButton;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import android.database.sqlite.SQLiteConstraintException;


public class FoodSearchActivity extends AppCompatActivity {

    private static final String TAG = "FoodSearchActivity";
    private FoodAdapter adapter;
    private String mealType;
    private AppDatabase db;
    private ExecutorService executorService;
    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        mealType = getIntent().getStringExtra("MEAL_TYPE");
        db = AppDatabase.getDatabase(this);
        executorService = AppDatabase.databaseWriteExecutor;

        setupRecyclerView();
        checkAndPrepopulateDatabase();

        EditText etSearch = findViewById(R.id.etSearchFood);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) searchDatabase(s.toString());
                else if (adapter != null) adapter.updateData(new ArrayList<>());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(new ArrayList<>(), this::showAddFoodDialog);
        recyclerView.setAdapter(adapter);
    }

    private void searchDatabase(String query) {
        String searchQuery = "%" + query + "%";
        executorService.execute(() -> {
            List<FoodEntity> results = db.foodDao().searchByName(searchQuery);
            Log.d(TAG, "Busca por '" + query + "' encontrou " + results.size() + " resultados.");
            runOnUiThread(() -> {
                adapter.updateData(results);
            });
        });
    }

    private void checkAndPrepopulateDatabase() {
        executorService.execute(() -> {
            if (db.foodDao().countFoods() == 0) {
                Log.d(TAG, "Banco de dados vazio. Iniciando carregamento...");
                runOnUiThread(() -> Toast.makeText(FoodSearchActivity.this, "Carregando base de dados...", Toast.LENGTH_LONG).show());

                loadFoodsFromCSV();
                // Adicione as chamadas para os outros arquivos se precisar deles
                // loadAminoAcidsFromCSV();
                // loadFattyAcidsFromCSV();

                Log.d(TAG, "Carregamento finalizado.");
                runOnUiThread(() -> Toast.makeText(FoodSearchActivity.this, "Base de dados carregada!", Toast.LENGTH_SHORT).show());
            } else {
                Log.d(TAG, "Banco de dados já populado.");
            }
        });
    }


    private void showAddFoodDialog(FoodEntity food) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        TextView tvFoodName = dialogView.findViewById(R.id.tv_dialog_food_name);
        TextView tvCaloriesInfo = dialogView.findViewById(R.id.tv_dialog_food_calories);
        TextView tvQuantity = dialogView.findViewById(R.id.tv_quantity);
        TextView tvServingUnit = dialogView.findViewById(R.id.tv_dialog_serving_unit);
        ImageButton btnDecrease = dialogView.findViewById(R.id.btn_decrease_quantity);
        ImageButton btnIncrease = dialogView.findViewById(R.id.btn_increase_quantity);
        Button btnAdd = dialogView.findViewById(R.id.btn_dialog_add);

        float caloriesPerServingUnit = (food.calories / 100f) * food.servingWeightInGrams;
        String caloriesText = String.format("%.0f kcal por %s", caloriesPerServingUnit, food.servingUnit);

        tvFoodName.setText(food.name);
        tvCaloriesInfo.setText(caloriesText);
        tvServingUnit.setText(food.servingUnit + "(s)");
        tvQuantity.setText("1");

        currentDialog = builder.create();

        btnDecrease.setOnClickListener(v -> {
            try {
                float currentValue = Float.parseFloat(tvQuantity.getText().toString());
                if (currentValue > 0.5) {
                    currentValue -= 0.5;
                    tvQuantity.setText(currentValue % 1 == 0 ? String.valueOf((int) currentValue) : String.valueOf(currentValue));
                }
            } catch (NumberFormatException e) {
                tvQuantity.setText("1");
            }
        });

        btnIncrease.setOnClickListener(v -> {
            try {
                float currentValue = Float.parseFloat(tvQuantity.getText().toString());
                currentValue += 0.5;
                tvQuantity.setText(currentValue % 1 == 0 ? String.valueOf((int) currentValue) : String.valueOf(currentValue));
            } catch (NumberFormatException e) {
                tvQuantity.setText("1");
            }
        });

        // A correção do erro de digitação está aqui (btnAdd)
        btnAdd.setOnClickListener(v -> {
            try {
                float quantity = Float.parseFloat(tvQuantity.getText().toString());
                float finalCalories = caloriesPerServingUnit * quantity;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("FOOD_NAME", food.name);
                resultIntent.putExtra("FOOD_CALORIES", (int) finalCalories);
                resultIntent.putExtra("MEAL_TYPE", mealType);

                // Adicione aqui os outros nutrientes que você quer passar de volta
                // Ex: resultIntent.putExtra("FOOD_PROTEIN", (food.protein_g / 100f) * food.servingWeightInGrams * quantity);

                setResult(RESULT_OK, resultIntent);
                currentDialog.dismiss();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Erro inesperado na quantidade.", Toast.LENGTH_SHORT).show();
            }
        });

        currentDialog.show();
    }
    private void loadFoodsFromCSV() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("alimentos.csv")))) {
            List<FoodEntity> foods = new ArrayList<>();
            String line;
            reader.readLine(); // Pula o cabeçalho

            while ((line = reader.readLine()) != null) {
                // Usamos um limite negativo para garantir que colunas vazias no final sejam contadas
                String[] tokens = line.split(";", -1);

                try {
                    float protein = parseNutrientValue(tokens, 6);
                    float fat = parseNutrientValue(tokens, 7);
                    float carbohydrate = parseNutrientValue(tokens, 9);
                    float fiber = parseNutrientValue(tokens, 10);
                    float cholesterol = parseNutrientValue(tokens, 8);
                    float sodium = parseNutrientValue(tokens, 17);
                    // O CSV de alimentos não tem uma coluna específica para "açúcares", então usaremos 0
                    float sugars = 0f;

                    String servingUnit = (tokens.length > 29 && !tokens[29].trim().isEmpty()) ? tokens[29].trim() : "100g";
                    float servingWeight = (tokens.length > 30 && !tokens[30].trim().isEmpty()) ? parseNutrientValue(tokens, 30) : 100.0f;

                    foods.add(new FoodEntity(
                            Integer.parseInt(tokens[0].trim()), // ID
                            tokens[2].trim(), // Nome
                            (int) parseNutrientValue(tokens, 4), // Calorias
                            protein, fat, carbohydrate, fiber, cholesterol, sodium, sugars,
                            servingUnit, servingWeight
                    ));

                } catch (Exception e) {
                    Log.e(TAG, "Erro de parsing ou índice na linha (alimentos.csv): " + line, e);
                }
            }
            db.foodDao().insertAllFoods(foods);
            Log.d(TAG, "Carregados " + foods.size() + " itens de alimentos.csv com todos os nutrientes.");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler alimentos.csv", e);
        }
    }

    private float parseNutrientValue(String[] tokens, int index) {
        if (index >= tokens.length || tokens[index] == null) return 0;
        String value = tokens[index].trim();
        if (value.isEmpty() || value.equalsIgnoreCase("NA") || value.equalsIgnoreCase("*") || value.equalsIgnoreCase("Tr")) {
            return 0;
        }
        try {
            return Float.parseFloat(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}