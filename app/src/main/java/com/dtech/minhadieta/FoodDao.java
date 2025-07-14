package com.dtech.minhadieta;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {

    // --- MÉTODOS PARA O BANCO DE DADOS INICIAL ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFoods(List<FoodEntity> foods);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAminoAcid(AminoAcidEntity aminoAcid);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFattyAcid(FattyAcidEntity fattyAcid);

    @Query("SELECT COUNT(*) FROM food_table")
    int countFoods();

    // --- MÉTODOS DE BUSCA E MANIPULAÇÃO ---

    /**
     * Busca por alimentos cujo nome contenha o texto da query.
     * A coluna foi corrigida de "food_name" para "name".
     * A query agora espera que os '%' sejam adicionados antes da chamada.
     */
    @Query("SELECT * FROM food_table WHERE name LIKE :query")
    List<FoodEntity> searchByName(String query);

    @Query("SELECT * FROM water_intake_table WHERE date = :date LIMIT 1")
    WaterIntakeEntity getWaterForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertWaterIntake(WaterIntakeEntity waterIntake);
    @Insert
    void insertMealEntry(MealEntryEntity mealEntry);

    @Query("SELECT * FROM meal_entry_table WHERE date = :date")
    List<MealEntryEntity> getMealsForDate(String date);

    @Query("DELETE FROM meal_entry_table WHERE id = :entryId")
    void deleteMealEntryById(int entryId);
}