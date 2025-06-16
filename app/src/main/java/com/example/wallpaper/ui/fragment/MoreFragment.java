package com.example.wallpaper.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.wallpaper.R;
import com.example.wallpaper.databinding.DialogThemeSelectionBinding;
import com.example.wallpaper.databinding.FragmentMoreBinding;
import com.example.wallpaper.ui.common.ThemePreferenceManager;
import com.example.wallpaper.ui.viewmodel.MoreViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MoreFragment extends Fragment {

    private FragmentMoreBinding binding;
    private MoreViewModel viewModel;

    public MoreFragment() {
        // Required empty public constructor
    }

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MoreViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupThemeSettingButton();
        observeViewModel();
    }

    private void setupThemeSettingButton() {
        updateThemeSummary();
        binding.themeSettingButton.setOnClickListener(v -> showThemeDialog());
    }

    private void showThemeDialog() {
        DialogThemeSelectionBinding dialogBinding = DialogThemeSelectionBinding.inflate(getLayoutInflater());
        
        int currentTheme = viewModel.getCurrentThemeMode();
        updateDialogThemeSelection(dialogBinding, currentTheme);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        
        dialogBinding.radioSystem.setOnClickListener(v -> {
            setTheme(ThemePreferenceManager.THEME_SYSTEM);
            dialog.dismiss();
        });
        
        dialogBinding.radioLight.setOnClickListener(v -> {
            setTheme(ThemePreferenceManager.THEME_LIGHT);
            dialog.dismiss();
        });
        
        dialogBinding.radioDark.setOnClickListener(v -> {
            setTheme(ThemePreferenceManager.THEME_DARK);
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void setTheme(int themeMode) {
        viewModel.setThemeMode(themeMode);
        updateThemeSummary();
    }

    private void updateDialogThemeSelection(DialogThemeSelectionBinding dialogBinding, int themeMode) {
        dialogBinding.radioSystem.setChecked(themeMode == ThemePreferenceManager.THEME_SYSTEM);
        dialogBinding.radioLight.setChecked(themeMode == ThemePreferenceManager.THEME_LIGHT);
        dialogBinding.radioDark.setChecked(themeMode == ThemePreferenceManager.THEME_DARK);
    }

    private void updateThemeSummary() {
        int currentTheme = viewModel.getCurrentThemeMode();
        String themeName = getThemeDisplayName(currentTheme);
        binding.themeSummary.setText(themeName);
    }

    private String getThemeDisplayName(int themeMode) {
        switch (themeMode) {
            case ThemePreferenceManager.THEME_LIGHT:
                return getString(R.string.theme_light);
            case ThemePreferenceManager.THEME_DARK:
                return getString(R.string.theme_dark);
            case ThemePreferenceManager.THEME_SYSTEM:
            default:
                return getString(R.string.theme_system);
        }
    }

    private void observeViewModel() {
        viewModel.themeMode.observe(getViewLifecycleOwner(), themeMode -> updateThemeSummary());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}