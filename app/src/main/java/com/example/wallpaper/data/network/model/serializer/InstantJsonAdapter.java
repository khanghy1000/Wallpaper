package com.example.wallpaper.data.network.model.serializer;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InstantJsonAdapter {
    private static final DateTimeFormatter WALLHAVEN_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @ToJson
    public String toJson(Instant value) {
        return value.toString();
    }

    @FromJson
    public Instant fromJson(String json) {
        try {
            // Parse the Wallhaven format "2025-06-08 12:50:35" as UTC
            LocalDateTime localDateTime = LocalDateTime.parse(json, WALLHAVEN_FORMATTER);
            return localDateTime.toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            // Fallback to original logic if the new format fails
            return Instant.parse(json.replace(" ", "T") + "Z");
        }
    }
}
