package com.example.myapplication.ui.screens.training;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentTrainingBinding;
import com.example.myapplication.model.ExerciseDatabase;
import com.example.myapplication.model.ExercisePlan;
import com.example.myapplication.model.TrainingTask;
import com.example.myapplication.model.WorkoutRecord;
import com.example.myapplication.util.AchievementManager;
import com.example.myapplication.util.ExercisePlanManager;
import com.example.myapplication.util.TrainingTaskManager;
import com.example.myapplication.util.WorkoutRecordManager;

import java.util.Calendar;
import java.util.List;

public class TrainingFragment extends Fragment {

    private FragmentTrainingBinding binding;
    private TrainingTaskManager trainingTaskManager;
    private WorkoutRecordManager workoutRecordManager;
    private ExercisePlanManager exercisePlanManager;
    private AchievementManager achievementManager;
    private int currentWeek = 1;
    private int selectedDayIndex = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingTaskManager = TrainingTaskManager.getInstance(requireContext());
        workoutRecordManager = WorkoutRecordManager.getInstance(requireContext());
        exercisePlanManager = ExercisePlanManager.getInstance(requireContext());
        achievementManager = AchievementManager.getInstance(requireContext());
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
        updateWeekProgress();
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
        labelView.setTextSize(12);
        labelView.setGravity(Gravity.CENTER);

        // Duration/Status text - show task count for this day
        TextView statusView = new TextView(requireContext());
        List<TrainingTask> dayTasks = getTasksForDay(index);
        int taskCount = dayTasks.size();
        int completedCount = 0;
        for (TrainingTask task : dayTasks) {
            if (task.isCompleted()) completedCount++;
        }

        // 判断这一天应该如何显示
        boolean isCompletedDay = taskCount > 0 && completedCount == taskCount; // 全部完成
        boolean isPartialComplete = taskCount > 0 && completedCount > 0; // 部分完成
        boolean isRestDay = (index == 5 || index == 6) && taskCount == 0; // 休息日（周六日且没有任务）
        boolean isPastDayWithNoTask = index < todayIndex && taskCount == 0; // 过去的日期且没有任务

        // 过去的日期没有任务时显示"休"
        if (isPastDayWithNoTask) {
            labelView.setText("休");
            statusView.setText("--");
        } else {
            labelView.setText(dayLabel);
            if (taskCount > 0) {
                statusView.setText(completedCount + "/" + taskCount);
            } else {
                statusView.setText("--");
            }
        }
        statusView.setTextSize(10);
        statusView.setGravity(Gravity.CENTER);

