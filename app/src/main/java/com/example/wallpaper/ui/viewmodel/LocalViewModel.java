package com.example.wallpaper.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wallpaper.data.repository.LocalWallpaperRepository;
import com.example.wallpaper.data.repository.FavoritesRepository;
import com.example.wallpaper.data.database.entity.FavoriteEntity;
import com.example.wallpaper.model.LocalWallpaper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class LocalViewModel extends AndroidViewModel {
    
    private final LocalWallpaperRepository repository;
    private final FavoritesRepository favoritesRepository;
    private final ExecutorService executor;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    private final MutableLiveData<List<LocalWallpaper>> _wallpapers = new MutableLiveData<>();
    public final LiveData<List<LocalWallpaper>> wallpapers = _wallpapers;
    
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public final LiveData<Boolean> loading = _loading;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    
    // Track favorite wallpaper IDs for quick checking
    private final MutableLiveData<List<String>> _favoriteIds = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<String>> favoriteIds = _favoriteIds;
    
    @Inject
    public LocalViewModel(Application application, LocalWallpaperRepository repository, FavoritesRepository favoritesRepository) {
        super(application);
        this.repository = repository;
        this.favoritesRepository = favoritesRepository;
        this.executor = Executors.newSingleThreadExecutor();
        observeFavoriteIds();
    }
    
    public void loadLocalWallpapers() {
        _loading.setValue(true);
        _error.setValue(null);
        
        executor.execute(() -> {
            try {
                List<LocalWallpaper> localWallpapers = repository.getLocalWallpapers(getApplication());
                _wallpapers.postValue(localWallpapers);
                _loading.postValue(false);
            } catch (Exception e) {
                _error.postValue("Failed to load local wallpapers: " + e.getMessage());
                _loading.postValue(false);
            }
        });
    }
    
    public void refreshWallpapers() {
        loadLocalWallpapers();
    }
    
    public void toggleFavorite(LocalWallpaper wallpaper) {
        disposables.add(
            favoritesRepository.toggleFavorite(wallpaper)
                .subscribe(
                    () -> {
                        // Success - favorite toggled
                        // No need to reload favorite IDs manually since we're observing changes
                    },
                    throwable -> {
                        _error.postValue("Error toggling favorite: " + throwable.getMessage());
                    }
                )
        );
    }
    
    public boolean isFavorite(LocalWallpaper wallpaper) {
        List<String> currentFavorites = _favoriteIds.getValue();
        return currentFavorites != null && currentFavorites.contains(wallpaper.getId());
    }
    
    private void observeFavoriteIds() {
        disposables.add(
            favoritesRepository.observeAllFavorites()
                .subscribe(
                    favorites -> {
                        List<String> favoriteIds = new ArrayList<>();
                        for (FavoriteEntity favorite : favorites) {
                            if ("LOCAL".equals(favorite.getSource())) {
                                favoriteIds.add(favorite.getSourceId());
                            }
                        }
                        _favoriteIds.postValue(favoriteIds);
                    },
                    throwable -> {
                        // Handle error silently for favorite loading
                    }
                )
        );
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        executor.shutdown();
    }
}
