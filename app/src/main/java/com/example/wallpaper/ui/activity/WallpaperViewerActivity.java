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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wallpaper.R;
import com.example.wallpaper.databinding.ActivityWallpaperViewerBinding;
import com.example.wallpaper.ui.viewmodel.WallpaperViewerViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WallpaperViewerActivity extends AppCompatActivity {
    
    private static final String EXTRA_WALLPAPER_PATH = "wallpaper_path";
    private static final String EXTRA_WALLPAPER_ID = "wallpaper_id";
    private static final int REQUEST_CROP_WALLPAPER = 1001;
    
    private ActivityWallpaperViewerBinding binding;
    private WallpaperViewerViewModel viewModel;
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
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WallpaperViewerViewModel.class);
        
        getIntentData();
        setupUI();
        setupObservers();
        loadWallpaper();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        wallpaperPath = intent.getStringExtra(EXTRA_WALLPAPER_PATH);
        wallpaperId = intent.getStringExtra(EXTRA_WALLPAPER_ID);
        
        if (wallpaperPath == null || wallpaperPath.isEmpty()) {
            Toast.makeText(this, "Error: Invalid wallpaper", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set wallpaper info in ViewModel
        viewModel.setWallpaperInfo(wallpaperId, wallpaperPath);
    }
    
    private void setupUI() {
        setSupportActionBar(binding.toolbar); 
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // Set up button click listeners for the new BottomAppBar
        binding.infoButton.setOnClickListener(v -> showWallpaperInfo());
        binding.favoriteButton.setOnClickListener(v -> viewModel.toggleFavorite());
        binding.saveButton.setOnClickListener(v -> viewModel.saveToDevice());
        
        // Set up Apply Wallpaper Button
        binding.applyWallpaperButton.setOnClickListener(v -> setWallpaper());
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Show/hide save button based on wallpaper source
        binding.saveButton.setVisibility(viewModel.shouldShowSaveButton() ? View.VISIBLE : View.GONE);
    }
    
    private void setupObservers() {
        viewModel.isFavorite.observe(this, isFavorite -> {
            updateFavoriteButton(isFavorite);
        });
        
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateFavoriteButton(Boolean isFavorite) {
        if (isFavorite != null && isFavorite) {
            binding.favoriteButton.setIcon(getDrawable(R.drawable.ic_favorite_filled_red));
            binding.favoriteButton.setContentDescription(getString(R.string.remove_from_favorites));
        } else {
            binding.favoriteButton.setIcon(getDrawable(R.drawable.ic_favorite_border));
            binding.favoriteButton.setContentDescription(getString(R.string.add_to_favorites));
        }
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
                        binding.bottomActionContainer.setVisibility(View.VISIBLE);
                        binding.applyWallpaperButton.setVisibility(View.VISIBLE);
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
    
    private void showWallpaperInfo() {
        // Show wallpaper info dialog or sheet
        // For now, just show a simple toast
        String info = String.format("Wallpaper ID: %s\nPath: %s", wallpaperId, wallpaperPath);
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
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
