package com.example.wallpaper.data.repository;

import com.example.wallpaper.data.network.NetworkWallhavenService;
import com.example.wallpaper.data.network.model.NetworkWallhavenTag;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaperResponse;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.data.repository.util.WallhavenTagsDocumentParser;

import org.jsoup.nodes.Document;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class NetworkWallhavenRepository {
    
    private final NetworkWallhavenService networkWallhavenService;
    
    @Inject
    public NetworkWallhavenRepository(NetworkWallhavenService networkWallhavenService) {
        this.networkWallhavenService = networkWallhavenService;
    }
    
    public void searchWallpapers(
            String query,
            String categories,
            String purity,
            String sorting,
            String order,
            String topRange,
            String atleast,
            String resolutions,
            String colors,
            String ratios,
            Integer page,
            String seed,
            WallpapersCallback callback
    ) {
        Call<NetworkWallhavenWallpapersResponse> call = networkWallhavenService.search(
                query, categories, purity, sorting, order, topRange,
                atleast, resolutions, colors, ratios, page, seed
        );
        
        call.enqueue(new Callback<NetworkWallhavenWallpapersResponse>() {
            @Override
            public void onResponse(Call<NetworkWallhavenWallpapersResponse> call, 
                                 Response<NetworkWallhavenWallpapersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch wallpapers: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<NetworkWallhavenWallpapersResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    public void getWallpaper(String id, WallpaperCallback callback) {
        Call<NetworkWallhavenWallpaperResponse> call = networkWallhavenService.wallpaper(id);
        
        call.enqueue(new Callback<NetworkWallhavenWallpaperResponse>() {
            @Override
            public void onResponse(Call<NetworkWallhavenWallpaperResponse> call,
                                 Response<NetworkWallhavenWallpaperResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch wallpaper: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<NetworkWallhavenWallpaperResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    public void getPopularTags(PopularTagsCallback callback) {
        try {
            Document document = networkWallhavenService.popularTags();
            if (document != null) {
                List<NetworkWallhavenTag> tags = WallhavenTagsDocumentParser.parsePopularTags(document);
                callback.onSuccess(tags);
            } else {
                callback.onError("Failed to fetch popular tags: Document is null");
            }
        } catch (Exception e) {
            callback.onError("Error fetching popular tags: " + e.getMessage());
        }
    }
    
    // Callback interfaces
    public interface WallpapersCallback {
        void onSuccess(NetworkWallhavenWallpapersResponse response);
        void onError(String error);
    }
    
    public interface WallpaperCallback {
        void onSuccess(NetworkWallhavenWallpaperResponse response);
        void onError(String error);
    }
    
    public interface PopularTagsCallback {
        void onSuccess(List<NetworkWallhavenTag> tags);
        void onError(String error);
    }
}
