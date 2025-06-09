package com.example.wallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wallpaper.R;
import com.example.wallpaper.databinding.ActivityWallpaperViewerBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WallpaperViewerActivity extends AppCompatActivity {
    
    private static final String EXTRA_WALLPAPER_PATH = "wallpaper_path";
    private static final String EXTRA_WALLPAPER_ID = "wallpaper_id";
    private static final int REQUEST_CROP_WALLPAPER = 1001;
    
    private ActivityWallpaperViewerBinding binding;
    private String wallpaperPath;
    private String wallpaperId;
    private Bitmap currentBitmap;
    
    public static void start(Context context, String wallpaperPath, String wallpaperId) {
        Intent intent = new Intent(context, WallpaperViewerActivity.class);
        intent.putExtra(EXTRA_WALLPAPER_PATH, wallpaperPath);
        intent.putExtra(EXTRA_WALLPAPER_ID, wallpaperId);
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWallpaperViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
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
        binding.setWallpaperFab.setOnClickListener(v -> setWallpaper());
        binding.progressBar.setVisibility(View.VISIBLE);
    }
    
    private void loadWallpaper() {
        Glide.with(this)
                .asBitmap()
                .load(wallpaperPath)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        currentBitmap = resource;
                        binding.wallpaperImageView.setImageBitmap(resource);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.setWallpaperFab.setVisibility(View.VISIBLE);
                    }
                    
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle cleanup if needed
                    }
                    
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(WallpaperViewerActivity.this, "Failed to load wallpaper", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void setWallpaper() {
        if (currentBitmap == null) {
            Toast.makeText(this, "Wallpaper not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Start cropping activity
        Intent intent = new Intent(this, WallpaperCropActivity.class);
        intent.putExtra(WallpaperCropActivity.EXTRA_WALLPAPER_PATH, wallpaperPath);
        intent.putExtra(WallpaperCropActivity.EXTRA_WALLPAPER_ID, wallpaperId);
        startActivityForResult(intent, REQUEST_CROP_WALLPAPER);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CROP_WALLPAPER && resultCode == RESULT_OK) {
            Toast.makeText(this, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show();
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
