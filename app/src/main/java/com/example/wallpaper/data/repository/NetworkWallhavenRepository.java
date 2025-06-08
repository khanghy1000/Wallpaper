package com.example.wallpaper.data.repository;

import com.example.wallpaper.data.network.NetworkWallhavenDataSource;
import com.example.wallpaper.data.network.model.NetworkWallhavenTag;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaperResponse;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.data.repository.util.WallhavenTagsDocumentParser;
import com.example.wallpaper.model.search.WallhavenSearch;

import org.jsoup.nodes.Document;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkWallhavenRepository {
    
    private final NetworkWallhavenDataSource networkWallhavenDataSource;
    
    @Inject
    public NetworkWallhavenRepository(NetworkWallhavenDataSource networkWallhavenDataSource) {
        this.networkWallhavenDataSource = networkWallhavenDataSource;
    }
    
    public void searchWallpapers(
            WallhavenSearch search,
            Integer page,
            WallpapersCallback callback
    ) {
        Call<NetworkWallhavenWallpapersResponse> call = networkWallhavenDataSource.search(search, page);
        
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
        Call<NetworkWallhavenWallpaperResponse> call = networkWallhavenDataSource.wallpaper(id);
        
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
            Document document = networkWallhavenDataSource.popularTags();
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
