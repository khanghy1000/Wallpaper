package com.example.wallpaper.model.search;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for Wallhaven search metadata.
 * Java equivalent of the Kotlin sealed class WallhavenSearchMeta.
 */
@Getter
@Setter
public abstract class WallhavenSearchMeta extends SearchMeta {
    public WallhavenSearchMeta() {}
}
