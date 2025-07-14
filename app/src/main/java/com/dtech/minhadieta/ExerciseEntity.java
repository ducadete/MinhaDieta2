package com.dtech.minhadieta;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise_table")
public class ExerciseEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String activity; // Nome do exercício

    // Vamos armazenar uma média de calorias queimadas por minuto para facilitar os cálculos
    public float caloriesPerMinute;

    public ExerciseEntity(@NonNull String activity, float caloriesPerMinute) {
        this.activity = activity;
        this.caloriesPerMinute = caloriesPerMinute;
    }
}