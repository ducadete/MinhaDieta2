package com.dtech.minhadieta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Verifica se o usuário já fez a configuração inicial
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            boolean isSetupComplete = prefs.getBoolean("isSetupComplete", false);

            if (isSetupComplete) {
                // Se sim, vai para a tela principal
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Se não, vai para a tela de configuração
                startActivity(new Intent(SplashActivity.this, InitialSetupActivity.class));
            }
            finish(); // Fecha a SplashActivity para que o usuário não possa voltar para ela
        }, 2000); // Atraso de 2 segundos
    }
}