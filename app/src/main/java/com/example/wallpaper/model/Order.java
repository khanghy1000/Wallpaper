package com.example.wallpaper.model;

/**
 * Enum representing sort order.
 * Java equivalent of the Kotlin Order enum.
 */
public enum Order {
    DESC("desc"),
    ASC("asc");
    
    private final String value;
    
    Order(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static Order fromValue(String value) {
        return "desc".equals(value) ? DESC : ASC;
    }
}
