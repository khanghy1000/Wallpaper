package com.example.wallpaper.model;

import java.util.Set;

/**
 * Enum representing content purity levels.
 * Java equivalent of the Kotlin Purity enum.
 */
public enum Purity {
    SFW("sfw", 100),
    SKETCHY("sketchy", 10),
    NSFW("nsfw", 1);
    
    private final String purityName;
    private final int wallhavenValue;
    
    Purity(String purityName, int wallhavenValue) {
        this.purityName = purityName;
        this.wallhavenValue = wallhavenValue;
    }
    
    public String getPurityName() {
        return purityName;
    }
    
    public int getWallhavenValue() {
        return wallhavenValue;
    }
    
    public static Purity fromName(String name) {
        switch (name) {
            case "nsfw":
                return NSFW;
            case "sketchy":
                return SKETCHY;
            default:
                return SFW;
        }
    }
    
    /**
     * Converts a set of purities to Wallhaven purity integer representation.
     */
    public static int toWallhavenPurityInt(Set<Purity> purities) {
        return purities.stream()
                .mapToInt(Purity::getWallhavenValue)
                .sum();
    }
}
