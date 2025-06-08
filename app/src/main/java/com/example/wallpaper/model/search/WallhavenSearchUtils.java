package com.example.wallpaper.model.search;

import android.content.Context;
import com.example.wallpaper.R;
import com.example.wallpaper.model.Purity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for WallhavenSearch operations.
 * Contains helper methods equivalent to Kotlin extension functions.
 */
public class WallhavenSearchUtils {
    
    /**
     * Gets supporting text for a WallhavenSearch object.
     * Java equivalent of the Kotlin getSupportingText extension function.
     */
    public static String getSupportingText(WallhavenSearch search, Context context) {
        List<String> supportingTexts = new ArrayList<>();
        WallhavenFilters filters = search.getFilters();
        
        // Included tags
        if (!filters.getIncludedTags().isEmpty()) {
            String includedTagsText = filters.getIncludedTags().stream()
                    .map(tag -> "#" + tag)
                    .collect(Collectors.joining(", "));
            supportingTexts.add(context.getString(R.string.included_tags_supp, includedTagsText));
        }
        
        // Excluded tags
        if (!filters.getExcludedTags().isEmpty()) {
            String excludedTagsText = filters.getExcludedTags().stream()
                    .map(tag -> "#" + tag)
                    .collect(Collectors.joining(", "));
            supportingTexts.add(context.getString(R.string.excluded_tags_supp, excludedTagsText));
        }
        
        // Categories
        if (!filters.getCategories().equals(WallhavenFilters.getDefaultCategories())) {
            String categoriesText = filters.getCategories().stream()
                    .map(WallhavenCategory::getValue)
                    .collect(Collectors.joining(", "));
            supportingTexts.add(context.getString(R.string.categories_supp, categoriesText));
        }
        
        // Purity
        if (!filters.getPurity().equals(WallhavenFilters.getDefaultPurities())) {
            String purityText = filters.getPurity().stream()
                    .map(Purity::getPurityName)
                    .collect(Collectors.joining(", "));
            supportingTexts.add(context.getString(R.string.purities_supp, purityText));
        }
        
        // Username
        if (filters.getUsername() != null && !filters.getUsername().trim().isEmpty()) {
            supportingTexts.add(context.getString(R.string.username_supp, "@" + filters.getUsername()));
        }
        
        // Tag ID
        if (filters.getTagId() != null) {
            supportingTexts.add(context.getString(R.string.tag_id_supp, filters.getTagId().toString()));
        }
        
        // Wallpaper ID
        if (filters.getWallpaperId() != null && !filters.getWallpaperId().trim().isEmpty()) {
            supportingTexts.add(context.getString(R.string.wallpaper_id_supp, filters.getWallpaperId()));
        }
        
        String result = String.join(", ", supportingTexts);
        return result.isEmpty() ? null : result;
    }
    
    private WallhavenSearchUtils() {
        // Utility class - prevent instantiation
    }
}
