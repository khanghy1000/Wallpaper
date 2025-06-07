package com.example.wallpaper.data.network.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagNetworkWallhavenMetaQuery implements NetworkWallhavenMetaQuery {
    private Long id;
    private String tag;
}
