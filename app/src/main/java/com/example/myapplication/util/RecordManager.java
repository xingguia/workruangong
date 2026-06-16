package com.example.myapplication.util;

import android.content.Context;

import com.example.myapplication.api.ApiClient;
import com.example.myapplication.model.BodyRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RecordManager {

    private static RecordManager instance;
    private ApiClient api;
    private List<BodyRecord> cachedRecords = new ArrayList<>();
    private boolean loaded = false;

    private RecordManager(Context context) {
        api = ApiClient.getInstance(context.getApplicationContext());
    }

    public static synchronized RecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecordManager(context);
        }
        return instance;
    }

    // ==================== Async load ====================

    public void loadRecords(Runnable onDone) {
        api.getBodyRecords(new ApiClient.Callback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                cachedRecords.clear();
                if (data != null) {
                    for (Map<String, Object> item : data) {
                        cachedRecords.add(mapToBodyRecord(item));
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

    private BodyRecord mapToBodyRecord(Map<String, Object> item) {
        BodyRecord record = new BodyRecord();
        record.setId(((Number) item.get("id")).longValue());
        record.setTimestamp(((Number) item.get("timestamp")).longValue());
        record.setHeight(((Number) item.getOrDefault("height", 0)).intValue());
        record.setWeight(((Number) item.getOrDefault("weight", 0)).floatValue());
        record.setBodyFat(((Number) item.getOrDefault("body_fat", 0)).floatValue());
        record.setWaist(((Number) item.getOrDefault("waist", 0)).floatValue());
        record.setHip(((Number) item.getOrDefault("hip", 0)).floatValue());
        return record;
    }

    // ==================== CRUD ====================

    public void saveRecord(BodyRecord record) {
        record.setTimestamp(System.currentTimeMillis());
        cachedRecords.add(0, record);
        api.createBodyRecord(record.getHeight(), record.getWeight(), record.getBodyFat(),
                record.getWaist(), record.getHip(), new ApiClient.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                record.setId(((Number) data.get("id")).longValue());
                record.setTimestamp(((Number) data.get("timestamp")).longValue());
            }
            @Override
            public void onError(String error) { }
        });
    }

    public void updateRecord(BodyRecord record) {
        for (int i = 0; i < cachedRecords.size(); i++) {
            if (cachedRecords.get(i).getId() == record.getId()) {
                cachedRecords.set(i, record);
                break;
            }
        }
    }

    public void deleteRecord(long recordId) {
        cachedRecords.removeIf(record -> record.getId() == recordId);
        api.deleteBodyRecord(recordId, null);
    }

    public List<BodyRecord> getRecords() {
        if (!loaded) return new ArrayList<>();
        return new ArrayList<>(cachedRecords);
    }

    public List<BodyRecord> getRecordsByPeriod(int days) {
        List<BodyRecord> allRecords = getRecords();
        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        List<BodyRecord> filtered = new ArrayList<>();
        for (BodyRecord record : allRecords) {
            if (record.getTimestamp() >= cutoffTime) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    public List<BodyRecord> getLatestRecord() {
        List<BodyRecord> records = getRecords();
        if (records.isEmpty()) return new ArrayList<>();
        return Collections.singletonList(records.get(0));
    }

    public int getRecordCount() {
        return cachedRecords.size();
    }

    public boolean isLoaded() { return loaded; }
}
