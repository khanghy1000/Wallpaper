package com.example.wallpaper.data.network.model;

import com.squareup.moshi.Json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkWallhavenWallpaperResponse {
    @Json(name = "data")
    private NetworkWallhavenWallpaper data;
}
