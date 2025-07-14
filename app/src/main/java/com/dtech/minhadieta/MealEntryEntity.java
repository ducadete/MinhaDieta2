package com.dtech.minhadieta;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_entry_table")
public class MealEntryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String foodName;
    public int calories;
    public String mealType; // ex: "breakfast", "lunch"
    public String date;     // ex: "2025-07-15"

    public MealEntryEntity(String foodName, int calories, String mealType, String date) {
        this.foodName = foodName;
        this.calories = calories;
        this.mealType = mealType;
        this.date = date;
    }
}