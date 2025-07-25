package com.example.wallpaper.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wallpaper.data.network.model.NetworkWallhavenTag;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.databinding.FragmentHomeBinding;
import com.example.wallpaper.ui.activity.SearchFilterActivity;
import com.example.wallpaper.ui.activity.WallpaperViewerActivity;
import com.example.wallpaper.ui.adapter.HomeAdapter;
import com.example.wallpaper.ui.viewmodel.HomeViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private HomeAdapter adapter;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearchFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter();
        
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        
        binding.recyclerView.setItemAnimator(null);
        
        adapter.setOnWallpaperClickListener(new HomeAdapter.OnWallpaperClickListener() {
            @Override
            public void onWallpaperClick(NetworkWallhavenWallpaper wallpaper) {
                WallpaperViewerActivity.start(requireContext(), wallpaper.getPath(), wallpaper.getId());
            }
        });
        
        adapter.setOnTagClickListener(new HomeAdapter.OnTagClickListener() {
            @Override
            public void onTagClick(NetworkWallhavenTag tag) {
                // Navigate to SearchFilterActivity with the selected tag
                Intent intent = SearchFilterActivity.newIntentWithTag(requireContext(), tag.getName());
                startActivity(intent);
            }
        });
        
        adapter.setOnFavoriteClickListener(new HomeAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(NetworkWallhavenWallpaper wallpaper) {
                viewModel.toggleFavorite(wallpaper);
            }
        });
        
        adapter.setFavoriteChecker(new HomeAdapter.FavoriteChecker() {
            @Override
            public boolean isFavorite(NetworkWallhavenWallpaper wallpaper) {
                return viewModel.isFavorite(wallpaper);
            }
        });
        
        // Add infinite scrolling
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0) { // Only check when scrolling down
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        // Get the last visible item positions for both spans
                        int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
                        int lastVisibleItem = Math.max(lastVisibleItemPositions[0], lastVisibleItemPositions[1]);
                        int totalItemCount = layoutManager.getItemCount();
                        
                        // Load more when near the end (5 items before the end)
                        if (lastVisibleItem >= totalItemCount - 5 && 
                            !Boolean.TRUE.equals(viewModel.loading.getValue()) &&
                            !Boolean.TRUE.equals(viewModel.loadingMore.getValue())) {
                            viewModel.loadMoreWallpapers();
                        }
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshWallpapers();
        });
    }

    private void setupSearchFab() {
        binding.fabSearch.setOnClickListener(v -> {
            startActivity(SearchFilterActivity.newIntent(requireContext()));
        });
    }

    private void observeViewModel() {
        viewModel.wallpapers.observe(getViewLifecycleOwner(), wallpapers -> {
            adapter.setWallpapers(wallpapers);
            
            if (Boolean.TRUE.equals(viewModel.loading.getValue()) && wallpapers != null && !wallpapers.isEmpty()) {
                binding.recyclerView.scrollToPosition(0);
            }
        });

        viewModel.popularTags.observe(getViewLifecycleOwner(), tags -> {
            boolean hadTags = adapter.isShowingPopularTags();
            
            adapter.setPopularTags(tags);
            
            // If we just added popular tags for the first time and there are wallpapers,
            // scroll to top to ensure tags are visible
            if (!hadTags && tags != null && !tags.isEmpty() && adapter.getItemCount() > 1) {
                binding.recyclerView.scrollToPosition(0);
            }
        });

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            binding.swipeRefresh.setRefreshing(loading);
        });

        viewModel.loadingMore.observe(getViewLifecycleOwner(), loadingMore -> {
        });

        viewModel.loadingTags.observe(getViewLifecycleOwner(), loadingTags -> {
        });

        viewModel.favoriteIds.observe(getViewLifecycleOwner(), favoriteIds -> {
            adapter.notifyDataSetChanged();
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e("HomeFragment", "Error loading wallpapers: " + error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}