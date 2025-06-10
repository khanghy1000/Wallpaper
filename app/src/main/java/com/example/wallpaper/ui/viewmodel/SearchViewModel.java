package com.example.wallpaper.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Size;

import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.data.repository.NetworkWallhavenRepository;
import com.example.wallpaper.model.search.WallhavenFilters;
import com.example.wallpaper.model.search.WallhavenSearch;
import com.example.wallpaper.model.Order;
import com.example.wallpaper.model.Purity;
import com.example.wallpaper.model.search.WallhavenCategory;
import com.example.wallpaper.model.search.WallhavenRatio;
import com.example.wallpaper.model.search.WallhavenSorting;
import com.example.wallpaper.model.search.WallhavenTopRange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final NetworkWallhavenRepository repository;

    private final MutableLiveData<List<NetworkWallhavenWallpaper>> _wallpapers = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<NetworkWallhavenWallpaper>> wallpapers = _wallpapers;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public final LiveData<Boolean> loading = _loading;
    
    private final MutableLiveData<Boolean> _loadingMore = new MutableLiveData<>(false);
    public final LiveData<Boolean> loadingMore = _loadingMore;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _showResults = new MutableLiveData<>(false);
    public final LiveData<Boolean> showResults = _showResults;

    private WallhavenSearch currentSearch = createDefaultSearch();
    private int currentPage = 1;
    private boolean hasMorePages = true;
    private boolean isLoadingMore = false;

    @Inject
    public SearchViewModel(NetworkWallhavenRepository repository) {
        this.repository = repository;
    }
    
    public void searchWallpapers(String keyword, String tags, List<WallhavenCategory> categories,
                                List<Purity> purities, WallhavenSorting sorting, Order order,
                                String minWidth, String minHeight, List<String> resolutions,
                                List<WallhavenRatio> ratios, WallhavenTopRange topRange) {
        
        // Build search filters
        WallhavenFilters filters = new WallhavenFilters();
        
        // Handle tags
        if (tags != null && !tags.trim().isEmpty()) {
            HashSet<String> includedTags = new HashSet<>();
            String[] tagArray = tags.trim().split(","); // Split by comma only
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    includedTags.add(trimmedTag);
                }
            }
            if (!includedTags.isEmpty()) {
                filters.setIncludedTags(includedTags);
            }
        }
        
        if (categories != null && !categories.isEmpty()) {
            filters.setCategories(new HashSet<>(categories));
        }
        
        if (purities != null && !purities.isEmpty()) {
            filters.setPurity(new HashSet<>(purities));
        }
        
        if (sorting != null) {
            filters.setSorting(sorting);
        }
        
        if (order != null) {
            filters.setOrder(order);
        }
        
        // Set top range if sorting is TOPLIST and a valid top range was provided
        if (sorting == WallhavenSorting.TOPLIST && topRange != null) {
            filters.setTopRange(topRange);
        }
        
        // Handle minimum resolution
        if (minWidth != null && !minWidth.trim().isEmpty() && 
            minHeight != null && !minHeight.trim().isEmpty()) {
            try {
                int width = Integer.parseInt(minWidth.trim());
                int height = Integer.parseInt(minHeight.trim());
                filters.setAtleast(new Size(width, height));
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        }
        
        if (resolutions != null && !resolutions.isEmpty()) {
            HashSet<Size> sizeSet = new HashSet<>();
            for (String resolution : resolutions) {
                String[] parts = resolution.split("x");
                if (parts.length == 2) {
                    try {
                        int width = Integer.parseInt(parts[0]);
                        int height = Integer.parseInt(parts[1]);
                        sizeSet.add(new Size(width, height));
                    } catch (NumberFormatException e) {
                        // Ignore invalid resolution
                    }
                }
            }
            filters.setResolutions(sizeSet);
        }
        
        if (ratios != null && !ratios.isEmpty()) {
            filters.setRatios(new HashSet<>(ratios));
        }
        
        // Build search object
        WallhavenSearch search = new WallhavenSearch();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            search.setQuery(keyword.trim());
        }
        
        search.setFilters(filters);
        
        currentSearch = search;
        
        // Reset pagination for new search
        currentPage = 1;
        hasMorePages = true;
        isLoadingMore = false;
        
        performSearch();
    }

    private void performSearch() {
        if (isLoadingMore) return;
        
        _loading.setValue(true);
        _error.setValue(null);
        
        repository.searchWallpapers(currentSearch, currentPage, new NetworkWallhavenRepository.WallpapersCallback() {
            @Override
            public void onSuccess(NetworkWallhavenWallpapersResponse response) {
                _loading.setValue(false);
                if (response != null && response.getData() != null) {
                    _wallpapers.setValue(response.getData());
                    _showResults.setValue(true);
                    
                    // Check if there are more pages
                    if (response.getMeta() != null) {
                        hasMorePages = response.getMeta().getCurrentPage() < response.getMeta().getLastPage();
                    }
                } else {
                    _wallpapers.setValue(new ArrayList<>());
                    _showResults.setValue(true);
                    hasMorePages = false;
                }
            }

            @Override
            public void onError(String error) {
                _loading.setValue(false);
                _error.setValue("Failed to search wallpapers: " + error);
                _wallpapers.setValue(new ArrayList<>());
                _showResults.setValue(true);
                hasMorePages = false;
            }
        });
    }
    
    public void loadMoreWallpapers() {
        if (isLoadingMore || !hasMorePages || currentSearch == null) return;
        
        isLoadingMore = true;
        _loadingMore.setValue(true);
        _error.setValue(null);
        
        repository.searchWallpapers(currentSearch, currentPage + 1, new NetworkWallhavenRepository.WallpapersCallback() {
            @Override
            public void onSuccess(NetworkWallhavenWallpapersResponse response) {
                isLoadingMore = false;
                _loadingMore.setValue(false);
                if (response != null && response.getData() != null && !response.getData().isEmpty()) {
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
                _error.setValue("Failed to load more wallpapers: " + error);
            }
        });
    }

    public void refreshSearch() {
        if (currentSearch != null) {
            // Reset pagination for refresh
            currentPage = 1;
            hasMorePages = true;
            isLoadingMore = false;
            performSearch();
        }
    }

    public void clearFilters() {
        currentSearch = createDefaultSearch();
        currentPage = 1;
        hasMorePages = true;
        isLoadingMore = false;
        _wallpapers.setValue(new ArrayList<>());
        _showResults.setValue(false);
        _error.setValue(null);
    }

    public void hideResults() {
        _showResults.setValue(false);
    }

    private WallhavenSearch createDefaultSearch() {
        WallhavenFilters filters = new WallhavenFilters();
        
        // Set default categories: all 3 (General, Anime, People)
        HashSet<WallhavenCategory> defaultCategories = new HashSet<>();
        defaultCategories.add(WallhavenCategory.GENERAL);
        defaultCategories.add(WallhavenCategory.ANIME);
        defaultCategories.add(WallhavenCategory.PEOPLE);
        filters.setCategories(defaultCategories);
        
        // Set default purity: SFW only
        HashSet<Purity> defaultPurities = new HashSet<>();
        defaultPurities.add(Purity.SFW);
        filters.setPurity(defaultPurities);
        
        // Set default sorting: Relevance
        filters.setSorting(WallhavenSorting.RELEVANCE);
        
        // Set default order: Descending
        filters.setOrder(Order.DESC);
        
        // Set default top range: One Month (even though it won't be used unless sorting is TOPLIST)
        filters.setTopRange(WallhavenTopRange.ONE_MONTH);
        
        WallhavenSearch search = new WallhavenSearch();
        search.setFilters(filters);
        
        return search;
    }
}
