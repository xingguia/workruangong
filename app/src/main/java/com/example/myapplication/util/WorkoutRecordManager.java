package com.example.myapplication.util;

import android.content.Context;

import com.example.myapplication.api.ApiClient;
import com.example.myapplication.model.WorkoutRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WorkoutRecordManager {

    private static WorkoutRecordManager instance;
    private ApiClient api;
    private List<WorkoutRecord> cachedRecords = new ArrayList<>();
    private boolean loaded = false;

    public interface DataCallback {
        void onDataReady();
    }

    private WorkoutRecordManager(Context context) {
        api = ApiClient.getInstance(context.getApplicationContext());
    }

    public static synchronized WorkoutRecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new WorkoutRecordManager(context);
        }
        return instance;
    }

    public void loadRecords(Runnable onDone) {
        api.getWorkoutRecords(new ApiClient.Callback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                cachedRecords.clear();
                if (data != null) {
                    java.util.Set<Long> seenTaskIds = new java.util.HashSet<>();
                    java.util.List<Long> duplicateIds = new java.util.ArrayList<>();
                    for (Map<String, Object> item : data) {
                        WorkoutRecord record = mapToRecord(item);
                        long taskId = record.getTaskId();
                        if (taskId > 0) {
                            if (!seenTaskIds.add(taskId)) {
                                // Duplicate found by taskId, mark for server cleanup
                                duplicateIds.add(record.getId());
                                continue;
                            }
                        }
                        cachedRecords.add(record);
                    }
                    // Clean up duplicates on server
                    for (Long dupId : duplicateIds) {
                        api.deleteWorkoutRecord(dupId, null);
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

    private WorkoutRecord mapToRecord(Map<String, Object> item) {
        WorkoutRecord r = new WorkoutRecord();
        r.setId(((Number) item.get("id")).longValue());
        r.setExerciseName((String) item.get("exercise_name"));
        r.setTimestamp(((Number) item.get("timestamp")).longValue());
        r.setDuration(((Number) item.getOrDefault("duration", 0)).intValue());
        r.setSets(((Number) item.getOrDefault("sets", 0)).intValue());
        r.setReps(((Number) item.getOrDefault("reps", 0)).intValue());
        r.setWeight(((Number) item.getOrDefault("weight", 0)).floatValue());
        r.setCaloriesBurned(((Number) item.getOrDefault("calories_burned", 0)).floatValue());
        r.setCalories(((Number) item.getOrDefault("calories", 0)).floatValue());
        r.setNotes((String) item.get("notes"));
        r.setMuscleGroup((String) item.get("muscle_group"));
        if (item.get("task_id") != null) {
            r.setTaskId(((Number) item.get("task_id")).longValue());
        }
        return r;
    }

    public long addRecord(WorkoutRecord record) {
        record.setId(System.currentTimeMillis());
        // 防止重复记录：如果已有相同 taskId 的记录则先删除
        long taskId = record.getTaskId();
        if (taskId > 0) {
            cachedRecords.removeIf(r -> r.getTaskId() == taskId);
        }
        cachedRecords.add(0, record);
        api.createWorkoutRecord(record, new ApiClient.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                record.setId(((Number) data.get("id")).longValue());
                record.setTimestamp(((Number) data.get("timestamp")).longValue());
            }
            @Override
            public void onError(String error) { }
        });
        return record.getId();
    }

    public void updateRecord(WorkoutRecord record) {
        for (int i = 0; i < cachedRecords.size(); i++) {
            if (cachedRecords.get(i).getId() == record.getId()) {
                cachedRecords.set(i, record);
                break;
            }
        }
    }

    public void deleteRecord(long recordId) {
        cachedRecords.removeIf(r -> r.getId() == recordId);
        api.deleteWorkoutRecord(recordId, null);
    }

    public void deleteRecordByTaskId(long taskId) {
        // 先找到记录ID用于服务器删除
        for (WorkoutRecord r : cachedRecords) {
            if (r.getTaskId() == taskId) {
                api.deleteWorkoutRecord(r.getId(), null);
                break;
            }
        }
        cachedRecords.removeIf(r -> r.getTaskId() == taskId);
    }

    public List<WorkoutRecord> getAllRecords() {
        return new ArrayList<>(cachedRecords);
    }

    public List<WorkoutRecord> getRecordsForDate(Date date) {
        List<WorkoutRecord> filtered = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 0); cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0); cal1.set(Calendar.MILLISECOND, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal2.set(Calendar.HOUR_OF_DAY, 23); cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59); cal2.set(Calendar.MILLISECOND, 999);

        for (WorkoutRecord r : cachedRecords) {
            if (r.getTimestamp() >= cal1.getTimeInMillis() && r.getTimestamp() <= cal2.getTimeInMillis()) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    public List<WorkoutRecord> getRecordsForToday() {
        return getRecordsForDate(new Date());
    }

    public List<WorkoutRecord> getRecordsForDateRange(Date startDate, Date endDate) {
        List<WorkoutRecord> filtered = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);
        cal1.set(Calendar.HOUR_OF_DAY, 0); cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0); cal1.set(Calendar.MILLISECOND, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);
        cal2.set(Calendar.HOUR_OF_DAY, 23); cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59); cal2.set(Calendar.MILLISECOND, 999);

        for (WorkoutRecord r : cachedRecords) {
            if (r.getTimestamp() >= cal1.getTimeInMillis() && r.getTimestamp() <= cal2.getTimeInMillis()) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    public int getTotalWorkouts() {
        return cachedRecords.size();
    }

    public int getConsecutiveDays() {
        Set<String> dates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (WorkoutRecord r : cachedRecords) {
            dates.add(sdf.format(new Date(r.getTimestamp())));
        }
        Calendar cal = Calendar.getInstance();
        int consecutive = 0;
        for (int i = 0; i < 365; i++) {
            String dateStr = sdf.format(cal.getTime());
            if (dates.contains(dateStr)) {
                consecutive++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                if (i == 0) {
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    dateStr = sdf.format(cal.getTime());
                    if (dates.contains(dateStr)) {
                        consecutive++;
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                    } else break;
                } else break;
            }
        }
        return consecutive;
    }

    public int getWorkoutDays() {
        Set<String> dates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (WorkoutRecord r : cachedRecords) dates.add(sdf.format(new Date(r.getTimestamp())));
        return dates.size();
    }

    public float getCaloriesForToday() {
        float total = 0;
        for (WorkoutRecord r : getRecordsForToday()) total += r.getCaloriesBurned();
        return total;
    }

    public int getWorkoutCountForToday() {
        return getRecordsForToday().size();
    }

    public int getTotalMinutesForToday() {
        int total = 0;
        for (WorkoutRecord r : getRecordsForToday()) {
            int dur = r.getDuration();
            if (dur <= 0 && r.getSets() > 0 && r.getReps() > 0) {
                dur = r.getSets() * r.getReps() * 3 / 60 + 1;
            }
            total += dur;
        }
        return total;
    }

    public int getTotalMinutes() {
        int total = 0;
        for (WorkoutRecord r : cachedRecords) {
            int dur = r.getDuration();
            if (dur <= 0 && r.getSets() > 0 && r.getReps() > 0) {
                dur = r.getSets() * r.getReps() * 3 / 60 + 1;
            }
            total += dur;
        }
        return total;
    }

    public float getTotalCalories() {
        float total = 0;
        for (WorkoutRecord r : cachedRecords) total += r.getCaloriesBurned();
        return total;
    }

    public void recalculateStatistics() { }

    public void clearAll() {
        cachedRecords.clear();
        loaded = false;
    }

    public boolean isLoaded() { return loaded; }
}
