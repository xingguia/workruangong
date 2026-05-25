package com.example.myapplication.ui.screens.profile;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentProfileBinding;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.UsernameValidator;
import com.example.myapplication.util.WorkoutRecordManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private WorkoutRecordManager workoutRecordManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());
        workoutRecordManager = WorkoutRecordManager.getInstance(requireContext());

        setupUserInfo();
        setupListeners();

        // Check if username needs to be set
        if (!sessionManager.isUsernameSet()) {
            showUsernameSetDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        setupUserInfo();
    }

    private void showUsernameSetDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_username, null);

        TextInputLayout usernameInputLayout = dialogView.findViewById(R.id.usernameInputLayout);
        TextInputEditText usernameInput = dialogView.findViewById(R.id.usernameInput);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("确认", null)
                .setNegativeButton("跳过", (d, which) -> {
                    // Set default nickname if skipped
                    sessionManager.saveNickname("健身爱好者");
                    sessionManager.markUsernameSet();
                    setupUserInfo();
                })
                .setCancelable(false)
                .create();

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                if (!username.isEmpty()) {
                    UsernameValidator.ValidationResult result = UsernameValidator.validate(username);
                    if (!result.valid) {
                        usernameInputLayout.setError(result.errorMessage);
                    } else if (sessionManager.isNicknameAvailable(username)) {
                        usernameInputLayout.setError(null);
                    } else {
                        usernameInputLayout.setError("该用户名已被使用");
                    }
                } else {
                    usernameInputLayout.setError(null);
                }
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String username = usernameInput.getText() != null ?
                        usernameInput.getText().toString().trim() : "";

                if (username.isEmpty()) {
                    usernameInputLayout.setError("请输入用户名");
                    return;
                }

                UsernameValidator.ValidationResult result = UsernameValidator.validateWithAvailability(requireContext(), username);
                if (!result.valid) {
                    usernameInputLayout.setError(result.errorMessage);
                    return;
                }

                // Save the username
                sessionManager.saveNickname(username);
                sessionManager.addUsernameToSet(username);
                sessionManager.markUsernameSet();

                dialog.dismiss();
                setupUserInfo();
            });
        });

        dialog.show();
    }

    private void setupUserInfo() {
        // Get user nickname from session
        String nickname = sessionManager.getNickname();

        // Check if username is set, if not use default
        if (nickname == null || nickname.isEmpty() || nickname.equals("健身爱好者")) {
            nickname = "健身爱好者";
        }

        binding.userName.setText(nickname);

        // Load avatar
        String avatar = sessionManager.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(avatar, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                binding.avatarImage.setImageBitmap(bitmap);
                binding.avatarImage.setVisibility(View.VISIBLE);
                binding.avatarPlaceholder.setVisibility(View.GONE);
            } catch (Exception e) {
                // Show default avatar
                binding.avatarPlaceholder.setText(String.valueOf(nickname.charAt(0)).toUpperCase());
                binding.avatarImage.setVisibility(View.GONE);
                binding.avatarPlaceholder.setVisibility(View.VISIBLE);
            }
        } else {
            // Set avatar placeholder with first character
            if (nickname.length() > 0) {
                String firstChar = nickname.substring(0, 1).toUpperCase();
                binding.avatarPlaceholder.setText(firstChar);
            } else {
                binding.avatarPlaceholder.setText("U");
            }
            binding.avatarImage.setVisibility(View.GONE);
            binding.avatarPlaceholder.setVisibility(View.VISIBLE);
        }

        // Set level
        binding.levelBadge.setText("LV" + sessionManager.getLevel());

        // Load statistics from WorkoutRecordManager
        loadStatistics();

        // Get VIP status
        boolean isVip = sessionManager.isVip();
        if (isVip) {
            binding.vipStatusCard.setVisibility(View.VISIBLE);
            binding.vipTag.setVisibility(View.GONE);
        } else {
            binding.vipStatusCard.setVisibility(View.GONE);
            binding.vipTag.setVisibility(View.VISIBLE);
        }

        // Load displayed badges
        loadDisplayedBadges();
    }

    private void loadStatistics() {
        // Load total workouts count
        int totalWorkouts = workoutRecordManager.getTotalWorkouts();
        binding.totalWorkouts.setText(String.valueOf(totalWorkouts));

        // Load consecutive days
        int consecutiveDays = workoutRecordManager.getConsecutiveDays();
        binding.totalDays.setText(String.valueOf(consecutiveDays));

        // Load total calories
        float totalCalories = workoutRecordManager.getTotalCalories();
        binding.totalCalories.setText(String.format("%.1f", totalCalories));

        // Load badge count
        loadBadgeCount();
    }

    private void loadBadgeCount() {
        com.example.myapplication.util.AchievementManager achievementManager =
                com.example.myapplication.util.AchievementManager.getInstance(requireContext());
        int unlockedCount = achievementManager.getUnlockedAchievements().size();
        binding.badgeWallCount.setText(String.valueOf(unlockedCount));
    }

    private void loadDisplayedBadges() {
        com.example.myapplication.util.AchievementManager achievementManager =
                com.example.myapplication.util.AchievementManager.getInstance(requireContext());

        java.util.List<com.example.myapplication.model.Achievement> displayedBadges =
                achievementManager.getDisplayedAchievements();

        if (displayedBadges.isEmpty()) {
            binding.badgesDisplayContainer.setVisibility(View.VISIBLE);
            binding.badgesEmptyHint.setVisibility(View.VISIBLE);
            binding.badgeSlot1.setVisibility(View.GONE);
            binding.badgeSlot2.setVisibility(View.GONE);
            binding.badgeSlot3.setVisibility(View.GONE);
        } else {
            binding.badgesDisplayContainer.setVisibility(View.VISIBLE);
            binding.badgesEmptyHint.setVisibility(View.GONE);

            // Sort by display position
            Map<Integer, com.example.myapplication.model.Achievement> badgeMap = new HashMap<>();
            for (com.example.myapplication.model.Achievement badge : displayedBadges) {
                badgeMap.put(badge.getDisplayPosition(), badge);
            }

            // Set badge 1
            com.example.myapplication.model.Achievement badge1 = badgeMap.get(0);
            if (badge1 != null) {
                binding.badgeSlot1.setVisibility(View.VISIBLE);
                binding.badgeIcon1.setImageResource(getAchievementIcon(badge1.getType()));
                binding.badgeIcon1.setColorFilter(requireContext().getColor(R.color.vip_gold));
            } else {
                binding.badgeSlot1.setVisibility(View.GONE);
            }

            // Set badge 2
            com.example.myapplication.model.Achievement badge2 = badgeMap.get(1);
            if (badge2 != null) {
                binding.badgeSlot2.setVisibility(View.VISIBLE);
                binding.badgeIcon2.setImageResource(getAchievementIcon(badge2.getType()));
                binding.badgeIcon2.setColorFilter(requireContext().getColor(R.color.vip_gold));
            } else {
                binding.badgeSlot2.setVisibility(View.GONE);
            }

            // Set badge 3
            com.example.myapplication.model.Achievement badge3 = badgeMap.get(2);
            if (badge3 != null) {
                binding.badgeSlot3.setVisibility(View.VISIBLE);
                binding.badgeIcon3.setImageResource(getAchievementIcon(badge3.getType()));
                binding.badgeIcon3.setColorFilter(requireContext().getColor(R.color.vip_gold));
            } else {
                binding.badgeSlot3.setVisibility(View.GONE);
            }
        }
    }

    private int getAchievementIcon(com.example.myapplication.model.Achievement.AchievementType type) {
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

    private void setupListeners() {
        // 设置按钮 - 跳转到设置页面
        binding.settingsBtn.setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_settings);
        });

        // 编辑头像 - 跳转到个人信息编辑页面
        binding.editAvatarBtn.setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_profile_edit);
        });

        // 用户卡片点击 - 跳转到个人信息编辑页面
        View userCard = binding.getRoot().findViewById(R.id.userCardContainer);
        if (userCard != null) {
            userCard.setOnClickListener(v -> {
                navController.navigate(R.id.action_profile_to_profile_edit);
            });
        }

        // VIP中心
        binding.vipCenterItem.setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_vip);
        });

        // 身体数据 - 跳转到进度页面
        binding.bodyDataItem.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_progress);
        });

        // 训练记录
        binding.workoutRecordItem.setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_workout_history);
        });

        // 成就
        binding.achievementsItem.setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_achievements);
        });

        // 徽章墙
        binding.badgeWallItem.setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_badge_wall);
        });

        // 退出登录
        binding.logoutBtn.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performLogout() {
        sessionManager.logout();
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        navController.navigate(R.id.action_profile_to_login);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
