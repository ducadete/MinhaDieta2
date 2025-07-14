package com.dtech.minhadieta;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.room.Database;

@Database(entities = {FoodEntity.class, AminoAcidEntity.class, FattyAcidEntity.class, MealEntryEntity.class, WaterIntakeEntity.class},
        version = 4)public abstract class AppDatabase extends RoomDatabase {

    public abstract FoodDao foodDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "minha_dieta_db")
                            // 3. Adicione esta linha
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}