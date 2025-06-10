package com.example.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wallpaper.R;
import com.example.wallpaper.data.network.model.NetworkWallhavenWallpaper;
import com.example.wallpaper.databinding.FragmentSearchBinding;
import com.example.wallpaper.model.Order;
import com.example.wallpaper.model.Purity;
import com.example.wallpaper.model.search.WallhavenCategory;
import com.example.wallpaper.model.search.WallhavenRatio;
import com.example.wallpaper.model.search.WallhavenSorting;
import com.example.wallpaper.model.search.WallhavenTopRange;
import com.example.wallpaper.ui.activity.WallpaperViewerActivity;
import com.example.wallpaper.ui.adapter.WallpaperAdapter;
import com.example.wallpaper.ui.viewmodel.SearchViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private WallpaperAdapter adapter;
    private boolean isSwipeRefreshing = false;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        setupRecyclerView();
        setupClickListeners();
        setupKeyboardHandling();
        observeViewModel();
        handleIntentData();
    }

    private void setupUI() {
        setupCategoryChips();
        setupPurityChips();
        setupResolutionChips();
        setupRatioChips();
        setupDropdowns();
        setDefaultValues();
    }

    // Check if the search query is valid (either keyword or tags must not be empty)
    private boolean isSearchQueryValid() {
        String keyword = binding.etKeyword.getText() != null ? binding.etKeyword.getText().toString().trim() : "";
        String tags = binding.etTags.getText() != null ? binding.etTags.getText().toString().trim() : "";

        return !keyword.isEmpty() || !tags.isEmpty();
    }

    // Show error on search field if validation fails
    private void showSearchError() {
        binding.tilKeyword.setError("Please enter a search query or tags");
    }

    // Clear any displayed errors
    private void clearSearchError() {
        binding.tilKeyword.setError(null);
    }

    private void handleIntentData() {
        // Check if there's a search tag from the intent
        String searchTag = requireActivity().getIntent().getStringExtra("search_tag");
        if (searchTag != null && !searchTag.isEmpty()) {
            // Pre-fill the tags field with the search tag
            binding.etTags.setText(searchTag);
            
            // Automatically perform the search
            performSearch();
        }
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews();
        for (WallhavenCategory category : WallhavenCategory.values()) {
            Chip chip = new Chip(requireContext());
            chip.setText(category.getDisplayName());
            chip.setCheckable(true);
            chip.setTag(category);
            setupChipIcon(chip);
            binding.chipGroupCategories.addView(chip);
        }
    }

    private void setupPurityChips() {
        binding.chipGroupPurity.removeAllViews();
        for (Purity purity : Purity.values()) {
            Chip chip = new Chip(requireContext());
            chip.setText(purity.getDisplayName());
            chip.setCheckable(true);
            chip.setTag(purity);
            setupChipIcon(chip);
            binding.chipGroupPurity.addView(chip);
        }
    }

    private void setupResolutionChips() {
        binding.chipGroupResolutions.removeAllViews();
        List<String> commonResolutions = Arrays.asList(
                "1920x1080", "2560x1440", "3840x2160", "1366x768",
                "1600x900", "2048x1536", "1280x720"
        );

        for (String resolution : commonResolutions) {
            Chip chip = new Chip(requireContext());
            chip.setText(resolution);
            chip.setCheckable(true);
            chip.setTag(resolution);
            setupChipIcon(chip);
            binding.chipGroupResolutions.addView(chip);
        }
    }

    private void setupRatioChips() {
        binding.chipGroupRatios.removeAllViews();

        // Add category-based ratios
        for (WallhavenRatio.CategoryWallhavenRatio.Category category : WallhavenRatio.CategoryWallhavenRatio.Category.values()) {
            Chip chip = new Chip(requireContext());
            chip.setText(category.getDisplayName());
            chip.setCheckable(true);
            chip.setTag(WallhavenRatio.fromCategory(category));
            setupChipIcon(chip);
            binding.chipGroupRatios.addView(chip);
        }
    }

    private void setupDropdowns() {
        // Setup sorting dropdown
        List<String> sortingOptions = new ArrayList<>();
        for (WallhavenSorting sorting : WallhavenSorting.values()) {
            sortingOptions.add(sorting.getDisplayName());
        }
        ArrayAdapter<String> sortingAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                sortingOptions
        );
        binding.actvSorting.setAdapter(sortingAdapter);
        binding.actvSorting.setText(WallhavenSorting.RELEVANCE.getDisplayName(), false);

        // Setup TopRange dropdown (only visible when Top List is selected)
        List<String> topRangeOptions = new ArrayList<>();
        for (WallhavenTopRange topRange : WallhavenTopRange.values()) {
            topRangeOptions.add(getTopRangeDisplayName(topRange));
        }
        ArrayAdapter<String> topRangeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                topRangeOptions
        );
        binding.actvTopRange.setAdapter(topRangeAdapter);
        binding.actvTopRange.setText(getTopRangeDisplayName(WallhavenTopRange.ONE_MONTH), false);

        // Add listener to sorting dropdown to show/hide top range
        binding.actvSorting.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            boolean isTopList = WallhavenSorting.TOPLIST.getDisplayName().equals(selectedItem);
            binding.topRangeContainer.setVisibility(isTopList ? View.VISIBLE : View.GONE);
        });

        // Setup order dropdown
        List<String> orderOptions = new ArrayList<>();
        for (Order order : Order.values()) {
            orderOptions.add(order.getDisplayName());
        }
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                orderOptions
        );
        binding.actvOrder.setAdapter(orderAdapter);
        binding.actvOrder.setText(Order.DESC.getDisplayName(), false);
    }

    private void setDefaultValues() {
        // Set default category chips: all 3 categories (General, Anime, People)
        setChipChecked(binding.chipGroupCategories, WallhavenCategory.GENERAL, true);
        setChipChecked(binding.chipGroupCategories, WallhavenCategory.ANIME, true);
        setChipChecked(binding.chipGroupCategories, WallhavenCategory.PEOPLE, true);

        // Set default purity chip: SFW only
        setChipChecked(binding.chipGroupPurity, Purity.SFW, true);

        // Set initial visibility of top range container
        // Default sorting is RELEVANCE, so hide top range initially
        binding.topRangeContainer.setVisibility(View.GONE);
    }

    private void setChipChecked(ViewGroup chipGroup, Object tag, boolean checked) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (tag.equals(chip.getTag())) {
                chip.setChecked(checked);
                break;
            }
        }
    }

    private void setupChipIcon(Chip chip) {
        // Set initial icon state
        updateChipIcon(chip);

        // Add listener to update icon when checked state changes
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateChipIcon(chip);
        });
    }

    private void updateChipIcon(Chip chip) {
        if (chip.isChecked()) {
            chip.setChipIcon(getResources().getDrawable(R.drawable.baseline_check_24, requireContext().getTheme()));
        } else {
            chip.setChipIcon(null);
        }
    }

    private void setupRecyclerView() {
        adapter = new WallpaperAdapter();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);

        adapter.setOnWallpaperClickListener(new WallpaperAdapter.OnWallpaperClickListener() {
            @Override
            public void onWallpaperClick(NetworkWallhavenWallpaper wallpaper) {
                WallpaperViewerActivity.start(requireContext(), wallpaper.getPath(), wallpaper.getId());
            }
        });
        
        // Add infinite scrolling for search results
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0 && Boolean.TRUE.equals(viewModel.showResults.getValue())) { // Only check when scrolling down and showing results
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

    private void setupClickListeners() {
        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.btnClearFilters.setOnClickListener(v -> clearAllFilters());
        binding.btnFilter.setOnClickListener(v -> viewModel.hideResults());

        binding.swipeRefresh.setOnRefreshListener(() -> {
            isSwipeRefreshing = true;
            viewModel.refreshSearch();
        });

        // Setup toolbar navigation
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.showResults.getValue())) {
                // If showing results, go back to search form
                viewModel.hideResults();
            } else {
                // If showing search form, close activity
                requireActivity().finish();
            }
        });
    }

    private void setupKeyboardHandling() {
        // Set up WindowInsets listener to detect keyboard visibility
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Calculate keyboard height
            int keyboardHeight = imeInsets.bottom - systemInsets.bottom;

            // Adjust FAB margin based on keyboard visibility
            adjustFABMargin(keyboardHeight);

            return insets;
        });
    }

    private void adjustFABMargin(int keyboardHeight) {
        // Get layout params for both FABs
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams searchFabParams =
                (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) binding.btnSearch.getLayoutParams();
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams filterFabParams =
                (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) binding.btnFilter.getLayoutParams();

        // Convert 16dp to pixels for default margin
        int defaultMarginPx = (int) (16 * getResources().getDisplayMetrics().density);

        if (keyboardHeight > 0) {
            // Keyboard is visible - add keyboard height to bottom margin
            int newBottomMargin = defaultMarginPx + keyboardHeight;
            searchFabParams.bottomMargin = newBottomMargin;
            filterFabParams.bottomMargin = newBottomMargin;
        } else {
            // Keyboard is hidden - use default margin
            searchFabParams.bottomMargin = defaultMarginPx;
            filterFabParams.bottomMargin = defaultMarginPx;
        }

        // Apply the updated layout params
        binding.btnSearch.setLayoutParams(searchFabParams);
        binding.btnFilter.setLayoutParams(filterFabParams);
    }

    private void hideKeyboard() {
        View currentFocus = requireActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private void performSearch() {
        // Hide keyboard first
        hideKeyboard();
        
        String keyword = binding.etKeyword.getText() != null ? binding.etKeyword.getText().toString().trim() : "";
        String tags = binding.etTags.getText() != null ? binding.etTags.getText().toString().trim() : "";

        // Check if both keyword and tags are empty
        if (binding.actvSorting.getText().toString()
                .equals(WallhavenSorting.RELEVANCE.getDisplayName()) && !isSearchQueryValid()) {
            showSearchError(); // Only show error when search button is clicked
            Toast.makeText(requireContext(), "Please enter a search query or tags", Toast.LENGTH_SHORT).show();
            return;
        } else {
            clearSearchError(); // Clear any existing error
        }

        List<WallhavenCategory> selectedCategories = getSelectedCategories();
        List<Purity> selectedPurities = getSelectedPurities();
        WallhavenSorting selectedSorting = getSelectedSorting();
        Order selectedOrder = getSelectedOrder();

        // Only get topRange if sorting is TOPLIST
        WallhavenTopRange selectedTopRange = null;
        if (selectedSorting == WallhavenSorting.TOPLIST) {
            selectedTopRange = getSelectedTopRange();
        }

        String minWidth = binding.etMinWidth.getText() != null ? binding.etMinWidth.getText().toString().trim() : "";
        String minHeight = binding.etMinHeight.getText() != null ? binding.etMinHeight.getText().toString().trim() : "";

        List<String> selectedResolutions = getSelectedResolutions();
        List<WallhavenRatio> selectedRatios = getSelectedRatios();

        viewModel.searchWallpapers(keyword, tags, selectedCategories, selectedPurities,
                selectedSorting, selectedOrder, minWidth, minHeight,
                selectedResolutions, selectedRatios, selectedTopRange);
    }

    private List<WallhavenCategory> getSelectedCategories() {
        List<WallhavenCategory> selected = new ArrayList<>();
        for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupCategories.getChildAt(i);
            if (chip.isChecked()) {
                selected.add((WallhavenCategory) chip.getTag());
            }
        }
        return selected;
    }

    private List<Purity> getSelectedPurities() {
        List<Purity> selected = new ArrayList<>();
        for (int i = 0; i < binding.chipGroupPurity.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupPurity.getChildAt(i);
            if (chip.isChecked()) {
                selected.add((Purity) chip.getTag());
            }
        }
        return selected;
    }

    private WallhavenSorting getSelectedSorting() {
        String selectedText = binding.actvSorting.getText().toString();
        for (WallhavenSorting sorting : WallhavenSorting.values()) {
            if (sorting.getDisplayName().equals(selectedText)) {
                return sorting;
            }
        }
        return WallhavenSorting.DATE_ADDED; // Default
    }

    private Order getSelectedOrder() {
        String selectedText = binding.actvOrder.getText().toString();
        for (Order order : Order.values()) {
            if (order.getDisplayName().equals(selectedText)) {
                return order;
            }
        }
        return Order.DESC; // Default
    }

    private WallhavenTopRange getSelectedTopRange() {
        String selected = binding.actvTopRange.getText().toString();

        if (selected.equals("1 Day")) return WallhavenTopRange.ONE_DAY;
        if (selected.equals("3 Days")) return WallhavenTopRange.THREE_DAYS;
        if (selected.equals("1 Week")) return WallhavenTopRange.ONE_WEEK;
        if (selected.equals("1 Month")) return WallhavenTopRange.ONE_MONTH;
        if (selected.equals("3 Months")) return WallhavenTopRange.THREE_MONTHS;
        if (selected.equals("6 Months")) return WallhavenTopRange.SIX_MONTHS;
        if (selected.equals("1 Year")) return WallhavenTopRange.ONE_YEAR;

        return WallhavenTopRange.ONE_MONTH; // Default
    }

    private List<String> getSelectedResolutions() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < binding.chipGroupResolutions.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupResolutions.getChildAt(i);
            if (chip.isChecked()) {
                selected.add((String) chip.getTag());
            }
        }
        return selected;
    }

    private List<WallhavenRatio> getSelectedRatios() {
        List<WallhavenRatio> selected = new ArrayList<>();
        for (int i = 0; i < binding.chipGroupRatios.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupRatios.getChildAt(i);
            if (chip.isChecked()) {
                selected.add((WallhavenRatio) chip.getTag());
            }
        }
        return selected;
    }

    private void clearAllFilters() {
        hideKeyboard();

        binding.etKeyword.setText("");
        binding.etTags.setText("");
        binding.etMinWidth.setText("");
        binding.etMinHeight.setText("");

        // Clear any error state
        clearSearchError();

        // Clear all chips
        clearChipGroup(binding.chipGroupCategories);
        clearChipGroup(binding.chipGroupPurity);
        clearChipGroup(binding.chipGroupResolutions);
        clearChipGroup(binding.chipGroupRatios);

        // Reset dropdowns and chips to default values
        binding.actvSorting.setText(WallhavenSorting.RELEVANCE.getDisplayName(), false);
        binding.actvOrder.setText(Order.DESC.getDisplayName(), false);
        binding.actvTopRange.setText(getTopRangeDisplayName(WallhavenTopRange.ONE_MONTH), false);

        // Update TopRange visibility based on current sorting
        binding.topRangeContainer.setVisibility(View.GONE);

        // Set default values after clearing
        setDefaultValues();

        viewModel.clearFilters();

        Toast.makeText(requireContext(), "Filters Reset", Toast.LENGTH_SHORT).show();
    }

    private void clearChipGroup(ViewGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setChecked(false);
        }
    }

    private void observeViewModel() {
        viewModel.wallpapers.observe(getViewLifecycleOwner(), wallpapers -> {
            if (wallpapers != null) {
                adapter.setWallpapers(wallpapers);
                updateUIState(wallpapers.isEmpty());
            } else {
                // Loading state - clear adapter and show loading UI
                adapter.setWallpapers(new ArrayList<>());
                updateUIState(false); // Pass false because we're in loading state, not empty state
                
                // Ensure center loading indicator is visible during loading state
                if (Boolean.TRUE.equals(viewModel.showResults.getValue()) && 
                    Boolean.TRUE.equals(viewModel.loading.getValue()) && 
                    !isSwipeRefreshing) {
                    binding.progressIndicator.setIndeterminate(true);
                    binding.progressIndicator.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            // Only show center loading when switching from filter to results (when results are showing)
            // Don't show center loading during swipe refresh
            if (Boolean.TRUE.equals(viewModel.showResults.getValue())) {
                // Show center loading indicator only if not doing swipe refresh AND wallpapers is null (loading state)
                boolean isLoadingState = loading && !isSwipeRefreshing && viewModel.wallpapers.getValue() == null;
                if (isLoadingState) {
                    binding.progressIndicator.setIndeterminate(true);
                    showProgressIndicatorAnimated();
                } else {
                    hideProgressIndicatorAnimated();
                }
                // Handle swipe refresh loading
                binding.swipeRefresh.setRefreshing(loading && isSwipeRefreshing);
            } else {
                // Hide center loading when not showing results (in filter UI)
                hideProgressIndicatorAnimated();
                binding.swipeRefresh.setRefreshing(false);
            }
            
            // Reset swipe refresh flag when loading completes
            if (!loading) {
                isSwipeRefreshing = false;
            }
        });

        viewModel.loadingMore.observe(getViewLifecycleOwner(), loadingMore -> {
            // You can add a loading indicator at the bottom if needed
            // For now, we just observe it to prevent multiple simultaneous requests
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.showResults.observe(getViewLifecycleOwner(), showResults -> {
            if (showResults) {
                // Animate from search form to results container
                animateToResults();
            } else {
                // Animate from results container to search form
                animateToSearchForm();
            }
        });
    }

    private String getTopRangeDisplayName(WallhavenTopRange topRange) {
        switch (topRange) {
            case ONE_DAY:
                return "1 Day";
            case THREE_DAYS:
                return "3 Days";
            case ONE_WEEK:
                return "1 Week";
            case ONE_MONTH:
                return "1 Month";
            case THREE_MONTHS:
                return "3 Months";
            case SIX_MONTHS:
                return "6 Months";
            case ONE_YEAR:
                return "1 Year";
            default:
                return topRange.name();
        }
    }

    private void updateUIState(boolean isEmpty) {
        // Only manage empty state when showing results
        if (Boolean.TRUE.equals(viewModel.showResults.getValue())) {
            // Check if wallpapers is null (loading state)
            List<NetworkWallhavenWallpaper> wallpapers = viewModel.wallpapers.getValue();
            if (wallpapers == null) {
                // Loading state - hide both empty state and RecyclerView, center loading will show
                binding.emptyState.setVisibility(View.GONE);
                binding.swipeRefresh.setVisibility(View.GONE);
            } else if (isEmpty) {
                // Show empty state, hide RecyclerView
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.swipeRefresh.setVisibility(View.GONE);
            } else {
                // Show RecyclerView, hide empty state
                binding.emptyState.setVisibility(View.GONE);
                binding.swipeRefresh.setVisibility(View.VISIBLE);
            }
        } else {
            // When not showing results (in filter UI), hide both empty state and RecyclerView
            binding.emptyState.setVisibility(View.GONE);
            binding.swipeRefresh.setVisibility(View.GONE);
        }
    }
    
    private void animateToResults() {
        // Duration for animations
        int animDuration = 300;
        
        // First, set up the results container for the animation
        binding.resultsContainer.setVisibility(View.VISIBLE);
        binding.resultsContainer.setAlpha(0f);
        binding.resultsContainer.setTranslationX(binding.getRoot().getWidth());
        
        // Animate search form out (slide left and fade out)
        binding.searchForm.animate()
                .alpha(0f)
                .translationX(-binding.getRoot().getWidth())
                .setDuration(animDuration)
                .withEndAction(() -> {
                    binding.searchForm.setVisibility(View.GONE);
                    // Reset translation for next time
                    binding.searchForm.setTranslationX(0);
                    binding.searchForm.setAlpha(1f);
                })
                .start();
        
        // Animate results container in (slide from right and fade in)
        binding.resultsContainer.animate()
                .alpha(1f)
                .translationX(0)
                .setDuration(animDuration)
                .withStartAction(() -> {
                    // Update UI elements at the start of animation
                    updateToolbarForResults();
                    updateFABsForResults();
                })
                .withEndAction(() -> {
                    // Finalize results view setup
                    finalizeResultsView();
                })
                .start();
    }
    
    private void animateToSearchForm() {
        // Duration for animations
        int animDuration = 300;
        
        // First, set up the search form for the animation
        binding.searchForm.setVisibility(View.VISIBLE);
        binding.searchForm.setAlpha(0f);
        binding.searchForm.setTranslationX(-binding.getRoot().getWidth());
        
        // Animate results container out (slide right and fade out)
        binding.resultsContainer.animate()
                .alpha(0f)
                .translationX(binding.getRoot().getWidth())
                .setDuration(animDuration)
                .withEndAction(() -> {
                    binding.resultsContainer.setVisibility(View.GONE);
                    // Reset translation for next time
                    binding.resultsContainer.setTranslationX(0);
                    binding.resultsContainer.setAlpha(1f);
                })
                .start();
        
        // Animate search form in (slide from left and fade in)
        binding.searchForm.animate()
                .alpha(1f)
                .translationX(0)
                .setDuration(animDuration)
                .withStartAction(() -> {
                    // Update UI elements at the start of animation
                    updateToolbarForSearchForm();
                    updateFABsForSearchForm();
                })
                .withEndAction(() -> {
                    // Finalize search form view setup
                    finalizeSearchFormView();
                })
                .start();
    }
    
    private void updateToolbarForResults() {
        // Animate toolbar title change
        binding.toolbar.animate()
                .alpha(0.5f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.toolbar.setTitle(R.string.search_results);
                    binding.toolbar.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }
    
    private void updateToolbarForSearchForm() {
        // Animate toolbar title change
        binding.toolbar.animate()
                .alpha(0.5f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.toolbar.setTitle(R.string.search_wallpapers);
                    binding.toolbar.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }
    
    private void updateFABsForResults() {
        // Animate FABs with slight delay for better visual effect
        binding.btnSearch.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.btnSearch.setVisibility(View.GONE);
                    binding.btnSearch.setAlpha(1f);
                    binding.btnSearch.setScaleX(1f);
                    binding.btnSearch.setScaleY(1f);
                })
                .start();
                
        binding.btnClearFilters.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.btnClearFilters.setVisibility(View.GONE);
                    binding.btnClearFilters.setAlpha(1f);
                })
                .start();
        
        // Show filter button with animation
        binding.btnFilter.setVisibility(View.VISIBLE);
        binding.btnFilter.setAlpha(0f);
        binding.btnFilter.setScaleX(0.8f);
        binding.btnFilter.setScaleY(0.8f);
        binding.btnFilter.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setStartDelay(100)
                .start();
    }
    
    private void updateFABsForSearchForm() {
        // Animate filter FAB out
        binding.btnFilter.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.btnFilter.setVisibility(View.GONE);
                    binding.btnFilter.setAlpha(1f);
                    binding.btnFilter.setScaleX(1f);
                    binding.btnFilter.setScaleY(1f);
                })
                .start();
        
        // Show search and clear buttons with animation
        binding.btnSearch.setVisibility(View.VISIBLE);
        binding.btnSearch.setAlpha(0f);
        binding.btnSearch.setScaleX(0.8f);
        binding.btnSearch.setScaleY(0.8f);
        binding.btnSearch.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setStartDelay(100)
                .start();
                
        binding.btnClearFilters.setVisibility(View.VISIBLE);
        binding.btnClearFilters.setAlpha(0f);
        binding.btnClearFilters.animate()
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(100)
                .start();
    }
    
    private void finalizeResultsView() {
        // Scroll to top when showing results
        binding.recyclerView.scrollToPosition(0);
        
        // Update UI state for results view
        List<NetworkWallhavenWallpaper> wallpapers = viewModel.wallpapers.getValue();
        if (wallpapers != null) {
            updateUIState(wallpapers.isEmpty());
        } else {
            updateUIState(false); // Loading state
            // Explicitly show center loading when transitioning to results with null wallpapers
            if (Boolean.TRUE.equals(viewModel.loading.getValue()) && !isSwipeRefreshing) {
                showProgressIndicatorAnimated();
            }
        }
    }
    
    private void finalizeSearchFormView() {
        // Hide progress indicator when going back to filter UI
        hideProgressIndicatorAnimated();
        
        // Reset swipe refresh state
        isSwipeRefreshing = false;
        binding.swipeRefresh.setRefreshing(false);
        
        // Update UI state for filter view (this will hide empty state and RecyclerView)
        updateUIState(false);
    }
    
    private void showProgressIndicatorAnimated() {
        if (binding.progressIndicator.getVisibility() != View.VISIBLE) {
            binding.progressIndicator.setVisibility(View.VISIBLE);
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
    
    private void hideProgressIndicatorAnimated() {
        if (binding.progressIndicator.getVisibility() == View.VISIBLE) {
            binding.progressIndicator.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        binding.progressIndicator.setVisibility(View.GONE);
                        binding.progressIndicator.setAlpha(1f);
                        binding.progressIndicator.setScaleX(1f);
                        binding.progressIndicator.setScaleY(1f);
                    })
                    .start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
