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
    public int foodOwnerId; // Chave estrangeira que liga ao FoodEntity

    // Apenas alguns exemplos de colunas
    @ColumnInfo(name = "saturados_g")
    public String saturados;

    @ColumnInfo(name = "monoinsaturados_g")
    public String monoinsaturados;

    @ColumnInfo(name = "poliinsaturados_g")
    public String poliinsaturados;

    public FattyAcidEntity(int foodOwnerId, String saturados, String monoinsaturados, String poliinsaturados) {
        this.foodOwnerId = foodOwnerId;
        this.saturados = saturados;
        this.monoinsaturados = monoinsaturados;
        this.poliinsaturados = poliinsaturados;
    }
}