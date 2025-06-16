package com.example.wallpaper.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallpaper.ui.common.ThemePreferenceManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MoreViewModel extends ViewModel {
    
    private final ThemePreferenceManager themePreferenceManager;
    
    private final MutableLiveData<Integer> _themeMode = new MutableLiveData<>();
    public final LiveData<Integer> themeMode = _themeMode;
    
    @Inject
    public MoreViewModel(ThemePreferenceManager themePreferenceManager) {
        this.themePreferenceManager = themePreferenceManager;
        _themeMode.setValue(themePreferenceManager.getThemeMode());
    }
    
    public void setThemeMode(int themeMode) {
        themePreferenceManager.setThemeMode(themeMode);
        _themeMode.setValue(themeMode);
    }
    
    public int getCurrentThemeMode() {
        return themePreferenceManager.getThemeMode();
    }
    
    public String getThemeModeDisplayName(int themeMode) {
        return themePreferenceManager.getThemeModeDisplayName(themeMode);
    }
}
