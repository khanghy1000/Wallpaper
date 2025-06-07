package com.example.wallpaper.data.network.model.serializer;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.time.Instant;

public class InstantJsonAdapter {
    @ToJson
    public String toJson(Instant value) {
        return value.toString();
    }

    @FromJson
    public Instant fromJson(String json) {
        // Replace any space with 'T', append 'Z', then parse
        return Instant.parse(json.replace(" ", "T") + "Z");
    }
}
