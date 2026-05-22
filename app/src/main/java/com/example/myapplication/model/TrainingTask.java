package com.example.myapplication.model;

import java.io.Serializable;

public class TrainingTask implements Serializable {

    public enum TaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    private long id;
    private long date; // 日期时间戳
    private String name;
    private String description;
    private int duration; // 分钟
    private TaskStatus status;
    private long createdAt;

    public TrainingTask() {
        this.status = TaskStatus.NOT_STARTED;
        this.createdAt = System.currentTimeMillis();
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
}
