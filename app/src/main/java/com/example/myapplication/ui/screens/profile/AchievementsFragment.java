package com.example.myapplication.ui.screens.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentAchievementsBinding;

public class AchievementsFragment extends Fragment {

    private FragmentAchievementsBinding binding;
    private NavController navController;

    private String[] achievementNames = {
            "初次训练", "坚持7天", "坚持30天", "完成100次训练",
            "燃烧1000卡", "燃烧5000卡", "突破体重", "达到标准体重",
            "早起鸟", "夜猫子", "周末战士", "连续打卡"
    };

    private int[] achievementIcons = {
            R.drawable.ic_star, R.drawable.ic_star, R.drawable.ic_star, R.drawable.ic_star,
            R.drawable.ic_star, R.drawable.ic_star, R.drawable.ic_star, R.drawable.ic_star,
            R.drawable.ic_star, R.drawable.ic_star, R.drawable.ic_star, R.drawable.ic_star
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAchievementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        setupHeader();
        loadAchievements();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });
    }

    private void loadAchievements() {
        binding.achievementsContainer.removeAllViews();

        // Create a row with 4 achievements
        LinearLayout row = null;
        int count = 0;
        int unlocked = 3; // Sample unlocked count

        binding.achievementCount.setText(unlocked + "/" + achievementNames.length);

        for (int i = 0; i < achievementNames.length; i++) {
            if (count % 4 == 0) {
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                binding.achievementsContainer.addView(row);
            }

            View itemView = createAchievementItem(achievementNames[i], i < unlocked);
            row.addView(itemView);
            count++;
        }
    }

    private View createAchievementItem(String name, boolean unlocked) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(4, 4, 4, 4);
        container.setLayoutParams(params);
        container.setPadding(8, 16, 8, 16);

        // Achievement icon
        View iconView = new View(requireContext());
        int size = (int) (48 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        iconView.setLayoutParams(iconParams);
        iconView.setBackgroundResource(R.drawable.bg_avatar_placeholder);

        if (unlocked) {
            iconView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.vip_gold, null)));
        } else {
            iconView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.text_muted, null)));
        }

        // Achievement name
        TextView nameView = new TextView(requireContext());
        nameView.setText(name);
        nameView.setTextSize(10);
        nameView.setGravity(android.view.Gravity.CENTER);
        nameView.setTextColor(getResources().getColor(unlocked ? R.color.text_primary : R.color.text_muted, null));
        nameView.setMaxLines(2);

        container.addView(iconView);
        container.addView(nameView);

        return container;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
