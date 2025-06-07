package com.example.wallpaper.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.wallpaper.ui.fragment.CollectionsFragment;
import com.example.wallpaper.ui.fragment.HomeFragment;
import com.example.wallpaper.ui.fragment.LocalFragment;
import com.example.wallpaper.ui.fragment.MoreFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return HomeFragment.newInstance();
            case 1:
                return CollectionsFragment.newInstance();
            case 2:
                return LocalFragment.newInstance();
            case 3:
                return MoreFragment.newInstance();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
