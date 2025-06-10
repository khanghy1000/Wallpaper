package com.example.wallpaper.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.wallpaper.R;
import com.example.wallpaper.databinding.ActivityMainBinding;
import com.example.wallpaper.ui.adapter.MainPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,0);
            return insets;
        });

        binding.viewPager.setAdapter(new MainPagerAdapter(this));
        binding.viewPager.setOffscreenPageLimit(4);
        binding.viewPager.setUserInputEnabled(false);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                binding.viewPager.setCurrentItem(0, false);
            }
            if (id == R.id.collections) {
                binding.viewPager.setCurrentItem(1, false);
            }
            if (id == R.id.local) {
                binding.viewPager.setCurrentItem(2, false);
            }
            if (id == R.id.more) {
                binding.viewPager.setCurrentItem(3, false);
            }
            return true;
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigation.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        binding.bottomNavigation.setSelectedItemId(R.id.collections);
                        break;
                    case 2:
                        binding.bottomNavigation.setSelectedItemId(R.id.local);
                        break;
                    case 3:
                        binding.bottomNavigation.setSelectedItemId(R.id.more);
                        break;
                }
            }
        });
    }
}