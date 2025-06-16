package com.example.wallpaper.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.wallpaper.data.database.dao.FavoriteDao;
import com.example.wallpaper.data.database.entity.FavoriteEntity;

@Database(
    entities = {FavoriteEntity.class},
    version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteDao favoriteDao();
}
