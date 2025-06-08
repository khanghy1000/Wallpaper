package com.example.wallpaper.data.network.model;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkWallhavenMeta {
    @Json(name = "current_page")
    private int currentPage;

    @Json(name = "last_page")
    private int lastPage;

    @Json(name = "per_page")
    private int perPage;

    @Json(name = "total")
    private int total;

    @Json(name = "query")
    @Nullable
    private NetworkWallhavenMetaQuery query;

    @Json(name = "seed")
    private String seed;
}
