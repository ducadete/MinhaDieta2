package com.dtech.minhadieta;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_table")
public class FoodEntity {

    @PrimaryKey
    @ColumnInfo(name = "food_id")
    public int id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "calories_per_100g")
    public int calories; // Representa calorias por 100g

    // --- NOVOS CAMPOS PARA PORÇÕES ---
    @ColumnInfo(name = "serving_unit", defaultValue = "100g")
    public String servingUnit; // Ex: "fatia", "unidade", "xícara", "100g"

    @ColumnInfo(name = "serving_weight_grams", defaultValue = "100")
    public float servingWeightInGrams; // O peso em gramas da "serving_unit"

    // Construtor atualizado
    public FoodEntity(int id, @NonNull String name, int calories, String servingUnit, float servingWeightInGrams) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.servingUnit = servingUnit;
        this.servingWeightInGrams = servingWeightInGrams;
    }
}