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

    // --- CAMPOS PARA TODOS OS NUTRIENTES ---
    public int calories;
    public float protein_g;
    public float fat_g;
    public float carbohydrate_g;
    public float fiber_g;
    public float cholesterol_mg;
    public float sodium_mg;
    public float sugars_g;

    @ColumnInfo(defaultValue = "100g")
    public String servingUnit;

    @ColumnInfo(defaultValue = "100")
    public float servingWeightInGrams;

    // Construtor completo com 12 argumentos
    public FoodEntity(int id, @NonNull String name, int calories, float protein_g, float fat_g, float carbohydrate_g, float fiber_g, float cholesterol_mg, float sodium_mg, float sugars_g, String servingUnit, float servingWeightInGrams) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.protein_g = protein_g;
        this.fat_g = fat_g;
        this.carbohydrate_g = carbohydrate_g;
        this.fiber_g = fiber_g;
        this.cholesterol_mg = cholesterol_mg;
        this.sodium_mg = sodium_mg;
        this.sugars_g = sugars_g;
        this.servingUnit = servingUnit;
        this.servingWeightInGrams = servingWeightInGrams;
    }
}