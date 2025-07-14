package com.dtech.minhadieta;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "fatty_acid_table",
        foreignKeys = @ForeignKey(entity = FoodEntity.class,
                parentColumns = "food_id",
                childColumns = "food_owner_id",
                onDelete = ForeignKey.CASCADE))
public class FattyAcidEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "food_owner_id", index = true)
    public int foodOwnerId;

    public float saturados;
    public float monoinsaturados;
    public float poliinsaturados;
    public float colesterol;

    public FattyAcidEntity(int foodOwnerId, float saturados, float monoinsaturados, float poliinsaturados, float colesterol) {
        this.foodOwnerId = foodOwnerId;
        this.saturados = saturados;
        this.monoinsaturados = monoinsaturados;
        this.poliinsaturados = poliinsaturados;
        this.colesterol = colesterol;
    }
}