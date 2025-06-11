package com.example.wallpaper.data.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(
    tableName = "favorites",
    indices = {
        @Index(
            value = {"source_id", "source"},
            unique = true
        )
    }
)
@Data
@Builder
@NoArgsConstructor
public class FavoriteEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "source_id")
    private String sourceId;
    
    private String source; // "WALLHAVEN", "LOCAL"
    
    @ColumnInfo(name = "favorited_on")
    private long favoritedOn; // timestamp in milliseconds
    
    @ColumnInfo(name = "thumb_url")
    private String thumbUrl; // URL for wallpaper thumbnail
    
    private float ratio; // width/height ratio for proper display
    
    @Ignore
    public FavoriteEntity(long id, String sourceId, String source, long favoritedOn, String thumbUrl, float ratio) {
        this.id = id;
        this.sourceId = sourceId;
        this.source = source;
        this.favoritedOn = favoritedOn;
        this.thumbUrl = thumbUrl;
        this.ratio = ratio;
    }
}
