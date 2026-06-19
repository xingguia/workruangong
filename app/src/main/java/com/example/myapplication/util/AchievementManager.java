package com.example.myapplication.util;

import android.content.Context;

import com.example.myapplication.api.ApiClient;
import com.example.myapplication.model.Achievement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AchievementManager {

    private static AchievementManager instance;
    private ApiClient api;
    private List<Achievement> achievements = new ArrayList<>();
    private boolean loaded = false;
    private int earlyBirdCount = 0;
    private int nightOwlCount = 0;

    public interface AchievementUnlockListener {
        void onAchievementUnlocked(Achievement achievement);
    }

    private AchievementUnlockListener unlockListener;

    private AchievementManager(Context context) {
        api = ApiClient.getInstance(context.getApplicationContext());
        for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
            achievements.add(new Achievement(type));
        }
    }

    public static synchronized AchievementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AchievementManager(context);
        }
        return instance;
    }

    public void setUnlockListener(AchievementUnlockListener listener) {
        this.unlockListener = listener;
    }

    public void loadAchievements(Runnable onDone) {
        api.getAchievements(new ApiClient.Callback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                if (data != null) {
                    for (Map<String, Object> item : data) {
                        String typeName = (String) item.get("achievement_type");
                        try {
                            Achievement.AchievementType type = Achievement.AchievementType.valueOf(typeName);
                            Achievement a = getAchievement(type);
                            if (a != null) {
                                a.setUnlocked((Boolean) item.getOrDefault("unlocked", false));
                                a.setUnlockTime(((Number) item.getOrDefault("unlock_time", 0)).longValue());
                                a.setDisplayed((Boolean) item.getOrDefault("displayed", false));
                                a.setDisplayPosition(((Number) item.getOrDefault("display_position", -1)).intValue());
                            }
                        } catch (IllegalArgumentException ignored) { }
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

    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements);
    }

    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> result = new ArrayList<>();
        for (Achievement a : achievements) {
            if (a.isUnlocked()) result.add(a);
        }
        return result;
    }

    public List<Achievement> getDisplayedAchievements() {
        List<Achievement> result = new ArrayList<>();
        for (Achievement a : achievements) {
            if (a.isUnlocked() && a.isDisplayed()) result.add(a);
        }
        return result;
    }

    public Achievement getAchievement(Achievement.AchievementType type) {
        for (Achievement a : achievements) {
            if (a.getType() == type) return a;
        }
        return null;
    }

    private void unlockAchievement(Achievement.AchievementType type) {
        Achievement achievement = getAchievement(type);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.setUnlocked(true);
            achievement.setUnlockTime(System.currentTimeMillis());
            api.unlockAchievement(type.name(), null);
            if (unlockListener != null) {
                unlockListener.onAchievementUnlocked(achievement);
            }
        }
    }

    public void checkAllAchievements(Context context) {
        WorkoutRecordManager workoutManager = WorkoutRecordManager.getInstance(context);
        SessionManager sessionManager = SessionManager.getInstance(context);

        int totalWorkouts = workoutManager.getTotalWorkouts();
        float totalCalories = workoutManager.getTotalCalories();
        int consecutiveDays = workoutManager.getConsecutiveDays();

        if (totalWorkouts >= 1) unlockAchievement(Achievement.AchievementType.FIRST_WORKOUT);
        if (consecutiveDays >= 7) unlockAchievement(Achievement.AchievementType.STREAK_7_DAYS);
        if (consecutiveDays >= 30) unlockAchievement(Achievement.AchievementType.STREAK_30_DAYS);
        if (totalWorkouts >= 100) unlockAchievement(Achievement.AchievementType.WORKOUT_100);
        if (totalWorkouts >= 500) unlockAchievement(Achievement.AchievementType.WORKOUT_500);

        if (totalCalories >= 1000) unlockAchievement(Achievement.AchievementType.BURN_1000_CAL);
        if (totalCalories >= 5000) unlockAchievement(Achievement.AchievementType.BURN_5000_CAL);
        if (totalCalories >= 10000) unlockAchievement(Achievement.AchievementType.BURN_10000_CAL);

        if (consecutiveDays >= 7) unlockAchievement(Achievement.AchievementType.CHECKIN_7);
        if (consecutiveDays >= 30) unlockAchievement(Achievement.AchievementType.CHECKIN_30);

        checkWeightAchievements(sessionManager);
        checkTimeAchievements(context);
    }

    private void checkWeightAchievements(SessionManager sessionManager) {
        float initialWeight = sessionManager.getInitialWeight();
        float currentWeight = sessionManager.getWeight();
        if (initialWeight > 0 && currentWeight > 0 && initialWeight != currentWeight) {
            unlockAchievement(Achievement.AchievementType.WEIGHT_CHANGE);
        }
    }

    private void checkTimeAchievements(Context context) {
        if (earlyBirdCount >= 10) unlockAchievement(Achievement.AchievementType.EARLY_BIRD);
        if (nightOwlCount >= 10) unlockAchievement(Achievement.AchievementType.NIGHT_OWL);
    }

    public void recordWorkoutCompletion(Context context) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        if (hour < 6) earlyBirdCount++;
        if (hour >= 21) nightOwlCount++;

        checkAllAchievements(context);
    }

    public void setAchievementDisplayed(Achievement achievement, boolean displayed, int position) {
        if (displayed) {
            for (Achievement a : achievements) {
                if (a.isDisplayed() && a.getDisplayPosition() == position && a != achievement) {
                    a.setDisplayed(false);
                    a.setDisplayPosition(-1);
                }
            }
            achievement.setDisplayed(true);
            achievement.setDisplayPosition(position);
        } else {
            achievement.setDisplayed(false);
            achievement.setDisplayPosition(-1);
        }
        api.updateAchievementDisplay(achievement.getType().name(), displayed, position, null);
    }

    public int getUnlockedButNotDisplayedCount() {
        int count = 0;
        for (Achievement a : achievements) {
            if (a.isUnlocked() && !a.isDisplayed()) count++;
        }
        return count;
    }

    public void resetAllAchievements() {
        for (Achievement a : achievements) {
            a.setUnlocked(false);
            a.setUnlockTime(0);
            a.setDisplayed(false);
            a.setDisplayPosition(-1);
        }
        earlyBirdCount = 0;
        nightOwlCount = 0;
    }

    public void clearAll() {
        resetAllAchievements();
        loaded = false;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }

    public void unlockAllAchievements() {
        for (Achievement achievement : achievements) {
            achievement.setUnlocked(true);
            achievement.setUnlockTime(System.currentTimeMillis());
        }
        earlyBirdCount = 20;
        nightOwlCount = 20;
    }

    public boolean isLoaded() { return loaded; }
}
