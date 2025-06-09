package com.example.wallpaper.model.search;

/**
 * Enum representing Wallhaven sorting options.
 * Java equivalent of the Kotlin WallhavenSorting enum.
 */
public enum WallhavenSorting {
    TOPLIST("toplist"),
    DATE_ADDED("date_added"),
    RELEVANCE("relevance"),
    RANDOM("random"),
    VIEWS("views"),
    FAVORITES("favorites");
    
    private final String value;
    
    WallhavenSorting(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        switch (this) {
            case TOPLIST:
                return "Top List";
            case DATE_ADDED:
                return "Date Added";
            case RELEVANCE:
                return "Relevance";
            case RANDOM:
                return "Random";
            case VIEWS:
                return "Views";
            case FAVORITES:
                return "Favorites";
            default:
                return value;
        }
    }
    
    public static WallhavenSorting fromValue(String value) {
        switch (value) {
            case "date_added":
                return DATE_ADDED;
            case "relevance":
                return RELEVANCE;
            case "random":
                return RANDOM;
            case "views":
                return VIEWS;
            case "favorites":
                return FAVORITES;
            default:
                return TOPLIST;
        }
    }
}
