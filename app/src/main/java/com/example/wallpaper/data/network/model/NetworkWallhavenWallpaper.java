package com.example.wallpaper.data.network.model;

import com.squareup.moshi.Json;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkWallhavenWallpaper {
    @Json(name = "id")
    private String id;
    
    @Json(name = "url")
    private String url;
    
    @Json(name = "short_url")
    private String shortUrl;
    
    @Json(name = "uploader")
    private NetworkWallhavenUploader uploader;
    
    @Json(name = "views")
    private int views;
    
    @Json(name = "favorites")
    private int favorites;
    
    @Json(name = "source")
    private String source;
    
    @Json(name = "purity")
    private String purity;
    
    @Json(name = "category")
    private String category;
    
    @Json(name = "dimension_x")
    private int dimensionX;
    
    @Json(name = "dimension_y")
    private int dimensionY;
    
    @Json(name = "resolution")
    private String resolution;
    
    @Json(name = "ratio")
    private float ratio;
    
    @Json(name = "file_size")
    private long fileSize;
    
    @Json(name = "file_type")
    private String fileType;
    
    @Json(name = "created_at")
    private Instant createdAt;
    
    @Json(name = "colors")
    private List<String> colors;
    
    @Json(name = "path")
    private String path;
    
    @Json(name = "thumbs")
    private NetworkWallhavenThumbs thumbs;
    
    @Json(name = "tags")
    private List<NetworkWallhavenTag> tags;
}
