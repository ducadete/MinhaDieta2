package com.dtech.minhadieta;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_intake_table")
public class WaterIntakeEntity {

    @PrimaryKey
    @NonNull
    public String date; // A data no formato "yyyy-MM-dd" será a chave primária

    public int bottleCount; // Número de garrafas de 500ml

    public WaterIntakeEntity(@NonNull String date, int bottleCount) {
        this.date = date;
        this.bottleCount = bottleCount;
    }
}