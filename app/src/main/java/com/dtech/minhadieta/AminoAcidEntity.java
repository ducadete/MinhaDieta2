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
    public int foodOwnerId;

    // Campos para todos os aminoácidos do CSV
    public float triptofano;
    public float treonina;
    public float isoleucina;
    public float leucina;
    public float lisina;
    public float metionina;
    public float cistina;
    public float fenilalanina;
    public float tirosina;
    public float valina;
    public float arginina;
    public float histidina;
    public float alanina;
    @ColumnInfo(name = "acido_aspartico")
    public float acidoAspartico;
    @ColumnInfo(name = "acido_glutamico")
    public float acidoGlutamico;
    public float glicina;
    public float prolina;
    public float serina;

    // Construtor que aceita todos os valores
    public AminoAcidEntity(int foodOwnerId, float triptofano, float treonina, float isoleucina, float leucina, float lisina, float metionina, float cistina, float fenilalanina, float tirosina, float valina, float arginina, float histidina, float alanina, float acidoAspartico, float acidoGlutamico, float glicina, float prolina, float serina) {
        this.foodOwnerId = foodOwnerId;
        this.triptofano = triptofano;
        this.treonina = treonina;
        this.isoleucina = isoleucina;
        this.leucina = leucina;
        this.lisina = lisina;
        this.metionina = metionina;
        this.cistina = cistina;
        this.fenilalanina = fenilalanina;
        this.tirosina = tirosina;
        this.valina = valina;
        this.arginina = arginina;
        this.histidina = histidina;
        this.alanina = alanina;
        this.acidoAspartico = acidoAspartico;
        this.acidoGlutamico = acidoGlutamico;
        this.glicina = glicina;
        this.prolina = prolina;
        this.serina = serina;
    }
}