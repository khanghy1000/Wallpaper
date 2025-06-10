package com.example.wallpaper.model.search;

import android.os.Parcel;
import android.os.Parcelable;
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
public abstract class WallhavenRatio implements Parcelable {
    
    public WallhavenRatio() {}
    
    public abstract String toRatioString();
    
    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Parcelable.Creator<WallhavenRatio> CREATOR = new Parcelable.Creator<WallhavenRatio>() {
        @Override
        public WallhavenRatio createFromParcel(Parcel in) {
            // Read the type identifier
            String type = in.readString();
            if ("CategoryWallhavenRatio".equals(type)) {
                return new CategoryWallhavenRatio(in);
            } else if ("SizeWallhavenRatio".equals(type)) {
                return new SizeWallhavenRatio(in);
            }
            throw new IllegalArgumentException("Unknown WallhavenRatio type: " + type);
        }

        @Override
        public WallhavenRatio[] newArray(int size) {
            return new WallhavenRatio[size];
        }
    };
    
    /**
     * Category-based ratio (landscape/portrait).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryWallhavenRatio extends WallhavenRatio {
        private Category category;
        
        // Parcelable constructor
        protected CategoryWallhavenRatio(Parcel in) {
            category = Category.valueOf(in.readString());
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString("CategoryWallhavenRatio");  // Type identifier
            dest.writeString(category.name());
        }
        
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
        
        // Parcelable constructor
        protected SizeWallhavenRatio(Parcel in) {
            int width = in.readInt();
            int height = in.readInt();
            size = new Size(width, height);
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString("SizeWallhavenRatio");  // Type identifier
            dest.writeInt(size.getWidth());
            dest.writeInt(size.getHeight());
        }
        
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
