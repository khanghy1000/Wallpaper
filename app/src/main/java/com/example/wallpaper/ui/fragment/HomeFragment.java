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
import com.example.wallpaper.ui.activity.SearchActivity;
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
        // Required empty public constructor
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
        
        // Setup masonry layout with 2 columns for optimal masonry effect
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Prevent items from moving between spans to reduce layout shifts
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        
        // Disable item animator to prevent visual glitches during scrolling
        binding.recyclerView.setItemAnimator(null);
        
        // Set wallpaper click listener
        adapter.setOnWallpaperClickListener(new HomeAdapter.OnWallpaperClickListener() {
            @Override
            public void onWallpaperClick(NetworkWallhavenWallpaper wallpaper) {
                // Navigate to wallpaper viewer
                WallpaperViewerActivity.start(requireContext(), wallpaper.getPath(), wallpaper.getId());
            }
        });
        
        // Set tag click listener
        adapter.setOnTagClickListener(new HomeAdapter.OnTagClickListener() {
            @Override
            public void onTagClick(NetworkWallhavenTag tag) {
                // Navigate to SearchActivity with the selected tag
                Intent intent = SearchActivity.newIntentWithTag(requireContext(), tag.getName());
                startActivity(intent);
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
                        
                        // Load more when we're near the end (5 items before the end)
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
            // Navigate to SearchActivity
            startActivity(com.example.wallpaper.ui.activity.SearchActivity.newIntent(requireContext()));
        });
    }

    private void observeViewModel() {
        viewModel.wallpapers.observe(getViewLifecycleOwner(), wallpapers -> {
            // Always use setWallpapers - the adapter will now handle pagination vs refresh intelligently
            adapter.setWallpapers(wallpapers);
            
            // Only scroll to top for complete refresh (when it's the first load or after refresh)
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
            // You can add a loading indicator at the bottom if needed
            // For now, we just observe it to prevent multiple simultaneous requests
        });

        viewModel.loadingTags.observe(getViewLifecycleOwner(), loadingTags -> {
            // You can add a loading indicator for tags if needed
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