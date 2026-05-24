package com.example.myapplication.model;

import java.io.Serializable;

public class TrainingTask implements Serializable {

    public enum TaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    public enum ExerciseType {
        STRENGTH,       // 力量训练（无器材）
        EQUIPMENT,      // 器材训练
        CARDIO,         // 有氧训练
        HIIT,           // HIIT训练
        FLEXIBILITY,    // 柔韧/拉伸
        CORE            // 核心训练
    }

    private long id;
    private long date; // 日期时间戳
    private String name;
    private String description;
    private int duration; // 分钟
    private TaskStatus status;
    private long createdAt;
    private ExerciseType exerciseType;
    private int reps;        // 每组次数
    private int sets;        // 组数
    private float weight;    // 重量（公斤）
    private boolean caloriesRecorded; // 是否已记录卡路里（防止重复记录）

    // 跑步机专用字段
    private float treadmillSpeed;    // 跑步机速度（公里/小时）
    private float treadmillIncline;   // 跑步机坡度（%）

    public TrainingTask() {
        this.status = TaskStatus.NOT_STARTED;
        this.createdAt = System.currentTimeMillis();
        this.exerciseType = ExerciseType.STRENGTH;
        this.reps = 0;
        this.sets = 0;
        this.weight = 0f;
        this.caloriesRecorded = false;
        this.treadmillSpeed = 0f;
        this.treadmillIncline = 0f;
    }

    public TrainingTask(String name, String description, int duration) {
        this();
        this.name = name;
        this.description = description;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isCaloriesRecorded() {
        return caloriesRecorded;
    }

    public void setCaloriesRecorded(boolean caloriesRecorded) {
        this.caloriesRecorded = caloriesRecorded;
    }

    public float getTreadmillSpeed() {
        return treadmillSpeed;
    }

    public void setTreadmillSpeed(float treadmillSpeed) {
        this.treadmillSpeed = treadmillSpeed;
    }

    public float getTreadmillIncline() {
        return treadmillIncline;
    }

    public void setTreadmillIncline(float treadmillIncline) {
        this.treadmillIncline = treadmillIncline;
    }

    public boolean isTreadmillExercise() {
        String nameLower = name != null ? name.toLowerCase() : "";
        return nameLower.contains("跑步机") || nameLower.contains("treadmill") || exerciseType == ExerciseType.CARDIO;
    }
}
