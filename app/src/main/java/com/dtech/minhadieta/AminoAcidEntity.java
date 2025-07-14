package com.dtech.minhadieta;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "amino_acid_table",
        foreignKeys = @ForeignKey(entity = FoodEntity.class,
                parentColumns = "food_id",
                childColumns = "food_owner_id",
                onDelete = ForeignKey.CASCADE))
public class AminoAcidEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "food_owner_id", index = true)
    public int foodOwnerId; // Chave estrangeira

    // Apenas alguns exemplos
    @ColumnInfo(name = "triptofano_g")
    public String triptofano;

    @ColumnInfo(name = "isoleucina_g")
    public String isoleucina;

    public AminoAcidEntity(int foodOwnerId, String triptofano, String isoleucina) {
        this.foodOwnerId = foodOwnerId;
        this.triptofano = triptofano;
        this.isoleucina = isoleucina;
    }
}