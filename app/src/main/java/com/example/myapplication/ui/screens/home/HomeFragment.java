package com.example.myapplication.ui.screens.home;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
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

import com.example.myapplication.R;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.api.AICalorieService;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.model.ExercisePlan;
import com.example.myapplication.model.TrainingTask;
import com.example.myapplication.model.WorkoutRecord;
import com.example.myapplication.util.ExercisePlanManager;
import com.example.myapplication.util.RecordManager;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.TrainingTaskManager;
import com.example.myapplication.util.WorkoutRecordManager;
import com.google.android.material.textfield.TextInputEditText;

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
        boolean isToday = (index == todayIndex);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, 0, margin, 0);
        container.setLayoutParams(params);
        container.setPadding(8, 12, 8, 12);

        // Day label
        TextView labelView = new TextView(requireContext());
        labelView.setText(dayLabel);
        labelView.setTextSize(12);
        labelView.setGravity(Gravity.CENTER);
        labelView.setTextColor(ContextCompat.getColor(requireContext(),
                isToday ? R.color.white : R.color.text_muted));

        // Status indicator
        View statusIndicator = new View(requireContext());
        int indicatorSize = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(indicatorSize, indicatorSize);
        indicatorParams.topMargin = (int) (6 * getResources().getDisplayMetrics().density);
        indicatorParams.gravity = Gravity.CENTER_HORIZONTAL;
        statusIndicator.setLayoutParams(indicatorParams);

        // Set indicator color based on status
        if (isToday) {
            if (plan.isWorkoutDay()) {
                if (plan.isCompleted()) {
                    statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.success));
                    container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.success));
                } else {
                    statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.warning));
                    container.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.warning));
                }
                labelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.text_muted));
            }
        } else {
            if (plan.isWorkoutDay()) {
                if (plan.isCompleted()) {
                    statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.success));
                } else {
                    statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.warning));
                }
            } else {
                statusIndicator.setVisibility(View.INVISIBLE);
            }
        }

        container.addView(statusIndicator);
        container.addView(labelView);

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
        } else {
            radioRest.setChecked(true);
            completionContainer.setVisibility(View.GONE);
        }

        statusRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWorkout) {
                completionContainer.setVisibility(View.VISIBLE);
                radioPending.setChecked(true);
            } else {
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
                ExercisePlan newPlan = new ExercisePlan(dayIndex + 1);

                if (statusRadio.getCheckedRadioButtonId() == R.id.radioWorkout) {
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
                .inflate(R.layout.dialog_add_task, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
        TextInputEditText durationInput = dialogView.findViewById(R.id.taskDurationInput);
        TextInputEditText descInput = dialogView.findViewById(R.id.taskDescInput);
        TextInputEditText setsInput = dialogView.findViewById(R.id.taskSetsInput);
        TextInputEditText repsInput = dialogView.findViewById(R.id.taskRepsInput);
        TextInputEditText weightInput = dialogView.findViewById(R.id.taskWeightInput);
        TextInputEditText speedInput = dialogView.findViewById(R.id.taskSpeedInput);
        TextInputEditText inclineInput = dialogView.findViewById(R.id.taskInclineInput);
        RadioGroup exerciseTypeGroup = dialogView.findViewById(R.id.exerciseTypeGroup);
        LinearLayout treadmillSettings = dialogView.findViewById(R.id.treadmillSettings);

        // Listen for exercise type changes to show/hide treadmill settings
        exerciseTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Check if name contains treadmill keywords or if cardio is selected
            String name = nameInput.getText() != null ? nameInput.getText().toString().toLowerCase() : "";
            boolean isTreadmill = name.contains("跑步机") || name.contains("treadmill")
                    || checkedId == R.id.radioCardio;
            treadmillSettings.setVisibility(isTreadmill ? View.VISIBLE : View.GONE);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("添加", null)
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
                String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                String durationStr = durationInput.getText() != null ? durationInput.getText().toString().trim() : "30";
                String desc = descInput.getText() != null ? descInput.getText().toString().trim() : "";
                String setsStr = setsInput.getText() != null ? setsInput.getText().toString().trim() : "3";
                String repsStr = repsInput.getText() != null ? repsInput.getText().toString().trim() : "10";
                String weightStr = weightInput.getText() != null ? weightInput.getText().toString().trim() : "0";
                String speedStr = speedInput.getText() != null ? speedInput.getText().toString().trim() : "8";
                String inclineStr = inclineInput.getText() != null ? inclineInput.getText().toString().trim() : "0";

                if (name.isEmpty()) {
                    nameInput.setError("请输入任务名称");
                    return;
                }

                int duration = 30;
                try {
                    duration = Integer.parseInt(durationStr);
                } catch (NumberFormatException e) {
                    duration = 30;
                }

                int sets = 3;
                try {
                    sets = Integer.parseInt(setsStr);
                } catch (NumberFormatException e) {
                    sets = 3;
                }

                int reps = 10;
                try {
                    reps = Integer.parseInt(repsStr);
                } catch (NumberFormatException e) {
                    reps = 10;
                }

                float weight = 0f;
                try {
                    weight = Float.parseFloat(weightStr);
                } catch (NumberFormatException e) {
                    weight = 0f;
                }

                float speed = 8f;
                try {
                    speed = Float.parseFloat(speedStr);
                } catch (NumberFormatException e) {
                    speed = 8f;
                }

                float incline = 0f;
                try {
                    incline = Float.parseFloat(inclineStr);
                } catch (NumberFormatException e) {
                    incline = 0f;
                }

                // Get exercise type
                TrainingTask.ExerciseType exerciseType = TrainingTask.ExerciseType.STRENGTH;
                int checkedId = exerciseTypeGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.radioEquipment) {
                    exerciseType = TrainingTask.ExerciseType.EQUIPMENT;
                } else if (checkedId == R.id.radioCardio) {
                    exerciseType = TrainingTask.ExerciseType.CARDIO;
                } else if (checkedId == R.id.radioHIIT) {
                    exerciseType = TrainingTask.ExerciseType.HIIT;
                } else if (checkedId == R.id.radioFlexibility) {
                    exerciseType = TrainingTask.ExerciseType.FLEXIBILITY;
                } else if (checkedId == R.id.radioCore) {
                    exerciseType = TrainingTask.ExerciseType.CORE;
                }

                TrainingTask task = new TrainingTask(name, desc, duration);
                task.setDate(System.currentTimeMillis());
                task.setExerciseType(exerciseType);
                task.setSets(sets);
                task.setReps(reps);
                task.setWeight(weight);

                // Set treadmill parameters if applicable
                if (name.contains("跑步机") || name.contains("treadmill") || exerciseType == TrainingTask.ExerciseType.CARDIO) {
                    task.setTreadmillSpeed(speed);
                    task.setTreadmillIncline(incline);
                }

                trainingTaskManager.addTask(task);

                dialog.dismiss();
                setupTrainingTasks();
            });
        });

        dialog.show();
    }

    private void setupListeners() {
        binding.addTaskBtn.setOnClickListener(v -> showAddTaskDialog());

        binding.startTrainingBtn.setOnClickListener(v -> {
            // Start training
        });
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
