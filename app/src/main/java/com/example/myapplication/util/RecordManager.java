package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.BodyRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordManager {

    private static final String PREF_NAME = "body_records";
    private static final String KEY_RECORDS = "records";

    private static RecordManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private RecordManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized RecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecordManager(context);
        }
        return instance;
    }

    public void saveRecord(BodyRecord record) {
        List<BodyRecord> records = getRecords();
        record.setId(System.currentTimeMillis());
        records.add(record);
        saveRecords(records);
    }

    public void updateRecord(BodyRecord record) {
        List<BodyRecord> records = getRecords();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId() == record.getId()) {
                records.set(i, record);
                break;
            }
        }
        saveRecords(records);
    }

    public void deleteRecord(long recordId) {
        List<BodyRecord> records = getRecords();
        records.removeIf(record -> record.getId() == recordId);
        saveRecords(records);
    }

    public List<BodyRecord> getRecords() {
        String json = prefs.getString(KEY_RECORDS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<BodyRecord>>() {}.getType();
        List<BodyRecord> records = gson.fromJson(json, type);
        if (records == null) {
            return new ArrayList<>();
        }
        // Sort by timestamp descending (newest first)
        Collections.sort(records, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        return records;
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
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        return Collections.singletonList(records.get(0));
    }

    private void saveRecords(List<BodyRecord> records) {
        String json = gson.toJson(records);
        prefs.edit().putString(KEY_RECORDS, json).apply();
    }

    public int getRecordCount() {
        return getRecords().size();
    }
}
