package com.example.wallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wallpaper.R;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.databinding.ActivitySearchResultsBinding;
import com.example.wallpaper.model.Order;
import com.example.wallpaper.model.Purity;
import com.example.wallpaper.model.search.WallhavenCategory;
import com.example.wallpaper.model.search.WallhavenRatio;
import com.example.wallpaper.model.search.WallhavenSorting;
import com.example.wallpaper.model.search.WallhavenTopRange;
import com.example.wallpaper.ui.adapter.WallpaperAdapter;
import com.example.wallpaper.ui.viewmodel.SearchResultsViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchResultsActivity extends AppCompatActivity {

    private ActivitySearchResultsBinding binding;
    private SearchResultsViewModel viewModel;
    private WallpaperAdapter adapter;
    private boolean isSwipeRefreshing = false;

    public static Intent newIntent(Context context, String keyword, String tags,
                                  List<WallhavenCategory> categories, List<Purity> purities,
                                  WallhavenSorting sorting, Order order, WallhavenTopRange topRange,
                                  String minWidth, String minHeight, List<String> resolutions,
                                  List<WallhavenRatio> ratios) {
        Intent intent = new Intent(context, SearchResultsActivity.class);
        intent.putExtra("keyword", keyword);
        intent.putExtra("tags", tags);
        intent.putExtra("categories", new ArrayList<>(categories));
        intent.putExtra("purities", new ArrayList<>(purities));
        intent.putExtra("sorting", sorting);
        intent.putExtra("order", order);
        intent.putExtra("topRange", topRange);
        intent.putExtra("minWidth", minWidth);
        intent.putExtra("minHeight", minHeight);
        intent.putExtra("resolutions", new ArrayList<>(resolutions));
        intent.putParcelableArrayListExtra("ratios", new ArrayList<>(ratios));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(SearchResultsViewModel.class);

        setupUI();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        performSearchFromIntent();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_results);
        }
    }

    private void setupRecyclerView() {
        adapter = new WallpaperAdapter();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);

        adapter.setOnWallpaperClickListener(wallpaper -> 
            WallpaperViewerActivity.start(this, wallpaper.getPath(), wallpaper.getId())
        );
        
        adapter.setOnFavoriteClickListener(wallpaper -> {
            viewModel.toggleFavorite(wallpaper);
        });
        
        adapter.setFavoriteChecker(wallpaper -> {
            return viewModel.isFavorite(wallpaper);
        });

        // Add infinite scrolling
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // Only check when scrolling down
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
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

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnFilter.setOnClickListener(v -> {
            finish();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            isSwipeRefreshing = true;
            viewModel.refreshSearch();
        });
    }

    private void observeViewModel() {
        viewModel.wallpapers.observe(this, wallpapers -> {
            if (wallpapers != null) {
                adapter.setWallpapers(wallpapers);
                updateUIState(wallpapers.isEmpty());
            } else {
                // Loading state
                adapter.setWallpapers(new ArrayList<>());
                updateUIState(false);
            }
        });

        viewModel.loading.observe(this, loading -> {
            // Show center loading indicator only if not doing swipe refresh and wallpapers is null
            boolean isLoadingState = loading && !isSwipeRefreshing && viewModel.wallpapers.getValue() == null;
            if (isLoadingState) {
                binding.progressIndicator.setIndeterminate(true);
                showProgressIndicator();
            } else {
                hideProgressIndicator();
            }
            
            // Handle swipe refresh loading
            binding.swipeRefresh.setRefreshing(loading && isSwipeRefreshing);
            
            // Reset swipe refresh flag when loading completes
            if (!loading) {
                isSwipeRefreshing = false;
            }
        });

        viewModel.loadingMore.observe(this, loadingMore -> {
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.favoriteIds.observe(this, favoriteIds -> {
            // Notify adapter that favorite states might have changed
            adapter.notifyDataSetChanged();
        });
    }

    private void performSearchFromIntent() {
        Intent intent = getIntent();
        
        String keyword = intent.getStringExtra("keyword");
        String tags = intent.getStringExtra("tags");
        ArrayList<WallhavenCategory> categories = (ArrayList<WallhavenCategory>) intent.getSerializableExtra("categories");
        ArrayList<Purity> purities = (ArrayList<Purity>) intent.getSerializableExtra("purities");
        WallhavenSorting sorting = (WallhavenSorting) intent.getSerializableExtra("sorting");
        Order order = (Order) intent.getSerializableExtra("order");
        WallhavenTopRange topRange = (WallhavenTopRange) intent.getSerializableExtra("topRange");
        String minWidth = intent.getStringExtra("minWidth");
        String minHeight = intent.getStringExtra("minHeight");
        ArrayList<String> resolutions = (ArrayList<String>) intent.getSerializableExtra("resolutions");
        ArrayList<WallhavenRatio> ratios = intent.getParcelableArrayListExtra("ratios");

        viewModel.searchWallpapers(keyword, tags, categories, purities, sorting, order,
                minWidth, minHeight, resolutions, ratios, topRange);
    }

    private void updateUIState(boolean isEmpty) {
        List<NetworkWallhavenWallpaper> wallpapers = viewModel.wallpapers.getValue();
        if (wallpapers == null) {
            // Loading state - hide both empty state and RecyclerView, center loading will show
            binding.emptyState.setVisibility(android.view.View.GONE);
            binding.swipeRefresh.setVisibility(android.view.View.GONE);
        } else if (isEmpty) {
            // Show empty state, hide RecyclerView
            binding.emptyState.setVisibility(android.view.View.VISIBLE);
            binding.swipeRefresh.setVisibility(android.view.View.GONE);
        } else {
            // Show RecyclerView, hide empty state
            binding.emptyState.setVisibility(android.view.View.GONE);
            binding.swipeRefresh.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void showProgressIndicator() {
        if (binding.progressIndicator.getVisibility() != android.view.View.VISIBLE) {
            binding.progressIndicator.setVisibility(android.view.View.VISIBLE);
            binding.progressIndicator.setAlpha(0f);
            binding.progressIndicator.setScaleX(0.8f);
            binding.progressIndicator.setScaleY(0.8f);
            binding.progressIndicator.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        }
    }

    private void hideProgressIndicator() {
        if (binding.progressIndicator.getVisibility() == android.view.View.VISIBLE) {
            binding.progressIndicator.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        binding.progressIndicator.setVisibility(android.view.View.GONE);
                        binding.progressIndicator.setAlpha(1f);
                        binding.progressIndicator.setScaleX(1f);
                        binding.progressIndicator.setScaleY(1f);
                    })
                    .start();
        }
    }
}
