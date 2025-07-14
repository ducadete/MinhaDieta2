package com.dtech.minhadieta;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logged_exercise_table"// A anotação @Entity é o que cria a tabela com o nome "logged_exercise_table"
)
public class LoggedExerciseEntity {

    // @PrimaryKey define a coluna "id"
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String exerciseName;
    public int durationInMinutes;
    public int caloriesBurned;

    // A coluna "date" é definida aqui
    public String date;

    public LoggedExerciseEntity(String exerciseName, int durationInMinutes, int caloriesBurned, String date) {
        this.exerciseName = exerciseName;
        this.durationInMinutes = durationInMinutes;
        this.caloriesBurned = caloriesBurned;
        this.date = date;
    }
}