        // 设置颜色
        if (index == todayIndex) {
            // 今天：显示蓝色（无论有没有任务）
            container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            selectedDayIndex = index;
        } else if (isCompletedDay) {
            // 已全部完成：显示绿色
            container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.success));
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else if (isPastDayWithNoTask || isRestDay) {
            // 休息日（过去没有任务的日期 或 周六日没有任务）：显示默认灰色
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        } else if (isPartialComplete) {
            // 部分完成：显示黄色/橙色
            container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.warning));
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        } else {
            // 有任务但未开始（工作日）：显示蓝色
            container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
            labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
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

        // Normalize to start of day
        targetDay.set(Calendar.HOUR_OF_DAY, 0);
        targetDay.set(Calendar.MINUTE, 0);
        targetDay.set(Calendar.SECOND, 0);
        targetDay.set(Calendar.MILLISECOND, 0);

        List<TrainingTask> allTasks = trainingTaskManager.getTasks();
        List<TrainingTask> dayTasks = new java.util.ArrayList<>();
        for (TrainingTask task : allTasks) {
            Calendar taskDay = Calendar.getInstance();
            taskDay.setTimeInMillis(task.getDate());
            taskDay.set(Calendar.HOUR_OF_DAY, 0);
            taskDay.set(Calendar.MINUTE, 0);
            taskDay.set(Calendar.SECOND, 0);
            taskDay.set(Calendar.MILLISECOND, 0);

            if (taskDay.getTimeInMillis() == targetDay.getTimeInMillis()) {
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
        binding.tasksContainer.removeAllViews();

        List<TrainingTask> dayTasks = getTasksForDay(selectedDayIndex);

        if (dayTasks.isEmpty()) {
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            binding.tasksScrollView.setVisibility(View.GONE);
            binding.summaryContainer.setVisibility(View.GONE);
        } else {
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.tasksScrollView.setVisibility(View.VISIBLE);
            binding.summaryContainer.setVisibility(View.VISIBLE);

            for (TrainingTask task : dayTasks) {
                View taskView = createTrainingTaskItem(task);
                binding.tasksContainer.addView(taskView);
            }

            // Update summary
            int completedCount = 0;
            for (TrainingTask task : dayTasks) {
                if (task.isCompleted()) completedCount++;
            }
            binding.summaryText.setText(String.format("%d/%d 项已完成", completedCount, dayTasks.size()));
        }
    }

    private View createTrainingTaskItem(TrainingTask task) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(16, 12, 16, 12);
        container.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 8);
        container.setLayoutParams(params);

        // Header row
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        // Checkbox
        ImageView checkbox = new ImageView(requireContext());
        int size = (int) (24 * getResources().getDisplayMetrics().density);
        checkbox.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        checkbox.setImageResource(task.isCompleted() ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
        checkbox.setColorFilter(ContextCompat.getColor(requireContext(),
                task.isCompleted() ? R.color.success : R.color.text_muted));
        checkbox.setOnClickListener(v -> toggleTaskComplete(task));
        headerRow.addView(checkbox);

        // Task info
        LinearLayout infoContainer = new LinearLayout(requireContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(12, 0, 0, 0);
        infoContainer.setLayoutParams(infoParams);

        TextView nameView = new TextView(requireContext());
        nameView.setText(task.getName());
        nameView.setTextSize(16);
        nameView.setTextColor(ContextCompat.getColor(requireContext(),
                task.isCompleted() ? R.color.text_muted : R.color.text_primary));
        if (task.isCompleted()) {
            nameView.setPaintFlags(nameView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        infoContainer.addView(nameView);

        // Training details
        StringBuilder detailText = new StringBuilder();
        if (task.getSets() > 0) {
            detailText.append(task.getSets()).append("组");
        }
        if (task.getReps() > 0) {
            detailText.append(" × ").append(task.getReps()).append("次");
        }
        if (task.getWeight() > 0) {
            detailText.append(" · ").append(task.getWeight()).append("kg");
        }
        if (task.getMuscleGroup() != null) {
            detailText.append(" · ").append(task.getMuscleGroup().getDisplayName());
        }

        if (detailText.length() > 0) {
            TextView detailView = new TextView(requireContext());
            detailView.setText(detailText.toString());
            detailView.setTextSize(12);
            detailView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            infoContainer.addView(detailView);
        }

        headerRow.addView(infoContainer);

        // Delete button
        ImageView deleteBtn = new ImageView(requireContext());
        deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        deleteBtn.setImageResource(R.drawable.ic_delete);
        deleteBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_muted));
        deleteBtn.setOnClickListener(v -> deleteTask(task));
        headerRow.addView(deleteBtn);

        container.addView(headerRow);

        return container;
    }

    private void toggleTaskComplete(TrainingTask task) {
        if (task.isCompleted()) {
            task.setStatus(TrainingTask.TaskStatus.NOT_STARTED);
            task.setCaloriesRecorded(false);
            trainingTaskManager.updateTask(task);
            // 取消完成时删除对应的训练记录
            workoutRecordManager.deleteRecordByTaskId(task.getId());
        } else {
            task.setStatus(TrainingTask.TaskStatus.COMPLETED);
            trainingTaskManager.updateTask(task);
            // 防止重复记录：检查是否已记录过卡路里
            if (!task.isCaloriesRecorded()) {
                saveWorkoutRecord(task);
                achievementManager.recordWorkoutCompletion(requireContext());
            }
        }

        syncTodayExercisePlan();
        updateTrainingDetail();
        setupWeekCalendar();
        updateWeekProgress();
    }

    private void deleteTask(TrainingTask task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除训练")
                .setMessage("确定要删除这个训练任务吗？")
                .setPositiveButton("删除", (d, w) -> {
                    trainingTaskManager.deleteTask(task.getId());
                    syncTodayExercisePlan();
                    updateTrainingDetail();
                    setupWeekCalendar();
                    updateWeekProgress();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveWorkoutRecord(TrainingTask task) {
        float calories = calculateCalories(task);
        int duration = task.getSets() > 0 && task.getReps() > 0 ? task.getSets() * task.getReps() * 3 / 60 + 1 : 0;
        WorkoutRecord record = new WorkoutRecord();
        record.setExerciseName(task.getName());
        record.setDate(System.currentTimeMillis());
        record.setDuration(duration);
        record.setCalories(calories);
        record.setCaloriesBurned(calories);
        record.setSets(task.getSets());
        record.setReps(task.getReps());
        record.setWeight(task.getWeight());
        record.setTaskId(task.getId());
        if (task.getMuscleGroup() != null) {
            record.setMuscleGroup(task.getMuscleGroup().name());
        }
        workoutRecordManager.addRecord(record);
        // 标记卡路里已记录，防止重复
        task.setCaloriesRecorded(true);
        trainingTaskManager.updateTask(task);
    }

    private float calculateCalories(TrainingTask task) {
        // 优先查找动作数据库中的基础卡路里系数
        List<ExerciseDatabase.Exercise> allExercises = ExerciseDatabase.getAllExercises();
        for (ExerciseDatabase.Exercise ex : allExercises) {
            if (ex.name.equals(task.getName())) {
                float calories = ex.baseCaloriesPerRep * task.getSets() * task.getReps();
                if (task.getWeight() > 0) {
                    calories += task.getWeight() * 0.05f * task.getSets();
                }
                return Math.max(calories, 1f);
            }
        }

        // 数据库中找不到则根据训练类型估算
        float baseCaloriesPerRep;
        switch (task.getExerciseType()) {
            case CARDIO:      baseCaloriesPerRep = 8f;  break;
            case HIIT:        baseCaloriesPerRep = 10f; break;
            case STRENGTH:    baseCaloriesPerRep = 5f;  break;
            case EQUIPMENT:   baseCaloriesPerRep = 6f;  break;
            case CORE:        baseCaloriesPerRep = 4f;  break;
            case FLEXIBILITY: baseCaloriesPerRep = 3f;  break;
            default:          baseCaloriesPerRep = 5f;  break;
        }

        float calories = baseCaloriesPerRep * task.getSets() * task.getReps();
        if (task.getWeight() > 0) {
            calories += task.getWeight() * 0.05f * task.getSets();
        }
        return Math.max(calories, 1f);
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

        binding.addTrainingBtn.setOnClickListener(v -> showAddTrainingDialog());
    }

    private void showAddTrainingDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_training_v2, null);

        // 找到控件
        LinearLayout muscleGroupContainer = dialogView.findViewById(R.id.muscleGroupContainer);
        TextView step2Label = dialogView.findViewById(R.id.step2Label);
        androidx.core.widget.NestedScrollView exerciseScrollView = dialogView.findViewById(R.id.exerciseScrollView);
        LinearLayout exerciseContainer = dialogView.findViewById(R.id.exerciseContainer);
        LinearLayout paramsContainer = dialogView.findViewById(R.id.paramsContainer);
        EditText setsInput = dialogView.findViewById(R.id.setsInput);
        EditText repsInput = dialogView.findViewById(R.id.repsInput);
        EditText weightInput = dialogView.findViewById(R.id.weightInput);
        TextView caloriesPreview = dialogView.findViewById(R.id.caloriesPreview);
        com.google.android.material.button.MaterialButton cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        com.google.android.material.button.MaterialButton confirmBtn = dialogView.findViewById(R.id.confirmBtn);

        // 加减按钮
        dialogView.findViewById(R.id.repsMinusBtn).setOnClickListener(v -> {
            int current = parseInt(setsInput.getText().toString());
            if (current > 1) setsInput.setText(String.valueOf(current - 1));
        });
        dialogView.findViewById(R.id.repsPlusBtn).setOnClickListener(v -> {
            int current = parseInt(setsInput.getText().toString());
            if (current < 20) setsInput.setText(String.valueOf(current + 1));
        });
        dialogView.findViewById(R.id.repsMinus2Btn).setOnClickListener(v -> {
            int current = parseInt(repsInput.getText().toString());
            if (current > 1) repsInput.setText(String.valueOf(current - 1));
        });
        dialogView.findViewById(R.id.repsPlus2Btn).setOnClickListener(v -> {
            int current = parseInt(repsInput.getText().toString());
            if (current < 30) repsInput.setText(String.valueOf(current + 1));
        });

        // 选择状态
        final ExerciseDatabase.MuscleGroup[] selectedGroup = {null};
        final ExerciseDatabase.Exercise[] selectedExercise = {null};

        // 第一步：显示肌群选择
        List<ExerciseDatabase.MuscleGroup> muscleGroups = ExerciseDatabase.getAllMuscleGroups();
        for (ExerciseDatabase.MuscleGroup group : muscleGroups) {
            View groupView = createMuscleGroupChip(group);
            groupView.setOnClickListener(v -> {
                // 取消之前的选择
                for (int i = 0; i < muscleGroupContainer.getChildCount(); i++) {
                    muscleGroupContainer.getChildAt(i).setBackgroundResource(R.drawable.bg_stat_icon);
                }
                // 选中当前
                groupView.setBackgroundResource(R.drawable.bg_stat_icon_selected);
                selectedGroup[0] = group;
                selectedExercise[0] = null;

                // 显示第二步
                step2Label.setVisibility(View.VISIBLE);
                exerciseScrollView.setVisibility(View.VISIBLE);
                paramsContainer.setVisibility(View.GONE);
                confirmBtn.setEnabled(false);

                // 加载该肌群的动作
                loadExercises(group, exerciseContainer, selectedExercise, () -> {
                    paramsContainer.setVisibility(View.VISIBLE);
                    confirmBtn.setEnabled(true);
                    updateCaloriesPreview(caloriesPreview, selectedExercise[0], setsInput, repsInput, weightInput);
                });
            });
            muscleGroupContainer.addView(groupView);
        }

        // 输入变化监听
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateCaloriesPreview(caloriesPreview, selectedExercise[0], setsInput, repsInput, weightInput);
            }
        };
        setsInput.addTextChangedListener(watcher);
        repsInput.addTextChangedListener(watcher);
        weightInput.addTextChangedListener(watcher);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        confirmBtn.setOnClickListener(v -> {
            if (selectedExercise[0] != null) {
                // 创建训练任务
                TrainingTask task = new TrainingTask();
                task.setName(selectedExercise[0].name);
                int sets = parseInt(setsInput.getText().toString());
                int reps = parseInt(repsInput.getText().toString());
                task.setSets(sets);
                task.setReps(reps);
                String weightStr = weightInput.getText().toString();
                task.setWeight(weightStr.isEmpty() ? 0 : Float.parseFloat(weightStr));
                // 设置预估时长
                task.setDuration(sets > 0 && reps > 0 ? sets * reps * 3 / 60 + 1 : 0);

                // 设置肌群
                if (selectedGroup[0] != null) {
                    task.setMuscleGroup(TrainingTask.MuscleGroup.valueOf(selectedGroup[0].name()));
                }

                // 设置日期为当前选中日期
                Calendar today = Calendar.getInstance();
                int todayIndex = (today.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                int daysToAdd = selectedDayIndex - todayIndex;
                if (currentWeek > 1) {
                    daysToAdd += (currentWeek - 1) * 7;
                }
                Calendar targetDay = (Calendar) today.clone();
                targetDay.add(Calendar.DAY_OF_MONTH, daysToAdd);
                targetDay.set(Calendar.HOUR_OF_DAY, 0);
                targetDay.set(Calendar.MINUTE, 0);
                targetDay.set(Calendar.SECOND, 0);
                targetDay.set(Calendar.MILLISECOND, 0);
                task.setDate(targetDay.getTimeInMillis());

                trainingTaskManager.addTask(task);
                dialog.dismiss();
                syncTodayExercisePlan();
                updateTrainingDetail();
                setupWeekCalendar();
                updateWeekProgress();
            }
        });

        dialog.show();
    }

    private View createMuscleGroupChip(ExerciseDatabase.MuscleGroup group) {
        TextView chip = new TextView(requireContext());
        chip.setText(group.getDisplayName());
        chip.setTextSize(14);
        chip.setPadding(24, 12, 24, 12);
        chip.setGravity(Gravity.CENTER);
        chip.setBackgroundResource(R.drawable.bg_stat_icon);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        chip.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return chip;
    }

    private void loadExercises(ExerciseDatabase.MuscleGroup group, LinearLayout container,
                               ExerciseDatabase.Exercise[] selectedExercise, Runnable onComplete) {
        container.removeAllViews();
        List<ExerciseDatabase.Exercise> exercises = ExerciseDatabase.getExercisesForGroup(group);

        for (ExerciseDatabase.Exercise exercise : exercises) {
            View itemView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_exercise_choice, container, false);

            TextView nameView = itemView.findViewById(R.id.exerciseName);
            TextView infoView = itemView.findViewById(R.id.exerciseInfo);
            ImageView selectedIcon = itemView.findViewById(R.id.selectedIcon);

            nameView.setText(exercise.name);
            StringBuilder info = new StringBuilder();
            if (exercise.needsEquipment) {
                info.append("需要器材");
            } else {
                info.append("徒手");
            }
            if (!exercise.subMuscles.isEmpty()) {
                info.append(" · ").append(exercise.subMuscles.get(0).getDisplayName());
            }
            infoView.setText(info.toString());

            itemView.setOnClickListener(v -> {
                // 取消之前的选择
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    ImageView icon = child.findViewById(R.id.selectedIcon);
                    icon.setVisibility(View.GONE);
                }
                // 选中当前
                selectedIcon.setVisibility(View.VISIBLE);
                selectedExercise[0] = exercise;
                onComplete.run();
            });

            container.addView(itemView);
        }
    }

    private void updateCaloriesPreview(TextView preview, ExerciseDatabase.Exercise exercise,
                                      EditText setsInput, EditText repsInput, EditText weightInput) {
        if (exercise == null) {
            preview.setText("0 kcal");
            return;
        }

        int sets = parseInt(setsInput.getText().toString());
        int reps = parseInt(repsInput.getText().toString());
        float weight = weightInput.getText().toString().isEmpty() ? 0 :
                Float.parseFloat(weightInput.getText().toString());

        // 计算预估卡路里
        float calories = exercise.baseCaloriesPerRep * sets * reps;
        if (weight > 0) {
            calories += weight * 0.05f * sets; // 重量加成
        }

        preview.setText(String.format("%.0f kcal", calories));
    }

    private int parseInt(String str) {
        try {
            return str.isEmpty() ? 0 : Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateWeekLabel() {
        binding.weekLabel.setText("第" + currentWeek + "周");
    }

    private void syncTodayExercisePlan() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayIndex = (dayOfWeek + 5) % 7;

        List<TrainingTask> todayTasks = trainingTaskManager.getTasksForToday();

        if (todayTasks.isEmpty()) {
            ExercisePlan existingPlan = exercisePlanManager.getPlan(dayIndex + 1);
            if (!existingPlan.isNotSet()) {
                ExercisePlan resetPlan = new ExercisePlan(dayIndex + 1);
                resetPlan.setStatus(ExercisePlan.DayStatus.NOT_SET);
                exercisePlanManager.savePlan(resetPlan);
            }
            return;
        }

        ExercisePlan plan = new ExercisePlan(dayIndex + 1);
        plan.setStatus(ExercisePlan.DayStatus.WORKOUT);

        boolean allCompleted = true;
        for (TrainingTask task : todayTasks) {
            if (!task.isCompleted()) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            plan.setCompletionStatus(ExercisePlan.CompletionStatus.COMPLETED);
        } else {
            plan.setCompletionStatus(ExercisePlan.CompletionStatus.PENDING);
        }

        exercisePlanManager.savePlan(plan);
    }

    private void updateWeekProgress() {
        // 获取本周的训练数据
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_MONTH, 7);
        long weekEnd = cal.getTimeInMillis();

        List<TrainingTask> allTasks = trainingTaskManager.getTasks();
        int totalDaysWithTasks = 0;
        int completedDays = 0;

        // 遍历本周7天
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTimeInMillis(weekStart);
        for (int d = 0; d < 7; d++) {
            // 判断这一天是否有任务
            boolean hasTask = false;
            boolean allDone = true;
            for (TrainingTask task : allTasks) {
                Calendar taskDay = Calendar.getInstance();
                taskDay.setTimeInMillis(task.getDate());
                if (taskDay.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR) &&
                    taskDay.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR)) {
                    hasTask = true;
                    if (!task.isCompleted()) {
                        allDone = false;
                    }
                }
            }
            if (hasTask) {
                totalDaysWithTasks++;
                if (allDone) {
                    completedDays++;
                }
            }
            dayCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        int remainingDays = totalDaysWithTasks - completedDays;

        binding.completedDays.setText(String.valueOf(completedDays));
        binding.remainingDays.setText(String.valueOf(Math.max(0, remainingDays)));

        int completionRate = totalDaysWithTasks > 0
                ? (completedDays * 100) / totalDaysWithTasks : 0;
        binding.completionRate.setText(completionRate + "%");
        binding.progressBar.setProgress(completionRate);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 立即显示缓存数据
        syncTodayExercisePlan();
        setupWeekCalendar();
        updateTrainingDetail();
        updateWeekProgress();

        // 从服务器加载动作数据
        ExerciseDatabase.loadExercisesFromServer(requireContext(), () -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded() && binding != null) {
                        // 动作数据已更新，刷新UI
                    }
                });
            }
        });

        // 后台静默刷新
        trainingTaskManager.loadTasks(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded() && binding != null) {
                        syncTodayExercisePlan();
                        setupWeekCalendar();
                        updateTrainingDetail();
                        updateWeekProgress();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
