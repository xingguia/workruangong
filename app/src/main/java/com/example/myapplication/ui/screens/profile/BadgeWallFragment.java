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
import com.example.myapplication.databinding.FragmentBadgeWallBinding;

public class BadgeWallFragment extends Fragment {

    private FragmentBadgeWallBinding binding;
    private NavController navController;

    private String[] badgeNames = {
            "初次训练", "7天坚持", "30天坚持", "训练达人",
            "减脂达人", "增肌达人", "早起鸟", "夜猫子",
            "周末战士", "完美打卡"
    };

    private String[] badgeDescs = {
            "完成第一次训练", "连续训练7天", "连续训练30天", "完成100次训练",
            "累计减脂10斤", "成功增肌5斤", "早起训练10次", "夜间训练10次",
            "连续4周周末训练", "连续30天打卡"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBadgeWallBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        setupHeader();
        loadBadges();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });
    }

    private void loadBadges() {
        binding.badgesContainer.removeAllViews();

        int unlockedCount = 5; // Sample
        binding.badgeCount.setText(String.valueOf(unlockedCount));

        for (int i = 0; i < badgeNames.length; i++) {
            View itemView = createBadgeItem(badgeNames[i], badgeDescs[i], i < unlockedCount);
            binding.badgesContainer.addView(itemView);
        }
    }

    private View createBadgeItem(String name, String desc, boolean unlocked) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackgroundResource(R.drawable.bg_card);
        container.setPadding(16, 12, 16, 12);
        container.setGravity(android.view.Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 8);
        container.setLayoutParams(params);

        // Badge icon
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

        // Badge info
        LinearLayout infoContainer = new LinearLayout(requireContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.setMargins(12, 0, 0, 0);
        infoParams.weight = 1;
        infoContainer.setLayoutParams(infoParams);

        TextView nameView = new TextView(requireContext());
        nameView.setText(name);
        nameView.setTextSize(16);
        nameView.setTextColor(getResources().getColor(unlocked ? R.color.text_primary : R.color.text_muted, null));

        TextView descView = new TextView(requireContext());
        descView.setText(desc);
        descView.setTextSize(12);
        descView.setTextColor(getResources().getColor(R.color.text_muted, null));

        infoContainer.addView(nameView);
        infoContainer.addView(descView);

        // Status
        TextView statusView = new TextView(requireContext());
        if (unlocked) {
            statusView.setText("已获得");
            statusView.setTextColor(getResources().getColor(R.color.success, null));
        } else {
            statusView.setText("未获得");
            statusView.setTextColor(getResources().getColor(R.color.text_muted, null));
        }
        statusView.setTextSize(12);

        container.addView(iconView);
        container.addView(infoContainer);
        container.addView(statusView);

        return container;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
