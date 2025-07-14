package com.dtech.minhadieta;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {

    // --- MÉTODOS PARA ALIMENTOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFoods(List<FoodEntity> foods);

    @Query("SELECT * FROM food_table WHERE name LIKE :query")
    List<FoodEntity> searchByName(String query);

    @Query("SELECT COUNT(*) FROM food_table")
    int countFoods();

    // --- MÉTODOS PARA REFEIÇÕES DIÁRIAS ---
    @Insert
    void insertMealEntry(MealEntryEntity mealEntry);

    @Query("SELECT * FROM meal_entry_table WHERE date = :date")
    List<MealEntryEntity> getMealsForDate(String date);

    @Query("DELETE FROM meal_entry_table WHERE id = :entryId")
    void deleteMealEntryById(int entryId);

    // --- MÉTODOS PARA ÁGUA DIÁRIA ---
    @Query("SELECT * FROM water_intake_table WHERE date = :date LIMIT 1")
    WaterIntakeEntity getWaterForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertWaterIntake(WaterIntakeEntity waterIntake);

    // --- MÉTODOS PARA A LISTA GERAL DE EXERCÍCIOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllExercises(List<ExerciseEntity> exercises);

    @Query("SELECT * FROM exercise_table WHERE activity LIKE :query")
    List<ExerciseEntity> searchExercisesByName(String query);

    @Query("SELECT COUNT(*) FROM exercise_table")
    int countExercises();

    // --- MÉTODOS PARA EXERCÍCIOS REGISTRADOS PELO USUÁRIO ---
    @Insert
    void insertLoggedExercise(LoggedExerciseEntity entry);

    @Query("DELETE FROM logged_exercise_table WHERE id = :entryId")
    void deleteLoggedExerciseById(int entryId);

    @Query("SELECT * FROM logged_exercise_table WHERE date = :date")
    List<LoggedExerciseEntity> getLoggedExercisesForDate(String date);
}