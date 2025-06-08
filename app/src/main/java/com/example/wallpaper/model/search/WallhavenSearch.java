package com.example.wallpaper.model.search;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Wallhaven search implementation.
 * Java equivalent of the Kotlin WallhavenSearch data class.
 */
@Getter
@Setter
@NoArgsConstructor
public class WallhavenSearch extends Search {
    private String query = "";
    private WallhavenFilters filters = new WallhavenFilters();
    private WallhavenSearchMeta meta;
    
    public WallhavenSearch(String query, WallhavenFilters filters, WallhavenSearchMeta meta) {
        super(query, filters, meta);
        this.query = query;
        this.filters = filters;
        this.meta = meta;
    }
    
    /**
     * Converts this search object to JSON string.
     */
    public String toJson() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<WallhavenSearch> adapter = moshi.adapter(WallhavenSearch.class);
        return adapter.toJson(this);
    }
    
    /**
     * Creates WallhavenSearch from JSON string.
     */
    public static WallhavenSearch fromJson(String json) {
        try {
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<WallhavenSearch> adapter = moshi.adapter(WallhavenSearch.class);
            return adapter.fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Generates API query string for Wallhaven API.
     */
    public String getApiQueryString() {
        WallhavenFilters combinedFilters = getQueryCombinedFilters();
        List<String> queryParts = new ArrayList<>();
        
        // Handle included tags
        if (!combinedFilters.getIncludedTags().isEmpty()) {
            StringBuilder includedBuilder = new StringBuilder();
            for (String tag : combinedFilters.getIncludedTags()) {
                if (!tag.trim().isEmpty()) {
                    if (includedBuilder.length() > 0) {
                        includedBuilder.append(" ");
                    }
                    includedBuilder.append("+").append(quoteIfSpaced(tag));
                }
            }
            if (includedBuilder.length() > 0) {
                queryParts.add(includedBuilder.toString());
            }
        }
        
        // Handle excluded tags
        if (!combinedFilters.getExcludedTags().isEmpty()) {
            StringBuilder excludedBuilder = new StringBuilder();
            for (String tag : combinedFilters.getExcludedTags()) {
                if (!tag.trim().isEmpty()) {
                    if (excludedBuilder.length() > 0) {
                        excludedBuilder.append(" ");
                    }
                    excludedBuilder.append("-").append(quoteIfSpaced(tag));
                }
            }
            if (excludedBuilder.length() > 0) {
                queryParts.add(excludedBuilder.toString());
            }
        }
        
        // Handle username
        if (combinedFilters.getUsername() != null && !combinedFilters.getUsername().trim().isEmpty()) {
            queryParts.add("@" + combinedFilters.getUsername());
        }
        
        // Handle tag ID
        if (combinedFilters.getTagId() != null) {
            queryParts.add("id:" + combinedFilters.getTagId());
        }
        
        // Handle wallpaper ID
        if (combinedFilters.getWallpaperId() != null && !combinedFilters.getWallpaperId().trim().isEmpty()) {
            queryParts.add("like:" + combinedFilters.getWallpaperId());
        }
        
        return String.join(" ", queryParts);
    }
    
    private WallhavenFilters getQueryCombinedFilters() {
        if (query == null || query.trim().isEmpty()) {
            return filters;
        }
        
        String trimmedQuery = query.trim();
        
        // Handle special query prefixes
        if (trimmedQuery.startsWith("id:")) {
            String tagIdString = trimmedQuery.substring(3);
            try {
                Long tagId = Long.parseLong(tagIdString);
                WallhavenFilters newFilters = copyFilters(filters);
                newFilters.setTagId(tagId);
                return newFilters;
            } catch (NumberFormatException e) {
                return filters;
            }
        }
        
        if (trimmedQuery.startsWith("like:")) {
            String wallpaperId = trimmedQuery.substring(5);
            if (!wallpaperId.trim().isEmpty()) {
                WallhavenFilters newFilters = copyFilters(filters);
                newFilters.setWallpaperId(wallpaperId);
                return newFilters;
            }
            return filters;
        }
        
        if (trimmedQuery.startsWith("@")) {
            String username = trimmedQuery.substring(1);
            if (!username.trim().isEmpty()) {
                WallhavenFilters newFilters = copyFilters(filters);
                newFilters.setUsername(username);
                return newFilters;
            }
            return filters;
        }
        
        // Default: add query as included tag
        WallhavenFilters newFilters = copyFilters(filters);
        newFilters.getIncludedTags().add(trimmedQuery);
        return newFilters;
    }
    
    private WallhavenFilters copyFilters(WallhavenFilters original) {
        WallhavenFilters copy = new WallhavenFilters();
        copy.setIncludedTags(new HashSet<>(original.getIncludedTags()));
        copy.setExcludedTags(new HashSet<>(original.getExcludedTags()));
        copy.setUsername(original.getUsername());
        copy.setTagId(original.getTagId());
        copy.setWallpaperId(original.getWallpaperId());
        copy.setCategories(new HashSet<>(original.getCategories()));
        copy.setPurity(new HashSet<>(original.getPurity()));
        copy.setSorting(original.getSorting());
        copy.setOrder(original.getOrder());
        copy.setTopRange(original.getTopRange());
        copy.setAtleast(original.getAtleast());
        copy.setResolutions(new HashSet<>(original.getResolutions()));
        copy.setColors(original.getColors());
        copy.setSeed(original.getSeed());
        copy.setRatios(new HashSet<>(original.getRatios()));
        return copy;
    }
    
    private String quoteIfSpaced(String text) {
        if (text.contains(" ")) {
            return "\"" + text + "\"";
        }
        return text;
    }
}
