package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.ExercisePlan;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ExercisePlanManager {

    private static final String PREF_NAME = "exercise_plan";
    private static final String KEY_PLANS = "plans";

    private static ExercisePlanManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private ExercisePlanManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized ExercisePlanManager getInstance(Context context) {
        if (instance == null) {
            instance = new ExercisePlanManager(context);
        }
        return instance;
    }

    public void savePlan(ExercisePlan plan) {
        List<ExercisePlan> plans = getPlans();
        // Update existing or add new
        boolean found = false;
        for (int i = 0; i < plans.size(); i++) {
            if (plans.get(i).getDayOfWeek() == plan.getDayOfWeek()) {
                plans.set(i, plan);
                found = true;
                break;
            }
        }
        if (!found) {
            plans.add(plan);
        }
        savePlans(plans);
    }

    public ExercisePlan getPlan(int dayOfWeek) {
        List<ExercisePlan> plans = getPlans();
        for (ExercisePlan plan : plans) {
            if (plan.getDayOfWeek() == dayOfWeek) {
                return plan;
            }
        }
        // Return default plan if not found
        return new ExercisePlan(dayOfWeek);
    }

    public List<ExercisePlan> getPlans() {
        String json = prefs.getString(KEY_PLANS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<ExercisePlan>>() {}.getType();
        List<ExercisePlan> plans = gson.fromJson(json, type);
        return plans != null ? plans : new ArrayList<>();
    }

    private void savePlans(List<ExercisePlan> plans) {
        String json = gson.toJson(plans);
        prefs.edit().putString(KEY_PLANS, json).apply();
    }

    public void clearAll() {
        prefs.edit().remove(KEY_PLANS).apply();
    }
}
