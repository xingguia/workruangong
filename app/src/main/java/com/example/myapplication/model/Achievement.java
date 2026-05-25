package com.example.myapplication.model;

import java.io.Serializable;

/**
 * 成就/徽章模型
 */
public class Achievement implements Serializable {

    public enum AchievementType {
        // 训练类
        FIRST_WORKOUT("初次训练", "完成第一次训练", "完成第一次训练即可获得", Category.TRAINING),
        STREAK_7_DAYS("坚持7天", "连续训练7天", "连续7天完成训练任务", Category.TRAINING),
        STREAK_30_DAYS("坚持30天", "连续训练30天", "连续30天完成训练任务", Category.TRAINING),
        WORKOUT_100("训练达人", "完成100次训练", "累计完成100次训练", Category.TRAINING),
        WORKOUT_500("训练大师", "完成500次训练", "累计完成500次训练", Category.TRAINING),

        // 卡路里类
        BURN_1000_CAL("燃烧1000卡", "累计燃烧1000卡", "累计消耗1000千卡", Category.CALORIES),
        BURN_5000_CAL("燃烧5000卡", "累计燃烧5000卡", "累计消耗5000千卡", Category.CALORIES),
        BURN_10000_CAL("燃脂高手", "累计燃烧10000卡", "累计消耗10000千卡", Category.CALORIES),

        // 体重类
        WEIGHT_CHANGE("突破体重", "体重有变化", "体重相比初始记录有变化", Category.WEIGHT),
        TARGET_WEIGHT("目标达成", "达到目标体重", "达到设定的目标体重", Category.WEIGHT),

        // 时间类
        EARLY_BIRD("早起鸟", "早起训练", "早上6点前完成训练10次", Category.TIME),
        NIGHT_OWL("夜猫子", "夜间训练", "晚上9点后完成训练10次", Category.TIME),
        WEEKEND_WARRIOR("周末战士", "周末训练", "连续4周在周末完成训练", Category.TIME),

        // 连续类
        CHECKIN_7("连续打卡7天", "连续打卡7天", "连续7天完成训练打卡", Category.STREAK),
        CHECKIN_30("连续打卡30天", "连续打卡30天", "连续30天完成训练打卡", Category.STREAK);

        public enum Category {
            TRAINING("训练"),
            CALORIES("卡路里"),
            WEIGHT("体重"),
            TIME("时间"),
            STREAK("连续");
            private String displayName;
            Category(String name) { this.displayName = name; }
            public String getDisplayName() { return displayName; }
        }

        private String displayName;
        private String shortDesc;
        private String fullDesc;
        private Category category;

        AchievementType(String displayName, String shortDesc, String fullDesc, Category category) {
            this.displayName = displayName;
            this.shortDesc = shortDesc;
            this.fullDesc = fullDesc;
            this.category = category;
        }

        public String getDisplayName() { return displayName; }
        public String getShortDesc() { return shortDesc; }
        public String getFullDesc() { return fullDesc; }
        public Category getCategory() { return category; }
    }

    private AchievementType type;
    private boolean unlocked;           // 是否已解锁
    private long unlockTime;            // 解锁时间
    private boolean displayed;          // 是否在主页展示
    private int displayPosition;       // 展示位置（0, 1, 2）

    public Achievement(AchievementType type) {
        this.type = type;
        this.unlocked = false;
        this.unlockTime = 0;
        this.displayed = false;
        this.displayPosition = -1;
    }

    public AchievementType getType() { return type; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public long getUnlockTime() { return unlockTime; }
    public void setUnlockTime(long unlockTime) { this.unlockTime = unlockTime; }
    public boolean isDisplayed() { return displayed; }
    public void setDisplayed(boolean displayed) { this.displayed = displayed; }
    public int getDisplayPosition() { return displayPosition; }
    public void setDisplayPosition(int displayPosition) { this.displayPosition = displayPosition; }

    public String getDisplayName() { return type.getDisplayName(); }
    public String getShortDesc() { return type.getShortDesc(); }
    public String getFullDesc() { return type.getFullDesc(); }
    public AchievementType.Category getCategory() { return type.getCategory(); }
}
