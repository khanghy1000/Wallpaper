package com.example.wallpaper.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.data.repository.NetworkWallhavenRepository;
import com.example.wallpaper.model.Order;
import com.example.wallpaper.model.search.WallhavenFilters;
import com.example.wallpaper.model.search.WallhavenSearch;
import com.example.wallpaper.model.search.WallhavenSorting;

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
    
    private final MutableLiveData<Boolean> _loadingMore = new MutableLiveData<>(false);
    public final LiveData<Boolean> loadingMore = _loadingMore;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    
    private int currentPage = 1;
    private boolean hasMorePages = true;
    private boolean isLoadingMore = false;
    
    @Inject
    public HomeViewModel(NetworkWallhavenRepository repository) {
        this.repository = repository;
        loadWallpapers();
    }
    
    public void loadWallpapers() {
        if (isLoadingMore) return;
        
        _loading.setValue(true);
        _error.setValue(null);
        
        // Reset pagination for fresh load
        currentPage = 1;
        hasMorePages = true;
        
        // Create WallhavenSearch with default parameters and random sorting
        WallhavenFilters filters = new WallhavenFilters();
        filters.setSorting(WallhavenSorting.DATE_ADDED);
        filters.setOrder(Order.DESC);
        
        WallhavenSearch search = new WallhavenSearch("", filters, null);
        
        repository.searchWallpapers(
                search,
                currentPage,
                new NetworkWallhavenRepository.WallpapersCallback() {
                    @Override
                    public void onSuccess(NetworkWallhavenWallpapersResponse response) {
                        _loading.setValue(false);
                        if (response.getData() != null) {
                            _wallpapers.setValue(response.getData());
                            // Check if there are more pages
                            if (response.getMeta() != null) {
                                hasMorePages = response.getMeta().getCurrentPage() < response.getMeta().getLastPage();
                            }
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
    
    public void loadMoreWallpapers() {
        if (isLoadingMore || !hasMorePages) return;
        
        isLoadingMore = true;
        _loadingMore.setValue(true);
        _error.setValue(null);
        
        // Create WallhavenSearch with default parameters and random sorting
        WallhavenFilters filters = new WallhavenFilters();
        filters.setSorting(WallhavenSorting.DATE_ADDED);
        filters.setOrder(Order.DESC);
        
        WallhavenSearch search = new WallhavenSearch("", filters, null);
        
        repository.searchWallpapers(
                search,
                currentPage + 1,
                new NetworkWallhavenRepository.WallpapersCallback() {
                    @Override
                    public void onSuccess(NetworkWallhavenWallpapersResponse response) {
                        isLoadingMore = false;
                        _loadingMore.setValue(false);
                        if (response.getData() != null && !response.getData().isEmpty()) {
                            currentPage++;
                            List<NetworkWallhavenWallpaper> currentWallpapers = _wallpapers.getValue();
                            if (currentWallpapers == null) {
                                currentWallpapers = new ArrayList<>();
                            }
                            List<NetworkWallhavenWallpaper> updatedList = new ArrayList<>(currentWallpapers);
                            updatedList.addAll(response.getData());
                            _wallpapers.setValue(updatedList);
                            
                            // Check if there are more pages
                            if (response.getMeta() != null) {
                                hasMorePages = response.getMeta().getCurrentPage() < response.getMeta().getLastPage();
                            }
                        } else {
                            hasMorePages = false;
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        isLoadingMore = false;
                        _loadingMore.setValue(false);
                        _error.setValue(error);
                    }
                }
        );
    }
    
    public void refreshWallpapers() {
        loadWallpapers();
    }
}
