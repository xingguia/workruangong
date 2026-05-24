package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.WorkoutRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WorkoutRecordManager {

    private static final String PREF_NAME = "workout_records";
    private static final String KEY_RECORDS = "records";
    private static final String KEY_TOTAL_WORKOUTS = "total_workouts";
    private static final String KEY_TOTAL_CALORIES = "total_calories";
    private static final String KEY_COMPLETED_DATES = "completed_dates";

    private static WorkoutRecordManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private WorkoutRecordManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized WorkoutRecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new WorkoutRecordManager(context);
        }
        return instance;
    }

    public long addRecord(WorkoutRecord record) {
        List<WorkoutRecord> records = getAllRecords();
        record.setId(System.currentTimeMillis());
        records.add(record);
        saveRecords(records);

        // Update statistics
        updateStatistics(record);

        return record.getId();
    }

    public void updateRecord(WorkoutRecord record) {
        List<WorkoutRecord> records = getAllRecords();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId() == record.getId()) {
                records.set(i, record);
                break;
            }
        }
        saveRecords(records);
        recalculateStatistics();
    }

    public void deleteRecord(long recordId) {
        List<WorkoutRecord> records = getAllRecords();
        records.removeIf(record -> record.getId() == recordId);
        saveRecords(records);
        recalculateStatistics();
    }

    public void deleteRecordByTaskId(long taskId) {
        List<WorkoutRecord> records = getAllRecords();
        records.removeIf(record -> record.getTaskId() == taskId);
        saveRecords(records);
        recalculateStatistics();
    }

    public List<WorkoutRecord> getAllRecords() {
        String json = prefs.getString(KEY_RECORDS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<WorkoutRecord>>() {}.getType();
        List<WorkoutRecord> records = gson.fromJson(json, type);
        return records != null ? records : new ArrayList<>();
    }

    public List<WorkoutRecord> getRecordsForDate(Date date) {
        List<WorkoutRecord> allRecords = getAllRecords();
        List<WorkoutRecord> filteredRecords = new ArrayList<>();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);
        cal2.set(Calendar.MILLISECOND, 999);

        for (WorkoutRecord record : allRecords) {
            if (record.getTimestamp() >= cal1.getTimeInMillis() &&
                record.getTimestamp() <= cal2.getTimeInMillis()) {
                filteredRecords.add(record);
            }
        }

        return filteredRecords;
    }

    public List<WorkoutRecord> getRecordsForToday() {
        return getRecordsForDate(new Date());
    }

    public List<WorkoutRecord> getRecordsForDateRange(Date startDate, Date endDate) {
        List<WorkoutRecord> allRecords = getAllRecords();
        List<WorkoutRecord> filteredRecords = new ArrayList<>();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);
        cal2.set(Calendar.MILLISECOND, 999);

        for (WorkoutRecord record : allRecords) {
            if (record.getTimestamp() >= cal1.getTimeInMillis() &&
                record.getTimestamp() <= cal2.getTimeInMillis()) {
                filteredRecords.add(record);
            }
        }

        return filteredRecords;
    }

    // Statistics methods
    public int getTotalWorkouts() {
        return prefs.getInt(KEY_TOTAL_WORKOUTS, 0);
    }

    public int getConsecutiveDays() {
        Set<String> completedDates = getCompletedDates();
        if (completedDates.isEmpty()) {
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int consecutiveDays = 0;

        // Check consecutive days backwards from today
        for (int i = 0; i < 365; i++) {
            String dateStr = sdf.format(cal.getTime());
            if (completedDates.contains(dateStr)) {
                consecutiveDays++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                // If today is not completed, check from yesterday
                if (i == 0) {
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    dateStr = sdf.format(cal.getTime());
                    if (completedDates.contains(dateStr)) {
                        consecutiveDays++;
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return consecutiveDays;
    }

    public int getWorkoutDays() {
        return getCompletedDates().size();
    }

    private Set<String> getCompletedDates() {
        Set<String> defaultSet = new HashSet<>();
        Set<String> dates = prefs.getStringSet(KEY_COMPLETED_DATES, defaultSet);
        return dates != null ? new HashSet<>(dates) : new HashSet<>();
    }

    private void updateStatistics(WorkoutRecord record) {
        int totalWorkouts = prefs.getInt(KEY_TOTAL_WORKOUTS, 0) + 1;
        float totalCalories = prefs.getFloat(KEY_TOTAL_CALORIES, 0f) + record.getCaloriesBurned();

        prefs.edit()
                .putInt(KEY_TOTAL_WORKOUTS, totalWorkouts)
                .putFloat(KEY_TOTAL_CALORIES, totalCalories)
                .apply();

        // Add today's date to completed dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(new Date(record.getTimestamp()));
        Set<String> completedDates = getCompletedDates();
        completedDates.add(dateStr);
        prefs.edit().putStringSet(KEY_COMPLETED_DATES, completedDates).apply();
    }

    public void recalculateStatistics() {
        List<WorkoutRecord> records = getAllRecords();

        int totalWorkouts = records.size();
        float totalCalories = 0f;
        Set<String> completedDates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (WorkoutRecord record : records) {
            totalCalories += record.getCaloriesBurned();
            completedDates.add(sdf.format(new Date(record.getTimestamp())));
        }

        prefs.edit()
                .putInt(KEY_TOTAL_WORKOUTS, totalWorkouts)
                .putFloat(KEY_TOTAL_CALORIES, totalCalories)
                .putStringSet(KEY_COMPLETED_DATES, completedDates)
                .apply();
    }

    public float getCaloriesForToday() {
        List<WorkoutRecord> todayRecords = getRecordsForToday();
        float total = 0f;
        for (WorkoutRecord record : todayRecords) {
            total += record.getCaloriesBurned();
        }
        return total;
    }

    public int getWorkoutCountForToday() {
        return getRecordsForToday().size();
    }

    public int getTotalMinutesForToday() {
        List<WorkoutRecord> todayRecords = getRecordsForToday();
        int total = 0;
        for (WorkoutRecord record : todayRecords) {
            total += record.getDuration();
        }
        return total;
    }

    public int getTotalMinutes() {
        List<WorkoutRecord> records = getAllRecords();
        int total = 0;
        for (WorkoutRecord record : records) {
            total += record.getDuration();
        }
        return total;
    }

    public float getTotalCalories() {
        List<WorkoutRecord> records = getAllRecords();
        float total = 0f;
        for (WorkoutRecord record : records) {
            total += record.getCaloriesBurned();
        }
        return total;
    }

    private void saveRecords(List<WorkoutRecord> records) {
        String json = gson.toJson(records);
        prefs.edit().putString(KEY_RECORDS, json).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
