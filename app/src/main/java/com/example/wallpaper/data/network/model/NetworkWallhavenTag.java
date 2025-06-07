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
public class NetworkWallhavenTag {
    @Json(name = "id")
    private long id;
    
    @Json(name = "name")
    private String name;
    
    @Json(name = "alias")
    private String alias;
    
    @Json(name = "category_id")
    private long categoryId;
    
    @Json(name = "category")
    private String category;
    
    @Json(name = "purity")
    private String purity;
    
    @Json(name = "created_at")
    private Instant createdAt;
}
