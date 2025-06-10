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
public class LocalWallpaper {
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
}
