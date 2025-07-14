package com.dtech.minhadieta;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        FoodEntity.class, AminoAcidEntity.class, FattyAcidEntity.class,
        MealEntryEntity.class, WaterIntakeEntity.class, ExerciseEntity.class,
        LoggedExerciseEntity.class
}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FoodDao foodDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "minha_dieta_db")
                            .fallbackToDestructiveMigration() // Mantém a recriação automática
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}