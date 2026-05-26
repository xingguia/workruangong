package com.example.myapplication.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 健身动作数据库
 * 包含六大肌群分类和对应的动作
 */
public class ExerciseDatabase {

    /**
     * 六大肌群分类
     */
    public enum MuscleGroup {
        CHEST("胸肌"),
        BACK("背部"),
        SHOULDERS("肩部"),
        ARMS("手臂"),
        LEGS("腿部"),
        CORE("核心");

        private final String displayName;
        MuscleGroup(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 子肌群分类
     */
    public enum SubMuscle {
        // 胸部
        UPPER_CHEST("上胸"),
        MIDDLE_CHEST("中胸"),
        LOWER_CHEST("下胸"),
        INNER_CHEST("胸内侧"),
        OUTER_CHEST("胸外侧"),

        // 背部
        LAT("背阔肌"),
        MIDDLE_BACK("中背部"),
        LOWER_BACK("下背/腰"),
        TRAPS("斜方肌"),
        RHOMBOID("菱形肌"),

        // 肩部
        FRONT_DELTS("三角肌前束"),
        SIDE_DELTS("三角肌中束"),
        REAR_DELTS("三角肌后束"),

        // 手臂
        BICEPS("肱二头肌"),
        TRICEPS("肱三头肌"),
        FOREARMS("前臂"),

        // 腿部
        QUADS("股四头肌"),
        HAMSTRINGS("腘绳肌"),
        GLUTES("臀部"),
        CALVES("小腿"),

        // 核心
        UPPER_ABS("上腹"),
        LOWER_ABS("下腹"),
        OBLIQUES("腹斜肌"),
        TRANSVERSE_ABS("腹横肌"),
        HIP_FLEXOR("髂腰肌");

        private final String displayName;
        SubMuscle(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 获取所有大肌群
     */
    public static List<MuscleGroup> getAllMuscleGroups() {
        return Arrays.asList(MuscleGroup.values());
    }

    /**
     * 获取某大肌群下的所有子肌群
     */
    public static List<SubMuscle> getSubMusclesForGroup(MuscleGroup group) {
        switch (group) {
            case CHEST:
                return Arrays.asList(
                    SubMuscle.UPPER_CHEST,
                    SubMuscle.MIDDLE_CHEST,
                    SubMuscle.LOWER_CHEST,
                    SubMuscle.INNER_CHEST,
                    SubMuscle.OUTER_CHEST
                );
            case BACK:
                return Arrays.asList(
                    SubMuscle.LAT,
                    SubMuscle.MIDDLE_BACK,
                    SubMuscle.LOWER_BACK,
                    SubMuscle.TRAPS,
                    SubMuscle.RHOMBOID
                );
            case SHOULDERS:
                return Arrays.asList(
                    SubMuscle.FRONT_DELTS,
                    SubMuscle.SIDE_DELTS,
                    SubMuscle.REAR_DELTS
                );
            case ARMS:
                return Arrays.asList(
                    SubMuscle.BICEPS,
                    SubMuscle.TRICEPS,
                    SubMuscle.FOREARMS
                );
            case LEGS:
                return Arrays.asList(
                    SubMuscle.QUADS,
                    SubMuscle.HAMSTRINGS,
                    SubMuscle.GLUTES,
                    SubMuscle.CALVES
                );
            case CORE:
                return Arrays.asList(
                    SubMuscle.UPPER_ABS,
                    SubMuscle.LOWER_ABS,
                    SubMuscle.OBLIQUES,
                    SubMuscle.TRANSVERSE_ABS,
                    SubMuscle.HIP_FLEXOR
                );
            default:
                return Arrays.asList();
        }
    }

    /**
     * 获取某大肌群下的所有动作
     */
    public static List<Exercise> getExercisesForGroup(MuscleGroup group) {
        switch (group) {
            case CHEST:
                return getChestExercises();
            case BACK:
                return getBackExercises();
            case SHOULDERS:
                return getShoulderExercises();
            case ARMS:
                return getArmExercises();
            case LEGS:
                return getLegExercises();
            case CORE:
                return getCoreExercises();
            default:
                return Arrays.asList();
        }
    }

    /**
     * 获取某子肌群下的所有动作
     */
    public static List<Exercise> getExercisesForSubMuscle(SubMuscle subMuscle) {
        List<Exercise> allExercises = getAllExercises();
        List<Exercise> filtered = new java.util.ArrayList<>();
        for (Exercise ex : allExercises) {
            if (ex.subMuscles.contains(subMuscle)) {
                filtered.add(ex);
            }
        }
        return filtered;
    }

    /**
     * 获取所有动作
     */
    public static List<Exercise> getAllExercises() {
        List<Exercise> all = new java.util.ArrayList<>();
        all.addAll(getChestExercises());
        all.addAll(getBackExercises());
        all.addAll(getShoulderExercises());
        all.addAll(getArmExercises());
        all.addAll(getLegExercises());
        all.addAll(getCoreExercises());
        return all;
    }

    /**
     * 胸肌动作库
     */
    private static List<Exercise> getChestExercises() {
        return Arrays.asList(
            // 上胸
            new Exercise("上斜哑铃卧推", Arrays.asList(SubMuscle.UPPER_CHEST, SubMuscle.MIDDLE_CHEST), true, 8f),
            new Exercise("上斜杠铃卧推", Arrays.asList(SubMuscle.UPPER_CHEST, SubMuscle.MIDDLE_CHEST), true, 10f),
            new Exercise("上斜卧推（机器）", Arrays.asList(SubMuscle.UPPER_CHEST, SubMuscle.MIDDLE_CHEST), true, 9f),
            new Exercise("上斜飞鸟", Arrays.asList(SubMuscle.UPPER_CHEST), false, 6f),

            // 中胸
            new Exercise("平板哑铃卧推", Arrays.asList(SubMuscle.MIDDLE_CHEST, SubMuscle.UPPER_CHEST), true, 9f),
            new Exercise("平板杠铃卧推", Arrays.asList(SubMuscle.MIDDLE_CHEST), true, 10f),
            new Exercise("平板卧推（机器）", Arrays.asList(SubMuscle.MIDDLE_CHEST), true, 8f),
            new Exercise("俯卧撑", Arrays.asList(SubMuscle.MIDDLE_CHEST, SubMuscle.UPPER_CHEST), false, 5f),

            // 下胸
            new Exercise("下斜哑铃卧推", Arrays.asList(SubMuscle.LOWER_CHEST), true, 8f),
            new Exercise("下斜杠铃卧推", Arrays.asList(SubMuscle.LOWER_CHEST), true, 9f),
            new Exercise("双杠臂屈伸", Arrays.asList(SubMuscle.LOWER_CHEST, SubMuscle.TRICEPS), false, 6f),

            // 胸内侧/外侧
            new Exercise("蝴蝶机夹胸", Arrays.asList(SubMuscle.INNER_CHEST), true, 5f),
            new Exercise("绳索夹胸", Arrays.asList(SubMuscle.INNER_CHEST, SubMuscle.OUTER_CHEST), true, 4f),
            new Exercise("哑铃飞鸟", Arrays.asList(SubMuscle.INNER_CHEST, SubMuscle.OUTER_CHEST), false, 6f)
        );
    }

    /**
     * 背部动作库
     */
    private static List<Exercise> getBackExercises() {
        return Arrays.asList(
            // 背阔肌
            new Exercise("引体向上", Arrays.asList(SubMuscle.LAT, SubMuscle.MIDDLE_BACK), false, 8f),
            new Exercise("高位下拉", Arrays.asList(SubMuscle.LAT, SubMuscle.MIDDLE_BACK), true, 7f),
            new Exercise("直臂下拉", Arrays.asList(SubMuscle.LAT), true, 5f),
            new Exercise("单臂哑铃划船", Arrays.asList(SubMuscle.LAT, SubMuscle.MIDDLE_BACK), true, 7f),

            // 中背部
            new Exercise("杠铃划船", Arrays.asList(SubMuscle.MIDDLE_BACK, SubMuscle.LAT), true, 8f),
            new Exercise("坐姿划船", Arrays.asList(SubMuscle.MIDDLE_BACK, SubMuscle.LAT), true, 7f),
            new Exercise("T杠划船", Arrays.asList(SubMuscle.MIDDLE_BACK), true, 8f),

            // 下背
            new Exercise("硬拉", Arrays.asList(SubMuscle.LOWER_BACK, SubMuscle.GLUTES, SubMuscle.HAMSTRINGS), true, 12f),
            new Exercise("山羊挺身", Arrays.asList(SubMuscle.LOWER_BACK), false, 6f),
            new Exercise("早安式", Arrays.asList(SubMuscle.LOWER_BACK, SubMuscle.GLUTES), true, 7f),

            // 斜方肌/菱形肌
            new Exercise("耸肩", Arrays.asList(SubMuscle.TRAPS), true, 8f),
            new Exercise("面拉", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 5f),
            new Exercise("反向蝴蝶机", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 4f)
        );
    }

    /**
     * 肩部动作库
     */
    private static List<Exercise> getShoulderExercises() {
        return Arrays.asList(
            // 前束
            new Exercise("哑铃推举", Arrays.asList(SubMuscle.FRONT_DELTS, SubMuscle.SIDE_DELTS), true, 8f),
            new Exercise("杠铃推举", Arrays.asList(SubMuscle.FRONT_DELTS, SubMuscle.SIDE_DELTS), true, 9f),
            new Exercise("阿诺德推举", Arrays.asList(SubMuscle.FRONT_DELTS, SubMuscle.SIDE_DELTS), true, 7f),
            new Exercise("前平举", Arrays.asList(SubMuscle.FRONT_DELTS), true, 5f),

            // 中束
            new Exercise("侧平举", Arrays.asList(SubMuscle.SIDE_DELTS), true, 5f),
            new Exercise("机器侧平举", Arrays.asList(SubMuscle.SIDE_DELTS), true, 5f),
            new Exercise("绳索侧平举", Arrays.asList(SubMuscle.SIDE_DELTS), true, 5f),

            // 后束
            new Exercise("俯身侧平举", Arrays.asList(SubMuscle.REAR_DELTS), true, 4f),
            new Exercise("俯身飞鸟", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 4f),
            new Exercise("面拉", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 5f)
        );
    }

    /**
     * 手臂动作库
     */
    private static List<Exercise> getArmExercises() {
        return Arrays.asList(
            // 二头肌
            new Exercise("杠铃弯举", Arrays.asList(SubMuscle.BICEPS), true, 6f),
            new Exercise("哑铃弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f),
            new Exercise("锤式弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f),
            new Exercise("集中弯举", Arrays.asList(SubMuscle.BICEPS), true, 4f),
            new Exercise("牧师凳弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f),
            new Exercise("绳索弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f),

            // 三头肌
            new Exercise("窄距卧推", Arrays.asList(SubMuscle.TRICEPS), true, 8f),
            new Exercise("绳索下压", Arrays.asList(SubMuscle.TRICEPS), true, 5f),
            new Exercise("过头臂屈伸", Arrays.asList(SubMuscle.TRICEPS), true, 5f),
            new Exercise("哑铃臂屈伸", Arrays.asList(SubMuscle.TRICEPS), true, 5f),
            new Exercise("双杠臂屈伸", Arrays.asList(SubMuscle.TRICEPS, SubMuscle.LOWER_CHEST), false, 6f),
            new Exercise("凳上臂屈伸", Arrays.asList(SubMuscle.TRICEPS), false, 5f),

            // 前臂
            new Exercise("腕弯举", Arrays.asList(SubMuscle.FOREARMS), true, 4f),
            new Exercise("反腕弯举", Arrays.asList(SubMuscle.FOREARMS), true, 4f),
            new Exercise("握力器练习", Arrays.asList(SubMuscle.FOREARMS), false, 3f)
        );
    }

    /**
     * 腿部动作库
     */
    private static List<Exercise> getLegExercises() {
        return Arrays.asList(
            // 股四头肌
            new Exercise("深蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), true, 10f),
            new Exercise("腿举", Arrays.asList(SubMuscle.QUADS), true, 10f),
            new Exercise("哑铃深蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), true, 8f),
            new Exercise("腿屈伸", Arrays.asList(SubMuscle.QUADS), true, 6f),
            new Exercise("箭步蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), false, 6f),
            new Exercise("保加利亚深蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), true, 7f),

            // 腘绳肌
            new Exercise("腿弯举", Arrays.asList(SubMuscle.HAMSTRINGS), true, 6f),
            new Exercise("罗马尼亚硬拉", Arrays.asList(SubMuscle.HAMSTRINGS, SubMuscle.GLUTES), true, 8f),
            new Exercise("俯卧腿弯举", Arrays.asList(SubMuscle.HAMSTRINGS), true, 6f),
            new Exercise("北欧腿弯举", Arrays.asList(SubMuscle.HAMSTRINGS), false, 5f),

            // 臀部
            new Exercise("臀桥", Arrays.asList(SubMuscle.GLUTES), false, 6f),
            new Exercise("臀推", Arrays.asList(SubMuscle.GLUTES), true, 8f),
            new Exercise("侧卧抬腿", Arrays.asList(SubMuscle.GLUTES), false, 4f),
            new Exercise("螃蟹步", Arrays.asList(SubMuscle.GLUTES), true, 4f),
            new Exercise("驴式后踢", Arrays.asList(SubMuscle.GLUTES), false, 5f),

            // 小腿
            new Exercise("提踵", Arrays.asList(SubMuscle.CALVES), true, 5f),
            new Exercise("坐姿提踵", Arrays.asList(SubMuscle.CALVES), true, 5f),
            new Exercise("骑驴提踵", Arrays.asList(SubMuscle.CALVES), true, 6f)
        );
    }

    /**
     * 核心动作库
     */
    private static List<Exercise> getCoreExercises() {
        return Arrays.asList(
            // 上腹
            new Exercise("卷腹", Arrays.asList(SubMuscle.UPPER_ABS), false, 4f),
            new Exercise("卷腹（器械）", Arrays.asList(SubMuscle.UPPER_ABS), true, 5f),
            new Exercise("仰卧起坐", Arrays.asList(SubMuscle.UPPER_ABS), false, 4f),

            // 下腹
            new Exercise("举腿", Arrays.asList(SubMuscle.LOWER_ABS), false, 4f),
            new Exercise("悬垂举腿", Arrays.asList(SubMuscle.LOWER_ABS, SubMuscle.HIP_FLEXOR), false, 5f),
            new Exercise("反向卷腹", Arrays.asList(SubMuscle.LOWER_ABS), false, 4f),

            // 腹斜肌
            new Exercise("俄罗斯转体", Arrays.asList(SubMuscle.OBLIQUES), false, 4f),
            new Exercise("侧卷腹", Arrays.asList(SubMuscle.OBLIQUES), false, 4f),
            new Exercise("伐木", Arrays.asList(SubMuscle.OBLIQUES), true, 5f),

            // 腹横肌/深层
            new Exercise("平板支撑", Arrays.asList(SubMuscle.TRANSVERSE_ABS, SubMuscle.OBLIQUES), false, 5f),
            new Exercise("死虫式", Arrays.asList(SubMuscle.TRANSVERSE_ABS), false, 4f),
            new Exercise("鸟狗式", Arrays.asList(SubMuscle.TRANSVERSE_ABS), false, 4f),

            // 髂腰肌
            new Exercise("登山跑", Arrays.asList(SubMuscle.HIP_FLEXOR, SubMuscle.LOWER_ABS), false, 6f),
            new Exercise("悬垂举腿（垂悬）", Arrays.asList(SubMuscle.HIP_FLEXOR, SubMuscle.LOWER_ABS), false, 5f)
        );
    }

    /**
     * 动作类
     */
    public static class Exercise {
        public final String name;
        public final List<SubMuscle> subMuscles;
        public final boolean needsEquipment;
        public final float baseCaloriesPerRep; // 每个动作的基础卡路里系数

        public Exercise(String name, List<SubMuscle> subMuscles, boolean needsEquipment, float baseCaloriesPerRep) {
            this.name = name;
            this.subMuscles = subMuscles;
            this.needsEquipment = needsEquipment;
            this.baseCaloriesPerRep = baseCaloriesPerRep;
        }

        public MuscleGroup getPrimaryMuscleGroup() {
            if (subMuscles.isEmpty()) return null;
            SubMuscle primary = subMuscles.get(0);
            if (primary == SubMuscle.UPPER_CHEST || primary == SubMuscle.MIDDLE_CHEST ||
                primary == SubMuscle.LOWER_CHEST || primary == SubMuscle.INNER_CHEST ||
                primary == SubMuscle.OUTER_CHEST) {
                return MuscleGroup.CHEST;
            } else if (primary == SubMuscle.LAT || primary == SubMuscle.MIDDLE_BACK ||
                       primary == SubMuscle.LOWER_BACK || primary == SubMuscle.TRAPS ||
                       primary == SubMuscle.RHOMBOID) {
                return MuscleGroup.BACK;
            } else if (primary == SubMuscle.FRONT_DELTS || primary == SubMuscle.SIDE_DELTS ||
                       primary == SubMuscle.REAR_DELTS) {
                return MuscleGroup.SHOULDERS;
            } else if (primary == SubMuscle.BICEPS || primary == SubMuscle.TRICEPS ||
                       primary == SubMuscle.FOREARMS) {
                return MuscleGroup.ARMS;
            } else if (primary == SubMuscle.QUADS || primary == SubMuscle.HAMSTRINGS ||
                       primary == SubMuscle.GLUTES || primary == SubMuscle.CALVES) {
                return MuscleGroup.LEGS;
            } else {
                return MuscleGroup.CORE;
            }
        }
    }
}
