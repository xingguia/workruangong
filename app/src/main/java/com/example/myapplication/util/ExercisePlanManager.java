package com.example.myapplication.util;

import android.content.Context;

import com.example.myapplication.api.ApiClient;
import com.example.myapplication.model.ExercisePlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExercisePlanManager {

    private static ExercisePlanManager instance;
    private ApiClient api;
    private List<ExercisePlan> cachedPlans = new ArrayList<>();
    private boolean loaded = false;

    private ExercisePlanManager(Context context) {
        api = ApiClient.getInstance(context.getApplicationContext());
    }

    public static synchronized ExercisePlanManager getInstance(Context context) {
        if (instance == null) {
            instance = new ExercisePlanManager(context);
        }
        return instance;
    }

    public void loadPlans(Runnable onDone) {
        api.getExercisePlans(new ApiClient.Callback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                cachedPlans.clear();
                if (data != null) {
                    for (Map<String, Object> item : data) {
                        ExercisePlan plan = new ExercisePlan();
                        plan.setDayOfWeek(((Number) item.get("day_of_week")).intValue());
                        plan.setStatus(ExercisePlan.DayStatus.valueOf((String) item.get("status")));
                        plan.setCompletionStatus(ExercisePlan.CompletionStatus.valueOf((String) item.get("completion_status")));
                        cachedPlans.add(plan);
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

    public void savePlan(ExercisePlan plan) {
        boolean found = false;
        for (int i = 0; i < cachedPlans.size(); i++) {
            if (cachedPlans.get(i).getDayOfWeek() == plan.getDayOfWeek()) {
                cachedPlans.set(i, plan);
                found = true;
                break;
            }
        }
        if (!found) cachedPlans.add(plan);

        api.saveExercisePlan(plan, null);
    }

    public ExercisePlan getPlan(int dayOfWeek) {
        for (ExercisePlan plan : cachedPlans) {
            if (plan.getDayOfWeek() == dayOfWeek) return plan;
        }
        return new ExercisePlan(dayOfWeek);
    }

    public List<ExercisePlan> getPlans() {
        return new ArrayList<>(cachedPlans);
    }

    public void clearAll() { cachedPlans.clear(); }
    public boolean isLoaded() { return loaded; }
}
