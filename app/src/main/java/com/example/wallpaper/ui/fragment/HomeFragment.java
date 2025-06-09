package com.example.wallpaper.ui.fragment;

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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.databinding.FragmentHomeBinding;
import com.example.wallpaper.ui.activity.WallpaperViewerActivity;
import com.example.wallpaper.ui.adapter.WallpaperAdapter;
import com.example.wallpaper.ui.viewmodel.HomeViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private WallpaperAdapter adapter;

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
        adapter = new WallpaperAdapter();
        
        // Setup masonry layout with 2 columns for optimal masonry effect
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Prevent items from moving between spans to reduce layout shifts
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        
        // Disable item animator to prevent visual glitches during scrolling
        binding.recyclerView.setItemAnimator(null);
        
        // Set wallpaper click listener
        adapter.setOnWallpaperClickListener(new WallpaperAdapter.OnWallpaperClickListener() {
            @Override
            public void onWallpaperClick(NetworkWallhavenWallpaper wallpaper) {
                // Navigate to wallpaper viewer
                WallpaperViewerActivity.start(requireContext(), wallpaper.getPath(), wallpaper.getId());
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
            adapter.setWallpapers(wallpapers);
        });

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            binding.swipeRefresh.setRefreshing(loading);
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