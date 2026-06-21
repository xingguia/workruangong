package com.example.myapplication.ui.screens.profile;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentSettingsBinding;
import com.example.myapplication.util.SessionManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private NavController navController;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());

        setupHeader();
        loadSettings();
        setupListeners();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> navController.popBackStack());
    }

    private void loadSettings() {
        binding.workoutReminderSwitch.setChecked(sessionManager.isWorkoutReminderEnabled());
        binding.achievementSwitch.setChecked(sessionManager.isAchievementNotificationEnabled());
        binding.darkModeSwitch.setChecked(sessionManager.isDarkMode());
        updateUnitText();
        updateReminderTimeText();
    }

    private void updateUnitText() {
        String unit = sessionManager.getUnitSystem();
        if ("imperial".equals(unit)) {
            binding.unitValueText.setText("英制（磅/英寸）");
        } else {
            binding.unitValueText.setText("公制（公斤/厘米）");
        }
    }

    private void updateReminderTimeText() {
        String time = sessionManager.getReminderTime();
        binding.reminderTimeText.setText("每天 " + time);
    }

    private void setupListeners() {
        // ========== 推送通知 ==========
        binding.workoutReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setWorkoutReminderEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "训练提醒已开启" : "训练提醒已关闭", Toast.LENGTH_SHORT).show();
        });

        binding.achievementSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setAchievementNotificationEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "成就通知已开启" : "成就通知已关闭", Toast.LENGTH_SHORT).show();
        });

        // ========== 显示设置 ==========
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkMode(isChecked);
            // Save current destination for restoration after theme change
            requireActivity().getPreferences(android.content.Context.MODE_PRIVATE)
                    .edit().putString("last_destination", "settings").apply();
            // Apply theme change - this will recreate the Activity
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);
            Toast.makeText(requireContext(), isChecked ? "深色模式已开启" : "浅色模式已开启", Toast.LENGTH_SHORT).show();
        });

        binding.unitSettingItem.setOnClickListener(v -> showUnitDialog());
        binding.reminderTimeItem.setOnClickListener(v -> showTimePickerDialog());

        // ========== 数据管理 ==========
        binding.clearCacheItem.setOnClickListener(v -> showClearCacheDialog());
        binding.exportDataItem.setOnClickListener(v -> exportData());

        // ========== 账号安全 ==========
        binding.changePasswordItem.setOnClickListener(v -> showChangePasswordDialog());
        binding.logoutItem.setOnClickListener(v -> showLogoutDialog());
        binding.deleteAccountItem.setOnClickListener(v -> showDeleteAccountDialog());

        // ========== 法律 ==========
        binding.privacyPolicyItem.setOnClickListener(v ->
                Toast.makeText(requireContext(), "隐私协议", Toast.LENGTH_SHORT).show());
        binding.termsItem.setOnClickListener(v ->
                Toast.makeText(requireContext(), "服务条款", Toast.LENGTH_SHORT).show());

        // ========== 反馈与支持 ==========
        binding.messageCenterItem.setOnClickListener(v -> showMessageCenter());
        binding.feedbackItem.setOnClickListener(v -> showFeedbackDialog());
        binding.rateUsItem.setOnClickListener(v -> rateUs());

        // 加载未读消息数
        loadUnreadMessageCount();

        // ========== 关于 ==========
        binding.aboutUsItem.setOnClickListener(v ->
                Toast.makeText(requireContext(), "燃动时刻 - 您的专属健身助手", Toast.LENGTH_SHORT).show());
    }

    // ==================== 提醒时间设置 ====================

    private void showTimePickerDialog() {
        String currentTime = sessionManager.getReminderTime();
        String[] parts = currentTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    String newTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    sessionManager.setReminderTime(newTime);
                    updateReminderTimeText();
                    Toast.makeText(requireContext(), "提醒时间已设置为 " + newTime, Toast.LENGTH_SHORT).show();
                },
                hour,
                minute,
                true // 24小时制
        );
        timePickerDialog.setTitle("选择提醒时间");
        timePickerDialog.show();
    }

    // ==================== 单位设置对话框 ====================

    private void showUnitDialog() {
        String[] options = {"公制（公斤/厘米）", "英制（磅/英寸）"};
        String current = sessionManager.getUnitSystem();
        int checkedItem = "imperial".equals(current) ? 1 : 0;

        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("选择单位")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String unit = which == 1 ? "imperial" : "metric";
                    sessionManager.setUnitSystem(unit);
                    updateUnitText();
                    Toast.makeText(requireContext(),
                            which == 1 ? "已切换为英制单位" : "已切换为公制单位",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== 清除缓存 ====================

    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("清除缓存")
                .setMessage("确定要清除本地缓存数据吗？这将清除训练记录、任务和计划的本地缓存，但不会影响服务器数据。")
                .setPositiveButton("确定", (dialog, which) -> {
                    sessionManager.clearLocalCache(requireContext());
                    binding.cacheSizeText.setText("缓存已清除");
                    Toast.makeText(requireContext(), "缓存已清除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== 数据导出 ====================

    private void exportData() {
        Toast.makeText(requireContext(), "正在导出数据...", Toast.LENGTH_SHORT).show();
        sessionManager.exportData(new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onSuccess(java.util.Map<String, Object> data) {
                if (!isAdded()) return;
                StringBuilder sb = new StringBuilder();
                sb.append("=== 燃动时刻 - 个人数据导出 ===\n\n");

                // Profile
                sb.append("--- 个人信息 ---\n");
                if (data.containsKey("profile")) {
                    java.util.Map<String, Object> profile = (java.util.Map<String, Object>) data.get("profile");
                    sb.append("昵称: ").append(profile.getOrDefault("nickname", "")).append("\n");
                    sb.append("手机: ").append(profile.getOrDefault("phone", "")).append("\n");
                    sb.append("身高: ").append(profile.getOrDefault("height", 0)).append("cm\n");
                    sb.append("体重: ").append(profile.getOrDefault("weight", 0)).append("kg\n");
                }

                // Body records
                if (data.containsKey("body_records")) {
                    java.util.List<?> records = (java.util.List<?>) data.get("body_records");
                    sb.append("\n--- 身体记录 (").append(records.size()).append("条) ---\n");
                    for (Object r : records) {
                        java.util.Map<String, Object> rec = (java.util.Map<String, Object>) r;
                        sb.append("身高: ").append(rec.get("height")).append("cm, ")
                          .append("体重: ").append(rec.get("weight")).append("kg\n");
                    }
                }

                // Workout records
                if (data.containsKey("workout_records")) {
                    java.util.List<?> records = (java.util.List<?>) data.get("workout_records");
                    sb.append("\n--- 训练记录 (").append(records.size()).append("条) ---\n");
                    for (Object r : records) {
                        java.util.Map<String, Object> rec = (java.util.Map<String, Object>) r;
                        sb.append(rec.getOrDefault("exercise_name", "")).append(" - ")
                          .append(rec.getOrDefault("duration", 0)).append("分钟, ")
                          .append(rec.getOrDefault("calories", 0)).append("千卡\n");
                    }
                }

                String exportText = sb.toString();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "燃动时刻 - 数据导出");
                sendIntent.putExtra(Intent.EXTRA_TEXT, exportText);
                startActivity(Intent.createChooser(sendIntent, "导出数据"));
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "导出失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== 修改密码 ====================

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        if (dialogView == null) {
            // Fallback: simple programmatic dialog
            showSimpleChangePasswordDialog();
            return;
        }

        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("修改密码")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    EditText oldPwd = dialogView.findViewById(R.id.oldPasswordInput);
                    EditText newPwd = dialogView.findViewById(R.id.newPasswordInput);
                    EditText confirmPwd = dialogView.findViewById(R.id.confirmPasswordInput);

                    String old = oldPwd.getText().toString().trim();
                    String newP = newPwd.getText().toString().trim();
                    String confirm = confirmPwd.getText().toString().trim();

                    if (old.isEmpty() || newP.isEmpty() || confirm.isEmpty()) {
                        Toast.makeText(requireContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newP.length() < 6) {
                        Toast.makeText(requireContext(), "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newP.equals(confirm)) {
                        Toast.makeText(requireContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sessionManager.changePassword(old, newP, new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                        @Override
                        public void onSuccess(java.util.Map<String, Object> data) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "修改失败: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSimpleChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        EditText oldPwdInput = new EditText(requireContext());
        oldPwdInput.setHint("当前密码");
        oldPwdInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPwdInput);

        EditText newPwdInput = new EditText(requireContext());
        newPwdInput.setHint("新密码（至少6位）");
        newPwdInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPwdInput);

        EditText confirmPwdInput = new EditText(requireContext());
        confirmPwdInput.setHint("确认新密码");
        confirmPwdInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPwdInput);

        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("修改密码")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String old = oldPwdInput.getText().toString().trim();
                    String newP = newPwdInput.getText().toString().trim();
                    String confirm = confirmPwdInput.getText().toString().trim();

                    if (old.isEmpty() || newP.isEmpty() || confirm.isEmpty()) {
                        Toast.makeText(requireContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newP.length() < 6) {
                        Toast.makeText(requireContext(), "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newP.equals(confirm)) {
                        Toast.makeText(requireContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sessionManager.changePassword(old, newP, new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                        @Override
                        public void onSuccess(java.util.Map<String, Object> data) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "修改失败: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== 退出登录 ====================

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    sessionManager.logout();
                    Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
                    // Navigate to login screen
                    navController.navigate(R.id.navigation_login);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== 注销账号 ====================

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("注销账号")
                .setMessage("此操作不可恢复！确定要注销账号吗？\n\n注销后将删除您的所有数据，包括训练记录、身体数据和成就。")
                .setPositiveButton("确定注销", (dialog, which) -> {
                    // Second confirmation
                    new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                            .setTitle("再次确认")
                            .setMessage("请输入[确认]来完成注销")
                            .setPositiveButton("确认", (d2, w2) -> {
                                sessionManager.deleteAccount(new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                                    @Override
                                    public void onSuccess(java.util.Map<String, Object> data) {
                                        if (!isAdded()) return;
                                        sessionManager.logout();
                                        Toast.makeText(requireContext(), "账号已注销", Toast.LENGTH_SHORT).show();
                                        navController.navigate(R.id.navigation_login);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        if (!isAdded()) return;
                                        Toast.makeText(requireContext(), "注销失败: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("取消", null)
                            .show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== 意见反馈 ====================

    private void showFeedbackDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        // 分类选择
        TextView categoryLabel = new TextView(requireContext());
        categoryLabel.setText("反馈类型");
        categoryLabel.setTextSize(14);
        categoryLabel.setTextColor(getResources().getColor(R.color.text_primary, null));
        categoryLabel.setPadding(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
        layout.addView(categoryLabel);

        String[] categories = {"BUG反馈", "功能建议", "其他"};
        String[] categoryValues = {"bug", "suggestion", "other"};
        final int[] selectedCategory = {2}; // 默认"其他"

        RadioGroup categoryGroup = new RadioGroup(requireContext());
        categoryGroup.setOrientation(RadioGroup.HORIZONTAL);
        for (int i = 0; i < categories.length; i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(categories[i]);
            rb.setId(i);
            rb.setPadding(0, 0, (int) (16 * getResources().getDisplayMetrics().density), 0);
            categoryGroup.addView(rb);
            if (i == selectedCategory[0]) rb.setChecked(true);
        }
        categoryGroup.setOnCheckedChangeListener((group, checkedId) -> selectedCategory[0] = checkedId);
        layout.addView(categoryGroup);

        // 反馈内容
        EditText feedbackInput = new EditText(requireContext());
        feedbackInput.setHint("请输入您的意见或建议");
        feedbackInput.setMinLines(4);
        feedbackInput.setGravity(android.view.Gravity.TOP);
        feedbackInput.setPadding(0, (int) (12 * getResources().getDisplayMetrics().density), 0, 0);
        layout.addView(feedbackInput);

        // 联系方式
        EditText contactInput = new EditText(requireContext());
        contactInput.setHint("联系方式（可选）");
        contactInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(contactInput);

        new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                .setTitle("意见反馈")
                .setView(layout)
                .setPositiveButton("提交", (dialog, which) -> {
                    String content = feedbackInput.getText().toString().trim();
                    String contact = contactInput.getText().toString().trim();
                    String category = categoryValues[selectedCategory[0]];

                    if (content.isEmpty()) {
                        Toast.makeText(requireContext(), "请输入反馈内容", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sessionManager.submitFeedback(content, contact.isEmpty() ? null : contact, category,
                            new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                        @Override
                        public void onSuccess(java.util.Map<String, Object> data) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "感谢您的反馈！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "提交失败: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== 给我们评分 ====================

    private void rateUs() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + requireContext().getPackageName())));
        } catch (Exception e) {
            // If no market app, open browser
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName())));
            } catch (Exception e2) {
                Toast.makeText(requireContext(), "无法打开应用商店", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==================== 消息中心 ====================

    private void loadUnreadMessageCount() {
        com.example.myapplication.api.ApiClient.getInstance(requireContext())
                .getUnreadFeedbackCount(new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> data) {
                        if (!isAdded() || binding == null) return;
                        int count = data.get("count") instanceof Number ? ((Number) data.get("count")).intValue() : 0;
                        if (count > 0) {
                            binding.messageBadge.setVisibility(android.view.View.VISIBLE);
                            binding.messageBadge.setText(String.valueOf(count));
                        } else {
                            binding.messageBadge.setVisibility(android.view.View.GONE);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // 静默失败
                    }
                });
    }

    private void showMessageCenter() {
        com.example.myapplication.api.ApiClient.getInstance(requireContext())
                .getUserFeedback(new com.example.myapplication.api.ApiClient.Callback<java.util.List<java.util.Map<String, Object>>>() {
                    @Override
                    public void onSuccess(java.util.List<java.util.Map<String, Object>> data) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (data == null || data.isEmpty()) {
                                Toast.makeText(requireContext(), "暂无反馈记录", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            LinearLayout listView = new LinearLayout(requireContext());
                            listView.setOrientation(LinearLayout.VERTICAL);
                            listView.setPadding(0, 8, 0, 8);

                            final long[] ids = new long[data.size()];
                            for (int i = 0; i < data.size(); i++) {
                                java.util.Map<String, Object> item = data.get(i);
                                ids[i] = item.get("id") instanceof Number ? ((Number) item.get("id")).longValue() : 0;

                                View itemView = LayoutInflater.from(requireContext())
                                        .inflate(R.layout.item_feedback_list, listView, false);

                                String content = String.valueOf(item.getOrDefault("content", ""));
                                String status = String.valueOf(item.getOrDefault("status", "pending"));
                                String category = String.valueOf(item.getOrDefault("category", "other"));
                                String lastMessage = String.valueOf(item.getOrDefault("last_message", ""));

                                String categoryText;
                                switch (category) {
                                    case "bug": categoryText = "BUG反馈"; break;
                                    case "suggestion": categoryText = "功能建议"; break;
                                    default: categoryText = "其他"; break;
                                }

                                ((TextView) itemView.findViewById(R.id.feedbackCategoryTag)).setText(categoryText);
                                TextView statusTag = itemView.findViewById(R.id.feedbackStatusTag);
                                statusTag.setText(getStatusText(status));
                                if ("resolved".equals(status)) {
                                    statusTag.setTextColor(0xFF2ED573);
                                } else if ("pending".equals(status)) {
                                    statusTag.setTextColor(0xFFFFA502);
                                } else {
                                    statusTag.setTextColor(0xFF3498DB);
                                }

                                String preview = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                                ((TextView) itemView.findViewById(R.id.feedbackContentPreview)).setText(preview);

                                TextView lastMsgView = itemView.findViewById(R.id.feedbackLastMessage);
                                if (lastMessage != null && !lastMessage.isEmpty() && !lastMessage.equals("null")) {
                                    String displayMsg = lastMessage.length() > 35 ? lastMessage.substring(0, 35) + "..." : lastMessage;
                                    lastMsgView.setText("最新: " + displayMsg);
                                    lastMsgView.setVisibility(View.VISIBLE);
                                }

                                final int index = i;
                                itemView.setOnClickListener(v -> showFeedbackChat(ids[index]));

                                listView.addView(itemView);
                            }

                            new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                                    .setTitle("消息中心")
                                    .setView(listView)
                                    .setNegativeButton("关闭", null)
                                    .show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private android.app.AlertDialog chatDialog;

    private void showFeedbackChat(long feedbackId) {
        com.example.myapplication.api.ApiClient.getInstance(requireContext())
                .getFeedbackMessages(feedbackId, new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> data) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            String status = String.valueOf(data.getOrDefault("status", "pending"));
                            @SuppressWarnings("unchecked")
                            java.util.List<java.util.Map<String, Object>> messages =
                                    (java.util.List<java.util.Map<String, Object>>) data.get("messages");
                            if (messages == null) messages = new java.util.ArrayList<>();

                            boolean isResolved = "resolved".equals(status);

                            View chatView = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.dialog_feedback_chat, null);

                            // 设置状态标签
                            TextView statusText = chatView.findViewById(R.id.chatStatusText);
                            TextView categoryText = chatView.findViewById(R.id.chatCategoryText);
                            statusText.setText(getStatusText(status));
                            if (isResolved) {
                                statusText.setBackgroundResource(R.drawable.bg_status_tag_resolved);
                                statusText.setTextColor(0xFF2ED573);
                            } else {
                                statusText.setBackgroundResource(R.drawable.bg_status_tag);
                                statusText.setTextColor(0xFFFF6B35);
                            }

                            // 已解决提示
                            TextView resolvedHint = chatView.findViewById(R.id.chatResolvedHint);
                            LinearLayout inputArea = chatView.findViewById(R.id.chatInputArea);
                            if (isResolved) {
                                resolvedHint.setVisibility(View.VISIBLE);
                                inputArea.setVisibility(View.GONE);
                            } else {
                                resolvedHint.setVisibility(View.GONE);
                                inputArea.setVisibility(View.VISIBLE);
                            }

                            // 渲染消息
                            LinearLayout container = chatView.findViewById(R.id.chatMessageContainer);
                            container.removeAllViews();
                            if (messages.isEmpty()) {
                                TextView empty = new TextView(requireContext());
                                empty.setText("暂无消息");
                                empty.setTextSize(13);
                                empty.setTextColor(0xFF999999);
                                empty.setGravity(android.view.Gravity.CENTER);
                                empty.setPadding(0, 40, 0, 40);
                                container.addView(empty);
                            } else {
                                for (java.util.Map<String, Object> msg : messages) {
                                    addChatMessage(container, msg);
                                }
                            }

                            // 构建弹窗
                            chatDialog = new AlertDialog.Builder(requireContext(), R.style.DarkDialog)
                                    .setTitle("对话详情")
                                    .setView(chatView)
                                    .setNegativeButton("关闭", null)
                                    .create();

                            chatDialog.show();

                            // 发送按钮
                            EditText chatInput = chatView.findViewById(R.id.chatInput);
                            TextView sendBtn = chatView.findViewById(R.id.chatSendBtn);
                            sendBtn.setOnClickListener(v -> {
                                String message = chatInput.getText().toString().trim();
                                if (message.isEmpty()) {
                                    Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                sendBtn.setEnabled(false);
                                sendBtn.setText("发送中");
                                com.example.myapplication.api.ApiClient.getInstance(requireContext())
                                        .sendFeedbackMessage(feedbackId, message, new com.example.myapplication.api.ApiClient.Callback<java.util.Map<String, Object>>() {
                                            @Override
                                            public void onSuccess(java.util.Map<String, Object> result) {
                                                if (!isAdded()) return;
                                                requireActivity().runOnUiThread(() -> {
                                                    chatInput.setText("");
                                                    sendBtn.setEnabled(true);
                                                    sendBtn.setText("发送");
                                                    // 重新加载对话
                                                    showFeedbackChat(feedbackId);
                                                });
                                            }

                                            @Override
                                            public void onError(String error) {
                                                if (!isAdded()) return;
                                                requireActivity().runOnUiThread(() -> {
                                                    sendBtn.setEnabled(true);
                                                    sendBtn.setText("发送");
                                                    Toast.makeText(requireContext(), "发送失败: " + error, Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        });
                            });

                            // 标记已读
                            com.example.myapplication.api.ApiClient.getInstance(requireContext())
                                    .markFeedbackRead(feedbackId, null);
                            loadUnreadMessageCount();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void addChatMessage(LinearLayout container, java.util.Map<String, Object> msg) {
        String sender = String.valueOf(msg.getOrDefault("sender_type", ""));
        String content = String.valueOf(msg.getOrDefault("content", ""));
        String time = String.valueOf(msg.getOrDefault("created_at", ""));

        boolean isAdmin = "admin".equals(sender);
        View bubbleView = LayoutInflater.from(requireContext()).inflate(R.layout.item_chat_message, container, false);

        LinearLayout adminLayout = bubbleView.findViewById(R.id.adminMessageLayout);
        LinearLayout userLayout = bubbleView.findViewById(R.id.userMessageLayout);

        if (isAdmin) {
            adminLayout.setVisibility(View.VISIBLE);
            userLayout.setVisibility(View.GONE);
            ((TextView) bubbleView.findViewById(R.id.adminContentText)).setText(content);
            TextView timeView = bubbleView.findViewById(R.id.adminTimeText);
            timeView.setText(formatTimestamp(time));
        } else {
            userLayout.setVisibility(View.VISIBLE);
            adminLayout.setVisibility(View.GONE);
            ((TextView) bubbleView.findViewById(R.id.userContentText)).setText(content);
            TextView timeView = bubbleView.findViewById(R.id.userTimeText);
            timeView.setText(formatTimestamp(time));
        }

        container.addView(bubbleView);
    }

    private String formatTimestamp(String time) {
        try {
            long ts = Long.parseLong(time);
            if (ts > 9999999999L) ts = ts / 1000;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(ts * 1000));
        } catch (NumberFormatException e) {
            return time;
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "待处理";
            case "processing": return "处理中";
            case "replied": return "已回复";
            case "resolved": return "已解决";
            default: return "待处理";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
