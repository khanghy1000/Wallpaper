package com.example.wallpaper.data.network;

import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaperResponse;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.model.Purity;
import com.example.wallpaper.model.search.WallhavenCategory;
import com.example.wallpaper.model.search.WallhavenFilters;
import com.example.wallpaper.model.search.WallhavenRatio;
import com.example.wallpaper.model.search.WallhavenSearch;

import android.util.Size;

import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import retrofit2.Call;

/**
 * Java implementation of Retrofit-based Wallhaven network data source.
 * Converts from the Kotlin RetrofitWallhavenNetwork class.
 */
public class NetworkWallhavenDataSource {
    
    private final NetworkWallhavenApi networkWallhavenApi;
    
    @Inject
    public NetworkWallhavenDataSource(NetworkWallhavenApi networkWallhavenApi) {
        this.networkWallhavenApi = networkWallhavenApi;
    }
    
    /**
     * Search for wallpapers using WallhavenSearch parameters.
     */
    public Call<NetworkWallhavenWallpapersResponse> search(WallhavenSearch search, Integer page) {
        WallhavenFilters filters = search.getFilters();
        
        return networkWallhavenApi.search(
                search.getApiQueryString(),
                toCategoryString(filters.getCategories()),
                toPurityString(filters.getPurity()),
                filters.getSorting().getValue(),
                filters.getOrder().getValue(),
                filters.getTopRange().getValue(),
                sizeToString(filters.getAtleast()),
                resolutionsToString(filters.getResolutions()),
                colorToHexString(filters.getColors()),
                ratiosToString(filters.getRatios()),
                page,
                filters.getSeed()
        );
    }
    
    /**
     * Get a specific wallpaper by ID.
     */
    public Call<NetworkWallhavenWallpaperResponse> wallpaper(String wallpaperWallhavenId) {
        return networkWallhavenApi.wallpaper(wallpaperWallhavenId);
    }
    
    /**
     * Get popular tags document.
     */
    public Document popularTags() {
        return networkWallhavenApi.popularTags();
    }
    
    // Utility methods for converting Java objects to API strings
    
    /**
     * Converts categories set to padded string representation for API.
     */
    private String toCategoryString(Set<WallhavenCategory> categories) {
        int categoryInt = WallhavenFilters.toCategoryInt(categories);
        return String.format("%03d", categoryInt);
    }
    
    /**
     * Converts purity set to padded string representation for API.
     */
    private String toPurityString(Set<Purity> purities) {
        int purityInt = Purity.toWallhavenPurityInt(purities);
        return String.format("%03d", purityInt);
    }
    
    /**
     * Converts Size to string format without spaces.
     */
    private String sizeToString(Size size) {
        if (size == null) {
            return null;
        }
        return size.getWidth() + "x" + size.getHeight();
    }
    
    /**
     * Converts set of resolutions to comma-separated string.
     */
    private String resolutionsToString(Set<Size> resolutions) {
        if (resolutions == null || resolutions.isEmpty()) {
            return null;
        }
        return resolutions.stream()
                .map(this::sizeToString)
                .collect(Collectors.joining(","));
    }
    
    /**
     * Converts color integer to hex string without # prefix.
     */
    private String colorToHexString(Integer color) {
        if (color == null) {
            return null;
        }
        // Convert to hex string and remove the # prefix
        String hex = Integer.toHexString(color);
        // Ensure 6 digits by padding with zeros if needed
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }
    
    /**
     * Converts set of ratios to comma-separated string without spaces.
     */
    private String ratiosToString(Set<WallhavenRatio> ratios) {
        if (ratios == null || ratios.isEmpty()) {
            return null;
        }
        return ratios.stream()
                .map(ratio -> ratio.toRatioString().replace(" ", ""))
                .collect(Collectors.joining(","));
    }
}
