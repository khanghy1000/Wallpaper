package com.example.wallpaper.data.network.model;

import com.squareup.moshi.Json;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkWallhavenWallpapersResponse {
    @Json(name = "data")
    private List<NetworkWallhavenWallpaper> data;
    
    @Json(name = "meta")
    private NetworkWallhavenMeta meta;
}
