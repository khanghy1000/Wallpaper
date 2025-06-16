package com.example.wallpaper.di;

import android.content.Context;

import com.example.wallpaper.ui.common.ThemePreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ApplicationModule {
    
    @Provides
    @Singleton
    public ThemePreferenceManager provideThemePreferenceManager(@ApplicationContext Context context) {
        return new ThemePreferenceManager(context);
    }
}
