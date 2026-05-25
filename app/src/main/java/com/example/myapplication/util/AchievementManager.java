package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.Achievement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 成就管理器 - 检查成就条件并解锁
 */
public class AchievementManager {

    private static final String PREF_NAME = "achievement_prefs";
    private static final String KEY_ACHIEVEMENTS = "achievements";
    private static final String KEY_EARLY_BIRD_COUNT = "early_bird_count";
    private static final String KEY_NIGHT_OWL_COUNT = "night_owl_count";
    private static final String KEY_WEEKEND_TRAINING_WEEKS = "weekend_training_weeks";
    private static final String KEY_FIRST_WORKOUT_TIME = "first_workout_time";

    private static AchievementManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    private List<Achievement> achievements;

    public interface AchievementUnlockListener {
        void onAchievementUnlocked(Achievement achievement);
    }

    private AchievementUnlockListener unlockListener;

    private AchievementManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadAchievements();
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

    private void loadAchievements() {
        String json = prefs.getString(KEY_ACHIEVEMENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<Achievement>>(){}.getType();
            achievements = gson.fromJson(json, type);
        } else {
            achievements = new ArrayList<>();
            // Initialize all achievements as locked
            for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
                achievements.add(new Achievement(type));
            }
            saveAchievements();
        }
    }

    private void saveAchievements() {
        String json = gson.toJson(achievements);
        prefs.edit().putString(KEY_ACHIEVEMENTS, json).apply();
    }

    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements);
    }

    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked;
    }

    public List<Achievement> getDisplayedAchievements() {
        List<Achievement> displayed = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked() && achievement.isDisplayed()) {
                displayed.add(achievement);
            }
        }
        return displayed;
    }

    public Achievement getAchievement(Achievement.AchievementType type) {
        for (Achievement achievement : achievements) {
            if (achievement.getType() == type) {
                return achievement;
            }
        }
        return null;
    }

    private void unlockAchievement(Achievement.AchievementType type) {
        Achievement achievement = getAchievement(type);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.setUnlocked(true);
            achievement.setUnlockTime(System.currentTimeMillis());
            saveAchievements();

            if (unlockListener != null) {
                unlockListener.onAchievementUnlocked(achievement);
            }
        }
    }

    /**
     * 检查所有成就条件
     * 每次完成训练后调用
     */
    public void checkAllAchievements(Context context) {
        WorkoutRecordManager workoutManager = WorkoutRecordManager.getInstance(context);
        SessionManager sessionManager = SessionManager.getInstance(context);

        int totalWorkouts = workoutManager.getTotalWorkouts();
        float totalCalories = workoutManager.getTotalCalories();
        int consecutiveDays = workoutManager.getConsecutiveDays();

        // 训练类成就
        if (totalWorkouts >= 1) {
            unlockAchievement(Achievement.AchievementType.FIRST_WORKOUT);
        }
        if (consecutiveDays >= 7) {
            unlockAchievement(Achievement.AchievementType.STREAK_7_DAYS);
        }
        if (consecutiveDays >= 30) {
            unlockAchievement(Achievement.AchievementType.STREAK_30_DAYS);
        }
        if (totalWorkouts >= 100) {
            unlockAchievement(Achievement.AchievementType.WORKOUT_100);
        }
        if (totalWorkouts >= 500) {
            unlockAchievement(Achievement.AchievementType.WORKOUT_500);
        }

        // 卡路里类成就
        if (totalCalories >= 1000) {
            unlockAchievement(Achievement.AchievementType.BURN_1000_CAL);
        }
        if (totalCalories >= 5000) {
            unlockAchievement(Achievement.AchievementType.BURN_5000_CAL);
        }
        if (totalCalories >= 10000) {
            unlockAchievement(Achievement.AchievementType.BURN_10000_CAL);
        }

        // 体重类成就
        checkWeightAchievements(sessionManager);

        // 时间类成就
        checkTimeAchievements(context);

        // 连续打卡成就
        if (consecutiveDays >= 7) {
            unlockAchievement(Achievement.AchievementType.CHECKIN_7);
        }
        if (consecutiveDays >= 30) {
            unlockAchievement(Achievement.AchievementType.CHECKIN_30);
        }
    }

    private void checkWeightAchievements(SessionManager sessionManager) {
        float initialWeight = sessionManager.getInitialWeight();
        float currentWeight = sessionManager.getWeight();

        if (initialWeight > 0 && currentWeight > 0 && initialWeight != currentWeight) {
            unlockAchievement(Achievement.AchievementType.WEIGHT_CHANGE);
        }

        // 目标体重达成可以通过其他逻辑检查
    }

    private void checkTimeAchievements(Context context) {
        // 早起鸟检查
        int earlyBirdCount = prefs.getInt(KEY_EARLY_BIRD_COUNT, 0);
        if (earlyBirdCount >= 10) {
            unlockAchievement(Achievement.AchievementType.EARLY_BIRD);
        }

        // 夜猫子检查
        int nightOwlCount = prefs.getInt(KEY_NIGHT_OWL_COUNT, 0);
        if (nightOwlCount >= 10) {
            unlockAchievement(Achievement.AchievementType.NIGHT_OWL);
        }

        // 周末战士检查
        Set<String> weekendWeeks = prefs.getStringSet(KEY_WEEKEND_TRAINING_WEEKS, new HashSet<>());
        if (weekendWeeks.size() >= 4) {
            unlockAchievement(Achievement.AchievementType.WEEKEND_WARRIOR);
        }
    }

    /**
     * 记录训练完成 - 检查时间相关的成就
     */
    public void recordWorkoutCompletion(Context context) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        // 记录早起鸟（早上6点前）
        if (hour < 6) {
            int count = prefs.getInt(KEY_EARLY_BIRD_COUNT, 0) + 1;
            prefs.edit().putInt(KEY_EARLY_BIRD_COUNT, count).apply();
        }

        // 记录夜猫子（晚上9点后）
        if (hour >= 21) {
            int count = prefs.getInt(KEY_NIGHT_OWL_COUNT, 0) + 1;
            prefs.edit().putInt(KEY_NIGHT_OWL_COUNT, count).apply();
        }

        // 记录周末训练
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            // 获取当前周数作为标识
            int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
            int year = cal.get(Calendar.YEAR);
            String weekKey = year + "-" + weekOfYear;

            Set<String> weekendWeeks = new HashSet<>(prefs.getStringSet(KEY_WEEKEND_TRAINING_WEEKS, new HashSet<>()));
            weekendWeeks.add(weekKey);
            prefs.edit().putStringSet(KEY_WEEKEND_TRAINING_WEEKS, weekendWeeks).apply();
        }

        // 重新检查成就
        checkAllAchievements(context);
    }

    /**
     * 设置成就是否在主页展示
     */
    public void setAchievementDisplayed(Achievement achievement, boolean displayed, int position) {
        // 如果要展示，需要确保位置不冲突
        if (displayed) {
            // 先移除其他成就在这个位置的展示
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
        saveAchievements();
    }

    /**
     * 获取已解锁但未设置的成就数量
     */
    public int getUnlockedButNotDisplayedCount() {
        int count = 0;
        for (Achievement a : achievements) {
            if (a.isUnlocked() && !a.isDisplayed()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 重置所有成就（用于测试）
     */
    public void resetAllAchievements() {
        for (Achievement achievement : achievements) {
            achievement.setUnlocked(false);
            achievement.setUnlockTime(0);
            achievement.setDisplayed(false);
            achievement.setDisplayPosition(-1);
        }
        prefs.edit()
                .putInt(KEY_EARLY_BIRD_COUNT, 0)
                .putInt(KEY_NIGHT_OWL_COUNT, 0)
                .remove(KEY_WEEKEND_TRAINING_WEEKS)
                .remove(KEY_FIRST_WORKOUT_TIME)
                .apply();
        saveAchievements();
    }

    /**
     * 解锁所有成就（用于测试）
     */
    public void unlockAllAchievements() {
        for (Achievement achievement : achievements) {
            achievement.setUnlocked(true);
            achievement.setUnlockTime(System.currentTimeMillis());
        }
        prefs.edit()
                .putInt(KEY_EARLY_BIRD_COUNT, 20)
                .putInt(KEY_NIGHT_OWL_COUNT, 20)
                .apply();
        saveAchievements();
    }
}
