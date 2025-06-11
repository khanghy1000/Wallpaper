package com.example.wallpaper.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wallpaper.databinding.FragmentCollectionsBinding;
import com.example.wallpaper.model.FavoriteWallpaper;
import com.example.wallpaper.ui.activity.WallpaperViewerActivity;
import com.example.wallpaper.ui.adapter.FavoriteWallpaperAdapter;
import com.example.wallpaper.ui.viewmodel.CollectionsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CollectionsFragment extends Fragment {

    private FragmentCollectionsBinding binding;
    private CollectionsViewModel viewModel;
    private FavoriteWallpaperAdapter adapter;

    public static CollectionsFragment newInstance() {
        return new CollectionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CollectionsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new FavoriteWallpaperAdapter();
        
        // Setup masonry layout with 2 columns
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);
        
        // Set click listeners
        adapter.setOnFavoriteWallpaperClickListener(new FavoriteWallpaperAdapter.OnFavoriteWallpaperClickListener() {
            @Override
            public void onFavoriteWallpaperClick(FavoriteWallpaper wallpaper) {
                // For now, since we don't have full wallpaper data, we'll show a toast
                // In a full implementation, you'd navigate to the wallpaper viewer
                Toast.makeText(requireContext(), 
                    "Favorite wallpaper clicked: " + wallpaper.getSourceId(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        adapter.setOnFavoriteToggleListener(new FavoriteWallpaperAdapter.OnFavoriteToggleListener() {
            @Override
            public void onFavoriteToggle(FavoriteWallpaper wallpaper) {
                viewModel.toggleFavorite(wallpaper);
            }
        });
    }

    private void observeViewModel() {
        viewModel.favoriteWallpapers.observe(getViewLifecycleOwner(), favoriteWallpapers -> {
            if (favoriteWallpapers != null) {
                adapter.setFavoriteWallpapers(favoriteWallpapers);
                
                // Show/hide empty state
                if (favoriteWallpapers.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyStateText.setVisibility(View.GONE);
                }
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}