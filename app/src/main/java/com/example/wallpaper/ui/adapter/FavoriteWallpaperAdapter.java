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
import com.example.wallpaper.model.FavoriteWallpaper;

import java.util.ArrayList;
import java.util.List;

public class FavoriteWallpaperAdapter extends RecyclerView.Adapter<FavoriteWallpaperAdapter.FavoriteWallpaperViewHolder> {
    
    private List<FavoriteWallpaper> favoriteWallpapers = new ArrayList<>();
    private OnFavoriteWallpaperClickListener listener;
    private OnFavoriteToggleListener favoriteToggleListener;
    private int itemWidth = -1;

    public interface OnFavoriteWallpaperClickListener {
        void onFavoriteWallpaperClick(FavoriteWallpaper wallpaper);
    }
    
    public interface OnFavoriteToggleListener {
        void onFavoriteToggle(FavoriteWallpaper wallpaper);
    }

    public void setOnFavoriteWallpaperClickListener(OnFavoriteWallpaperClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnFavoriteToggleListener(OnFavoriteToggleListener listener) {
        this.favoriteToggleListener = listener;
    }

    public void setFavoriteWallpapers(List<FavoriteWallpaper> favoriteWallpapers) {
        List<FavoriteWallpaper> newWallpapers = favoriteWallpapers != null ? favoriteWallpapers : new ArrayList<>();
        
        if (!this.favoriteWallpapers.equals(newWallpapers)) {
            this.favoriteWallpapers = newWallpapers;
            notifyDataSetChanged();
        }
    }

    public void clearFavoriteWallpapers() {
        this.favoriteWallpapers.clear();
        notifyDataSetChanged();
    }

    private int getItemWidth(View view) {
        if (itemWidth == -1) {
            int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (8 * view.getResources().getDisplayMetrics().density);
            itemWidth = (screenWidth - padding * 3) / 2;
        }
        return itemWidth;
    }

    @NonNull
    @Override
    public FavoriteWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWallpaperBinding binding = ItemWallpaperBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteWallpaperViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteWallpaperViewHolder holder, int position) {
        holder.bind(favoriteWallpapers.get(position));
    }

    @Override
    public int getItemCount() {
        return favoriteWallpapers.size();
    }

    class FavoriteWallpaperViewHolder extends RecyclerView.ViewHolder {
        private final ItemWallpaperBinding binding;

        public FavoriteWallpaperViewHolder(ItemWallpaperBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FavoriteWallpaper wallpaper) {
            // Calculate and set dimensions
            float ratio = wallpaper.getRatio();
            int width = getItemWidth(binding.getRoot());
            int height = (int) (width / ratio);

            // Set layout params
            ViewGroup.LayoutParams layoutParams = binding.wallpaperImage.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.wallpaperImage.setLayoutParams(layoutParams);

            binding.wallpaperImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Load image placeholder or thumb URL if available
            String thumbUrl = wallpaper.getThumbUrl();
            if (thumbUrl != null && !thumbUrl.isEmpty()) {
                Glide.with(binding.wallpaperImage.getContext())
                        .load(thumbUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.wallpaperImage);
            } else {
                // Set a placeholder or default image
                binding.wallpaperImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Set favorite button to filled red state (since all items in this adapter are favorites)
            binding.favoriteButton.setImageResource(com.example.wallpaper.R.drawable.ic_favorite_filled_red);
            binding.favoriteButton.setContentDescription("Remove from favorites");

            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteWallpaperClick(wallpaper);
                }
            });
            
            binding.favoriteButton.setOnClickListener(v -> {
                if (favoriteToggleListener != null) {
                    favoriteToggleListener.onFavoriteToggle(wallpaper);
                }
            });
        }
    }
}
