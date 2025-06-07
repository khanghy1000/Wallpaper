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
public class NetworkWallhavenThumbs {
    @Json(name = "large")
    private String large;
    
    @Json(name = "original")
    private String original;
    
    @Json(name = "small")
    private String small;
}
