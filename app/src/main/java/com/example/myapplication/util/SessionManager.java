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
    private boolean darkMode = true;
    private String unitSystem = "metric";
    private String reminderTime = "18:00";
    private android.content.SharedPreferences settingsPrefs;

    // 初始数据
    private int initialHeight;
    private float initialWeight;
    private float initialBodyFat;
    private float initialWaist;
    private float initialHip;

    private SessionManager(Context context) {
        appContext = context.getApplicationContext();
        api = ApiClient.getInstance(appContext);
        settingsPrefs = appContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        nickname = "健身爱好者";
        level = 1;
        workoutReminder = true;
        achievementNotification = true;
        // Load persisted settings
        darkMode = settingsPrefs.getBoolean("dark_mode", true);
        unitSystem = settingsPrefs.getString("unit_system", "metric");
        reminderTime = settingsPrefs.getString("reminder_time", "18:00");
        // Load persisted user data for instant display
        nickname = settingsPrefs.getString("nickname", "健身爱好者");
        avatar = settingsPrefs.getString("avatar", null);
        gender = settingsPrefs.getString("gender", null);
        fitnessGoal = settingsPrefs.getString("fitness_goal", null);
        height = settingsPrefs.getInt("height", 0);
        weight = settingsPrefs.getFloat("weight", 0);
        initialHeight = settingsPrefs.getInt("initial_height", 0);
        initialWeight = settingsPrefs.getFloat("initial_weight", 0);
        initialBodyFat = settingsPrefs.getFloat("initial_body_fat", 0);
        initialWaist = settingsPrefs.getFloat("initial_waist", 0);
        initialHip = settingsPrefs.getFloat("initial_hip", 0);
        level = settingsPrefs.getInt("level", 1);
        assessmentCompleted = settingsPrefs.getBoolean("assessment_completed", false);
        usernameSet = settingsPrefs.getBoolean("username_set", false);
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

        // 清除本地缓存的用户数据
        settingsPrefs.edit()
                .remove("nickname")
                .remove("avatar")
                .remove("gender")
                .remove("fitness_goal")
                .remove("height")
                .remove("weight")
                .remove("initial_height")
                .remove("initial_weight")
                .remove("initial_body_fat")
                .remove("initial_waist")
                .remove("initial_hip")
                .remove("level")
                .remove("assessment_completed")
                .remove("username_set")
                .apply();

        // 清除其他管理器的缓存数据
        AchievementManager.resetInstance();
        WorkoutRecordManager.getInstance(appContext).clearAll();
        TrainingTaskManager.getInstance(appContext).clearAll();
        ExercisePlanManager.getInstance(appContext).clearAll();
    }

    private void applyUserData(Map<String, Object> user) {
        if (user == null) return;
        userId = String.valueOf(((Number) user.get("id")).longValue());
        // 保护本地已设置的非默认昵称，防止被服务器返回的默认值覆盖（解决竞态条件）
        String serverNickname = (String) user.getOrDefault("nickname", "健身爱好者");
        if (nickname == null || nickname.equals("健身爱好者") || !serverNickname.equals("健身爱好者")) {
            nickname = serverNickname;
        }
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
        // 本地设置优先，不覆盖 SharedPreferences 中的值
        // darkMode, unitSystem, reminderTime 由本地 SharedPreferences 管理
        // 读取初始数据：只在服务器有有效数据(>0)时才覆盖本地值，防止竞态条件导致数据丢失
        int serverInitH = ((Number) user.getOrDefault("initial_height", 0)).intValue();
        float serverInitW = ((Number) user.getOrDefault("initial_weight", 0)).floatValue();
        float serverInitBf = ((Number) user.getOrDefault("initial_body_fat", 0)).floatValue();
        float serverInitWaist = ((Number) user.getOrDefault("initial_waist", 0)).floatValue();
        float serverInitHip = ((Number) user.getOrDefault("initial_hip", 0)).floatValue();
        if (serverInitH > 0) initialHeight = serverInitH;
        if (serverInitW > 0) initialWeight = serverInitW;
        if (serverInitBf > 0) initialBodyFat = serverInitBf;
        if (serverInitWaist > 0) initialWaist = serverInitWaist;
        if (serverInitHip > 0) initialHip = serverInitHip;
        // 持久化到本地，重启后可立即显示
        saveUserCache();
    }

    private void saveUserCache() {
        settingsPrefs.edit()
                .putString("nickname", nickname)
                .putString("avatar", avatar)
                .putString("gender", gender)
                .putString("fitness_goal", fitnessGoal)
                .putInt("height", height)
                .putFloat("weight", weight)
                .putInt("initial_height", initialHeight)
                .putFloat("initial_weight", initialWeight)
                .putFloat("initial_body_fat", initialBodyFat)
                .putFloat("initial_waist", initialWaist)
                .putFloat("initial_hip", initialHip)
                .putInt("level", level)
                .putBoolean("assessment_completed", assessmentCompleted)
                .putBoolean("username_set", usernameSet)
                .apply();
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
        settingsPrefs.edit()
                .putInt("height", height)
                .putFloat("weight", weight)
                .apply();
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
        // 持久化到本地
        settingsPrefs.edit()
                .putInt("initial_height", height)
                .putFloat("initial_weight", weight)
                .putFloat("initial_body_fat", bodyFat)
                .putFloat("initial_waist", waist)
                .putFloat("initial_hip", hip)
                .apply();
        // 通过API保存到服务器
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("initial_height", height);
        updates.put("initial_weight", weight);
        updates.put("initial_body_fat", bodyFat);
        updates.put("initial_waist", waist);
        updates.put("initial_hip", hip);
        api.updateProfile(updates, null);
    }

    public void markAssessmentCompleted() {
        this.assessmentCompleted = true;
        settingsPrefs.edit().putBoolean("assessment_completed", true).apply();
        api.markAssessmentCompleted(null);
    }

    public void markUsernameSet() {
        this.usernameSet = true;
        settingsPrefs.edit().putBoolean("username_set", true).apply();
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
        saveNickname(nickname, null);
    }

    public void saveNickname(String nickname, Runnable onComplete) {
        this.nickname = nickname;
        settingsPrefs.edit().putString("nickname", nickname).apply();
        // 同步保存初始数据到服务器
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("nickname", nickname);
        // 把当前的 initial_* 也一起发送，确保服务器有完整数据
        if (initialHeight > 0) updates.put("initial_height", initialHeight);
        if (initialWeight > 0) updates.put("initial_weight", initialWeight);
        if (initialBodyFat > 0) updates.put("initial_body_fat", initialBodyFat);
        if (initialWaist > 0) updates.put("initial_waist", initialWaist);
        if (initialHip > 0) updates.put("initial_hip", initialHip);
        api.updateProfile(updates, new ApiClient.Callback<Map<String, Object>>() {
            @Override public void onSuccess(Map<String, Object> data) {
                if (onComplete != null) onComplete.run();
            }
            @Override public void onError(String error) {
                // 即使失败也继续，本地已保存
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        settingsPrefs.edit().putString("nickname", nickname).apply();
    }

    public void setHeight(int height) {
        this.height = height;
        settingsPrefs.edit().putInt("height", height).apply();
    }

    public void setWeight(float weight) {
        this.weight = weight;
        settingsPrefs.edit().putFloat("weight", weight).apply();
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        settingsPrefs.edit().putString("avatar", avatar).apply();
    }

    public void setGender(String gender) {
        this.gender = gender;
        settingsPrefs.edit().putString("gender", gender).apply();
    }

    public void setFitnessGoal(String goal) {
        this.fitnessGoal = goal;
        settingsPrefs.edit().putString("fitness_goal", goal).apply();
    }

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

    public void setDarkMode(boolean enabled) {
        this.darkMode = enabled;
        settingsPrefs.edit().putBoolean("dark_mode", enabled).apply();
        api.updateSettingsFull(null, null, enabled, null, null, null);
    }

    public boolean isDarkMode() { return darkMode; }

    public void setUnitSystem(String unit) {
        this.unitSystem = unit;
        settingsPrefs.edit().putString("unit_system", unit).apply();
        api.updateSettingsFull(null, null, null, unit, null, null);
    }

    public String getUnitSystem() { return unitSystem; }

    public void setReminderTime(String time) {
        this.reminderTime = time;
        settingsPrefs.edit().putString("reminder_time", time).apply();
        api.updateSettingsFull(null, null, null, null, time, null);
    }

    public String getReminderTime() { return reminderTime; }

    public void clearLocalCache(Context context) {
        WorkoutRecordManager.getInstance(context).clearAll();
        TrainingTaskManager.getInstance(context).clearAll();
        ExercisePlanManager.getInstance(context).clearAll();
        AchievementManager.resetInstance();
    }

    public void changePassword(String oldPwd, String newPwd, ApiClient.Callback<Map<String, Object>> callback) {
        api.changePassword(oldPwd, newPwd, callback);
    }

    public void deleteAccount(ApiClient.Callback<Map<String, Object>> callback) {
        api.deleteAccount(callback);
    }

    public void submitFeedback(String content, String contact, String category, ApiClient.Callback<Map<String, Object>> callback) {
        api.submitFeedback(content, contact, category, callback);
    }

    public void exportData(ApiClient.Callback<Map<String, Object>> callback) {
        api.exportData(callback);
    }
}
