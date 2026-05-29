package com.example.myapplication.ui.screens.home;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.api.AICalorieService;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.model.ExerciseDatabase;
import com.example.myapplication.model.ExercisePlan;
import com.example.myapplication.model.TrainingTask;
import com.example.myapplication.model.WorkoutRecord;
import com.example.myapplication.util.ExercisePlanManager;
import com.example.myapplication.util.RecordManager;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.TrainingTaskManager;
import com.example.myapplication.util.WorkoutRecordManager;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private ExercisePlanManager exercisePlanManager;
    private TrainingTaskManager trainingTaskManager;
    private RecordManager recordManager;
    private WorkoutRecordManager workoutRecordManager;
    private AICalorieService aiCalorieService;

    private int selectedDayIndex = -1;

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
        exercisePlanManager = ExercisePlanManager.getInstance(requireContext());
        trainingTaskManager = TrainingTaskManager.getInstance(requireContext());
        recordManager = RecordManager.getInstance(requireContext());
        workoutRecordManager = WorkoutRecordManager.getInstance(requireContext());
        aiCalorieService = AICalorieService.getInstance();

        setupHeader();
        setupWeekDays();
        setupBodyData();
        setupTrainingTasks();
        setupListeners();
    }

    private void setupHeader() {
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

        String nickname = sessionManager.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = "健身爱好者";
        }
        binding.userName.setText(nickname);

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        binding.dateBadge.setText(month + "月" + day + "日 " + weekdays[dayOfWeek - 1]);
    }

    private void setupWeekDays() {
        binding.weekDaysRow.removeAllViews();

        String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        Calendar calendar = Calendar.getInstance();
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int todayIndex = (todayDayOfWeek + 5) % 7; // Monday = 0

        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            ExercisePlan plan = exercisePlanManager.getPlan(i + 1);
            View dayView = createDayView(weekdays[i], dayIndex, todayIndex, plan);
            binding.weekDaysRow.addView(dayView);

            dayView.setOnClickListener(v -> showDayPlanDialog(dayIndex, weekdays[dayIndex]));
        }
    }

    private View createDayView(String dayLabel, int index, int todayIndex, ExercisePlan plan) {
        boolean isNotSet = plan.isNotSet();
        boolean isRestDay = plan.isRestDay();
        boolean isWorkoutDay = plan.isWorkoutDay();

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackgroundResource(R.drawable.bg_day_item);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, 0, margin, 0);
        container.setLayoutParams(params);
        container.setPadding(8, 12, 8, 12);

        // Day label - 默认显示周一到周日，选择休息日后显示"休"
        TextView labelView = new TextView(requireContext());
        if (isRestDay) {
            labelView.setText("休");
        } else {
            labelView.setText(dayLabel);
        }
        labelView.setTextSize(12);
        labelView.setGravity(Gravity.CENTER);
        labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        // Status indicator - 绿色圆点（已完成）或橙色圆点（未完成）
        View statusIndicator = new View(requireContext());
        int indicatorSize = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(indicatorSize, indicatorSize);
        indicatorParams.topMargin = (int) (6 * getResources().getDisplayMetrics().density);
        indicatorParams.gravity = Gravity.CENTER_HORIZONTAL;
        statusIndicator.setLayoutParams(indicatorParams);

        if (isWorkoutDay) {
            // 锻炼日：显示圆点
            int dotColor = plan.isCompleted() ? R.color.success : R.color.warning;
            android.graphics.drawable.GradientDrawable dotDrawable = new android.graphics.drawable.GradientDrawable();
            dotDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dotDrawable.setColor(ContextCompat.getColor(requireContext(), dotColor));
            dotDrawable.setSize(indicatorSize, indicatorSize);
            statusIndicator.setBackground(dotDrawable);
        } else {
            // 未设置或休息日：不显示圆点
            statusIndicator.setVisibility(View.GONE);
        }

        container.addView(labelView);
        container.addView(statusIndicator);

        return container;
    }

    private void showDayPlanDialog(int dayIndex, String dayName) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_day_plan, null);

        RadioGroup statusRadio = dialogView.findViewById(R.id.statusRadioGroup);
        RadioGroup completionRadio = dialogView.findViewById(R.id.completionRadioGroup);
        LinearLayout completionContainer = dialogView.findViewById(R.id.completionContainer);
        RadioButton radioRest = dialogView.findViewById(R.id.radioRest);
        RadioButton radioWorkout = dialogView.findViewById(R.id.radioWorkout);
        RadioButton radioPending = dialogView.findViewById(R.id.radioPending);
        RadioButton radioCompleted = dialogView.findViewById(R.id.radioCompleted);

        // Load existing plan
        ExercisePlan plan = exercisePlanManager.getPlan(dayIndex + 1);
        if (plan.isWorkoutDay()) {
            radioWorkout.setChecked(true);
            completionContainer.setVisibility(View.VISIBLE);
            if (plan.isCompleted()) {
                radioCompleted.setChecked(true);
            } else {
                radioPending.setChecked(true);
            }
        } else if (plan.isRestDay()) {
            radioRest.setChecked(true);
            completionContainer.setVisibility(View.GONE);
        } else {
            // NOT_SET: no radio checked by default, clear the group
            statusRadio.clearCheck();
            completionContainer.setVisibility(View.GONE);
        }

        statusRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWorkout) {
                completionContainer.setVisibility(View.VISIBLE);
                if (completionRadio.getCheckedRadioButtonId() == -1) {
                    radioPending.setChecked(true);
                }
            } else if (checkedId == R.id.radioRest) {
                completionContainer.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(dayName + " 计划")
                .setView(dialogView)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_background);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary, null));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_muted, null));
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int checkedId = statusRadio.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Toast.makeText(requireContext(), "请选择休息日或锻炼日", Toast.LENGTH_SHORT).show();
                    return;
                }

                ExercisePlan newPlan = new ExercisePlan(dayIndex + 1);

                if (checkedId == R.id.radioWorkout) {
                    newPlan.setStatus(ExercisePlan.DayStatus.WORKOUT);
                    if (completionRadio.getCheckedRadioButtonId() == R.id.radioCompleted) {
                        newPlan.setCompletionStatus(ExercisePlan.CompletionStatus.COMPLETED);
                    } else {
                        newPlan.setCompletionStatus(ExercisePlan.CompletionStatus.PENDING);
                    }
                } else {
                    newPlan.setStatus(ExercisePlan.DayStatus.REST);
                }

                exercisePlanManager.savePlan(newPlan);
                dialog.dismiss();
                setupWeekDays();
            });
        });

        dialog.show();
    }

    private void setupBodyData() {
        int height = sessionManager.getHeight();
        int initialHeight = sessionManager.getInitialHeight();
        float weight = sessionManager.getWeight();
        float initialWeight = sessionManager.getInitialWeight();
        float bodyFat = sessionManager.getBodyFat();
        float initialBodyFat = sessionManager.getInitialBodyFat();

        float displayHeight = height > 0 ? height : initialHeight;
        float displayWeight = weight > 0 ? weight : initialWeight;
        float displayBodyFat = bodyFat > 0 ? bodyFat : initialBodyFat;

        // Weight
        if (displayWeight > 0) {
            binding.weightValue.setText(String.format("%.1f", displayWeight));
        } else {
            binding.weightValue.setText("--");
        }

        // BMI
        if (displayHeight > 0 && displayWeight > 0) {
            float bmi = displayWeight / ((displayHeight / 100f) * (displayHeight / 100f));
            binding.bmiValue.setText(String.format("%.1f", bmi));
        } else {
            binding.bmiValue.setText("--");
        }

        // Body Fat
        if (displayBodyFat > 0) {
            binding.bodyFatValue.setText(String.format("%.1f", displayBodyFat));
        } else {
            binding.bodyFatValue.setText("--");
        }

        // Body data card click to navigate to progress
        binding.bodyDataCard.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_progress);
        });
    }

    private void setupTrainingTasks() {
        binding.tasksContainer.removeAllViews();

        List<TrainingTask> tasks = trainingTaskManager.getTasksForToday();
        int completedCount = trainingTaskManager.getTodayCompletedCount();
        int totalCount = trainingTaskManager.getTodayTotalCount();

        if (tasks.isEmpty()) {
            binding.emptyTrainingState.setVisibility(View.VISIBLE);
            binding.tasksContainer.setVisibility(View.GONE);
            binding.trainingProgress.setText("已完成 0/0");
            binding.trainingProgressBar.setProgress(0);
        } else {
            binding.emptyTrainingState.setVisibility(View.GONE);
            binding.tasksContainer.setVisibility(View.VISIBLE);

            binding.trainingProgress.setText(String.format("已完成 %d/%d", completedCount, totalCount));
            if (totalCount > 0) {
                binding.trainingProgressBar.setProgress((completedCount * 100) / totalCount);
            } else {
                binding.trainingProgressBar.setProgress(0);
            }

            for (TrainingTask task : tasks) {
                View taskView = createTaskView(task);
                binding.tasksContainer.addView(taskView);
            }
        }
    }

    private View createTaskView(TrainingTask task) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_training_task, binding.tasksContainer, false);

        CheckBox checkbox = view.findViewById(R.id.taskCheckbox);
        TextView nameView = view.findViewById(R.id.taskName);
        TextView durationView = view.findViewById(R.id.taskDuration);
        ImageView deleteBtn = view.findViewById(R.id.deleteTaskBtn);

        nameView.setText(task.getName());
        durationView.setText(task.getDuration() + "分钟");

        checkbox.setOnCheckedChangeListener(null);
        checkbox.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            nameView.setTextColor(Color.parseColor("#2ED573")); // 绿色-已完成
        } else {
            nameView.setTextColor(Color.parseColor("#666666")); // 灰色-未完成
        }

        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Prevent duplicate calorie records
            if (isChecked && !task.isCaloriesRecorded()) {
                task.setStatus(TrainingTask.TaskStatus.COMPLETED);
                nameView.setTextColor(Color.parseColor("#2ED573"));
                calculateAndSaveCalories(task);
            } else if (!isChecked) {
                task.setStatus(TrainingTask.TaskStatus.NOT_STARTED);
                task.setCaloriesRecorded(false); // Reset flag when unchecking
                nameView.setTextColor(Color.parseColor("#666666"));
                // Remove the calorie record for this task
                workoutRecordManager.deleteRecordByTaskId(task.getId());
                updateTodayProgress();
            }
            trainingTaskManager.updateTask(task);
            setupTrainingTasks();
        });

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除任务")
                    .setMessage("确定要删除这个训练任务吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // Delete associated workout record first
                        workoutRecordManager.deleteRecordByTaskId(task.getId());
                        // Then delete the task
                        trainingTaskManager.deleteTask(task.getId());
                        setupTrainingTasks();
                        updateTodayProgress();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        return view;
    }

    private void calculateAndSaveCalories(TrainingTask task) {
        // Get user info for better calorie calculation
        final float userWeight;
        if (sessionManager.getWeight() > 0) {
            userWeight = sessionManager.getWeight();
        } else {
            userWeight = 70f; // Default weight
        }

        final int userHeight;
        if (sessionManager.getHeight() > 0) {
            userHeight = sessionManager.getHeight();
        } else {
            userHeight = 170; // Default height
        }

        final String gender;
        String genderFromSession = sessionManager.getGender();
        if (genderFromSession != null && !genderFromSession.isEmpty()) {
            gender = genderFromSession;
        } else {
            gender = "未知";
        }

        // Calculate age (simplified - assume 25 if not available)
        final int userAge = 25;

        // Get exercise data from task
        final int reps = task.getReps();
        final int sets = task.getSets();
        final float weight = task.getWeight();

        // Check if this is a treadmill exercise
        if (task.isTreadmillExercise() && task.getTreadmillSpeed() > 0) {
            // Use treadmill calorie calculation
            calculateTreadmillCalories(task, userWeight, userAge, gender);
        } else {
            // Use AI service for regular exercises
            calculateCaloriesWithAI(task, userWeight, userHeight, userAge, gender, reps, sets, weight);
        }
    }

    /**
     * 跑步机卡路里计算
     */
    private void calculateTreadmillCalories(TrainingTask task, float userWeight, int userAge, String gender) {
        float speed = task.getTreadmillSpeed();
        float incline = task.getTreadmillIncline();
        int duration = task.getDuration();

        aiCalorieService.calculateTreadmillCalories(
                speed,
                incline,
                duration,
                userWeight,
                userAge,
                gender,
                new AICalorieService.AICalorieCallback() {
                    @Override
                    public void onSuccess(float calories) {
                        saveWorkoutRecord(task, calories);
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback: use MET formula directly
                        float met = calculateMET(speed, incline);
                        float calories = met * userWeight * (duration / 60.0f);
                        saveWorkoutRecord(task, calories);
                    }
                }
        );
    }

    /**
     * 根据速度和坡度计算MET值
     */
    private float calculateMET(float speed, float incline) {
        float met;
        if (speed < 4) {
            met = 3.0f + speed * 0.2f;
        } else if (speed < 6) {
            met = 4.0f + (speed - 4) * 0.5f;
        } else if (speed < 8) {
            met = 6.0f + (speed - 6) * 0.8f;
        } else if (speed < 10) {
            met = 8.0f + (speed - 8) * 0.7f;
        } else if (speed < 12) {
            met = 9.5f + (speed - 10) * 0.6f;
        } else {
            met = 11.0f + (speed - 12) * 0.5f;
        }
        // 坡度影响：每1%坡度增加约10%
        float inclineFactor = 1.0f + (incline / 100.0f) * 0.1f * incline;
        return met * inclineFactor;
    }

    /**
     * 使用AI计算常规运动卡路里
     */
    private void calculateCaloriesWithAI(TrainingTask task, float userWeight, int userHeight,
                                         int userAge, String gender, int reps, int sets, float weight) {
        // If reps/sets/weight are not set in task, try to parse from description
        if (reps <= 0 && sets <= 0 && weight <= 0 && task.getDescription() != null && !task.getDescription().isEmpty()) {
            parseExerciseDataFromDescription(task.getDescription());
        }

        final int totalReps = task.getReps() * task.getSets();

        aiCalorieService.calculateCalories(
                task.getName(),
                task.getDuration(),
                totalReps,
                task.getWeight(),
                userWeight,
                userHeight,
                userAge,
                gender,
                new AICalorieService.AICalorieCallback() {
                    @Override
                    public void onSuccess(float calories) {
                        saveWorkoutRecord(task, calories);
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback: use estimated calories based on exercise type
                        float estimatedCalories = estimateCaloriesWithType(
                                task.getName(),
                                task.getExerciseType(),
                                task.getDuration(),
                                totalReps,
                                task.getWeight(),
                                userWeight
                        );
                        saveWorkoutRecord(task, estimatedCalories);
                    }
                }
        );
    }

    /**
     * 保存训练记录
     */
    private void saveWorkoutRecord(TrainingTask task, float calories) {
        // Mark calories as recorded to prevent duplicates
        task.setCaloriesRecorded(true);
        trainingTaskManager.updateTask(task);

        // Save workout record with task ID for deduplication
        WorkoutRecord record = new WorkoutRecord(
                task.getName(),
                task.getDuration(),
                calories
        );
        record.setTaskId(task.getId());
        record.setReps(task.getReps() * task.getSets());
        record.setSets(task.getSets());
        record.setWeight(task.getWeight());

        // Include treadmill info in notes
        String notes = task.getDescription() != null ? task.getDescription() : "";
        if (task.isTreadmillExercise()) {
            notes += String.format(" [跑步机: %.1f km/h, %d%%坡度]", task.getTreadmillSpeed(), (int) task.getTreadmillIncline());
        }
        record.setNotes(notes);

        workoutRecordManager.addRecord(record);

        // Update the progress card
        updateTodayProgress();
    }

    private int[] parseExerciseDataFromDescription(String description) {
        int[] result = new int[]{0, 0}; // [reps, sets]
        try {
            StringBuilder currentNumber = new StringBuilder();
            for (char c : description.toCharArray()) {
                if (Character.isDigit(c)) {
                    currentNumber.append(c);
                } else {
                    if (currentNumber.length() > 0) {
                        int num = Integer.parseInt(currentNumber.toString());
                        if (description.contains("组") || description.contains("set")) {
                            result[1] = num;
                        } else if (description.contains("次") || description.contains("rep")) {
                            result[0] = num;
                        } else if (result[0] == 0) {
                            result[0] = num;
                        }
                        currentNumber = new StringBuilder();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return result;
    }

    private float estimateCaloriesFallback(String exerciseName, int duration) {
        return estimateCaloriesWithType(exerciseName, null, duration, 0, 0, 70f);
    }

    private float estimateCaloriesWithType(String exerciseName, TrainingTask.ExerciseType type,
                                            int duration, int totalReps, float weight, float userWeight) {
        // Base calories per minute depends on exercise type
        float baseCaloriesPerMinute;
        String name = exerciseName.toLowerCase();

        if (type != null) {
            switch (type) {
                case CARDIO:
                    baseCaloriesPerMinute = 10f;
                    break;
                case HIIT:
                    baseCaloriesPerMinute = 12f;
                    break;
                case EQUIPMENT:
                    baseCaloriesPerMinute = 7f;
                    break;
                case CORE:
                    baseCaloriesPerMinute = 6f;
                    break;
                case FLEXIBILITY:
                    baseCaloriesPerMinute = 3f;
                    break;
                case STRENGTH:
                default:
                    baseCaloriesPerMinute = 5f;
                    break;
            }
        } else {
            // Fallback to name-based detection
            if (name.contains("俯卧撑")) baseCaloriesPerMinute = 7f;
            else if (name.contains("仰卧起坐") || name.contains("卷腹")) baseCaloriesPerMinute = 5f;
            else if (name.contains("深蹲")) baseCaloriesPerMinute = 6f;
            else if (name.contains("跑步") || name.contains("慢跑")) baseCaloriesPerMinute = 10f;
            else if (name.contains("跳绳")) baseCaloriesPerMinute = 12f;
            else if (name.contains("游泳")) baseCaloriesPerMinute = 11f;
            else if (name.contains("骑行") || name.contains("自行车")) baseCaloriesPerMinute = 8f;
            else if (name.contains("瑜伽")) baseCaloriesPerMinute = 4f;
            else if (name.contains("拉伸")) baseCaloriesPerMinute = 3f;
            else if (name.contains("平板支撑")) baseCaloriesPerMinute = 5f;
            else if (name.contains("波比")) baseCaloriesPerMinute = 10f;
            else if (name.contains("引体向上")) baseCaloriesPerMinute = 6f;
            else if (name.contains("硬拉")) baseCaloriesPerMinute = 8f;
            else if (name.contains("卧推")) baseCaloriesPerMinute = 7f;
            else if (name.contains("哑铃")) baseCaloriesPerMinute = 6f;
            else if (name.contains("开合跳")) baseCaloriesPerMinute = 8f;
            else if (name.contains("高抬腿")) baseCaloriesPerMinute = 9f;
            else baseCaloriesPerMinute = 5f;
        }

        float calories = baseCaloriesPerMinute * duration;

        // Adjust for repetitions (if applicable)
        if (totalReps > 0) {
            calories += (totalReps / 10.0f) * baseCaloriesPerMinute * 0.5f;
        }

        // Adjust for added weight (if using equipment)
        if (weight > 0) {
            // More weight = more calories burned
            calories *= (1 + weight / 50.0f);
        }

        // Adjust for user body weight
        if (userWeight > 0) {
            float weightFactor = userWeight / 70.0f; // Normalize to 70kg
            calories *= weightFactor;
        }

        return calories;
    }

    private void updateTodayProgress() {
        int todayCount = workoutRecordManager.getWorkoutCountForToday();
        float todayCalories = workoutRecordManager.getCaloriesForToday();
        int todayMinutes = workoutRecordManager.getTotalMinutesForToday();

        // Update the training progress UI
        binding.trainingProgress.setText(String.format("已完成 %d/%d", todayCount, trainingTaskManager.getTodayTotalCount()));

        int totalCount = trainingTaskManager.getTodayTotalCount();
        if (totalCount > 0) {
            binding.trainingProgressBar.setProgress((todayCount * 100) / totalCount);
        } else {
            binding.trainingProgressBar.setProgress(0);
        }
    }

    private void showAddTaskDialog() {
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
            TextView chip = new TextView(requireContext());
            chip.setText(group.getDisplayName());
            chip.setTextSize(14);
            chip.setPadding(24, 12, 24, 12);
            chip.setGravity(Gravity.CENTER);
            chip.setBackgroundResource(R.drawable.bg_stat_icon);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            chipParams.setMargins(0, 0, 8, 0);
            chip.setLayoutParams(chipParams);

            chip.setOnClickListener(v -> {
                // 取消之前的选择
                for (int i = 0; i < muscleGroupContainer.getChildCount(); i++) {
                    muscleGroupContainer.getChildAt(i).setBackgroundResource(R.drawable.bg_stat_icon);
                }
                // 选中当前
                chip.setBackgroundResource(R.drawable.bg_stat_icon_selected);
                selectedGroup[0] = group;
                selectedExercise[0] = null;

                // 显示第二步
                step2Label.setVisibility(View.VISIBLE);
                exerciseScrollView.setVisibility(View.VISIBLE);
                paramsContainer.setVisibility(View.GONE);
                confirmBtn.setEnabled(false);

                // 加载该肌群的动作
                loadExercisesInDialog(group, exerciseContainer, selectedExercise, () -> {
                    paramsContainer.setVisibility(View.VISIBLE);
                    confirmBtn.setEnabled(true);
                    updateCaloriesPreview(caloriesPreview, selectedExercise[0], setsInput, repsInput, weightInput);
                });
            });
            muscleGroupContainer.addView(chip);
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

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_background);
            }
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        confirmBtn.setOnClickListener(v -> {
            if (selectedExercise[0] != null) {
                TrainingTask task = new TrainingTask();
                task.setName(selectedExercise[0].name);
                task.setDate(System.currentTimeMillis());
                task.setSets(parseInt(setsInput.getText().toString()));
                task.setReps(parseInt(repsInput.getText().toString()));
                String weightStr = weightInput.getText().toString();
                task.setWeight(weightStr.isEmpty() ? 0 : Float.parseFloat(weightStr));

                // 设置肌群
                if (selectedGroup[0] != null) {
                    task.setMuscleGroup(TrainingTask.MuscleGroup.valueOf(selectedGroup[0].name()));
                }

                trainingTaskManager.addTask(task);
                dialog.dismiss();
                setupTrainingTasks();
            }
        });

        dialog.show();
    }

    private void loadExercisesInDialog(ExerciseDatabase.MuscleGroup group, LinearLayout container,
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
            calories += weight * 0.05f * sets;
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

    private void setupListeners() {
        binding.addTaskBtn.setOnClickListener(v -> showAddTaskDialog());

        binding.startTrainingBtn.setOnClickListener(v -> showTrainingGuideDialog());
    }

    /**
     * 显示训练指导对话框
     */
    private void showTrainingGuideDialog() {
        // 获取今日的训练任务
        List<TrainingTask> tasks = trainingTaskManager.getTasksForToday();

        if (tasks == null || tasks.isEmpty()) {
            Toast.makeText(requireContext(), "请先添加训练任务", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSingleTask = tasks.size() == 1;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_training_guide, null);

        TextView titleView = dialogView.findViewById(R.id.exerciseName);
        ImageView imageView = dialogView.findViewById(R.id.exerciseImage);
        View noImageHint = dialogView.findViewById(R.id.noImageHint);
        TextView instructionsView = dialogView.findViewById(R.id.exerciseInstructions);
        TextView tipsView = dialogView.findViewById(R.id.trainingTips);
        TextView progressText = dialogView.findViewById(R.id.progressText);
        LinearLayout progressDots = dialogView.findViewById(R.id.progressDots);
        ImageButton prevBtn = dialogView.findViewById(R.id.prevBtn);
        ImageButton nextBtn = dialogView.findViewById(R.id.nextBtn);
        com.google.android.material.button.MaterialButton startBtn = dialogView.findViewById(R.id.startTrainingBtn);
        ImageButton closeBtn = dialogView.findViewById(R.id.closeBtn);

        // 找到第一个未完成的任务作为起始位置
        int startIndex = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (!tasks.get(i).isCompleted()) {
                startIndex = i;
                break;
            }
        }

        // 根据任务数量控制箭头显示
        if (isSingleTask) {
            // 单任务：隐藏箭头和进度指示
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.GONE);
            progressDots.setVisibility(View.GONE);
        } else {
            // 多任务：显示箭头
            prevBtn.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressDots.setVisibility(View.VISIBLE);

            // 创建进度点
            for (int i = 0; i < tasks.size(); i++) {
                View dot = new View(requireContext());
                LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(8, 8);
                dotParams.setMargins(4, 0, 4, 0);
                dot.setLayoutParams(dotParams);
                dot.setBackgroundResource(R.drawable.bg_progress_dot);
                progressDots.addView(dot);
            }
        }

        // 当前索引
        final int[] currentIndex = {startIndex};

        // 更新显示
        Runnable updateDisplay = () -> {
            TrainingTask task = tasks.get(currentIndex[0]);

            // 查找对应的动作信息
            ExerciseDatabase.Exercise exercise = findExerciseByName(task.getName());

            titleView.setText(task.getName());

            // 根据任务数量更新按钮文本
            if (isSingleTask) {
                startBtn.setText("开始训练");
            } else {
                if (task.isCompleted()) {
                    startBtn.setText("已完成");
                } else {
                    startBtn.setText("完成 " + task.getName());
                }
            }

            if (exercise != null) {
                instructionsView.setText(exercise.getInstructions());
                tipsView.setText(exercise.getTips());

                // 尝试加载图片
                if (exercise.imageResName != null) {
                    int resId = getResources().getIdentifier(
                            exercise.imageResName, "drawable", requireContext().getPackageName());
                    if (resId != 0) {
                        imageView.setImageResource(resId);
                        noImageHint.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        showNoImage(imageView, noImageHint);
                    }
                } else {
                    showNoImage(imageView, noImageHint);
                }
            } else {
                instructionsView.setText("【动作要点】\n请参考标准健身动作教程\n或咨询教练指导");
                tipsView.setText("动作名称：" + task.getName() + "\n所需器材：待定\n请员工制作对应图片");
                showNoImage(imageView, noImageHint);
            }

            // 更新进度
            progressText.setText(String.format("动作 %d / %d", currentIndex[0] + 1, tasks.size()));

            // 更新进度点
            for (int i = 0; i < progressDots.getChildCount(); i++) {
                progressDots.getChildAt(i).setBackgroundResource(
                        i == currentIndex[0] ? R.drawable.bg_progress_dot_active : R.drawable.bg_progress_dot);
            }

            // 更新箭头状态
            if (!isSingleTask) {
                prevBtn.setAlpha(currentIndex[0] > 0 ? 1.0f : 0.3f);
                nextBtn.setAlpha(currentIndex[0] < tasks.size() - 1 ? 1.0f : 0.3f);
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // 关闭按钮
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        // 开始训练按钮 - 完成当前任务
        startBtn.setOnClickListener(v -> {
            TrainingTask currentTask = tasks.get(currentIndex[0]);
            currentTask.setCompleted(true);
            trainingTaskManager.updateTask(currentTask);

            // 更新首页显示
            setupTrainingTasks();

            if (isSingleTask) {
                // 单任务：直接关闭对话框
                dialog.dismiss();
                Toast.makeText(requireContext(), "完成训练！", Toast.LENGTH_SHORT).show();
            } else {
                // 多任务：跳转到下一个未完成任务
                if (currentIndex[0] < tasks.size() - 1) {
                    currentIndex[0]++;
                    updateDisplay.run();
                    Toast.makeText(requireContext(), "已完成 " + currentTask.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    // 所有任务都已完成
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "恭喜完成所有训练！", Toast.LENGTH_LONG).show();
                }
            }
        });

        // 上一个按钮
        prevBtn.setOnClickListener(v -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateDisplay.run();
            }
        });

        // 下一个按钮
        nextBtn.setOnClickListener(v -> {
            if (currentIndex[0] < tasks.size() - 1) {
                currentIndex[0]++;
                updateDisplay.run();
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_background);
            }
            updateDisplay.run();
        });

        dialog.show();
    }

    private void showNoImage(ImageView imageView, View noImageHint) {
        imageView.setVisibility(View.GONE);
        noImageHint.setVisibility(View.VISIBLE);
    }

    private ExerciseDatabase.Exercise findExerciseByName(String name) {
        List<ExerciseDatabase.Exercise> all = ExerciseDatabase.getAllExercises();
        for (ExerciseDatabase.Exercise ex : all) {
            if (ex.name.equals(name)) {
                return ex;
            }
        }
        return null;
    }

    private void recordTrainingComplete(List<TrainingTask> tasks) {
        // 记录训练完成，可扩展为保存到数据库
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBodyData();
        setupTrainingTasks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
