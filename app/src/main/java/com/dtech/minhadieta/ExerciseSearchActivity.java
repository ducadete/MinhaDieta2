// Crie este novo arquivo: ExerciseSearchActivity.java
package com.dtech.minhadieta;

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

public class ExerciseSearchActivity extends AppCompatActivity {

    private static final String TAG = "ExerciseSearchActivity";
    private ExerciseAdapter adapter;
    private AppDatabase db;
    private ExecutorService executorService;
    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_search);

        db = AppDatabase.getDatabase(this);
        executorService = AppDatabase.databaseWriteExecutor;

        setupRecyclerView();
        checkAndPrepopulateDatabase();

        EditText etSearch = findViewById(R.id.etSearchExercise);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchDatabase(s.toString());
                } else if (adapter != null) {
                    adapter.updateData(new ArrayList<>());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(new ArrayList<>(), this::showAddExerciseDialog);
        recyclerView.setAdapter(adapter);
    }

    private void searchDatabase(String query) {
        String searchQuery = "%" + query + "%";
        executorService.execute(() -> {
            List<ExerciseEntity> results = db.foodDao().searchExercisesByName(searchQuery);
            runOnUiThread(() -> adapter.updateData(results));
        });
    }

    private void showAddExerciseDialog(ExerciseEntity exercise) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_exercise, null);
        builder.setView(dialogView);

        TextView tvExerciseName = dialogView.findViewById(R.id.tv_dialog_exercise_name);
        EditText etDuration = dialogView.findViewById(R.id.et_duration_minutes);
        Button btnAdd = dialogView.findViewById(R.id.btn_dialog_add_exercise);

        tvExerciseName.setText(exercise.activity);
        currentDialog = builder.create();

        btnAdd.setOnClickListener(v -> {
            String durationStr = etDuration.getText().toString();
            if (durationStr.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o tempo do exercício.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int durationInMinutes = Integer.parseInt(durationStr);
                float totalCaloriesBurned = exercise.caloriesPerMinute * durationInMinutes;

                // Aqui retornaremos o resultado para a MainActivity no futuro
                // Por enquanto, vamos apenas mostrar um Toast
                Toast.makeText(this, String.format("%s por %d min. Queima: %.0f kcal", exercise.activity, durationInMinutes, totalCaloriesBurned), Toast.LENGTH_LONG).show();

                currentDialog.dismiss();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tempo inválido.", Toast.LENGTH_SHORT).show();
            }
        });

        currentDialog.show();
    }

    private void checkAndPrepopulateDatabase() {
        executorService.execute(() -> {
            if (db.foodDao().countExercises() == 0) {
                Log.d(TAG, "Banco de exercícios vazio. Carregando do CSV...");
                loadExercisesFromCSV();
            }
        });
    }

    private void loadExercisesFromCSV() {
        // Peso médio em kg para o cálculo de calorias
        final float averageWeightKg = 70.0f;
        List<ExerciseEntity> exercises = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("exercise_dataset.csv")))) {
            String line;
            reader.readLine(); // Pula o cabeçalho

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2) {
                    try {
                        String activity = tokens[0].trim();
                        float caloriesPerKgPerHour = Float.parseFloat(tokens[1].trim());

                        // Calcula calorias por minuto para um peso médio de 70kg
                        float caloriesPerMinute = (caloriesPerKgPerHour * averageWeightKg) / 60.0f;

                        exercises.add(new ExerciseEntity(activity, caloriesPerMinute));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Erro de formato na linha do CSV de exercícios: " + line, e);
                    }
                }
            }
            db.foodDao().insertAllExercises(exercises);
            Log.d(TAG, "Carregados " + exercises.size() + " exercícios no banco de dados.");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler exercise_dataset.csv", e);
        }
    }
}
