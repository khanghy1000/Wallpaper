package com.example.wallpaper.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.wallpaper.data.database.entity.FavoriteEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FavoriteDao {
    
    @Query("SELECT * FROM favorites ORDER BY favorited_on DESC")
    Flowable<List<FavoriteEntity>> observeAll();
    
    @Query("SELECT * FROM favorites ORDER BY favorited_on DESC")
    Single<List<FavoriteEntity>> getAll();
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE source_id = :sourceId AND source = :source)")
    Single<Boolean> exists(String sourceId, String source);
    
    @Query("SELECT * FROM favorites WHERE source_id = :sourceId AND source = :source")
    Single<FavoriteEntity> getBySourceIdAndSource(String sourceId, String source);
    
    @Insert
    Completable insert(FavoriteEntity favoriteEntity);
    
    @Query("DELETE FROM favorites WHERE source_id = :sourceId AND source = :source")
    Completable deleteBySourceIdAndSource(String sourceId, String source);
    
    @Query("DELETE FROM favorites")
    Completable deleteAll();
}
