package com.example.wallpaper.ui.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ThemePreferenceManager {
    
    private static final String PREF_NAME = "theme_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    
    private final SharedPreferences sharedPreferences;
    
    @Inject
    public ThemePreferenceManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public void setThemeMode(int themeMode) {
        sharedPreferences.edit()
                .putInt(KEY_THEME_MODE, themeMode)
                .apply();
        
        applyTheme(themeMode);
    }
    
    public int getThemeMode() {
        try {
            return sharedPreferences.getInt(KEY_THEME_MODE, THEME_SYSTEM);
        } catch (ClassCastException e) {
            String stringValue = sharedPreferences.getString(KEY_THEME_MODE, String.valueOf(THEME_SYSTEM));
            int intValue;
            try {
                intValue = Integer.parseInt(stringValue);
            } catch (NumberFormatException nfe) {
                intValue = THEME_SYSTEM;
            }
            setThemeMode(intValue);
            return intValue;
        }
    }
    
    public void applyTheme(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    public void initializeTheme() {
        applyTheme(getThemeMode());
    }
    
    public String getThemeModeDisplayName(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                return "Light";
            case THEME_DARK:
                return "Dark";
            case THEME_SYSTEM:
            default:
                return "System";
        }
    }
    
    public void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}
