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
import com.example.myapplication.model.TrainingTask;
import com.example.myapplication.model.WorkoutRecord;
import com.example.myapplication.util.TrainingTaskManager;
import com.example.myapplication.util.WorkoutRecordManager;

import java.util.Calendar;
import java.util.List;

public class TrainingFragment extends Fragment {

    private FragmentTrainingBinding binding;
    private TrainingTaskManager trainingTaskManager;
    private WorkoutRecordManager workoutRecordManager;
    private int currentWeek = 1;
    private int selectedDayIndex = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingTaskManager = TrainingTaskManager.getInstance(requireContext());
        workoutRecordManager = WorkoutRecordManager.getInstance(requireContext());
    }

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
        updateTrainingDetail();
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

        // Duration/Status text - show task count for this day
        TextView statusView = new TextView(requireContext());
        List<TrainingTask> dayTasks = getTasksForDay(index);
        int taskCount = dayTasks.size();
        int completedCount = 0;
        for (TrainingTask task : dayTasks) {
            if (task.isCompleted()) completedCount++;
        }
        if (taskCount > 0) {
            statusView.setText(completedCount + "/" + taskCount);
        } else {
            statusView.setText("--");
        }
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

    private List<TrainingTask> getTasksForDay(int dayIndex) {
        Calendar today = Calendar.getInstance();
        int todayIndex = (today.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        // Calculate the target day based on the current week
        int daysToAdd = dayIndex - todayIndex;
        if (currentWeek > 1) {
            daysToAdd += (currentWeek - 1) * 7;
        }

        Calendar targetDay = (Calendar) today.clone();
        targetDay.add(Calendar.DAY_OF_MONTH, daysToAdd);
        targetDay.set(Calendar.HOUR_OF_DAY, 0);
        targetDay.set(Calendar.MINUTE, 0);
        targetDay.set(Calendar.SECOND, 0);
        targetDay.set(Calendar.MILLISECOND, 0);
        long dayStart = targetDay.getTimeInMillis();

        targetDay.add(Calendar.DAY_OF_MONTH, 1);
        long dayEnd = targetDay.getTimeInMillis();

        List<TrainingTask> allTasks = trainingTaskManager.getTasks();
        List<TrainingTask> dayTasks = new java.util.ArrayList<>();
        for (TrainingTask task : allTasks) {
            if (task.getDate() >= dayStart && task.getDate() < dayEnd) {
                dayTasks.add(task);
            }
        }
        return dayTasks;
    }

    private void selectDay(int index) {
        selectedDayIndex = index;
        setupWeekCalendar();
        updateTrainingDetail();
    }

    private void updateTrainingDetail() {
        List<TrainingTask> dayTasks = getTasksForDay(selectedDayIndex);

        if (dayTasks.isEmpty()) {
            // No tasks for this day
            binding.trainingDetailCard.setVisibility(View.VISIBLE);
            binding.restDayCard.setVisibility(View.GONE);
            binding.trainingTitle.setText("今日无训练计划");
            binding.trainingBadge.setText("休息");
            binding.trainingDuration.setText("0分钟");
            binding.trainingActions.setText("0个动作");
            binding.trainingCalories.setText("约0千卡");
            binding.actionListContainer.removeAllViews();

            // Add hint text
            TextView hintView = new TextView(requireContext());
            hintView.setText("点击下方按钮添加训练任务");
            hintView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            hintView.setTextSize(14);
            hintView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            hintParams.setMargins(0, 32, 0, 32);
            hintView.setLayoutParams(hintParams);
            binding.actionListContainer.addView(hintView);

            // Update week summary
            updateWeekSummary();
        } else {
            binding.trainingDetailCard.setVisibility(View.VISIBLE);
            binding.restDayCard.setVisibility(View.GONE);

            // Calculate total duration, completed count, and calories
            int totalDuration = 0;
            int completedCount = 0;
            float totalCalories = 0;
            for (TrainingTask task : dayTasks) {
                totalDuration += task.getDuration();
                if (task.isCompleted()) {
                    completedCount++;
                }
            }

            // Calculate calories from completed tasks only
            List<WorkoutRecord> allRecords = workoutRecordManager.getAllRecords();
            for (WorkoutRecord record : allRecords) {
                for (TrainingTask task : dayTasks) {
                    if (task.isCompleted() && record.getTaskId() == task.getId()) {
                        totalCalories += record.getCaloriesBurned();
                    }
                }
            }

            binding.trainingTitle.setText(totalDuration + "分钟训练");
            binding.trainingBadge.setText(dayTasks.size() + "个动作");
            binding.trainingDuration.setText(totalDuration + "分钟");
            binding.trainingActions.setText(completedCount + "/" + dayTasks.size() + "完成");
            binding.trainingCalories.setText("约" + (int) totalCalories + "千卡");

            // Display task list
            binding.actionListContainer.removeAllViews();
            for (TrainingTask task : dayTasks) {
                View taskView = createTrainingTaskItem(task);
                binding.actionListContainer.addView(taskView);
            }

            // Update week summary
            updateWeekSummary();
        }
    }

    private void updateWeekSummary() {
        // Calculate stats for the current week
        Calendar weekStart = Calendar.getInstance();
        weekStart.add(Calendar.DAY_OF_MONTH, -(currentWeek - 1) * 7);
        int weekDayIndex = (weekStart.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        weekStart.add(Calendar.DAY_OF_MONTH, -weekDayIndex);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);

        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 7);

        List<TrainingTask> allTasks = trainingTaskManager.getTasks();
        int completedDays = 0;
        int totalDays = 0;
        int remainingDays = 0;

        // Track which days have completed tasks
        boolean[] daysWithCompletedTasks = new boolean[7];
        boolean[] daysWithTasks = new boolean[7];

        for (TrainingTask task : allTasks) {
            long taskDate = task.getDate();
            if (taskDate >= weekStart.getTimeInMillis() && taskDate < weekEnd.getTimeInMillis()) {
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTimeInMillis(taskDate);
                int dayIndex = (taskCal.get(Calendar.DAY_OF_WEEK) + 5) % 7;

                daysWithTasks[dayIndex] = true;
                if (task.isCompleted()) {
                    daysWithCompletedTasks[dayIndex] = true;
                }
            }
        }

        for (int i = 0; i < 7; i++) {
            if (daysWithTasks[i]) {
                totalDays++;
                if (daysWithCompletedTasks[i]) {
                    completedDays++;
                } else {
                    remainingDays++;
                }
            }
        }

        int completionRate = totalDays > 0 ? (completedDays * 100) / totalDays : 0;

        binding.completedDays.setText(String.valueOf(completedDays));
        binding.remainingDays.setText(String.valueOf(remainingDays));
        binding.completionRate.setText(completionRate + "%");
        binding.progressBar.setProgress(completionRate);
    }

    private View createTrainingTaskItem(TrainingTask task) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(16, 12, 16, 12);

        // Status indicator
        View statusIndicator = new View(requireContext());
        int size = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(size, size);
        statusIndicator.setLayoutParams(indicatorParams);
        if (task.isCompleted()) {
            statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.success));
        } else {
            statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.warning));
        }

        // Task name
        TextView nameView = new TextView(requireContext());
        nameView.setText(task.getName());
        nameView.setTextSize(14);
        nameView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameParams.setMargins(12, 0, 12, 0);
        nameView.setLayoutParams(nameParams);

        // Duration
        TextView durationView = new TextView(requireContext());
        durationView.setText(task.getDuration() + "分钟");
        durationView.setTextSize(12);
        durationView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));

        container.addView(statusIndicator);
        container.addView(nameView);
        container.addView(durationView);

        return container;
    }

    private void setupListeners() {
        binding.prevWeekBtn.setOnClickListener(v -> {
            if (currentWeek > 1) {
                currentWeek--;
                updateWeekLabel();
                setupWeekCalendar();
                updateTrainingDetail();
            }
        });

        binding.nextWeekBtn.setOnClickListener(v -> {
            currentWeek++;
            updateWeekLabel();
            setupWeekCalendar();
            updateTrainingDetail();
        });

        binding.startTrainingBtn.setOnClickListener(v -> {
            // Navigate to home to add tasks
        });
    }

    private void updateWeekLabel() {
        binding.weekLabel.setText("第" + currentWeek + "周");
    }

    @Override
    public void onResume() {
        super.onResume();
        setupWeekCalendar();
        updateTrainingDetail();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
