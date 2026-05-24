package com.example.myapplication.ui.screens.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentWorkoutHistoryBinding;
import com.example.myapplication.model.TrainingTask;
import com.example.myapplication.model.WorkoutRecord;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.TrainingTaskManager;
import com.example.myapplication.util.WorkoutRecordManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryFragment extends Fragment {

    private FragmentWorkoutHistoryBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private TrainingTaskManager trainingTaskManager;
    private WorkoutRecordManager workoutRecordManager;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());
        trainingTaskManager = TrainingTaskManager.getInstance(requireContext());
        workoutRecordManager = WorkoutRecordManager.getInstance(requireContext());

        setupHeader();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });
    }

    private void loadData() {
        // Get stats from WorkoutRecordManager
        int totalWorkouts = workoutRecordManager.getTotalWorkouts();
        float totalCalories = workoutRecordManager.getTotalCalories();
        int totalMinutes = workoutRecordManager.getTotalMinutes();
        int consecutiveDays = workoutRecordManager.getConsecutiveDays();

        binding.totalWorkoutsValue.setText(String.valueOf(totalWorkouts));
        binding.totalMinutesValue.setText(String.valueOf(totalMinutes));
        binding.totalDaysValue.setText(String.valueOf(consecutiveDays));

        // Load records from database
        List<WorkoutRecord> records = workoutRecordManager.getAllRecords();

        // Check if empty
        if (records.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recordsContainer.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recordsContainer.setVisibility(View.VISIBLE);
            loadRecords(records);
        }
    }

    private void loadRecords(List<WorkoutRecord> records) {
        binding.recordsContainer.removeAllViews();

        // Group records by date
        String currentDate = "";
        for (WorkoutRecord record : records) {
            String recordDate = dateFormat.format(new Date(record.getTimestamp()));

            // Add date header if it's a new date
            if (!recordDate.equals(currentDate)) {
                addDateHeader(recordDate);
                currentDate = recordDate;
            }

            // Add record item
            addRecordItem(record);
        }
    }

    private void addDateHeader(String dateStr) {
        View headerView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_workout_date_header, binding.recordsContainer, false);

        TextView dateView = headerView.findViewById(R.id.dateText);

        // Convert to display format
        try {
            Date date = dateFormat.parse(dateStr);
            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA);
                dateView.setText(displayFormat.format(date));
            }
        } catch (Exception e) {
            dateView.setText(dateStr);
        }

        binding.recordsContainer.addView(headerView);
    }

    private void addRecordItem(WorkoutRecord record) {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_workout_record, binding.recordsContainer, false);

        TextView nameView = itemView.findViewById(R.id.workoutName);
        TextView timeView = itemView.findViewById(R.id.workoutTime);
        TextView caloriesView = itemView.findViewById(R.id.workoutCalories);
        ImageView deleteBtn = itemView.findViewById(R.id.deleteRecordBtn);

        nameView.setText(record.getExerciseName());
        timeView.setText(timeFormat.format(new Date(record.getTimestamp())));
        caloriesView.setText(String.format("%.1f千卡", record.getCaloriesBurned()));

        // Delete button click
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除记录")
                    .setMessage("确定要删除这条训练记录吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // Delete the record
                        workoutRecordManager.deleteRecord(record.getId());
                        // Also update the associated training task if exists
                        if (record.getTaskId() > 0) {
                            List<TrainingTask> tasks = trainingTaskManager.getTasks();
                            for (TrainingTask task : tasks) {
                                if (task.getId() == record.getTaskId()) {
                                    task.setStatus(TrainingTask.TaskStatus.NOT_STARTED);
                                    task.setCaloriesRecorded(false);
                                    trainingTaskManager.updateTask(task);
                                    break;
                                }
                            }
                        }
                        // Reload data
                        loadData();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        binding.recordsContainer.addView(itemView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
