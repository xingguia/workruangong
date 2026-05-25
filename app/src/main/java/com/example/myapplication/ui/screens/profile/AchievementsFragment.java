package com.example.myapplication.ui.screens.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentAchievementsBinding;
import com.example.myapplication.model.Achievement;
import com.example.myapplication.util.AchievementManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AchievementsFragment extends Fragment {

    private FragmentAchievementsBinding binding;
    private NavController navController;
    private AchievementManager achievementManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAchievementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        achievementManager = AchievementManager.getInstance(requireContext());

        setupHeader();
        loadAchievements();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAchievements();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });
    }

    private void loadAchievements() {
        binding.achievementsContainer.removeAllViews();

        List<Achievement> achievements = achievementManager.getAllAchievements();
        int unlockedCount = 0;

        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlockedCount++;
            }
        }

        binding.achievementCount.setText(unlockedCount + "/" + achievements.size());

        // Create a row with 4 achievements
        LinearLayout row = null;
        int count = 0;

        for (int i = 0; i < achievements.size(); i++) {
            Achievement achievement = achievements.get(i);

            if (count % 4 == 0) {
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                row.setPadding(0, 8, 0, 8);
                binding.achievementsContainer.addView(row);
            }

            View itemView = createAchievementItem(achievement);
            row.addView(itemView);
            count++;
        }

        // 如果最后一行不满4个，添加占位
        if (count % 4 != 0) {
            int remaining = 4 - (count % 4);
            for (int i = 0; i < remaining; i++) {
                View placeholder = new View(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                params.setMargins(4, 4, 4, 4);
                placeholder.setLayoutParams(params);
                row.addView(placeholder);
            }
        }
    }

    private View createAchievementItem(Achievement achievement) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackgroundResource(R.drawable.bg_card);
        container.setPadding(8, 16, 8, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(4, 4, 4, 4);
        container.setLayoutParams(params);

        container.setOnClickListener(v -> showAchievementDetail(achievement));

        // Achievement icon
        ImageView iconView = new ImageView(requireContext());
        int size = (int) (48 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        iconView.setLayoutParams(iconParams);

        if (achievement.isUnlocked()) {
            iconView.setImageResource(getAchievementIcon(achievement.getType()));
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.vip_gold));
        } else {
            iconView.setImageResource(R.drawable.ic_star);
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_muted));
            iconView.setAlpha(0.5f);
        }

        // Achievement name
        TextView nameView = new TextView(requireContext());
        nameView.setText(achievement.getDisplayName());
        nameView.setTextSize(11);
        nameView.setGravity(Gravity.CENTER);
        nameView.setTextColor(ContextCompat.getColor(requireContext(),
                achievement.isUnlocked() ? R.color.text_primary : R.color.text_muted));
        nameView.setMaxLines(2);
        nameView.setPadding(0, 8, 0, 0);

        // Status indicator
        if (achievement.isUnlocked()) {
            ImageView checkView = new ImageView(requireContext());
            int checkSize = (int) (16 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(checkSize, checkSize);
            checkParams.gravity = Gravity.END;
            checkView.setLayoutParams(checkParams);
            checkView.setImageResource(R.drawable.ic_check);
            checkView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success));
        }

        container.addView(iconView);
        container.addView(nameView);

        return container;
    }

    private void showAchievementDetail(Achievement achievement) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_achievement_detail, null);

        ImageView iconView = dialogView.findViewById(R.id.achievementIcon);
        TextView nameView = dialogView.findViewById(R.id.achievementName);
        TextView descView = dialogView.findViewById(R.id.achievementDesc);
        TextView statusView = dialogView.findViewById(R.id.achievementStatus);
        TextView dateView = dialogView.findViewById(R.id.achievementDate);

        if (achievement.isUnlocked()) {
            iconView.setImageResource(getAchievementIcon(achievement.getType()));
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.vip_gold));
            nameView.setTextColor(ContextCompat.getColor(requireContext(), R.color.vip_gold));
            statusView.setText("已解锁");
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            dateView.setText("解锁于 " + sdf.format(new Date(achievement.getUnlockTime())));
            dateView.setVisibility(View.VISIBLE);
        } else {
            iconView.setImageResource(R.drawable.ic_star);
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_muted));
            iconView.setAlpha(0.5f);
            nameView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            statusView.setText("未解锁");
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            dateView.setVisibility(View.GONE);
        }

        nameView.setText(achievement.getDisplayName());
        descView.setText(achievement.getFullDesc());

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("知道了", null)
                .show();
    }

    private int getAchievementIcon(Achievement.AchievementType type) {
        switch (type) {
            case FIRST_WORKOUT:
                return R.drawable.ic_training;
            case STREAK_7_DAYS:
            case STREAK_30_DAYS:
            case CHECKIN_7:
            case CHECKIN_30:
                return R.drawable.ic_fire;
            case WORKOUT_100:
            case WORKOUT_500:
                return R.drawable.ic_trophy;
            case BURN_1000_CAL:
            case BURN_5000_CAL:
            case BURN_10000_CAL:
                return R.drawable.ic_flame;
            case WEIGHT_CHANGE:
            case TARGET_WEIGHT:
                return R.drawable.ic_weight;
            case EARLY_BIRD:
                return R.drawable.ic_sun;
            case NIGHT_OWL:
                return R.drawable.ic_moon;
            case WEEKEND_WARRIOR:
                return R.drawable.ic_calendar;
            default:
                return R.drawable.ic_star;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
