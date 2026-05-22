package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            BottomNavigationView navView = findViewById(R.id.nav_view);

            navView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                    return true;
                } else if (itemId == R.id.navigation_training) {
                    navController.navigate(R.id.navigation_training);
                    return true;
                } else if (itemId == R.id.navigation_progress) {
                    navController.navigate(R.id.navigation_progress);
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    navController.navigate(R.id.navigation_profile);
                    return true;
                }
                return false;
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();

                // 登录、注册、体测页面隐藏底部导航栏
                if (destId == R.id.navigation_login ||
                    destId == R.id.navigation_register ||
                    destId == R.id.navigation_assessment) {
                    navView.setVisibility(View.GONE);
                } else {
                    navView.setVisibility(View.VISIBLE);
                }

                if (destId == R.id.navigation_home) {
                    navView.getMenu().findItem(R.id.navigation_home).setChecked(true);
                } else if (destId == R.id.navigation_training) {
                    navView.getMenu().findItem(R.id.navigation_training).setChecked(true);
                } else if (destId == R.id.navigation_progress) {
                    navView.getMenu().findItem(R.id.navigation_progress).setChecked(true);
                } else if (destId == R.id.navigation_profile) {
                    navView.getMenu().findItem(R.id.navigation_profile).setChecked(true);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
