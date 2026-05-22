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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.model.ExercisePlan;
import com.example.myapplication.model.TrainingTask;
import com.example.myapplication.util.ExercisePlanManager;
import com.example.myapplication.util.RecordManager;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.TrainingTaskManager;
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
            task.setStatus(isChecked ? TrainingTask.TaskStatus.COMPLETED : TrainingTask.TaskStatus.NOT_STARTED);
            trainingTaskManager.updateTask(task);

            if (isChecked) {
                nameView.setTextColor(Color.parseColor("#2ED573")); // 绿色-已完成
            } else {
                nameView.setTextColor(Color.parseColor("#666666")); // 灰色-未完成
            }
            setupTrainingTasks();
        });

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除任务")
                    .setMessage("确定要删除这个训练任务吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        trainingTaskManager.deleteTask(task.getId());
                        setupTrainingTasks();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        return view;
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_task, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
        TextInputEditText durationInput = dialogView.findViewById(R.id.taskDurationInput);
        TextInputEditText descInput = dialogView.findViewById(R.id.taskDescInput);

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

                TrainingTask task = new TrainingTask(name, desc, duration);
                task.setDate(System.currentTimeMillis());
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
