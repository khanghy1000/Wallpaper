package com.example.wallpaper.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallpaper.data.network.model.NetworkWallhavenTag;
import com.example.wallpaper.databinding.ItemPopularTagBinding;

import java.util.ArrayList;
import java.util.List;

public class PopularTagsAdapter extends RecyclerView.Adapter<PopularTagsAdapter.TagViewHolder> {
    
    private List<NetworkWallhavenTag> tags = new ArrayList<>();
    private OnTagClickListener listener;
    
    public interface OnTagClickListener {
        void onTagClick(NetworkWallhavenTag tag);
    }
    
    public void setOnTagClickListener(OnTagClickListener listener) {
        this.listener = listener;
    }
    
    public void setTags(List<NetworkWallhavenTag> tags) {
        List<NetworkWallhavenTag> newTags = tags != null ? tags : new ArrayList<>();
        
        if (!this.tags.equals(newTags)) {
            this.tags = newTags;
            notifyDataSetChanged();
        }
    }
    
    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPopularTagBinding binding = ItemPopularTagBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TagViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        holder.bind(tags.get(position));
    }
    
    @Override
    public int getItemCount() {
        return tags.size();
    }
    
    class TagViewHolder extends RecyclerView.ViewHolder {
        private final ItemPopularTagBinding binding;
        
        public TagViewHolder(ItemPopularTagBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(NetworkWallhavenTag tag) {
            binding.chipTag.setText("#" + tag.getName());
            
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTagClick(tag);
                }
            });
        }
    }
}
