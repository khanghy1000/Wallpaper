package com.example.wallpaper.data.network;

import com.example.wallpaper.data.network.model.NetworkWallhavenWallpapersResponse;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaperResponse;

import org.jsoup.nodes.Document;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NetworkWallhavenService {
    // Document: https://wallhaven.cc/help/api#search
    @GET("search")
    Call<NetworkWallhavenWallpapersResponse> search(
            @Query("q") String query,
            @Query("categories") String categories,
            @Query("purity") String purity,
            @Query("sorting") String sorting,
            @Query("order") String order,
            @Query("topRange") String topRange,
            @Query("atleast") String atleast,
            @Query("resolutions") String resolutions,
            @Query("colors") String colors,
            @Query("ratios") String ratios,
            @Query("page") Integer page,
            @Query("seed") String seed
    );

    // Document: http://wallhaven.cc/help/api#wallpapers 
    @GET("w/{id}")
    Call<NetworkWallhavenWallpaperResponse> wallpaper(
            @Path("id") String id
    );

    @GET("https://wallhaven.cc/tags/popular")
    Document popularTags();
}
