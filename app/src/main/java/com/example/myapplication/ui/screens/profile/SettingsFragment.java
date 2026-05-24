package com.example.myapplication.ui.screens.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });
    }

    private void loadSettings() {
        // Load notification settings
        binding.workoutReminderSwitch.setChecked(sessionManager.isWorkoutReminderEnabled());
        binding.achievementSwitch.setChecked(sessionManager.isAchievementNotificationEnabled());
    }

    private void setupListeners() {
        // Workout reminder switch
        binding.workoutReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setWorkoutReminderEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "训练提醒已开启" : "训练提醒已关闭", Toast.LENGTH_SHORT).show();
        });

        // Achievement notification switch
        binding.achievementSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setAchievementNotificationEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "成就通知已开启" : "成就通知已关闭", Toast.LENGTH_SHORT).show();
        });

        // Privacy policy
        binding.privacyPolicyItem.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "隐私协议", Toast.LENGTH_SHORT).show();
        });

        // Terms of service
        binding.termsItem.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "服务条款", Toast.LENGTH_SHORT).show();
        });

        // About us
        binding.aboutUsItem.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "关于我们", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
