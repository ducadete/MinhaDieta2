package com.dtech.minhadieta;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_table")
public class FoodEntity {

    @PrimaryKey
    @ColumnInfo(name = "food_id")
    public int id; // Corresponde ao 'numero_do_alimento'

    @NonNull
    @ColumnInfo(name = "food_name")
    public String name; // Corresponde ao 'descricao_do_alimento'

    @ColumnInfo(name = "calories_kcal")
    public int calories; // Corresponde a 'energia_kcal'

    public FoodEntity(int id, @NonNull String name, int calories) {
        this.id = id;
        this.name = name;
        this.calories = calories;
    }
}