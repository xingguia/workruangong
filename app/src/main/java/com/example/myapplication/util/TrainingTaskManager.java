package com.example.myapplication.util;

import android.content.Context;

import com.example.myapplication.api.ApiClient;
import com.example.myapplication.model.TrainingTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TrainingTaskManager {

    private static TrainingTaskManager instance;
    private ApiClient api;
    private List<TrainingTask> cachedTasks = new ArrayList<>();
    private boolean loaded = false;

    private TrainingTaskManager(Context context) {
        api = ApiClient.getInstance(context.getApplicationContext());
    }

    public static synchronized TrainingTaskManager getInstance(Context context) {
        if (instance == null) {
            instance = new TrainingTaskManager(context);
        }
        return instance;
    }

    public void loadTasks(Runnable onDone) {
        api.getTrainingTasks(new ApiClient.Callback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                cachedTasks.clear();
                if (data != null) {
                    for (Map<String, Object> item : data) {
                        cachedTasks.add(mapToTask(item));
                    }
                }
                loaded = true;
                if (onDone != null) onDone.run();
            }
            @Override
            public void onError(String error) {
                loaded = true;
                if (onDone != null) onDone.run();
            }
        });
    }

    private TrainingTask mapToTask(Map<String, Object> item) {
        TrainingTask task = new TrainingTask();
        task.setId(((Number) item.get("id")).longValue());
        task.setDate(((Number) item.get("date")).longValue());
        task.setName((String) item.get("name"));
        task.setDescription((String) item.get("description"));
        task.setDuration(((Number) item.getOrDefault("duration", 0)).intValue());
        String statusStr = (String) item.get("status");
        if (statusStr != null) task.setStatus(TrainingTask.TaskStatus.valueOf(statusStr));
        String typeStr = (String) item.get("exercise_type");
        if (typeStr != null) task.setExerciseType(TrainingTask.ExerciseType.valueOf(typeStr));
        task.setReps(((Number) item.getOrDefault("reps", 0)).intValue());
        task.setSets(((Number) item.getOrDefault("sets", 0)).intValue());
        task.setWeight(((Number) item.getOrDefault("weight", 0)).floatValue());
        String mgStr = (String) item.get("muscle_group");
        if (mgStr != null) task.setMuscleGroup(TrainingTask.MuscleGroup.valueOf(mgStr));
        task.setSubMuscle((String) item.getOrDefault("sub_muscle", ""));
        task.setCaloriesRecorded((Boolean) item.getOrDefault("calories_recorded", false));
        task.setTreadmillSpeed(((Number) item.getOrDefault("treadmill_speed", 0)).floatValue());
        task.setTreadmillIncline(((Number) item.getOrDefault("treadmill_incline", 0)).floatValue());
        return task;
    }

    public long addTask(TrainingTask task) {
        task.setId(System.currentTimeMillis());
        cachedTasks.add(0, task);
        api.createTrainingTask(task, new ApiClient.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                task.setId(((Number) data.get("id")).longValue());
            }
            @Override
            public void onError(String error) { }
        });
        return task.getId();
    }

    public void updateTask(TrainingTask task) {
        for (int i = 0; i < cachedTasks.size(); i++) {
            if (cachedTasks.get(i).getId() == task.getId()) {
                cachedTasks.set(i, task);
                break;
            }
        }
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", task.getStatus().name());
        updates.put("calories_recorded", task.isCaloriesRecorded());
        updates.put("reps", task.getReps());
        updates.put("sets", task.getSets());
        updates.put("weight", (double) task.getWeight());
        api.updateTrainingTask(task.getId(), updates, null);
    }

    public void deleteTask(long taskId) {
        cachedTasks.removeIf(task -> task.getId() == taskId);
        api.deleteTrainingTask(taskId, null);
    }

    public List<TrainingTask> getTasks() {
        return new ArrayList<>(cachedTasks);
    }

    public List<TrainingTask> getTasksForToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        long todayEnd = tomorrow.getTimeInMillis();

        List<TrainingTask> result = new ArrayList<>();
        for (TrainingTask task : cachedTasks) {
            if (task.getDate() >= todayStart && task.getDate() < todayEnd) {
                result.add(task);
            }
        }
        Collections.sort(result, (t1, t2) -> Long.compare(t1.getCreatedAt(), t2.getCreatedAt()));
        return result;
    }

    public List<TrainingTask> getTodayCompletedTasks() {
        List<TrainingTask> completed = new ArrayList<>();
        for (TrainingTask task : getTasksForToday()) {
            if (task.isCompleted()) completed.add(task);
        }
        return completed;
    }

    public int getTodayCompletedCount() { return getTodayCompletedTasks().size(); }
    public int getTodayTotalCount() { return getTasksForToday().size(); }
    public void clearAll() { cachedTasks.clear(); }
    public boolean isLoaded() { return loaded; }
}
