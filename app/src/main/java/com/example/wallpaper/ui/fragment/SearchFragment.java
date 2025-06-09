package com.example.wallpaper.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
                Toast.makeText(getContext(), "Clicked: " + wallpaper.getId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.btnClearFilters.setOnClickListener(v -> clearAllFilters());
        binding.btnFilter.setOnClickListener(v -> viewModel.hideResults());
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
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

    private void performSearch() {
        String keyword = binding.etKeyword.getText() != null ? binding.etKeyword.getText().toString().trim() : "";
        String tags = binding.etTags.getText() != null ? binding.etTags.getText().toString().trim() : "";
        
        // Check if both keyword and tags are empty
        if (!isSearchQueryValid()) {
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
    }

    private void clearChipGroup(ViewGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setChecked(false);
        }
    }

    private void observeViewModel() {
        viewModel.wallpapers.observe(getViewLifecycleOwner(), wallpapers -> {
            adapter.setWallpapers(wallpapers);
            updateUIState(wallpapers.isEmpty());
        });

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            binding.swipeRefresh.setRefreshing(loading);
            binding.progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.showResults.observe(getViewLifecycleOwner(), showResults -> {
            if (showResults) {
                // Show results container, hide search form
                binding.searchForm.setVisibility(View.GONE);
                binding.resultsContainer.setVisibility(View.VISIBLE);
                
                // Update toolbar for results view
                binding.toolbar.setTitle(R.string.search_results);
                
                // Show filter button, hide apply/clear buttons
                binding.btnFilter.setVisibility(View.VISIBLE);
                binding.btnSearch.setVisibility(View.GONE);
                binding.btnClearFilters.setVisibility(View.GONE);
            } else {
                // Show search form, hide results container
                binding.searchForm.setVisibility(View.VISIBLE);
                binding.resultsContainer.setVisibility(View.GONE);

                // Update toolbar for search form
                binding.toolbar.setTitle(R.string.search_wallpapers);

                // Show apply/clear buttons, hide filter button
                binding.btnFilter.setVisibility(View.GONE);
                binding.btnSearch.setVisibility(View.VISIBLE);
                binding.btnClearFilters.setVisibility(View.VISIBLE);
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
        if (isEmpty && Boolean.TRUE.equals(viewModel.showResults.getValue())) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.swipeRefresh.setVisibility(View.GONE);
        } else if (Boolean.TRUE.equals(viewModel.showResults.getValue())) {
            binding.emptyState.setVisibility(View.GONE);
            binding.swipeRefresh.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
