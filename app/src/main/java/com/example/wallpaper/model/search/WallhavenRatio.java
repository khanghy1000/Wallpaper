package com.example.wallpaper.model.search;

import android.util.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Abstract base class for Wallhaven ratios.
 * Java equivalent of the Kotlin sealed class WallhavenRatio.
 */
@Getter
@Setter
public abstract class WallhavenRatio {
    
    public WallhavenRatio() {}
    
    public abstract String toRatioString();
    
    /**
     * Category-based ratio (landscape/portrait).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryWallhavenRatio extends WallhavenRatio {
        private Category category;
        
        public enum Category {
            LANDSCAPE("landscape"),
            PORTRAIT("portrait");
            
            private final String categoryName;
            
            Category(String categoryName) {
                this.categoryName = categoryName;
            }
            
            public String getCategoryName() {
                return categoryName;
            }
            
            public String getDisplayName() {
                switch (this) {
                    case LANDSCAPE:
                        return "Landscape";
                    case PORTRAIT:
                        return "Portrait";
                    default:
                        return categoryName;
                }
            }
        }
        
        @Override
        public String toRatioString() {
            return category.getCategoryName();
        }
    }
    
    /**
     * Size-based ratio.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeWallhavenRatio extends WallhavenRatio {
        private Size size;
        
        @Override
        public String toRatioString() {
            return size.toString();
        }
    }
    
    public static WallhavenRatio fromSize(Size size) {
        return new SizeWallhavenRatio(size);
    }
    
    public static WallhavenRatio fromCategory(CategoryWallhavenRatio.Category category) {
        return new CategoryWallhavenRatio(category);
    }
}
