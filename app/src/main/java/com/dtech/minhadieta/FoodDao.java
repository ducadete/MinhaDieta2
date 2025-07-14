package com.dtech.minhadieta;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFoods(List<FoodEntity> foods);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFattyAcids(List<FattyAcidEntity> fattyAcids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllAminoAcids(List<AminoAcidEntity> aminoAcids);

    // CONSULTA CORRIGIDA: Adicionamos os '%' diretamente aqui.
    // Isso é mais robusto e garante que a busca "LIKE" funcione.
    @Query("SELECT * FROM food_table WHERE food_name LIKE '%' || :query || '%'")
    List<FoodEntity> searchByName(String query);

    @Query("SELECT COUNT(*) FROM food_table")
    int countFoods();
}