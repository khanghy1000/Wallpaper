package com.example.wallpaper.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.wallpaper.model.LocalWallpaper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocalWallpaperRepository {
    
    @Inject
    public LocalWallpaperRepository() {
    }
    
    public List<LocalWallpaper> getLocalWallpapers(Context context) {
        List<LocalWallpaper> wallpapers = new ArrayList<>();
        
        ContentResolver resolver = context.getContentResolver();
        
        // Define the columns we want to retrieve
        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED
        };
        
        // Sort by date modified (newest first)
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        
        try (Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
                int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                
                while (cursor.moveToNext()) {
                    String id = cursor.getString(idColumn);
                    String name = cursor.getString(nameColumn);
                    String path = cursor.getString(dataColumn);
                    long size = cursor.getLong(sizeColumn);
                    int width = cursor.getInt(widthColumn);
                    int height = cursor.getInt(heightColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    long dateModified = cursor.getLong(dateModifiedColumn);
                    
                    // Skip images that don't have proper dimensions
                    if (width <= 0 || height <= 0) {
                        continue;
                    }
                    
                    // Create URI for the image
                    Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    
                    LocalWallpaper wallpaper = new LocalWallpaper();
                    wallpaper.setId(id);
                    wallpaper.setName(name);
                    wallpaper.setUri(uri);
                    wallpaper.setPath(path);
                    wallpaper.setFileSize(size);
                    wallpaper.setWidth(width);
                    wallpaper.setHeight(height);
                    wallpaper.setMimeType(mimeType);
                    wallpaper.setDateModified(dateModified);
                    
                    wallpapers.add(wallpaper);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return wallpapers;
    }
    
    public io.reactivex.rxjava3.core.Single<LocalWallpaper> getLocalWallpaperById(Context context, String wallpaperId) {
        return io.reactivex.rxjava3.core.Single.fromCallable(() -> {
            ContentResolver resolver = context.getContentResolver();
            
            // Define the columns we want to retrieve
            String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATE_MODIFIED
            };
            
            // Selection to find the specific wallpaper by ID
            String selection = MediaStore.Images.Media._ID + " = ?";
            String[] selectionArgs = {wallpaperId};
            
            try (Cursor cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
                    int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
                    int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                    int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                    
                    String id = cursor.getString(idColumn);
                    String name = cursor.getString(nameColumn);
                    String path = cursor.getString(dataColumn);
                    long size = cursor.getLong(sizeColumn);
                    int width = cursor.getInt(widthColumn);
                    int height = cursor.getInt(heightColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    long dateModified = cursor.getLong(dateModifiedColumn);
                    
                    // Create URI for the image
                    Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    
                    LocalWallpaper wallpaper = new LocalWallpaper();
                    wallpaper.setId(id);
                    wallpaper.setName(name);
                    wallpaper.setUri(uri);
                    wallpaper.setPath(path);
                    wallpaper.setFileSize(size);
                    wallpaper.setWidth(width);
                    wallpaper.setHeight(height);
                    wallpaper.setMimeType(mimeType);
                    wallpaper.setDateModified(dateModified);
                    
                    return wallpaper;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting local wallpaper by ID", e);
            }
            
            return null;
        });
    }
}
