package com.example.myapplication.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BodyRecord implements Serializable {

    private long id;
    private long timestamp;
    private int height;
    private float weight;
    private float bodyFat;
    private float waist;
    private float hip;

    public BodyRecord() {
    }

    public BodyRecord(int height, float weight, float bodyFat, float waist, float hip) {
        this.timestamp = System.currentTimeMillis();
        this.height = height;
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.waist = waist;
        this.hip = hip;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getBodyFat() {
        return bodyFat;
    }

    public void setBodyFat(float bodyFat) {
        this.bodyFat = bodyFat;
    }

    public float getWaist() {
        return waist;
    }

    public void setWaist(float waist) {
        this.waist = waist;
    }

    public float getHip() {
        return hip;
    }

    public void setHip(float hip) {
        this.hip = hip;
    }

    public float getBmi() {
        if (height <= 0 || weight <= 0) {
            return 0;
        }
        float heightM = height / 100f;
        return weight / (heightM * heightM);
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINESE);
        return sdf.format(new Date(timestamp));
    }

    public String getDayOfWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.CHINESE);
        return sdf.format(new Date(timestamp));
    }

    public String getShortDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.CHINESE);
        return sdf.format(new Date(timestamp));
    }
}
