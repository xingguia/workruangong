package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {

    private static final String PREF_NAME = "fitness_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_BODY_FAT = "body_fat";
    private static final String KEY_WAIST = "waist";
    private static final String KEY_HIP = "hip";
    private static final String KEY_IS_VIP = "is_vip";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_VIP_EXPIRE_TIME = "vip_expire_time";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_INITIAL_HEIGHT = "initial_height";
    private static final String KEY_INITIAL_WEIGHT = "initial_weight";
    private static final String KEY_INITIAL_BODY_FAT = "initial_body_fat";
    private static final String KEY_INITIAL_WAIST = "initial_waist";
    private static final String KEY_INITIAL_HIP = "initial_hip";
    private static final String KEY_ASSESSMENT_COMPLETED = "assessment_completed";
    private static final String KEY_USERNAME_SET = "username_set";
    private static final String KEY_ALL_USERNAMES = "all_usernames";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_FITNESS_GOAL = "fitness_goal";
    private static final String KEY_WORKOUT_REMINDER = "workout_reminder";
    private static final String KEY_ACHIEVEMENT_NOTIFICATION = "achievement_notification";

    private static SessionManager instance;
    private SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveUserSession(String userId, String nickname, String phone, String token) {
        prefs.edit().putString(KEY_USER_ID, userId)
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_PHONE, phone)
                .putString(KEY_TOKEN, token)
                .commit();
    }

    public void saveUserInfo(String nickname, String avatar, int height, float weight, boolean isVip, int level) {
        prefs.edit().putString(KEY_NICKNAME, nickname)
                .putString(KEY_AVATAR, avatar)
                .putInt(KEY_HEIGHT, height)
                .putFloat(KEY_WEIGHT, weight)
                .putBoolean(KEY_IS_VIP, isVip)
                .putInt(KEY_LEVEL, level)
                .commit();
    }

    public void saveVipInfo(boolean isVip, String expireTime) {
        prefs.edit().putBoolean(KEY_IS_VIP, isVip)
                .putString(KEY_VIP_EXPIRE_TIME, expireTime)
                .commit();
    }

    public void saveBodyData(int height, float weight) {
        prefs.edit().putInt(KEY_HEIGHT, height)
                .putFloat(KEY_WEIGHT, weight)
                .commit();
    }

    public void saveFullBodyData(int height, float weight, float bodyFat, float waist, float hip) {
        prefs.edit().putInt(KEY_HEIGHT, height)
                .putFloat(KEY_WEIGHT, weight)
                .putFloat(KEY_BODY_FAT, bodyFat)
                .putFloat(KEY_WAIST, waist)
                .putFloat(KEY_HIP, hip)
                .commit();
    }

    public void saveInitialBodyData(int height, float weight, float bodyFat, float waist, float hip) {
        prefs.edit().putInt(KEY_INITIAL_HEIGHT, height)
                .putFloat(KEY_INITIAL_WEIGHT, weight)
                .putFloat(KEY_INITIAL_BODY_FAT, bodyFat)
                .putFloat(KEY_INITIAL_WAIST, waist)
                .putFloat(KEY_INITIAL_HIP, hip)
                .commit();
    }

    public void markAssessmentCompleted() {
        prefs.edit().putBoolean(KEY_ASSESSMENT_COMPLETED, true).commit();
    }

    public boolean isAssessmentCompleted() {
        return prefs.getBoolean(KEY_ASSESSMENT_COMPLETED, false);
    }

    public void markUsernameSet() {
        prefs.edit().putBoolean(KEY_USERNAME_SET, true).commit();
    }

    public boolean isUsernameSet() {
        return prefs.getBoolean(KEY_USERNAME_SET, false);
    }

    // Username management
    public void saveNickname(String nickname) {
        prefs.edit().putString(KEY_NICKNAME, nickname).commit();
    }

    public void setNickname(String nickname) {
        prefs.edit().putString(KEY_NICKNAME, nickname).commit();
    }

    public void setHeight(int height) {
        prefs.edit().putInt(KEY_HEIGHT, height).commit();
    }

    public void setWeight(float weight) {
        prefs.edit().putFloat(KEY_WEIGHT, weight).commit();
    }

    public void setAvatar(String avatar) {
        prefs.edit().putString(KEY_AVATAR, avatar).commit();
    }

    public void setGender(String gender) {
        prefs.edit().putString(KEY_GENDER, gender).commit();
    }

    public void setFitnessGoal(String goal) {
        prefs.edit().putString(KEY_FITNESS_GOAL, goal).commit();
    }

    public boolean isNicknameAvailable(String nickname) {
        Set<String> allUsernames = getAllUsernames();
        return !allUsernames.contains(nickname);
    }

    public void addUsernameToSet(String username) {
        Set<String> allUsernames = getAllUsernames();
        allUsernames.add(username);
        prefs.edit().putStringSet(KEY_ALL_USERNAMES, allUsernames).apply();
    }

    public Set<String> getAllUsernames() {
        return new HashSet<>(prefs.getStringSet(KEY_ALL_USERNAMES, new HashSet<>()));
    }

    public int getInitialHeight() {
        return prefs.getInt(KEY_INITIAL_HEIGHT, 0);
    }

    public float getInitialWeight() {
        return prefs.getFloat(KEY_INITIAL_WEIGHT, 0);
    }

    public float getInitialBodyFat() {
        return prefs.getFloat(KEY_INITIAL_BODY_FAT, 0);
    }

    public float getInitialWaist() {
        return prefs.getFloat(KEY_INITIAL_WAIST, 0);
    }

    public float getInitialHip() {
        return prefs.getFloat(KEY_INITIAL_HIP, 0);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getNickname() {
        return prefs.getString(KEY_NICKNAME, "健身爱好者");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, null);
    }

    public int getHeight() {
        return prefs.getInt(KEY_HEIGHT, 0);
    }

    public float getWeight() {
        return prefs.getFloat(KEY_WEIGHT, 0);
    }

    public float getBodyFat() {
        return prefs.getFloat(KEY_BODY_FAT, 0);
    }

    public float getWaist() {
        return prefs.getFloat(KEY_WAIST, 0);
    }

    public float getHip() {
        return prefs.getFloat(KEY_HIP, 0);
    }

    public boolean isVip() {
        return prefs.getBoolean(KEY_IS_VIP, false);
    }

    public int getLevel() {
        return prefs.getInt(KEY_LEVEL, 1);
    }

    public String getVipExpireTime() {
        return prefs.getString(KEY_VIP_EXPIRE_TIME, null);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    // Gender
    public String getGender() {
        return prefs.getString(KEY_GENDER, null);
    }

    // Fitness Goal
    public String getFitnessGoal() {
        return prefs.getString(KEY_FITNESS_GOAL, null);
    }

    // Workout Reminder
    public void setWorkoutReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WORKOUT_REMINDER, enabled).apply();
    }

    public boolean isWorkoutReminderEnabled() {
        return prefs.getBoolean(KEY_WORKOUT_REMINDER, true);
    }

    // Achievement Notification
    public void setAchievementNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ACHIEVEMENT_NOTIFICATION, enabled).apply();
    }

    public boolean isAchievementNotificationEnabled() {
        return prefs.getBoolean(KEY_ACHIEVEMENT_NOTIFICATION, true);
    }
}
