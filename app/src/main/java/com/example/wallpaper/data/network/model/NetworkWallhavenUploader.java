package com.example.wallpaper.data.network.model;

import com.squareup.moshi.Json;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkWallhavenUploader {
    @Json(name = "username")
    private String username;
    
    @Json(name = "group")
    private String group;
    
    @Json(name = "avatar")
    private Map<String, String> avatar;
}
