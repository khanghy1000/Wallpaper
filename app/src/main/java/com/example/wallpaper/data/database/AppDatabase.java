package com.example.wallpaper.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.wallpaper.data.database.dao.FavoriteDao;
import com.example.wallpaper.data.database.entity.FavoriteEntity;

@Database(
    entities = {FavoriteEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    
    public abstract FavoriteDao favoriteDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "wallpaper_database"
                    )
                    .fallbackToDestructiveMigration() // For development - removes all data on schema change
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
