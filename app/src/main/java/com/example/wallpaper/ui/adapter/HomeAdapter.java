package com.example.wallpaper.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wallpaper.data.network.model.NetworkWallhavenTag;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.databinding.ItemPopularTagsHeaderBinding;
import com.example.wallpaper.databinding.ItemWallpaperBinding;
import com.example.wallpaper.ui.adapter.PopularTagsAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_POPULAR_TAGS = 0;
    private static final int VIEW_TYPE_WALLPAPER = 1;
    
    private List<NetworkWallhavenWallpaper> wallpapers = new ArrayList<>();
    private List<NetworkWallhavenTag> popularTags = new ArrayList<>();
    private boolean showPopularTags = false;
    
    private OnWallpaperClickListener wallpaperClickListener;
    private OnTagClickListener tagClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private int itemWidth = -1; // Cache the calculated item width
    
    public interface OnWallpaperClickListener {
        void onWallpaperClick(NetworkWallhavenWallpaper wallpaper);
    }
    
    public interface OnTagClickListener {
        void onTagClick(NetworkWallhavenTag tag);
    }
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(NetworkWallhavenWallpaper wallpaper);
    }
    
    public interface FavoriteChecker {
        boolean isFavorite(NetworkWallhavenWallpaper wallpaper);
    }
    
    private FavoriteChecker favoriteChecker;
    
    public void setOnWallpaperClickListener(OnWallpaperClickListener listener) {
        this.wallpaperClickListener = listener;
    }
    
    public void setOnTagClickListener(OnTagClickListener listener) {
        this.tagClickListener = listener;
    }
    
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }
    
    public void setFavoriteChecker(FavoriteChecker checker) {
        this.favoriteChecker = checker;
    }
    
    public boolean isShowingPopularTags() {
        return showPopularTags;
    }
    
    public void setWallpapers(List<NetworkWallhavenWallpaper> wallpapers) {
        List<NetworkWallhavenWallpaper> newWallpapers = wallpapers != null ? wallpapers : new ArrayList<>();
        
        // If the lists are identical, don't do anything
        if (this.wallpapers.equals(newWallpapers)) {
            return;
        }
        
        int oldSize = this.wallpapers.size();
        int newSize = newWallpapers.size();
        
        // Check if this looks like pagination (new list contains all old items plus more)
        boolean isPagination = newSize > oldSize && 
                               oldSize > 0 && 
                               newWallpapers.subList(0, oldSize).equals(this.wallpapers);
        
        this.wallpapers = newWallpapers;
        
        if (isPagination) {
            // This is pagination - just add the new items
            int startPosition = (showPopularTags ? 1 : 0) + oldSize;
            int addedCount = newSize - oldSize;
            notifyItemRangeInserted(startPosition, addedCount);
        } else {
            // This is a complete refresh - use notifyDataSetChanged only for wallpapers section
            if (oldSize == 0 && newSize > 0) {
                // First load
                int startPosition = showPopularTags ? 1 : 0;
                notifyItemRangeInserted(startPosition, newSize);
            } else if (newSize == 0 && oldSize > 0) {
                // Clear all
                int startPosition = showPopularTags ? 1 : 0;
                notifyItemRangeRemoved(startPosition, oldSize);
            } else {
                // Complete refresh - but only notify wallpaper items, not the header
                int startPosition = showPopularTags ? 1 : 0;
                if (oldSize > 0) {
                    notifyItemRangeRemoved(startPosition, oldSize);
                }
                if (newSize > 0) {
                    notifyItemRangeInserted(startPosition, newSize);
                }
            }
        }
    }
    
    public void setPopularTags(List<NetworkWallhavenTag> tags) {
        List<NetworkWallhavenTag> newTags = tags != null ? tags : new ArrayList<>();
        boolean shouldShow = !newTags.isEmpty();
        
        // Check if tags actually changed to avoid unnecessary updates
        if (this.popularTags.equals(newTags) && showPopularTags == shouldShow) {
            return; // No change needed
        }
        
        this.popularTags = newTags;
        
        if (showPopularTags != shouldShow) {
            showPopularTags = shouldShow;
            if (shouldShow) {
                notifyItemInserted(0); // Header inserted at position 0
            } else {
                notifyItemRemoved(0); // Header removed from position 0
            }
        } else if (shouldShow) {
            notifyItemChanged(0); // Update existing header
        }
    }
    
    public void clearWallpapers() {
        int oldWallpaperCount = this.wallpapers.size();
        this.wallpapers.clear();
        if (oldWallpaperCount > 0) {
            int startPosition = showPopularTags ? 1 : 0;
            notifyItemRangeRemoved(startPosition, oldWallpaperCount);
        }
    }
    
    private int getItemWidth(View view) {
        if (itemWidth == -1) {
            int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (8 * view.getResources().getDisplayMetrics().density); // 4dp margin * 2 sides
            itemWidth = (screenWidth - padding * 3) / 2; // Divide by 2 columns, subtract margins
        }
        return itemWidth;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (showPopularTags && position == 0) {
            return VIEW_TYPE_POPULAR_TAGS;
        }
        return VIEW_TYPE_WALLPAPER;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POPULAR_TAGS) {
            ItemPopularTagsHeaderBinding binding = ItemPopularTagsHeaderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new PopularTagsViewHolder(binding);
        } else {
            ItemWallpaperBinding binding = ItemWallpaperBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new WallpaperViewHolder(binding);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PopularTagsViewHolder) {
            ((PopularTagsViewHolder) holder).bind(popularTags);
        } else if (holder instanceof WallpaperViewHolder) {
            int wallpaperPosition = showPopularTags ? position - 1 : position;
            ((WallpaperViewHolder) holder).bind(wallpapers.get(wallpaperPosition));
        }
    }
    
    @Override
    public int getItemCount() {
        int count = wallpapers.size();
        if (showPopularTags) {
            count += 1; // Add header
        }
        return count;
    }
    
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // Make sure the popular tags header spans full width in staggered grid
        if (holder instanceof PopularTagsViewHolder) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
        }
    }
    
    class PopularTagsViewHolder extends RecyclerView.ViewHolder {
        private final ItemPopularTagsHeaderBinding binding;
        private List<NetworkWallhavenTag> lastBoundTags = new ArrayList<>();
        
        public PopularTagsViewHolder(ItemPopularTagsHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(List<NetworkWallhavenTag> tags) {
            // Only update if tags have actually changed
            if (tags.equals(lastBoundTags)) {
                return; // No change needed
            }
            
            lastBoundTags = new ArrayList<>(tags);
            
            // Clear previous tags
            binding.linearPopularTags.removeAllViews();
            
            // Add each tag as a chip view
            for (NetworkWallhavenTag tag : tags) {
                com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(
                        binding.getRoot().getContext(),
                        null,
                        com.google.android.material.R.attr.chipStyle
                );
                chip.setText("#" + tag.getName());
                chip.setClickable(true);
                chip.setFocusable(true);
                
                // Remove close icon and make it non-checkable
                chip.setCloseIconVisible(false);
                chip.setCheckable(false);
                
                // Set margin for spacing between chips
                android.view.ViewGroup.MarginLayoutParams params = new android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMarginEnd((int) (8 * binding.getRoot().getContext().getResources().getDisplayMetrics().density));
                chip.setLayoutParams(params);
                
                chip.setOnClickListener(v -> {
                    if (tagClickListener != null) {
                        tagClickListener.onTagClick(tag);
                    }
                });
                
                binding.linearPopularTags.addView(chip);
            }
        }
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
                if (wallpaperClickListener != null) {
                    wallpaperClickListener.onWallpaperClick(wallpaper);
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
