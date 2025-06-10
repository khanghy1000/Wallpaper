package com.example.wallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.wallpaper.R;
import com.example.wallpaper.databinding.ActivitySearchBinding;
import com.example.wallpaper.ui.fragment.SearchFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchActivity extends AppCompatActivity {
    
    private ActivitySearchBinding binding;

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    public static Intent newIntentWithTag(Context context, String tag) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("search_tag", tag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add SearchFragment
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, SearchFragment.newInstance());
            transaction.commit();
        }
    }
}
