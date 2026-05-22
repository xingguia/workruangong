package com.example.myapplication.ui.screens.training;

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

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentTrainingBinding;

import java.util.Calendar;

public class TrainingFragment extends Fragment {

    private FragmentTrainingBinding binding;
    private int currentWeek = 1;
    private int selectedDayIndex = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTrainingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupWeekCalendar();
        setupListeners();
        updateWeekLabel();
    }

    private void setupWeekCalendar() {
        binding.weekCalendarContainer.removeAllViews();

        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};

        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            View dayView = createDayView(weekdays[i], dayIndex);
            binding.weekCalendarContainer.addView(dayView);
        }
    }

    private View createDayView(String dayLabel, int index) {
        Calendar calendar = Calendar.getInstance();
        int todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(16, 12, 16, 12);
        container.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (60 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 8, 0);
        container.setLayoutParams(params);

        container.setOnClickListener(v -> selectDay(index));

        // Day label
        TextView labelView = new TextView(requireContext());
        labelView.setText(dayLabel);
        labelView.setTextSize(12);
        labelView.setTextColor(ContextCompat.getColor(requireContext(),
                index == todayIndex ? R.color.primary : R.color.text_secondary));
        labelView.setGravity(Gravity.CENTER);

        // Duration/Status text
        TextView statusView = new TextView(requireContext());
        statusView.setText("30分钟");
        statusView.setTextSize(10);
        statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        statusView.setGravity(Gravity.CENTER);

        // If today, set selected state
        if (index == todayIndex) {
            container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            selectedDayIndex = index;
        }

        container.addView(labelView);
        container.addView(statusView);

        return container;
    }

    private void selectDay(int index) {
        selectedDayIndex = index;
        setupWeekCalendar();
        updateTrainingDetail();
    }

    private void updateTrainingDetail() {
        // For demo purposes, show training detail for selected day
        binding.trainingDetailCard.setVisibility(View.VISIBLE);
        binding.restDayCard.setVisibility(View.GONE);

        // Update training info based on selection
        String[] trainingNames = {"上肢力量训练", "核心训练", "下肢力量训练", "HIIT燃脂", "全身训练", "休息日", "有氧训练"};
        String[] trainingTypes = {"力量", "核心", "力量", "HIIT", "综合", "休息", "有氧"};

        binding.trainingTitle.setText(trainingNames[selectedDayIndex]);
        binding.trainingBadge.setText(trainingTypes[selectedDayIndex]);

        if (selectedDayIndex == 5) { // Saturday is rest day
            binding.trainingDetailCard.setVisibility(View.GONE);
            binding.restDayCard.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        binding.prevWeekBtn.setOnClickListener(v -> {
            if (currentWeek > 1) {
                currentWeek--;
                updateWeekLabel();
            }
        });

        binding.nextWeekBtn.setOnClickListener(v -> {
            currentWeek++;
            updateWeekLabel();
        });

        binding.startTrainingBtn.setOnClickListener(v -> {
            // Start training
        });
    }

    private void updateWeekLabel() {
        binding.weekLabel.setText("第" + currentWeek + "周");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
