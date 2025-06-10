package com.example.wallpaper.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wallpaper.databinding.ItemWallpaperBinding;
import com.example.wallpaper.model.LocalWallpaper;

import java.util.ArrayList;
import java.util.List;

public class LocalWallpaperAdapter extends RecyclerView.Adapter<LocalWallpaperAdapter.LocalWallpaperViewHolder> {
    
    private List<LocalWallpaper> wallpapers = new ArrayList<>();
    private OnWallpaperClickListener listener;
    private int itemWidth = -1; // Cache the calculated item width
    
    public interface OnWallpaperClickListener {
        void onWallpaperClick(LocalWallpaper wallpaper);
    }
    
    public void setOnWallpaperClickListener(OnWallpaperClickListener listener) {
        this.listener = listener;
    }
    
    public void setWallpapers(List<LocalWallpaper> wallpapers) {
        List<LocalWallpaper> newWallpapers = wallpapers != null ? wallpapers : new ArrayList<>();
        
        // Only update if the data has actually changed to prevent unnecessary refreshes
        if (!this.wallpapers.equals(newWallpapers)) {
            this.wallpapers = newWallpapers;
            notifyDataSetChanged();
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
    public LocalWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWallpaperBinding binding = ItemWallpaperBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LocalWallpaperViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LocalWallpaperViewHolder holder, int position) {
        holder.bind(wallpapers.get(position));
    }
    
    @Override
    public int getItemCount() {
        return wallpapers.size();
    }
    
    class LocalWallpaperViewHolder extends RecyclerView.ViewHolder {
        private final ItemWallpaperBinding binding;
        
        public LocalWallpaperViewHolder(ItemWallpaperBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(LocalWallpaper wallpaper) {  
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
            
            // Load image with Glide using the URI
            Glide.with(binding.wallpaperImage.getContext())
                    .load(wallpaper.getUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.wallpaperImage);
            
            // Set click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWallpaperClick(wallpaper);
                }
            });
        }
    }
}
