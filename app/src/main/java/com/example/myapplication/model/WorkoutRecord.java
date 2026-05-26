package com.example.myapplication.model;

import java.io.Serializable;

public class WorkoutRecord implements Serializable {

    private long id;
    private long taskId; // 关联的训练任务ID，用于去重
    private String exerciseName;
    private long timestamp;
    private int duration;
    private int sets;
    private int reps;
    private float weight;
    private float caloriesBurned;
    private String notes;
    private String muscleGroup;
    private float calories; // 实时计算的卡路里

    public WorkoutRecord() {
        this.timestamp = System.currentTimeMillis();
    }

    public WorkoutRecord(String exerciseName, int duration, float caloriesBurned) {
        this();
        this.exerciseName = exerciseName;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
    }

    public WorkoutRecord(String exerciseName, int duration, int reps, float weight, float caloriesBurned) {
        this(exerciseName, duration, caloriesBurned);
        this.reps = reps;
        this.weight = weight;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDate() {
        return timestamp;
    }

    public void setDate(long date) {
        this.timestamp = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(float caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public float getCalories() {
        return calories > 0 ? calories : caloriesBurned;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }
}
