package com.dtech.minhadieta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InitialSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setup);

        // Conecta as variáveis Java com os componentes do XML
        EditText etWeight = findViewById(R.id.etWeight);
        EditText etHeight = findViewById(R.id.etHeight);
        TextView tvImcResult = findViewById(R.id.tvImcResult);
        TextView tvCalorieGoal = findViewById(R.id.tvCalorieGoal);
        Button btnCalculate = findViewById(R.id.btnCalculate);

        btnCalculate.setOnClickListener(v -> {
            try {
                float weight = Float.parseFloat(etWeight.getText().toString());
                float height = Float.parseFloat(etHeight.getText().toString()) / 100.0f;

                float imc = weight / (height * height);
                String imcCategory = getImcCategory(imc);
                tvImcResult.setText(String.format("Seu IMC: %.2f (%s)", imc, imcCategory));
                tvImcResult.setVisibility(View.VISIBLE);

                float calorieGoal = calculateCalorieGoal(weight);
                tvCalorieGoal.setText(String.format("Meta de Calorias: %.0f kcal/dia", calorieGoal));
                tvCalorieGoal.setVisibility(View.VISIBLE);

                // Salva os dados e marca a configuração como completa
                SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat("calorieGoal", calorieGoal);
                editor.putBoolean("isSetupComplete", true);
                editor.apply();

                Toast.makeText(this, "Perfil salvo! Bem-vindo(a)!", Toast.LENGTH_LONG).show();

                // Navega para a MainActivity após um pequeno atraso
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }, 1500);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getImcCategory(float imc) {
        if (imc < 18.5) return "Abaixo do peso";
        if (imc < 24.9) return "Peso normal";
        if (imc < 29.9) return "Sobrepeso";
        return "Obesidade";
    }

    private float calculateCalorieGoal(float currentWeight) {
        // Fórmula de Harris-Benedict simplificada
        float tmb = (10 * currentWeight) + (6.25f * 170) - (5 * 30) + 5; // Exemplo
        return (tmb * 1.4f) - 500;
    }
}