package com.example.wallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wallpaper.R;
import com.example.wallpaper.databinding.ActivitySearchFilterBinding;
import com.example.wallpaper.model.Order;
import com.example.wallpaper.model.Purity;
import com.example.wallpaper.model.search.WallhavenCategory;
import com.example.wallpaper.model.search.WallhavenRatio;
import com.example.wallpaper.model.search.WallhavenSorting;
import com.example.wallpaper.model.search.WallhavenTopRange;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFilterActivity extends AppCompatActivity {

    private ActivitySearchFilterBinding binding;

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchFilterActivity.class);
    }

    public static Intent newIntentWithTag(Context context, String tag) {
        Intent intent = new Intent(context, SearchFilterActivity.class);
        intent.putExtra("search_tag", tag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySearchFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupClickListeners();
        setupKeyboardHandling();
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

    private void handleIntentData() {
        String searchTag = getIntent().getStringExtra("search_tag");
        if (searchTag != null && !searchTag.isEmpty()) {
            binding.etTags.setText(searchTag);
            // Auto-perform search
            performSearch();
        }
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews();
        for (WallhavenCategory category : WallhavenCategory.values()) {
            Chip chip = new Chip(this);
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
            Chip chip = new Chip(this);
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
            Chip chip = new Chip(this);
            chip.setText(resolution);
            chip.setCheckable(true);
            chip.setTag(resolution);
            setupChipIcon(chip);
            binding.chipGroupResolutions.addView(chip);
        }
    }

    private void setupRatioChips() {
        binding.chipGroupRatios.removeAllViews();

        for (WallhavenRatio.CategoryWallhavenRatio.Category category : WallhavenRatio.CategoryWallhavenRatio.Category.values()) {
            Chip chip = new Chip(this);
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
                this,
                android.R.layout.simple_dropdown_item_1line,
                sortingOptions
        );
        binding.actvSorting.setAdapter(sortingAdapter);
        binding.actvSorting.setText(WallhavenSorting.RELEVANCE.getDisplayName(), false);

        // Setup TopRange dropdown
        List<String> topRangeOptions = new ArrayList<>();
        for (WallhavenTopRange topRange : WallhavenTopRange.values()) {
            topRangeOptions.add(getTopRangeDisplayName(topRange));
        }
        ArrayAdapter<String> topRangeAdapter = new ArrayAdapter<>(
                this,
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
                this,
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
        updateChipIcon(chip);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateChipIcon(chip));
    }

    private void updateChipIcon(Chip chip) {
        if (chip.isChecked()) {
            chip.setChipIcon(getDrawable(R.drawable.baseline_check_24));
        } else {
            chip.setChipIcon(null);
        }
    }

    private void setupClickListeners() {
        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.btnClearFilters.setOnClickListener(v -> clearAllFilters());
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupKeyboardHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            // Handle system bars (status bar, navigation bar)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            
            // Handle keyboard (IME) insets
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int keyboardHeight = imeInsets.bottom - systemInsets.bottom;
            adjustFABMargin(keyboardHeight);

            return insets;
        });
    }

    private void adjustFABMargin(int keyboardHeight) {
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams searchFabParams =
                (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) binding.btnSearch.getLayoutParams();

        int defaultMarginPx = (int) (16 * getResources().getDisplayMetrics().density);

        if (keyboardHeight > 0) {
            int newBottomMargin = defaultMarginPx + keyboardHeight;
            searchFabParams.bottomMargin = newBottomMargin;
        } else {
            searchFabParams.bottomMargin = defaultMarginPx;
        }

        binding.btnSearch.setLayoutParams(searchFabParams);
    }

    private boolean isSearchQueryValid() {
        String keyword = binding.etKeyword.getText() != null ? binding.etKeyword.getText().toString().trim() : "";
        String tags = binding.etTags.getText() != null ? binding.etTags.getText().toString().trim() : "";
        return !keyword.isEmpty() || !tags.isEmpty();
    }

    private void showSearchError() {
        binding.tilKeyword.setError("Please enter a search query or tags");
    }

    private void clearSearchError() {
        binding.tilKeyword.setError(null);
    }

    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private void performSearch() {
        hideKeyboard();

        String keyword = binding.etKeyword.getText() != null ? binding.etKeyword.getText().toString().trim() : "";
        String tags = binding.etTags.getText() != null ? binding.etTags.getText().toString().trim() : "";

        // Validate search query for relevance sorting
        if (binding.actvSorting.getText().toString().equals(WallhavenSorting.RELEVANCE.getDisplayName()) && !isSearchQueryValid()) {
            showSearchError();
            Toast.makeText(this, "Please enter a search query or tags", Toast.LENGTH_SHORT).show();
            return;
        } else {
            clearSearchError();
        }

        // Collect all filter data
        List<WallhavenCategory> selectedCategories = getSelectedCategories();
        List<Purity> selectedPurities = getSelectedPurities();
        WallhavenSorting selectedSorting = getSelectedSorting();
        Order selectedOrder = getSelectedOrder();
        WallhavenTopRange selectedTopRange = null;
        if (selectedSorting == WallhavenSorting.TOPLIST) {
            selectedTopRange = getSelectedTopRange();
        }
        String minWidth = binding.etMinWidth.getText() != null ? binding.etMinWidth.getText().toString().trim() : "";
        String minHeight = binding.etMinHeight.getText() != null ? binding.etMinHeight.getText().toString().trim() : "";
        List<String> selectedResolutions = getSelectedResolutions();
        List<WallhavenRatio> selectedRatios = getSelectedRatios();

        // Start SearchResultsActivity with all the filter data
        Intent intent = SearchResultsActivity.newIntent(
                this, keyword, tags, selectedCategories, selectedPurities,
                selectedSorting, selectedOrder, selectedTopRange,
                minWidth, minHeight, selectedResolutions, selectedRatios
        );
        startActivity(intent);
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
        return WallhavenSorting.RELEVANCE;
    }

    private Order getSelectedOrder() {
        String selectedText = binding.actvOrder.getText().toString();
        for (Order order : Order.values()) {
            if (order.getDisplayName().equals(selectedText)) {
                return order;
            }
        }
        return Order.DESC;
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
        return WallhavenTopRange.ONE_MONTH;
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

        clearSearchError();

        clearChipGroup(binding.chipGroupCategories);
        clearChipGroup(binding.chipGroupPurity);
        clearChipGroup(binding.chipGroupResolutions);
        clearChipGroup(binding.chipGroupRatios);

        binding.actvSorting.setText(WallhavenSorting.RELEVANCE.getDisplayName(), false);
        binding.actvOrder.setText(Order.DESC.getDisplayName(), false);
        binding.actvTopRange.setText(getTopRangeDisplayName(WallhavenTopRange.ONE_MONTH), false);

        binding.topRangeContainer.setVisibility(View.GONE);

        setDefaultValues();
    }

    private void clearChipGroup(ViewGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setChecked(false);
        }
    }

    private String getTopRangeDisplayName(WallhavenTopRange topRange) {
        switch (topRange) {
            case ONE_DAY: return "1 Day";
            case THREE_DAYS: return "3 Days";
            case ONE_WEEK: return "1 Week";
            case ONE_MONTH: return "1 Month";
            case THREE_MONTHS: return "3 Months";
            case SIX_MONTHS: return "6 Months";
            case ONE_YEAR: return "1 Year";
            default: return topRange.name();
        }
    }
}
