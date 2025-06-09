package com.example.wallpaper.ui.activity;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canhub.cropper.CropImageView;
import com.example.wallpaper.R;
import com.example.wallpaper.databinding.ActivityWallpaperCropBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WallpaperCropActivity extends AppCompatActivity {
    
    public static final String EXTRA_WALLPAPER_PATH = "wallpaper_path";
    public static final String EXTRA_WALLPAPER_ID = "wallpaper_id";
    
    private ActivityWallpaperCropBinding binding;
    private String wallpaperPath;
    private String wallpaperId;
    private Bitmap originalBitmap;
    private WallpaperManager wallpaperManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWallpaperCropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        wallpaperManager = WallpaperManager.getInstance(this);
        
        getIntentData();
        setupUI();
        loadWallpaper();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        wallpaperPath = intent.getStringExtra(EXTRA_WALLPAPER_PATH);
        wallpaperId = intent.getStringExtra(EXTRA_WALLPAPER_ID);
        
        if (wallpaperPath == null || wallpaperPath.isEmpty()) {
            Toast.makeText(this, "Error: Invalid wallpaper", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.setHomeScreenButton.setOnClickListener(v -> setWallpaper(WallpaperManager.FLAG_SYSTEM));
        binding.setLockScreenButton.setOnClickListener(v -> setWallpaper(WallpaperManager.FLAG_LOCK));
        binding.setBothButton.setOnClickListener(v -> setWallpaper(WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK));
        
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cropButtonsLayout.setVisibility(View.GONE);
    }
    
    private void loadWallpaper() {
        Glide.with(this)
                .asBitmap()
                .load(wallpaperPath)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        originalBitmap = resource;
                        setupCropView(resource);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.cropButtonsLayout.setVisibility(View.VISIBLE);
                    }
                    
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle cleanup if needed
                    }
                    
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(WallpaperCropActivity.this, "Failed to load wallpaper", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void setupCropView(Bitmap bitmap) {
        // Set up the crop view with the bitmap
        binding.cropImageView.setImageBitmap(bitmap);
        
        // Get screen dimensions to set appropriate crop ratio
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // Set aspect ratio to match screen dimensions
        binding.cropImageView.setAspectRatio(screenWidth, screenHeight);
        binding.cropImageView.setFixedAspectRatio(true);
        
        // Configure crop image view for better user experience
        binding.cropImageView.setGuidelines(CropImageView.Guidelines.ON);
        binding.cropImageView.setScaleType(CropImageView.ScaleType.CENTER_INSIDE);
        binding.cropImageView.setAutoZoomEnabled(true);
        binding.cropImageView.setMaxZoom(4);
        binding.cropImageView.setMultiTouchEnabled(true);
        
        // Set crop shape to rectangle (default)
        binding.cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
    }
    
    private void setWallpaper(int flag) {
        if (originalBitmap == null) {
            Toast.makeText(this, "Wallpaper not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Get the cropped bitmap from CropImageView
            Bitmap croppedBitmap = binding.cropImageView.getCroppedImage();
            
            if (croppedBitmap == null) {
                Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Set the wallpaper
            wallpaperManager.setBitmap(croppedBitmap, null, true, flag);
            
            String message;
            switch (flag) {
                case WallpaperManager.FLAG_SYSTEM:
                    message = "Home screen wallpaper set successfully!";
                    break;
                case WallpaperManager.FLAG_LOCK:
                    message = "Lock screen wallpaper set successfully!";
                    break;
                case WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK:
                    message = "Wallpaper set for both screens successfully!";
                    break;
                default:
                    message = "Wallpaper set successfully!";
                    break;
            }
            
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to set wallpaper: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred while setting wallpaper", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't manually recycle bitmaps loaded by Glide
        // Glide manages bitmap lifecycle automatically
        binding = null;
    }
}
