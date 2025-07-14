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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) searchDatabase(s.toString());
                else if (adapter != null) adapter.updateData(new ArrayList<>());
            }
            @Override
            public void afterTextChanged(Editable s) {}
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

        // --- Referências aos componentes ---
        TextView tvFoodName = dialogView.findViewById(R.id.tv_dialog_food_name);
        TextView tvCaloriesInfo = dialogView.findViewById(R.id.tv_dialog_food_calories);
        TextView tvQuantity = dialogView.findViewById(R.id.tv_quantity);
        TextView tvServingUnit = dialogView.findViewById(R.id.tv_dialog_serving_unit);
        ImageButton btnDecrease = dialogView.findViewById(R.id.btn_decrease_quantity);
        ImageButton btnIncrease = dialogView.findViewById(R.id.btn_increase_quantity);
        Button btnAdd = dialogView.findViewById(R.id.btn_dialog_add);

        // --- LÓGICA DE CÁLCULO E EXIBIÇÃO (A PARTE MAIS IMPORTANTE) ---

        // 1. Pega os valores do banco de dados:
        //    - food.calories = Calorias por 100g (ex: 253 kcal)
        //    - food.servingWeightInGrams = Peso da porção (ex: 25g para 1 fatia)
        //    - food.servingUnit = Descrição da porção (ex: "1 fatia")

        // 2. Calcula as calorias para a porção real (a "regra de três"):
        //    (Calorias / 100g) * Peso da Porção
        float caloriesPerServingUnit = (food.calories / 100f) * food.servingWeightInGrams;

        // 3. Monta o texto de forma clara para o usuário:
        //    Ex: "63 kcal por 1 fatia"
        String caloriesText = String.format("%.0f kcal por %s", caloriesPerServingUnit, food.servingUnit);

        // 4. Preenche os componentes da tela com as informações corretas:
        tvFoodName.setText(food.name);
        tvCaloriesInfo.setText(caloriesText); // Mostra a caloria JÁ CALCULADA para a porção
        tvServingUnit.setText(food.servingUnit + "(s)"); // Ex: "fatia(s)", "unidade(s)"
        tvQuantity.setText("1");

        // -----------------------------------------------------------------

        currentDialog = builder.create();

        // --- Lógica dos botões (permanece a mesma) ---
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

        btnAdd.setOnClickListener(v -> {
            try {
                float quantity = Float.parseFloat(tvQuantity.getText().toString());
                // O cálculo final usa o mesmo valor por porção que já mostramos na tela
                float finalCalories = caloriesPerServingUnit * quantity;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("FOOD_NAME", food.name);
                resultIntent.putExtra("FOOD_CALORIES", (int) finalCalories);
                resultIntent.putExtra("MEAL_TYPE", mealType);
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
            reader.readLine(); // Pula cabeçalho
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);
                if (tokens.length > 4) { // Garante que temos pelo menos as colunas básicas
                    try {
                        String calorieString = tokens[4].trim();
                        if (calorieString.equalsIgnoreCase("NA") || calorieString.equals("*") || calorieString.trim().isEmpty()) {
                            continue;
                        }
                        int id = Integer.parseInt(tokens[0].trim());
                        String name = tokens[2].trim();
                        int calories = Integer.parseInt(calorieString);

                        // --- LÓGICA NOVA PARA PORÇÕES ---
                        String servingUnit = "100g"; // Valor padrão
                        float servingWeight = 100.0f; // Valor padrão

                        // Verifica se as colunas de porção existem e não estão vazias
                        if (tokens.length > 29 && !tokens[29].trim().isEmpty()) {
                            servingUnit = tokens[29].trim();
                        }
                        if (tokens.length > 30 && !tokens[30].trim().isEmpty()) {
                            servingWeight = Float.parseFloat(tokens[30].trim().replace(',', '.'));
                        }
                        // ------------------------------------

                        foods.add(new FoodEntity(id, name, calories, servingUnit, servingWeight));

                    } catch (Exception e) { // Usamos Exception para pegar qualquer erro (formato, índice, etc)
                        Log.e(TAG, "Erro de parsing na linha (alimentos.csv): " + line, e);
                    }
                }
            }
            db.foodDao().insertAllFoods(foods);
            Log.d(TAG, "Carregados " + foods.size() + " itens de alimentos.csv");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler alimentos.csv", e);
        }
    }
}