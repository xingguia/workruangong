package com.example.myapplication.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.myapplication.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/v1";
    private static final String PREF_NAME = "fitness_app_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static ApiClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Handler mainHandler;
    private SharedPreferences prefs;
    private String authToken;

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    private ApiClient(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        authToken = prefs.getString(KEY_TOKEN, null);
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    // ==================== Auth ====================

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String token) {
        this.authToken = token;
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public void clearAuthToken() {
        this.authToken = null;
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    // ==================== User / Auth API ====================

    public void register(String phone, String password, String nickname, Callback<Map<String, Object>> callback) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("phone", phone);
            body.put("password", password);
            body.put("nickname", nickname);
            post("/auth/register", body, new RawCallback() {
                @Override
                public void onResponse(String json) {
                    Map<String, Object> result = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                    String token = (String) result.get("access_token");
                    setAuthToken(token);
                    deliverSuccess(callback, result);
                }
                @Override
                public void onFailure(String error) {
                    deliverError(callback, error);
                }
            });
        } catch (Exception e) {
            deliverError(callback, e.getMessage());
        }
    }

    public void login(String phone, String password, Callback<Map<String, Object>> callback) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("phone", phone);
            body.put("password", password);
            post("/auth/login", body, new RawCallback() {
                @Override
                public void onResponse(String json) {
                    Map<String, Object> result = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                    String token = (String) result.get("access_token");
                    setAuthToken(token);
                    deliverSuccess(callback, result);
                }
                @Override
                public void onFailure(String error) {
                    deliverError(callback, error);
                }
            });
        } catch (Exception e) {
            deliverError(callback, e.getMessage());
        }
    }

    public void getProfile(Callback<Map<String, Object>> callback) {
        get("/user/profile", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Map<String, Object> result = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateProfile(Map<String, Object> updates, Callback<Map<String, Object>> callback) {
        put("/user/profile", updates, new RawCallback() {
            @Override
            public void onResponse(String json) {
                Map<String, Object> result = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateVip(boolean isVip, String expireTime, Callback<Map<String, Object>> callback) {
        String url = "/user/vip?is_vip=" + isVip;
        if (expireTime != null) url += "&expire_time=" + expireTime;
        put(url, null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateBodyData(int height, float weight, float bodyFat, float waist, float hip,
                                Callback<Map<String, Object>> callback) {
        String url = "/user/body-data?height=" + height + "&weight=" + weight +
                     "&body_fat=" + bodyFat + "&waist=" + waist + "&hip=" + hip;
        put(url, null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void markAssessmentCompleted(Callback<Map<String, Object>> callback) {
        put("/user/assessment-completed", null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void markUsernameSet(Callback<Map<String, Object>> callback) {
        put("/user/username-set", null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void checkNickname(String nickname, Callback<Map<String, Object>> callback) {
        get("/user/check-nickname?nickname=" + nickname, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void reserveNickname(String nickname, Callback<Map<String, Object>> callback) {
        post("/user/reserve-nickname?nickname=" + nickname, null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateSettings(Boolean workoutReminder, Boolean achievementNotification,
                                Callback<Map<String, Object>> callback) {
        StringBuilder url = new StringBuilder("/user/settings?");
        if (workoutReminder != null) url.append("workout_reminder=").append(workoutReminder).append("&");
        if (achievementNotification != null) url.append("achievement_notification=").append(achievementNotification);
        put(url.toString(), null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    // ==================== Body Records ====================

    public void getBodyRecords(Callback<List<Map<String, Object>>> callback) {
        get("/body-records", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void createBodyRecord(int height, float weight, float bodyFat, float waist, float hip,
                                  Callback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("height", height);
        body.put("weight", weight);
        body.put("body_fat", bodyFat);
        body.put("waist", waist);
        body.put("hip", hip);
        post("/body-records", body, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void deleteBodyRecord(long recordId, Callback<Map<String, Object>> callback) {
        delete("/body-records/" + recordId, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    // ==================== Workout Records ====================

    public void getWorkoutRecords(Callback<List<Map<String, Object>>> callback) {
        get("/workout-records", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void createWorkoutRecord(WorkoutRecord record, Callback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("task_id", record.getTaskId());
        body.put("exercise_name", record.getExerciseName());
        body.put("duration", record.getDuration());
        body.put("sets", record.getSets());
        body.put("reps", record.getReps());
        body.put("weight", (double) record.getWeight());
        body.put("calories_burned", (double) record.getCaloriesBurned());
        body.put("calories", (double) record.getCalories());
        body.put("notes", record.getNotes());
        body.put("muscle_group", record.getMuscleGroup());
        post("/workout-records", body, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void deleteWorkoutRecord(long recordId, Callback<Map<String, Object>> callback) {
        delete("/workout-records/" + recordId, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    // ==================== Training Tasks ====================

    public void getTrainingTasks(Callback<List<Map<String, Object>>> callback) {
        get("/training-tasks", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void createTrainingTask(TrainingTask task, Callback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("date", task.getDate());
        body.put("name", task.getName());
        body.put("description", task.getDescription());
        body.put("duration", task.getDuration());
        body.put("status", task.getStatus().name());
        body.put("exercise_type", task.getExerciseType().name());
        body.put("reps", task.getReps());
        body.put("sets", task.getSets());
        body.put("weight", (double) task.getWeight());
        body.put("muscle_group", task.getMuscleGroup() != null ? task.getMuscleGroup().name() : null);
        body.put("sub_muscle", task.getSubMuscle());
        body.put("calories_recorded", task.isCaloriesRecorded());
        body.put("treadmill_speed", (double) task.getTreadmillSpeed());
        body.put("treadmill_incline", (double) task.getTreadmillIncline());
        post("/training-tasks", body, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateTrainingTask(long taskId, Map<String, Object> updates, Callback<Map<String, Object>> callback) {
        put("/training-tasks/" + taskId, updates, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void deleteTrainingTask(long taskId, Callback<Map<String, Object>> callback) {
        delete("/training-tasks/" + taskId, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    // ==================== Exercise Plans ====================

    public void getExercisePlans(Callback<List<Map<String, Object>>> callback) {
        get("/exercise-plans", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void saveExercisePlan(ExercisePlan plan, Callback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("day_of_week", plan.getDayOfWeek());
        body.put("status", plan.getStatus().name());
        body.put("completion_status", plan.getCompletionStatus().name());
        post("/exercise-plans", body, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateExercisePlan(int dayOfWeek, String status, String completionStatus,
                                    Callback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        if (status != null) body.put("status", status);
        if (completionStatus != null) body.put("completion_status", completionStatus);
        put("/exercise-plans/" + dayOfWeek, body, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    // ==================== Achievements ====================

    public void getAchievements(Callback<List<Map<String, Object>>> callback) {
        get("/achievements", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void unlockAchievement(String type, Callback<Map<String, Object>> callback) {
        post("/achievements?achievement_type=" + type + "&unlocked=true", null, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void updateAchievementDisplay(String type, boolean displayed, int position,
                                          Callback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("displayed", displayed);
        body.put("display_position", position);
        put("/achievements/" + type, body, new RawCallback() {
            @Override
            public void onResponse(String json) {
                deliverSuccess(callback, gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    // ==================== HTTP Helpers ====================

    private interface RawCallback {
        void onResponse(String json);
        void onFailure(String error);
    }

    // ==================== Exercise API ====================

    public void getExercises(Callback<List<Map<String, Object>>> callback) {
        get("/exercises", new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    public void getExercisesByMuscleGroup(String muscleGroup, Callback<List<Map<String, Object>>> callback) {
        get("/exercises?muscle_group=" + muscleGroup, new RawCallback() {
            @Override
            public void onResponse(String json) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> result = gson.fromJson(json, listType);
                deliverSuccess(callback, result);
            }
            @Override
            public void onFailure(String error) {
                deliverError(callback, error);
            }
        });
    }

    private <T> void deliverSuccess(Callback<T> cb, T data) {
        if (cb != null) mainHandler.post(() -> cb.onSuccess(data));
    }

    private <T> void deliverError(Callback<T> cb, String error) {
        if (cb != null) mainHandler.post(() -> cb.onError(error));
    }

    private void get(String path, RawCallback callback) {
        Request.Builder builder = new Request.Builder().url(BASE_URL + path).get();
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        enqueue(builder.build(), callback);
    }

    private void post(String path, Object body, RawCallback callback) {
        Request.Builder builder = new Request.Builder().url(BASE_URL + path);
        if (body != null) {
            builder.post(RequestBody.create(gson.toJson(body), JSON));
        } else {
            builder.post(RequestBody.create("", JSON));
        }
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        enqueue(builder.build(), callback);
    }

    private void put(String path, Object body, RawCallback callback) {
        Request.Builder builder = new Request.Builder().url(BASE_URL + path);
        if (body != null) {
            builder.put(RequestBody.create(gson.toJson(body), JSON));
        } else {
            builder.put(RequestBody.create("", JSON));
        }
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        enqueue(builder.build(), callback);
    }

    private void delete(String path, RawCallback callback) {
        Request.Builder builder = new Request.Builder().url(BASE_URL + path).delete();
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        enqueue(builder.build(), callback);
    }

    private void enqueue(Request request, RawCallback callback) {
        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure(e.getMessage()));
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "{}";
                if (callback != null) {
                    if (response.isSuccessful()) {
                        mainHandler.post(() -> callback.onResponse(body));
                    } else {
                        mainHandler.post(() -> callback.onFailure("HTTP " + response.code() + ": " + body));
                    }
                }
            }
        });
    }
}
