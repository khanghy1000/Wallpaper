package com.example.wallpaper.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallpaper.data.repository.FavoritesRepository;
import com.example.wallpaper.data.database.entity.FavoriteEntity;
import com.example.wallpaper.model.FavoriteWallpaper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class CollectionsViewModel extends ViewModel {

    private final FavoritesRepository favoritesRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<FavoriteWallpaper>> _favoriteWallpapers = new MutableLiveData<>();
    public final LiveData<List<FavoriteWallpaper>> favoriteWallpapers = _favoriteWallpapers;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public CollectionsViewModel(FavoritesRepository favoritesRepository) {
        this.favoritesRepository = favoritesRepository;
        observeFavoriteWallpapers();
    }

    private void observeFavoriteWallpapers() {
        _isLoading.setValue(true);
        _error.setValue(null);

        disposables.add(
            favoritesRepository.observeAllFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    favorites -> {
                        // Convert FavoriteEntity list to FavoriteWallpaper list
                        List<FavoriteWallpaper> favoriteWallpapers = new ArrayList<>();
                        for (FavoriteEntity favorite : favorites) {
                            favoriteWallpapers.add(new FavoriteWallpaperImpl(favorite));
                        }
                        _favoriteWallpapers.setValue(favoriteWallpapers);
                        _isLoading.setValue(false);
                    },
                    throwable -> {
                        _error.setValue("Failed to load favorite wallpapers: " + throwable.getMessage());
                        _isLoading.setValue(false);
                    }
                )
        );
    }

    public void toggleFavorite(FavoriteWallpaper wallpaper) {
        disposables.add(
            favoritesRepository.toggleFavorite(wallpaper)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                    },
                    throwable -> {
                        _error.setValue("Failed to toggle favorite: " + throwable.getMessage());
                    }
                )
        );
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
    
    private static class FavoriteWallpaperImpl implements FavoriteWallpaper {
        private final FavoriteEntity entity;
        
        public FavoriteWallpaperImpl(FavoriteEntity entity) {
            this.entity = entity;
        }
        
        @Override
        public String getSourceId() {
            return entity.getSourceId();
        }
        
        @Override
        public String getSource() {
            return entity.getSource();
        }
        
        @Override
        public String getThumbUrl() {
            return entity.getThumbUrl();
        }

        @Override
        public String getMainImgUrl() {
            return entity.getMainImgUrl();
        }
        
        @Override
        public float getRatio() {
            return entity.getRatio();
        }
    }
}
