package com.example.myapplication.model;

import java.io.Serializable;

public class ExercisePlan implements Serializable {

    public enum DayStatus {
        NOT_SET,    // 未设置
        REST,       // 休息日
        WORKOUT,    // 锻炼日
    }

    public enum CompletionStatus {
        NOT_SET,
        PENDING,    // 未完成
        COMPLETED   // 已完成
    }

    private int dayOfWeek; // 1-7 (周一到周日)
    private DayStatus status;
    private CompletionStatus completionStatus;

    public ExercisePlan() {
        this.status = DayStatus.NOT_SET;
        this.completionStatus = CompletionStatus.NOT_SET;
    }

    public ExercisePlan(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        this.status = DayStatus.NOT_SET;
        this.completionStatus = CompletionStatus.NOT_SET;
    }

    public ExercisePlan(int dayOfWeek, DayStatus status) {
        this.dayOfWeek = dayOfWeek;
        this.status = status;
        this.completionStatus = status == DayStatus.WORKOUT ? CompletionStatus.PENDING : CompletionStatus.NOT_SET;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public DayStatus getStatus() {
        return status;
    }

    public void setStatus(DayStatus status) {
        this.status = status;
        if (status == DayStatus.WORKOUT) {
            if (this.completionStatus == CompletionStatus.NOT_SET) {
                this.completionStatus = CompletionStatus.PENDING;
            }
        } else if (status == DayStatus.REST) {
            this.completionStatus = CompletionStatus.NOT_SET;
        }
    }

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(CompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }

    public boolean isWorkoutDay() {
        return status == DayStatus.WORKOUT;
    }

    public boolean isRestDay() {
        return status == DayStatus.REST;
    }

    public boolean isNotSet() {
        return status == DayStatus.NOT_SET;
    }

    public boolean isCompleted() {
        return completionStatus == CompletionStatus.COMPLETED;
    }
}
