package com.example.wallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
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
import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wallpaper.R;
import com.example.wallpaper.data.network.model.NetworkWallhavenTag;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.data.network.model.NetworkWallhavenUploader;
import com.example.wallpaper.databinding.ActivityWallpaperViewerBinding;
import com.example.wallpaper.databinding.BottomSheetWallpaperInfoBinding;
import com.example.wallpaper.model.LocalWallpaper;
import com.example.wallpaper.ui.viewmodel.WallpaperViewerViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WallpaperViewerActivity extends AppCompatActivity {
    
    private static final String EXTRA_WALLPAPER_PATH = "wallpaper_path";
    private static final String EXTRA_WALLPAPER_ID = "wallpaper_id";
    private static final int REQUEST_CROP_WALLPAPER = 1001;
    
    private ActivityWallpaperViewerBinding binding;
    private BottomSheetWallpaperInfoBinding bottomSheetBinding;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private WallpaperViewerViewModel viewModel;
    private String wallpaperPath;
    private String wallpaperId;
    private Bitmap currentBitmap;
    private OnBackPressedCallback onBackPressedCallback;
    private boolean isBottomSheetReady = false;
    
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
        bottomSheetBinding = BottomSheetWallpaperInfoBinding.bind(binding.getRoot().findViewById(R.id.bottomSheetContent));
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        viewModel = new ViewModelProvider(this).get(WallpaperViewerViewModel.class);
        
        getIntentData();
        setupUI();
        setupObservers();
        observeWallpaperInfo();
        loadWallpaper();
        setupBottomSheet();
        setupBackPressedHandler();
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
        
        viewModel.setWallpaperInfo(wallpaperId, wallpaperPath);
    }
    
    private void setupUI() {
        setSupportActionBar(binding.toolbar); 
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.infoButton.setOnClickListener(v -> showWallpaperInfo());
        binding.favoriteButton.setOnClickListener(v -> viewModel.toggleFavorite());
        binding.saveButton.setOnClickListener(v -> viewModel.saveToDevice());
        binding.applyWallpaperButton.setOnClickListener(v -> setWallpaper());
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Hide save button if local
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
        if (isBottomSheetReady && bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    
    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottomSheetContent);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        isBottomSheetReady = true;
        
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }
    
    private void setupBackPressedHandler() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomSheetBehavior != null && 
                    (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || 
                     bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                     bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED)) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
    
    private void observeWallpaperInfo() {
        viewModel.networkWallpaperInfo.observe(this, networkWallpaper -> {
            if (networkWallpaper != null) {
                populateNetworkWallpaperInfo(networkWallpaper);
            }
        });
        
        viewModel.localWallpaperInfo.observe(this, localWallpaper -> {
            if (localWallpaper != null) {
                populateLocalWallpaperInfo(localWallpaper);
            }
        });
    }
    
    private void populateNetworkWallpaperInfo(NetworkWallhavenWallpaper wallpaper) {
        // Show network info container, hide local info container
        bottomSheetBinding.networkInfoContainer.setVisibility(View.VISIBLE);
        bottomSheetBinding.localInfoContainer.setVisibility(View.GONE);
        
        // Populate network wallpaper data
        if (wallpaper.getTags() != null && !wallpaper.getTags().isEmpty()) {
            bottomSheetBinding.tagsContainer.setVisibility(View.VISIBLE);
            populateTagsChips(wallpaper.getTags());
        } else {
            bottomSheetBinding.tagsContainer.setVisibility(View.GONE);
        }
        
        bottomSheetBinding.sourceText.setText(wallpaper.getSourceUrl());
        bottomSheetBinding.imageUrlText.setText(wallpaper.getPath());
        
        if (wallpaper.getUploader() != null) {
            bottomSheetBinding.uploaderContainer.setVisibility(View.VISIBLE);
            setupUploaderChip(wallpaper.getUploader());
        } else {
            bottomSheetBinding.uploaderContainer.setVisibility(View.GONE);
        }
        
        bottomSheetBinding.categoryText.setText(capitalizeFirst(wallpaper.getCategory()));
        bottomSheetBinding.networkResolutionText.setText(wallpaper.getResolution());
        bottomSheetBinding.networkSizeText.setText(Formatter.formatFileSize(this, wallpaper.getFileSize()));
        bottomSheetBinding.viewsText.setText(String.valueOf(wallpaper.getViews()));
        bottomSheetBinding.favoritesText.setText(String.valueOf(wallpaper.getFavorites()));
    }
    
    private void setupUploaderChip(NetworkWallhavenUploader uploader) {
        Chip uploaderChip = bottomSheetBinding.uploaderChip;
        uploaderChip.setText(uploader.getUsername());
        
        // Set avatar
        if (uploader.getAvatar() != null && !uploader.getAvatar().isEmpty()) {
            String avatarUrl = null;
            if (uploader.getAvatar().containsKey("128px")) {
                avatarUrl = uploader.getAvatar().get("128px");
            } else if (uploader.getAvatar().containsKey("32px")) {
                avatarUrl = uploader.getAvatar().get("32px");
            } else if (uploader.getAvatar().containsKey("200px")) {
                avatarUrl = uploader.getAvatar().get("200px");
            }
            
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            uploaderChip.setChipIcon(resource);
                        }
                        
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            uploaderChip.setChipIcon(placeholder);
                        }
                        
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            uploaderChip.setChipIcon(errorDrawable);
                        }
                    });
            } else {
                uploaderChip.setChipIcon(getDrawable(R.drawable.baseline_account_circle_24));
            }
        } else {
            uploaderChip.setChipIcon(getDrawable(R.drawable.baseline_account_circle_24));
        }
        
        uploaderChip.setOnClickListener(v -> openSearchForUploader(uploader.getUsername()));
    }
    
    private void populateLocalWallpaperInfo(LocalWallpaper wallpaper) {
        bottomSheetBinding.localInfoContainer.setVisibility(View.VISIBLE);
        bottomSheetBinding.networkInfoContainer.setVisibility(View.GONE);
        
        bottomSheetBinding.localLocationText.setText(wallpaper.getPath());
        bottomSheetBinding.localResolutionText.setText(wallpaper.getResolution());
        bottomSheetBinding.localSizeText.setText(Formatter.formatFileSize(this, wallpaper.getFileSize()));
    }
    
    private void populateTagsChips(java.util.List<NetworkWallhavenTag> tags) {
        bottomSheetBinding.tagsChipGroup.removeAllViews();
        
        for (NetworkWallhavenTag tag : tags) {
            Chip chip = new Chip(this);
            chip.setText("#" + tag.getName());
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setOnClickListener(v -> openSearchForTag(tag.getName()));
            bottomSheetBinding.tagsChipGroup.addView(chip);
        }
    }
    
    private void openSearchForTag(String tagName) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        Intent intent = SearchFilterActivity.newIntentWithTag(this, tagName);
        startActivity(intent);
    }
    
    private void openSearchForUploader(String username) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        Intent intent = SearchFilterActivity.newIntentWithSearchQuery(this, "@" + username);
        startActivity(intent);
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
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
        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
        // Don't manually recycle bitmaps loaded by Glide
        // Glide manages bitmap lifecycle automatically
        binding = null;
        bottomSheetBinding = null;
    }
    
}
