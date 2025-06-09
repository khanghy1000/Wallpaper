package com.example.wallpaper.model.search;

import android.util.Size;

import com.example.wallpaper.model.Order;
import com.example.wallpaper.model.Purity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wallhaven-specific filters implementation.
 * Java equivalent of the Kotlin WallhavenFilters data class.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WallhavenFilters extends Filters {
    private Set<String> includedTags = new HashSet<>();
    private Set<String> excludedTags = new HashSet<>();
    private String username;
    private Long tagId;
    private String wallpaperId;
    private Set<WallhavenCategory> categories = getDefaultCategories();
    private Set<Purity> purity = getDefaultPurities();
    private WallhavenSorting sorting = WallhavenSorting.DATE_ADDED;
    private Order order = Order.DESC;
    private WallhavenTopRange topRange = WallhavenTopRange.ONE_MONTH;
    private Size atleast;
    private Set<Size> resolutions = new HashSet<>();
    private Integer colors; // Using Integer to represent color
    private String seed;
    private Set<WallhavenRatio> ratios = new HashSet<>();
    
    public static Set<WallhavenCategory> getDefaultCategories() {
        return new HashSet<>(Arrays.asList(
            WallhavenCategory.GENERAL,
            WallhavenCategory.ANIME,
            WallhavenCategory.PEOPLE
        ));
    }
    
    public static Set<Purity> getDefaultPurities() {
        return new HashSet<>(Collections.singletonList(Purity.SFW));
    }
    
    /**
     * Converts categories set to category integer representation.
     */
    public static int toCategoryInt(Set<WallhavenCategory> categories) {
        return categories.stream()
                .mapToInt(WallhavenCategory::getFlag)
                .sum();
    }
}
