package com.example.wallpaper.ui.viewmodel;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wallpaper.data.repository.FavoritesRepository;
import com.example.wallpaper.data.repository.NetworkWallhavenRepository;
import com.example.wallpaper.model.FavoriteWallpaper;
import com.example.wallpaper.model.LocalWallpaper;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaperResponse;
import com.example.wallpaper.data.network.model.NetworkWallhavenThumbs;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class WallpaperViewerViewModel extends AndroidViewModel {
    
    private final FavoritesRepository favoritesRepository;
    private final NetworkWallhavenRepository networkRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    private final MutableLiveData<Boolean> _isFavorite = new MutableLiveData<>(false);
    public final LiveData<Boolean> isFavorite = _isFavorite;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    
    private String wallpaperId;
    private String wallpaperPath;
    
    @Inject
    public WallpaperViewerViewModel(Application application, FavoritesRepository favoritesRepository, NetworkWallhavenRepository networkRepository) {
        super(application);
        this.favoritesRepository = favoritesRepository;
        this.networkRepository = networkRepository;
    }
    
    public void setWallpaperInfo(String wallpaperId, String wallpaperPath) {
        this.wallpaperId = wallpaperId;
        this.wallpaperPath = wallpaperPath;
        checkIfFavorite();
    }
    
    private void checkIfFavorite() {
        if (wallpaperId == null) return;
        
        String source = isRemoteWallpaper() ? "WALLHAVEN" : "LOCAL";
        
        disposables.add(
            favoritesRepository.isFavorite(wallpaperId, source)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    _isFavorite::setValue,
                    throwable -> {
                        _error.setValue("Error checking favorite status");
                    }
                )
        );
    }
    
    public void toggleFavorite() {
        if (wallpaperId == null || wallpaperPath == null) return;
        
        if (isRemoteWallpaper()) {
            // For remote wallpapers, fetch complete data from API first
            networkRepository.getWallpaper(wallpaperId, new NetworkWallhavenRepository.WallpaperCallback() {
                @Override
                public void onSuccess(NetworkWallhavenWallpaperResponse response) {
                    if (response.getData() != null) {
                        // Use the complete wallpaper data
                        performToggleFavorite(response.getData());
                    } else {
                        // Show error if no data received
                        _error.setValue("Failed to fetch wallpaper info: No data received");
                    }
                }
                
                @Override
                public void onError(String error) {
                    // Show error message and don't create fallback
                    _error.setValue("Failed to fetch wallpaper info: " + error);
                }
            });
        } else {
            // For local wallpapers, create a LocalWallpaper instance
            LocalWallpaper wallpaper = new LocalWallpaper();
            wallpaper.setId(wallpaperId);
            wallpaper.setUri(Uri.parse(wallpaperPath));
            performToggleFavorite(wallpaper);
        }
    }
    
    private void performToggleFavorite(FavoriteWallpaper favoriteWallpaper) {
        disposables.add(
            favoritesRepository.toggleFavorite(favoriteWallpaper)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        // Success - favorite status will be updated automatically
                        // through the observer in checkIfFavorite
                        checkIfFavorite();
                    },
                    throwable -> {
                        _error.setValue("Error toggling favorite: " + throwable.getMessage());
                    }
                )
        );
    }
    
    public boolean isRemoteWallpaper() {
        return wallpaperPath != null && (wallpaperPath.startsWith("http://") || wallpaperPath.startsWith("https://"));
    }
    
    public boolean shouldShowSaveButton() {
        return isRemoteWallpaper();
    }
    
    public void saveToDevice() {
        if (!isRemoteWallpaper()) return;
        
        try {
            Context context = getApplication();
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            
            Uri uri = Uri.parse(wallpaperPath);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            
            // Set download title and description
            request.setTitle("Wallpaper Download");
            request.setDescription("Downloading wallpaper...");
            
            // Set destination directory
            String fileName = "wallpaper_" + wallpaperId + ".jpg";
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            
            // Set notification visibility
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            // Allow download over WiFi and mobile data
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            
            // Enqueue the download
            downloadManager.enqueue(request);
            
        } catch (Exception e) {
            _error.setValue("Error saving wallpaper: " + e.getMessage());
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
