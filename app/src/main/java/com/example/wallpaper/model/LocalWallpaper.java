package com.example.wallpaper.model;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalWallpaper implements FavoriteWallpaper {
    private String id;
    private String name;
    private Uri uri;
    private String path;
    private long fileSize;
    private int width;
    private int height;
    private String mimeType;
    private long dateModified;
    
    public float getRatio() {
        if (height == 0) return 1.0f;
        return (float) width / height;
    }
    
    public String getResolution() {
        return width + "x" + height;
    }
    
    // FavoriteWallpaper interface implementation
    @Override
    public String getSourceId() {
        return id;
    }
    
    @Override
    public String getSource() {
        return "LOCAL";
    }
    
    @Override
    public String getThumbUrl() {
        return uri != null ? uri.toString() : null;
    }

    @Override
    public String getMainImgUrl() {
        return uri != null ? uri.toString() : null; // Use the URI as the URL for local wallpapers
    }
}
