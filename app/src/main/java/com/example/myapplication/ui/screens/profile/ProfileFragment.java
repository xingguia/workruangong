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
