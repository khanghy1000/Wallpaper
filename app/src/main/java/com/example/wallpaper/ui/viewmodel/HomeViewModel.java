package com.example.wallpaper.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.data.repository.NetworkWallhavenRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    
    private final NetworkWallhavenRepository repository;
    
    private final MutableLiveData<List<NetworkWallhavenWallpaper>> _wallpapers = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<NetworkWallhavenWallpaper>> wallpapers = _wallpapers;
    
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public final LiveData<Boolean> loading = _loading;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    
    @Inject
    public HomeViewModel(NetworkWallhavenRepository repository) {
        this.repository = repository;
        loadWallpapers();
    }
    
    public void loadWallpapers() {
        _loading.setValue(true);
        _error.setValue(null);
        
        // Load wallpapers with default parameters (no query params as requested)
        repository.searchWallpapers(
                null,   // query
                null,   // categories
                null,   // purity
                "random",   // sorting
                null,   // order
                null,   // topRange
                null,   // atleast
                null,   // resolutions
                null,   // colors
                null,   // ratios
                1,      // page
                null,   // seed
                new NetworkWallhavenRepository.WallpapersCallback() {
                    @Override
                    public void onSuccess(NetworkWallhavenWallpapersResponse response) {
                        _loading.setValue(false);
                        if (response.getData() != null) {
                            _wallpapers.setValue(response.getData());
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        _loading.setValue(false);
                        _error.setValue(error);
                    }
                }
        );
    }
    
    public void refreshWallpapers() {
        loadWallpapers();
    }
}
