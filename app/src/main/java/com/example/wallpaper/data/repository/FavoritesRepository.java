package com.example.wallpaper.data.repository;

import com.example.wallpaper.data.database.dao.FavoriteDao;
import com.example.wallpaper.data.database.entity.FavoriteEntity;
import com.example.wallpaper.model.FavoriteWallpaper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class FavoritesRepository {
    
    private final FavoriteDao favoriteDao;
    
    @Inject
    public FavoritesRepository(FavoriteDao favoriteDao) {
        this.favoriteDao = favoriteDao;
    }
    
    public Flowable<List<FavoriteEntity>> observeAllFavorites() {
        return favoriteDao.observeAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Single<List<FavoriteEntity>> getAllFavorites() {
        return favoriteDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Single<Boolean> isFavorite(String sourceId, String source) {
        return favoriteDao.exists(sourceId, source)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Completable toggleFavorite(String sourceId, String source) {
        return favoriteDao.exists(sourceId, source)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable(exists -> {
                if (exists) {
                    return favoriteDao.deleteBySourceIdAndSource(sourceId, source);
                } else {
                    FavoriteEntity favorite = FavoriteEntity.builder()
                        .sourceId(sourceId)
                        .source(source)
                        .favoritedOn(System.currentTimeMillis())
                        .thumbUrl(null) // We'll need to update this to accept FavoriteWallpaper
                        .ratio(1.0f)    // Default ratio
                        .build();
                    return favoriteDao.insert(favorite);
                }
            })
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Completable toggleFavorite(FavoriteWallpaper wallpaper) {
        return favoriteDao.exists(wallpaper.getSourceId(), wallpaper.getSource())
            .subscribeOn(Schedulers.io())
            .flatMapCompletable(exists -> {
                if (exists) {
                    return favoriteDao.deleteBySourceIdAndSource(wallpaper.getSourceId(), wallpaper.getSource());
                } else {
                    FavoriteEntity favorite = FavoriteEntity.builder()
                        .sourceId(wallpaper.getSourceId())
                        .source(wallpaper.getSource())
                        .favoritedOn(System.currentTimeMillis())
                        .thumbUrl(wallpaper.getThumbUrl())
                        .ratio(wallpaper.getRatio())
                        .build();
                    return favoriteDao.insert(favorite);
                }
            })
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Completable addFavorite(String sourceId, String source) {
        return favoriteDao.exists(sourceId, source)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable(exists -> {
                if (!exists) {
                    FavoriteEntity favorite = FavoriteEntity.builder()
                        .sourceId(sourceId)
                        .source(source)
                        .favoritedOn(System.currentTimeMillis())
                        .thumbUrl(null) // Legacy method - no thumb URL
                        .ratio(1.0f)    // Default ratio
                        .build();
                    return favoriteDao.insert(favorite);
                }
                return Completable.complete();
            })
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Completable removeFavorite(String sourceId, String source) {
        return favoriteDao.deleteBySourceIdAndSource(sourceId, source)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    public Single<List<FavoriteWallpaper>> getAllFavoriteWallpapers() {
        return favoriteDao.getAll()
            .subscribeOn(Schedulers.io())
            .map(favorites -> {
                List<FavoriteWallpaper> favoriteWallpapers = new ArrayList<>();
                for (FavoriteEntity favorite : favorites) {
                    favoriteWallpapers.add(new FavoriteWallpaperImpl(favorite));
                }
                return favoriteWallpapers;
            })
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    // Simple implementation class for FavoriteWallpaper interface
    private static class FavoriteWallpaperImpl implements FavoriteWallpaper {
        private final FavoriteEntity entity;
        
        public FavoriteWallpaperImpl(FavoriteEntity entity) {
            this.entity = entity;
        }
        
        @Override
        public String getSourceId() {
            return entity.getSourceId();
        }
        
        @Override
        public String getSource() {
            return entity.getSource();
        }
        
        @Override
        public String getThumbUrl() {
            // For now, return a placeholder - in a full implementation, 
            // you'd fetch this from the original wallpaper data
            return null;
        }
        
        @Override
        public float getRatio() {
            // Default ratio - in a full implementation, you'd store or fetch this
            return 1.0f;
        }
    }
}
