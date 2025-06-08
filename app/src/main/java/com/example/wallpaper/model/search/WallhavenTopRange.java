package com.example.wallpaper.model.search;

/**
 * Enum representing Wallhaven top range options.
 * Java equivalent of the Kotlin WallhavenTopRange enum.
 */
public enum WallhavenTopRange {
    ONE_DAY("1d"),
    THREE_DAYS("3d"),
    ONE_WEEK("1w"),
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1y");
    
    private final String value;
    
    WallhavenTopRange(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static WallhavenTopRange fromValue(String value) {
        switch (value) {
            case "1d":
                return ONE_DAY;
            case "3d":
                return THREE_DAYS;
            case "1w":
                return ONE_WEEK;
            case "1M":
                return ONE_MONTH;
            case "3M":
                return THREE_MONTHS;
            case "6M":
                return SIX_MONTHS;
            default:
                return ONE_YEAR;
        }
    }
}
