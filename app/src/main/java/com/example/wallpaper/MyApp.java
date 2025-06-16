package com.example.wallpaper;

import android.app.Application;

import com.example.wallpaper.ui.common.ThemePreferenceManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApp extends Application {
    
    @Inject
    ThemePreferenceManager themePreferenceManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        if (themePreferenceManager != null) {
            themePreferenceManager.initializeTheme();
        }
    }
}
