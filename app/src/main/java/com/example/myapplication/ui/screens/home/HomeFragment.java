package com.example.myapplication.ui.screens.home;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.util.SessionManager;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NavController navController;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());

        setupHeader();
        setupWeekDays();
        setupListeners();
    }

    private void setupHeader() {
        // Set greeting based on time of day
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        } else if (hour < 18) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_evening);
        }
        binding.greetingText.setText(greeting);

        // Set user name from session
        String nickname = sessionManager.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = "健身爱好者";
        }
        binding.userName.setText(nickname);

        // Set date
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        binding.dateBadge.setText(month + "月" + day + "日 " + weekdays[dayOfWeek - 1]);
    }

    private void setupWeekDays() {
        binding.weekDaysContainer.removeAllViews();

        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};

        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            View dayView = createDayView(weekdays[i], dayIndex);
            binding.weekDaysContainer.addView(dayView);
        }
    }

    private View createDayView(String dayLabel, int index) {
        Calendar calendar = Calendar.getInstance();
        int todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Monday = 0

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        container.setLayoutParams(params);

        // Day label
        TextView labelView = new TextView(requireContext());
        labelView.setText(dayLabel);
        labelView.setTextSize(12);
        labelView.setTextColor(ContextCompat.getColor(requireContext(),
                index == todayIndex ? R.color.primary : R.color.text_muted));
        labelView.setGravity(Gravity.CENTER);

        // Indicator
        View indicator = new View(requireContext());
        int indicatorSize = (int) (32 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(indicatorSize, indicatorSize);
        indicatorParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        indicator.setLayoutParams(indicatorParams);
        indicator.setBackgroundResource(R.drawable.bg_avatar_placeholder);

        // If today, set different background
        if (index == todayIndex) {
            indicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }

        container.addView(labelView);
        container.addView(indicator);

        container.setPadding(8, 16, 8, 16);

        return container;
    }

    private void setupListeners() {
        // VIP card
        binding.vipCard.setOnClickListener(v -> {
            navController.navigate(R.id.action_home_to_vip);
        });

        // Body data card
        binding.bodyDataCard.setOnClickListener(v -> {
            // Navigate to progress tab
        });

        // Training card
        binding.trainingCard.setOnClickListener(v -> {
            // Navigate to training detail
        });

        // Start training button
        binding.startTrainingBtn.setOnClickListener(v -> {
            // Navigate to training
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
