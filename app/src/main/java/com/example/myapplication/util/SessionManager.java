package com.example.myapplication.util;

import android.content.Context;

import com.example.myapplication.api.ApiClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SessionManager {

    private static SessionManager instance;
    private ApiClient api;
    private Context appContext;

    // 本地缓存
    private String userId;
    private String nickname;
    private String phone;
    private String avatar;
    private String gender;
    private String fitnessGoal;
    private int height;
    private float weight;
    private float bodyFat;
    private float waist;
    private float hip;
    private boolean isVip;
    private int level;
    private String vipExpireTime;
    private boolean assessmentCompleted;
    private boolean usernameSet;
    private boolean workoutReminder;
    private boolean achievementNotification;

    // 初始数据
    private int initialHeight;
    private float initialWeight;
    private float initialBodyFat;
    private float initialWaist;
    private float initialHip;

    private SessionManager(Context context) {
        appContext = context.getApplicationContext();
        api = ApiClient.getInstance(appContext);
        nickname = "健身爱好者";
        level = 1;
        workoutReminder = true;
        achievementNotification = true;
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // ==================== Auth ====================

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }

    public void login(String phone, String password, AuthCallback callback) {
        api.login(phone, password, new ApiClient.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                applyUserData(user);
                callback.onSuccess();
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void register(String phone, String password, String nickname, AuthCallback callback) {
        api.register(phone, password, nickname, new ApiClient.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                applyUserData(user);
                callback.onSuccess();
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void fetchProfile(Runnable onDone) {
        api.getProfile(new ApiClient.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                applyUserData(data);
                if (onDone != null) onDone.run();
            }
            @Override
            public void onError(String error) {
                if (onDone != null) onDone.run();
            }
        });
    }

    public void logout() {
        api.clearAuthToken();
        userId = null;
        nickname = "健身爱好者";
        phone = null;
        avatar = null;
        height = 0;
        weight = 0;
        bodyFat = 0;
        waist = 0;
        hip = 0;
        isVip = false;
        level = 1;
        vipExpireTime = null;
        assessmentCompleted = false;
        usernameSet = false;
    }

    private void applyUserData(Map<String, Object> user) {
        if (user == null) return;
        userId = String.valueOf(((Number) user.get("id")).longValue());
        nickname = (String) user.getOrDefault("nickname", "健身爱好者");
        phone = (String) user.get("phone");
        avatar = (String) user.get("avatar");
        gender = (String) user.get("gender");
        fitnessGoal = (String) user.get("fitness_goal");
        height = ((Number) user.getOrDefault("height", 0)).intValue();
        weight = ((Number) user.getOrDefault("weight", 0)).floatValue();
        bodyFat = ((Number) user.getOrDefault("body_fat", 0)).floatValue();
        waist = ((Number) user.getOrDefault("waist", 0)).floatValue();
        hip = ((Number) user.getOrDefault("hip", 0)).floatValue();
        isVip = (Boolean) user.getOrDefault("is_vip", false);
        level = ((Number) user.getOrDefault("level", 1)).intValue();
        vipExpireTime = (String) user.get("vip_expire_time");
        assessmentCompleted = (Boolean) user.getOrDefault("assessment_completed", false) || assessmentCompleted;
        usernameSet = (Boolean) user.getOrDefault("username_set", false) || usernameSet;
        workoutReminder = (Boolean) user.getOrDefault("workout_reminder", true);
        achievementNotification = (Boolean) user.getOrDefault("achievement_notification", true);
    }

    // ==================== Save methods (API-backed) ====================

    public void saveUserInfo(String nickname, String avatar, int height, float weight, boolean isVip, int level) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.height = height;
        this.weight = weight;
        this.isVip = isVip;
        this.level = level;
        // Async push to server
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("nickname", nickname);
        updates.put("avatar", avatar);
        updates.put("height", height);
        updates.put("weight", weight);
        api.updateProfile(updates, new ApiClient.Callback<Map<String, Object>>() {
            @Override public void onSuccess(Map<String, Object> data) {}
            @Override public void onError(String error) {}
        });
    }

    public void saveVipInfo(boolean isVip, String expireTime) {
        this.isVip = isVip;
        this.vipExpireTime = expireTime;
        api.updateVip(isVip, expireTime, null);
    }

    public void saveBodyData(int height, float weight) {
        this.height = height;
        this.weight = weight;
        api.updateBodyData(height, weight, bodyFat, waist, hip, null);
    }

    public void saveFullBodyData(int height, float weight, float bodyFat, float waist, float hip) {
        this.height = height;
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.waist = waist;
        this.hip = hip;
        api.updateBodyData(height, weight, bodyFat, waist, hip, null);
    }

    public void saveInitialBodyData(int height, float weight, float bodyFat, float waist, float hip) {
        this.initialHeight = height;
        this.initialWeight = weight;
        this.initialBodyFat = bodyFat;
        this.initialWaist = waist;
        this.initialHip = hip;
    }

    public void markAssessmentCompleted() {
        this.assessmentCompleted = true;
        api.markAssessmentCompleted(null);
    }

    public void markUsernameSet() {
        this.usernameSet = true;
        api.markUsernameSet(null);
    }

    // ==================== Getters ====================

    public String getUserId() { return userId; }
    public String getNickname() { return nickname != null ? nickname : "健身爱好者"; }
    public String getPhone() { return phone; }
    public String getAvatar() { return avatar; }
    public String getGender() { return gender; }
    public String getFitnessGoal() { return fitnessGoal; }
    public int getHeight() { return height; }
    public float getWeight() { return weight; }
    public float getBodyFat() { return bodyFat; }
    public float getWaist() { return waist; }
    public float getHip() { return hip; }
    public boolean isVip() { return isVip; }
    public int getLevel() { return level; }
    public String getVipExpireTime() { return vipExpireTime; }
    public boolean isAssessmentCompleted() { return assessmentCompleted; }
    public boolean isUsernameSet() { return usernameSet; }
    public String getToken() { return api.getAuthToken(); }
    public boolean isLoggedIn() { return api.isLoggedIn(); }

    public int getInitialHeight() { return initialHeight; }
    public float getInitialWeight() { return initialWeight; }
    public float getInitialBodyFat() { return initialBodyFat; }
    public float getInitialWaist() { return initialWaist; }
    public float getInitialHip() { return initialHip; }

    // ==================== Setters ====================

    public void saveNickname(String nickname) {
        this.nickname = nickname;
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("nickname", nickname);
        api.updateProfile(updates, new ApiClient.Callback<Map<String, Object>>() {
            @Override public void onSuccess(Map<String, Object> data) {}
            @Override public void onError(String error) {}
        });
    }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setHeight(int height) { this.height = height; }
    public void setWeight(float weight) { this.weight = weight; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setGender(String gender) { this.gender = gender; }
    public void setFitnessGoal(String goal) { this.fitnessGoal = goal; }

    // ==================== Username ====================

    public boolean isNicknameAvailable(String nickname) {
        // Synchronous check not possible with API; return true and validate on server
        return true;
    }

    public void checkNickname(String nickname, ApiClient.Callback<Map<String, Object>> callback) {
        api.checkNickname(nickname, callback);
    }

    public void addUsernameToSet(String username) {
        api.reserveNickname(username, null);
    }

    public Set<String> getAllUsernames() {
        return new HashSet<>();
    }

    // ==================== Settings ====================

    public void setWorkoutReminderEnabled(boolean enabled) {
        this.workoutReminder = enabled;
        api.updateSettings(enabled, null, null);
    }

    public boolean isWorkoutReminderEnabled() { return workoutReminder; }

    public void setAchievementNotificationEnabled(boolean enabled) {
        this.achievementNotification = enabled;
        api.updateSettings(null, enabled, null);
    }

    public boolean isAchievementNotificationEnabled() { return achievementNotification; }
}
