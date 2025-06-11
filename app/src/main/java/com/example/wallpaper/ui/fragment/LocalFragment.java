package com.example.wallpaper.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wallpaper.databinding.FragmentLocalBinding;
import com.example.wallpaper.model.LocalWallpaper;
import com.example.wallpaper.ui.activity.WallpaperViewerActivity;
import com.example.wallpaper.ui.adapter.LocalWallpaperAdapter;
import com.example.wallpaper.ui.viewmodel.LocalViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LocalFragment extends Fragment {

    private static final String TAG = "LocalFragment";
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    private FragmentLocalBinding binding;
    private LocalViewModel viewModel;
    private LocalWallpaperAdapter adapter;

    public LocalFragment() {
        // Required empty public constructor
    }

    public static LocalFragment newInstance() {
        return new LocalFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LocalViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLocalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSwipeRefresh();
        observeViewModel();
        checkPermissionsAndLoadWallpapers();
    }

    private void setupRecyclerView() {
        adapter = new LocalWallpaperAdapter();
        
        // Setup masonry layout with 2 columns for optimal masonry effect
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Prevent items from moving between spans to reduce layout shifts
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        
        // Disable item animator to prevent visual glitches during scrolling
        binding.recyclerView.setItemAnimator(null);
        
        // Set wallpaper click listener
        adapter.setOnWallpaperClickListener(new LocalWallpaperAdapter.OnWallpaperClickListener() {
            @Override
            public void onWallpaperClick(LocalWallpaper wallpaper) {
                // Navigate to wallpaper viewer with local image URI
                WallpaperViewerActivity.start(requireContext(), wallpaper.getUri().toString(), wallpaper.getId());
            }
        });
        
        // Set favorite click listener
        adapter.setOnFavoriteClickListener(new LocalWallpaperAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(LocalWallpaper wallpaper) {
                viewModel.toggleFavorite(wallpaper);
            }
        });
        
        // Set favorite checker
        adapter.setFavoriteChecker(new LocalWallpaperAdapter.FavoriteChecker() {
            @Override
            public boolean isFavorite(LocalWallpaper wallpaper) {
                return viewModel.isFavorite(wallpaper);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (hasStoragePermission()) {
                viewModel.refreshWallpapers();
            } else {
                binding.swipeRefresh.setRefreshing(false);
                requestStoragePermission();
            }
        });
    }

    private void observeViewModel() {
        viewModel.wallpapers.observe(getViewLifecycleOwner(), wallpapers -> {
            Log.d(TAG, "Received " + wallpapers.size() + " wallpapers");
            adapter.setWallpapers(wallpapers);
            updateEmptyState(wallpapers.isEmpty());
        });

        viewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
            binding.progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error loading wallpapers: " + error);
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe favorite IDs changes to refresh adapter
        viewModel.favoriteIds.observe(getViewLifecycleOwner(), favoriteIds -> {
            adapter.notifyDataSetChanged();
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void checkPermissionsAndLoadWallpapers() {
        if (hasStoragePermission()) {
            viewModel.loadLocalWallpapers();
        } else {
            requestStoragePermission();
        }
    }

    private boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        
        ActivityCompat.requestPermissions(requireActivity(), permissions, STORAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load wallpapers
                viewModel.loadLocalWallpapers();
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Storage permission is required to view local wallpapers", Toast.LENGTH_LONG).show();
                updateEmptyState(true);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}