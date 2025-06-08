package com.example.wallpaper.model.search;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for search implementations.
 * Java equivalent of the Kotlin sealed class Search.
 */
@Getter
@Setter
public abstract class Search {
    protected String query;
    protected Filters filters;
    protected SearchMeta meta;
    
    public Search() {}
    
    public Search(String query, Filters filters, SearchMeta meta) {
        this.query = query;
        this.filters = filters;
        this.meta = meta;
    }
}
