package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.TrainingTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrainingTaskManager {

    private static final String PREF_NAME = "training_tasks";
    private static final String KEY_TASKS = "tasks";

    private static TrainingTaskManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private TrainingTaskManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized TrainingTaskManager getInstance(Context context) {
        if (instance == null) {
            instance = new TrainingTaskManager(context);
        }
        return instance;
    }

    public long addTask(TrainingTask task) {
        List<TrainingTask> tasks = getTasks();
        task.setId(System.currentTimeMillis());
        tasks.add(task);
        saveTasks(tasks);
        return task.getId();
    }

    public void updateTask(TrainingTask task) {
        List<TrainingTask> tasks = getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == task.getId()) {
                tasks.set(i, task);
                break;
            }
        }
        saveTasks(tasks);
    }

    public void deleteTask(long taskId) {
        List<TrainingTask> tasks = getTasks();
        tasks.removeIf(task -> task.getId() == taskId);
        saveTasks(tasks);
    }

    public List<TrainingTask> getTasks() {
        String json = prefs.getString(KEY_TASKS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<TrainingTask>>() {}.getType();
        List<TrainingTask> tasks = gson.fromJson(json, type);
        return tasks != null ? tasks : new ArrayList<>();
    }

    public List<TrainingTask> getTasksForToday() {
        List<TrainingTask> allTasks = getTasks();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        long todayEnd = tomorrow.getTimeInMillis();

        List<TrainingTask> todayTasks = new ArrayList<>();
        for (TrainingTask task : allTasks) {
            if (task.getDate() >= todayStart && task.getDate() < todayEnd) {
                todayTasks.add(task);
            }
        }
        // Sort by created time
        Collections.sort(todayTasks, (t1, t2) -> Long.compare(t1.getCreatedAt(), t2.getCreatedAt()));
        return todayTasks;
    }

    public List<TrainingTask> getTodayCompletedTasks() {
        List<TrainingTask> tasks = getTasksForToday();
        List<TrainingTask> completed = new ArrayList<>();
        for (TrainingTask task : tasks) {
            if (task.isCompleted()) {
                completed.add(task);
            }
        }
        return completed;
    }

    public int getTodayCompletedCount() {
        return getTodayCompletedTasks().size();
    }

    public int getTodayTotalCount() {
        return getTasksForToday().size();
    }

    private void saveTasks(List<TrainingTask> tasks) {
        String json = gson.toJson(tasks);
        prefs.edit().putString(KEY_TASKS, json).apply();
    }

    public void clearAll() {
        prefs.edit().remove(KEY_TASKS).apply();
    }
}
