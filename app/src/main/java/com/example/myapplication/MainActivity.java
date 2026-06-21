package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved dark mode setting before setting content view
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();

        // 已登录用户自动跳转到首页
        if (sessionManager.isLoggedIn()) {
            // 从服务器加载用户数据（昵称、最初数据等）
            sessionManager.fetchProfile(null);

            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_activity_main);
            if (navHostFragment != null) {
                // Check if we need to restore navigation state
                String lastDest = getPreferences(MODE_PRIVATE).getString("last_destination", null);
                if (lastDest != null) {
                    // Clear the saved destination
                    getPreferences(MODE_PRIVATE).edit().remove("last_destination").apply();
                    // Navigate to the saved destination
                    if ("settings".equals(lastDest)) {
                        // Navigate to profile first, then to settings
                        navHostFragment.getNavController().navigate(R.id.navigation_profile);
                        binding.getRoot().postDelayed(() -> {
                            try {
                                navHostFragment.getNavController().navigate(R.id.action_profile_to_settings);
                            } catch (Exception e) {
                                // Ignore if already on settings
                            }
                        }, 200);
                    } else {
                        navHostFragment.getNavController().navigate(R.id.navigation_home);
                    }
                } else {
                    navHostFragment.getNavController().navigate(R.id.navigation_home);
                }
            }
        }
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
