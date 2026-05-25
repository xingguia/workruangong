package com.example.myapplication.ui.screens.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentBadgeWallBinding;
import com.example.myapplication.model.Achievement;
import com.example.myapplication.util.AchievementManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import java.util.ArrayList;
import java.util.List;

public class BadgeWallFragment extends Fragment {

    private FragmentBadgeWallBinding binding;
    private NavController navController;
    private AchievementManager achievementManager;
    private List<Achievement> displayedAchievements = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBadgeWallBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        achievementManager = AchievementManager.getInstance(requireContext());

        setupHeader();
        loadBadges();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBadges();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });

        // 点击徽章数量区域5次解锁所有成就（测试用）
        binding.badgeCount.setOnClickListener(v -> {
            // 使用 SharedPreferences 记录点击次数
            android.content.SharedPreferences prefs = requireContext()
                    .getSharedPreferences("test_prefs", android.content.Context.MODE_PRIVATE);
            int clickCount = prefs.getInt("unlock_click_count", 0) + 1;
            prefs.edit().putInt("unlock_click_count", clickCount).apply();

            if (clickCount >= 5) {
                achievementManager.unlockAllAchievements();
                loadBadges();
                Toast.makeText(requireContext(), "已解锁所有成就！", Toast.LENGTH_SHORT).show();
                prefs.edit().putInt("unlock_click_count", 0).apply();
            } else {
                Toast.makeText(requireContext(), "再点击 " + (5 - clickCount) + " 次解锁全部", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBadges() {
        binding.badgesContainer.removeAllViews();

        List<Achievement> unlockedAchievements = achievementManager.getUnlockedAchievements();
        binding.badgeCount.setText(String.valueOf(unlockedAchievements.size()));

        // 添加提示说明
        TextView hintView = new TextView(requireContext());
        hintView.setText("点击已解锁的徽章可设置是否在主页展示（最多3个）");
        hintView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        hintView.setTextSize(12);
        hintView.setPadding(0, 0, 0, 16);
        binding.badgesContainer.addView(hintView);

        // 显示所有已解锁的徽章
        for (Achievement achievement : unlockedAchievements) {
            View itemView = createBadgeItem(achievement, true);
            binding.badgesContainer.addView(itemView);
        }

        // 添加未解锁的徽章
        if (!unlockedAchievements.isEmpty()) {
            TextView lockedTitle = new TextView(requireContext());
            lockedTitle.setText("未解锁的徽章");
            lockedTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            lockedTitle.setTextSize(14);
            lockedTitle.setPadding(0, 24, 0, 8);
            lockedTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            binding.badgesContainer.addView(lockedTitle);
        }

        List<Achievement> allAchievements = achievementManager.getAllAchievements();
        for (Achievement achievement : allAchievements) {
            if (!achievement.isUnlocked()) {
                View itemView = createBadgeItem(achievement, false);
                binding.badgesContainer.addView(itemView);
            }
        }
    }

    private View createBadgeItem(Achievement achievement, boolean unlocked) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackgroundResource(R.drawable.bg_card);
        container.setPadding(16, 12, 16, 12);
        container.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 8);
        container.setLayoutParams(params);

        // Badge icon
        ImageView iconView = new ImageView(requireContext());
        int size = (int) (48 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        iconView.setLayoutParams(iconParams);

        if (unlocked) {
            iconView.setImageResource(getAchievementIcon(achievement.getType()));
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.vip_gold));
        } else {
            iconView.setImageResource(R.drawable.ic_star);
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_muted));
            iconView.setAlpha(0.4f);
        }

        // Badge info
        LinearLayout infoContainer = new LinearLayout(requireContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(12, 0, 0, 0);
        infoContainer.setLayoutParams(infoParams);

        TextView nameView = new TextView(requireContext());
        nameView.setText(achievement.getDisplayName());
        nameView.setTextSize(16);
        nameView.setTextColor(ContextCompat.getColor(requireContext(),
                unlocked ? R.color.text_primary : R.color.text_muted));

        TextView descView = new TextView(requireContext());
        descView.setText(achievement.getFullDesc());
        descView.setTextSize(12);
        descView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));

        infoContainer.addView(nameView);
        infoContainer.addView(descView);

        // Status/Display indicator
        TextView statusView = new TextView(requireContext());
        statusView.setTextSize(12);

        if (unlocked) {
            if (achievement.isDisplayed()) {
                statusView.setText("展示中 #" + (achievement.getDisplayPosition() + 1));
                statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            } else {
                statusView.setText("未展示");
                statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            }

            // 点击打开全局设置对话框
            container.setOnClickListener(v -> showBadgeDisplayDialog());
        } else {
            statusView.setText("未获得");
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        }

        container.addView(iconView);
        container.addView(infoContainer);
        container.addView(statusView);

        return container;
    }

    private void showBadgeDisplayDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_badge_display_settings, null);

        // 获取当前已解锁的成就
        List<Achievement> unlockedAchievements = achievementManager.getUnlockedAchievements();
        List<Achievement> displayedAchievements = achievementManager.getDisplayedAchievements();

        // 临时保存选择状态（key: position, value: achievement or null）
        final Map<Integer, Achievement> tempSelection = new HashMap<>();
        // 初始化为当前状态
        for (Achievement a : displayedAchievements) {
            tempSelection.put(a.getDisplayPosition(), a);
        }

        // 初始化槽位视图
        FrameLayout[] slots = new FrameLayout[3];
        TextView[] slotLabels = new TextView[3];
        ImageView[] slotIcons = new ImageView[3];
        TextView[] slotNames = new TextView[3];

        slots[0] = dialogView.findViewById(R.id.slot1);
        slots[1] = dialogView.findViewById(R.id.slot2);
        slots[2] = dialogView.findViewById(R.id.slot3);
        slotLabels[0] = dialogView.findViewById(R.id.slot1Label);
        slotLabels[1] = dialogView.findViewById(R.id.slot2Label);
        slotLabels[2] = dialogView.findViewById(R.id.slot3Label);
        slotIcons[0] = dialogView.findViewById(R.id.slot1Icon);
        slotIcons[1] = dialogView.findViewById(R.id.slot2Icon);
        slotIcons[2] = dialogView.findViewById(R.id.slot3Icon);
        slotNames[0] = dialogView.findViewById(R.id.slot1BadgeName);
        slotNames[1] = dialogView.findViewById(R.id.slot2BadgeName);
        slotNames[2] = dialogView.findViewById(R.id.slot3BadgeName);

        // 徽章列表容器
        LinearLayout badgeListContainer = dialogView.findViewById(R.id.badgeListContainer);
        // 按钮
        com.google.android.material.button.MaterialButton cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        com.google.android.material.button.MaterialButton confirmBtn = dialogView.findViewById(R.id.confirmBtn);

        // 更新槽位显示
        Runnable updateSlots = () -> {
            for (int i = 0; i < 3; i++) {
                Achievement badge = tempSelection.get(i);
                if (badge != null) {
                    slots[i].setBackgroundResource(R.drawable.bg_badge_slot_filled);
                    slotLabels[i].setVisibility(View.GONE);
                    slotIcons[i].setVisibility(View.VISIBLE);
                    slotIcons[i].setImageResource(getAchievementIcon(badge.getType()));
                    slotIcons[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.vip_gold));
                    slotNames[i].setText(badge.getDisplayName());
                    slotNames[i].setVisibility(View.VISIBLE);
                } else {
                    slots[i].setBackgroundResource(R.drawable.bg_badge_slot_empty);
                    slotLabels[i].setVisibility(View.VISIBLE);
                    slotLabels[i].setText("+");
                    slotIcons[i].setVisibility(View.GONE);
                    slotNames[i].setVisibility(View.GONE);
                }
            }
        };

        // 点击槽位选择徽章
        for (int i = 0; i < 3; i++) {
            final int position = i;
            slots[i].setOnClickListener(v -> {
                // 如果槽位已有徽章，先移除
                if (tempSelection.get(position) != null) {
                    tempSelection.remove(position);
                    updateSlots.run();
                    return;
                }

                // 显示徽章选择对话框
                showBadgePickerDialog(unlockedAchievements, tempSelection, selectedBadge -> {
                    if (selectedBadge != null) {
                        // 检查是否已在其他位置
                        for (Map.Entry<Integer, Achievement> entry : tempSelection.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() == selectedBadge) {
                                tempSelection.remove(entry.getKey());
                                break;
                            }
                        }
                        tempSelection.put(position, selectedBadge);
                    }
                    updateSlots.run();
                });
            });
        };

        // 初始更新槽位
        updateSlots.run();

        // 添加徽章列表（快速选择）
        for (Achievement badge : unlockedAchievements) {
            View badgeItem = createBadgeListItem(badge);
            badgeListContainer.addView(badgeItem);
        }

        // 创建对话框
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // 取消按钮
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // 确定按钮
        confirmBtn.setOnClickListener(v -> {
            // 应用选择
            // 先清除所有展示
            for (Achievement a : achievementManager.getDisplayedAchievements()) {
                achievementManager.setAchievementDisplayed(a, false, -1);
            }
            // 设置新的展示
            for (Map.Entry<Integer, Achievement> entry : tempSelection.entrySet()) {
                if (entry.getValue() != null) {
                    achievementManager.setAchievementDisplayed(entry.getValue(), true, entry.getKey());
                }
            }
            dialog.dismiss();
            loadBadges();
        });

        dialog.show();
    }

    private void showBadgePickerDialog(List<Achievement> badges, Map<Integer, Achievement> tempSelection, OnBadgeSelectedListener listener) {
        AlertDialog[] pickerDialogHolder = new AlertDialog[1];

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_badge_picker, null);

        LinearLayout container = dialogView.findViewById(R.id.badgePickerContainer);

        for (Achievement badge : badges) {
            // 检查是否已在选择中
            boolean alreadySelected = false;
            for (Achievement selected : tempSelection.values()) {
                if (selected != null && selected == badge) {
                    alreadySelected = true;
                    break;
                }
            }

            LinearLayout itemView = (LinearLayout) createBadgeListItem(badge);
            if (alreadySelected) {
                itemView.setAlpha(0.5f);
            }
            final boolean finalAlreadySelected = alreadySelected;
            itemView.setOnClickListener(v -> {
                if (!finalAlreadySelected) {
                    listener.onBadgeSelected(badge);
                    if (pickerDialogHolder[0] != null) {
                        pickerDialogHolder[0].dismiss();
                    }
                }
            });
            container.addView(itemView);
        }

        AlertDialog pickerDialog = new AlertDialog.Builder(requireContext())
                .setTitle("选择徽章")
                .setView(dialogView)
                .setNegativeButton("取消", (d, which) -> listener.onBadgeSelected(null))
                .create();

        pickerDialogHolder[0] = pickerDialog;
        pickerDialog.show();
    }

    private View createBadgeListItem(Achievement badge) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(16, 12, 16, 12);
        container.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 4, 0, 4);
        container.setLayoutParams(params);

        ImageView iconView = new ImageView(requireContext());
        int size = (int) (40 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        iconView.setLayoutParams(iconParams);
        iconView.setImageResource(getAchievementIcon(badge.getType()));
        iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.vip_gold));

        TextView nameView = new TextView(requireContext());
        nameView.setText(badge.getDisplayName());
        nameView.setTextSize(14);
        nameView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        container.addView(iconView);
        container.addView(nameView);

        return container;
    }

    private interface OnBadgeSelectedListener {
        void onBadgeSelected(Achievement badge);
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
