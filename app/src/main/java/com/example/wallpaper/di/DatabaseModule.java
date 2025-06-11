package com.example.wallpaper.di;

import android.content.Context;

import androidx.room.Room;

import com.example.wallpaper.data.database.AppDatabase;
import com.example.wallpaper.data.database.dao.FavoriteDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "wallpaper_database"
        ).build();
    }

    @Provides
    public FavoriteDao provideFavoriteDao(AppDatabase database) {
        return database.favoriteDao();
    }
}
