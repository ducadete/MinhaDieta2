package com.dtech.minhadieta;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class FoodSearchActivity extends AppCompatActivity {

    private static final String TAG = "FoodSearchActivity";

    private FoodAdapter adapter;
    private String mealType;
    private AppDatabase db;
    private ExecutorService executorService;

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
                if (s.length() > 2) {
                    searchDatabase(s.toString());
                } else {
                    adapter.updateData(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(new ArrayList<>(), food -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("FOOD_NAME", food.name);
            resultIntent.putExtra("FOOD_CALORIES", food.calories);
            resultIntent.putExtra("MEAL_TYPE", mealType);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        recyclerView.setAdapter(adapter);
    }

    // ===================================================================
    // MÉTODO CORRIGIDO
    // ===================================================================
    private void searchDatabase(String query) {
        executorService.execute(() -> {
            // Agora passamos o texto da busca diretamente, sem os '%'
            List<FoodEntity> results = db.foodDao().searchByName(query);
            Log.d(TAG, "Busca por '" + query + "' encontrou " + results.size() + " resultados.");

            runOnUiThread(() -> {
                adapter.updateData(results);
            });
        });
    }

    private void checkAndPrepopulateDatabase() {
        executorService.execute(() -> {
            if (db.foodDao().countFoods() == 0) {
                Log.d(TAG, "Banco de dados vazio. Iniciando carregamento dos arquivos CSV.");
                runOnUiThread(() -> Toast.makeText(this, "Carregando base de dados...", Toast.LENGTH_LONG).show());

                loadFoodsFromCSV();
                loadFattyAcidsFromCSV();
                loadAminoAcidsFromCSV();

                Log.d(TAG, "Carregamento dos arquivos CSV finalizado.");
                runOnUiThread(() -> Toast.makeText(this, "Base de dados carregada!", Toast.LENGTH_SHORT).show());
            } else {
                Log.d(TAG, "Banco de dados já populado. Nenhum carregamento necessário.");
            }
        });
    }

    // ... (o resto do arquivo permanece o mesmo) ...
    private void loadFoodsFromCSV() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("alimentos.csv")))) {
            List<FoodEntity> foods = new ArrayList<>();
            String line;
            reader.readLine(); // Pula o cabeçalho
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);
                try {
                    if (tokens.length > 4) {
                        int id = Integer.parseInt(tokens[0].trim());
                        // Correção: O nome do alimento está no índice 2
                        String name = tokens[2].trim();
                        int calories = tokens[4].trim().equalsIgnoreCase("NA") ? 0 : Integer.parseInt(tokens[4].trim());
                        foods.add(new FoodEntity(id, name, calories));
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Erro de formatação de número na linha (alimentos.csv): " + line, e);
                }
            }
            db.foodDao().insertAllFoods(foods);
            Log.d(TAG, "Carregados " + foods.size() + " itens de alimentos.csv");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler alimentos.csv", e);
        }
    }

    private void loadFattyAcidsFromCSV() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("acidos-graxos.csv")))) {
            List<FattyAcidEntity> items = new ArrayList<>();
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);
                try {
                    if (tokens.length > 4) {
                        int foodId = Integer.parseInt(tokens[0].trim());
                        String saturados = tokens[2].trim();
                        String mono = tokens[3].trim();
                        String poli = tokens[4].trim();
                        items.add(new FattyAcidEntity(foodId, saturados, mono, poli));
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Erro de formatação de número na linha (acidos-graxos.csv): " + line, e);
                }
            }
            db.foodDao().insertAllFattyAcids(items);
            Log.d(TAG, "Carregados " + items.size() + " itens de acidos-graxos.csv");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler acidos-graxos.csv", e);
        }
    }

    private void loadAminoAcidsFromCSV() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("aminoacidos.csv")))) {
            List<AminoAcidEntity> items = new ArrayList<>();
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);
                try {
                    if (tokens.length > 4) {
                        int foodId = Integer.parseInt(tokens[0].trim());
                        String triptofano = tokens[2].trim();
                        String isoleucina = tokens[4].trim();
                        items.add(new AminoAcidEntity(foodId, triptofano, isoleucina));
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Erro de formatação de número na linha (aminoacidos.csv): " + line, e);
                }
            }
            db.foodDao().insertAllAminoAcids(items);
            Log.d(TAG, "Carregados " + items.size() + " itens de aminoacidos.csv");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler aminoacidos.csv", e);
        }
    }
}