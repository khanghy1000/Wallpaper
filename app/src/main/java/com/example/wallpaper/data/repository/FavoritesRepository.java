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
                        .mainImgUrl(wallpaper.getMainImgUrl())
                        .ratio(wallpaper.getRatio())
                        .build();
                    return favoriteDao.insert(favorite);
                }
            })
            .observeOn(AndroidSchedulers.mainThread());
    }
}
