package com.example.wallpaper.model.search;

/**
 * Enum representing Wallhaven categories.
 * Java equivalent of the Kotlin WallhavenCategory enum.
 */
public enum WallhavenCategory {
    GENERAL(100, "general"),
    ANIME(10, "anime"),
    PEOPLE(1, "people");
    
    private final int flag;
    private final String value;
    
    WallhavenCategory(int flag, String value) {
        this.flag = flag;
        this.value = value;
    }
    
    public int getFlag() {
        return flag;
    }
    
    public String getValue() {
        return value;
    }
    
    public static WallhavenCategory fromValue(String value) {
        switch (value) {
            case "general":
                return GENERAL;
            case "anime":
                return ANIME;
            default:
                return PEOPLE;
        }
    }
}
