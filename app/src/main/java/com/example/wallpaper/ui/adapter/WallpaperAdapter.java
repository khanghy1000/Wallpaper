package com.example.wallpaper.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.databinding.ItemWallpaperBinding;

import java.util.ArrayList;
import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    
    private List<NetworkWallhavenWallpaper> wallpapers = new ArrayList<>();
    private OnWallpaperClickListener listener;
    private OnFavoriteClickListener favoriteClickListener;
    private FavoriteChecker favoriteChecker;
    private int itemWidth = -1; // Cache the calculated item width

    public interface OnWallpaperClickListener {
        void onWallpaperClick(NetworkWallhavenWallpaper wallpaper);
    }
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(NetworkWallhavenWallpaper wallpaper);
    }
    
    public interface FavoriteChecker {
        boolean isFavorite(NetworkWallhavenWallpaper wallpaper);
    }

    public void setOnWallpaperClickListener(OnWallpaperClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }
    
    public void setFavoriteChecker(FavoriteChecker checker) {
        this.favoriteChecker = checker;
    }
    
    public void setWallpapers(List<NetworkWallhavenWallpaper> wallpapers) {
        List<NetworkWallhavenWallpaper> newWallpapers = wallpapers != null ? wallpapers : new ArrayList<>();
        
        // Only update if the data has actually changed to prevent unnecessary refreshes
        if (!this.wallpapers.equals(newWallpapers)) {
            this.wallpapers = newWallpapers;
            notifyDataSetChanged();
        }
    }
    
    public void addWallpapers(List<NetworkWallhavenWallpaper> newWallpapers) {
        if (newWallpapers != null && !newWallpapers.isEmpty()) {
            int oldSize = this.wallpapers.size();
            this.wallpapers.addAll(newWallpapers);
            notifyItemRangeInserted(oldSize, newWallpapers.size());
        }
    }
    
    public void clearWallpapers() {
        this.wallpapers.clear();
        notifyDataSetChanged();
    }
    
    private int getItemWidth(View view) {
        if (itemWidth == -1) {
            int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (8 * view.getResources().getDisplayMetrics().density); // 4dp margin * 2 sides
            itemWidth = (screenWidth - padding * 3) / 2; // Divide by 2 columns, subtract margins
        }
        return itemWidth;
    }
    
    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWallpaperBinding binding = ItemWallpaperBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WallpaperViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        holder.bind(wallpapers.get(position));
    }
    
    @Override
    public int getItemCount() {
        return wallpapers.size();
    }
    
    class WallpaperViewHolder extends RecyclerView.ViewHolder {
        private final ItemWallpaperBinding binding;
        
        public WallpaperViewHolder(ItemWallpaperBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(NetworkWallhavenWallpaper wallpaper) {  
            // Calculate and set dimensions immediately to prevent layout shifts
            float ratio = wallpaper.getRatio();
            int width = getItemWidth(binding.getRoot());
            int height = (int) (width / ratio);
            
            // Set layout params immediately to prevent shifts
            android.view.ViewGroup.LayoutParams layoutParams = binding.wallpaperImage.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            binding.wallpaperImage.setLayoutParams(layoutParams);
            
            binding.wallpaperImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Load image with Glide
            Glide.with(binding.wallpaperImage.getContext())
                    .load(wallpaper.getThumbs().getOriginal())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.wallpaperImage);
            
            // Setup favorite button
            boolean isFavorite = favoriteChecker != null && favoriteChecker.isFavorite(wallpaper);
            if (isFavorite) {
                binding.favoriteButton.setImageResource(com.example.wallpaper.R.drawable.ic_favorite_filled_red);
                binding.favoriteButton.setContentDescription("Remove from favorites");
            } else {
                binding.favoriteButton.setImageResource(com.example.wallpaper.R.drawable.ic_favorite_border);
                binding.favoriteButton.setContentDescription("Add to favorites");
            }
            
            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWallpaperClick(wallpaper);
                }
            });
            
            binding.favoriteButton.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(wallpaper);
                }
            });
        }
    }
}
