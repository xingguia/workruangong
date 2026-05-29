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
            new Exercise("上斜哑铃卧推", Arrays.asList(SubMuscle.UPPER_CHEST, SubMuscle.MIDDLE_CHEST), true, 8f, "exercise_incline_dumbbell_press"),
            new Exercise("上斜杠铃卧推", Arrays.asList(SubMuscle.UPPER_CHEST, SubMuscle.MIDDLE_CHEST), true, 10f, "exercise_incline_dumbbell_press"),
            new Exercise("上斜卧推（机器）", Arrays.asList(SubMuscle.UPPER_CHEST, SubMuscle.MIDDLE_CHEST), true, 9f, "exercise_incline_dumbbell_press"),
            new Exercise("上斜飞鸟", Arrays.asList(SubMuscle.UPPER_CHEST), false, 6f, "exercise_dumbbell_fly"),

            // 中胸
            new Exercise("平板哑铃卧推", Arrays.asList(SubMuscle.MIDDLE_CHEST, SubMuscle.UPPER_CHEST), true, 9f, "exercise_bench_press"),
            new Exercise("平板杠铃卧推", Arrays.asList(SubMuscle.MIDDLE_CHEST), true, 10f, "exercise_bench_press"),
            new Exercise("平板卧推（机器）", Arrays.asList(SubMuscle.MIDDLE_CHEST), true, 8f, "exercise_bench_press"),
            new Exercise("俯卧撑", Arrays.asList(SubMuscle.MIDDLE_CHEST, SubMuscle.UPPER_CHEST), false, 5f, "exercise_pushup"),

            // 下胸
            new Exercise("下斜哑铃卧推", Arrays.asList(SubMuscle.LOWER_CHEST), true, 8f, "exercise_decline_dumbbell_press"),
            new Exercise("下斜杠铃卧推", Arrays.asList(SubMuscle.LOWER_CHEST), true, 9f, "exercise_decline_dumbbell_press"),
            new Exercise("双杠臂屈伸", Arrays.asList(SubMuscle.LOWER_CHEST, SubMuscle.TRICEPS), false, 6f, "exercise_pushup"),

            // 胸内侧/外侧
            new Exercise("蝴蝶机夹胸", Arrays.asList(SubMuscle.INNER_CHEST), true, 5f, "exercise_pec_deck"),
            new Exercise("绳索夹胸", Arrays.asList(SubMuscle.INNER_CHEST, SubMuscle.OUTER_CHEST), true, 4f, "exercise_pec_deck"),
            new Exercise("哑铃飞鸟", Arrays.asList(SubMuscle.INNER_CHEST, SubMuscle.OUTER_CHEST), false, 6f, "exercise_dumbbell_fly")
        );
    }

    /**
     * 背部动作库
     */
    private static List<Exercise> getBackExercises() {
        return Arrays.asList(
            // 背阔肌
            new Exercise("引体向上", Arrays.asList(SubMuscle.LAT, SubMuscle.MIDDLE_BACK), false, 8f, "exercise_pullup"),
            new Exercise("高位下拉", Arrays.asList(SubMuscle.LAT, SubMuscle.MIDDLE_BACK), true, 7f, "exercise_lat_pulldown"),
            new Exercise("直臂下拉", Arrays.asList(SubMuscle.LAT), true, 5f, "exercise_straight_arm_pulldown"),
            new Exercise("单臂哑铃划船", Arrays.asList(SubMuscle.LAT, SubMuscle.MIDDLE_BACK), true, 7f, "exercise_single_arm_row"),

            // 中背部
            new Exercise("杠铃划船", Arrays.asList(SubMuscle.MIDDLE_BACK, SubMuscle.LAT), true, 8f, "exercise_barbell_row"),
            new Exercise("坐姿划船", Arrays.asList(SubMuscle.MIDDLE_BACK, SubMuscle.LAT), true, 7f, "exercise_barbell_row"),
            new Exercise("T杠划船", Arrays.asList(SubMuscle.MIDDLE_BACK), true, 8f, "exercise_barbell_row"),

            // 下背
            new Exercise("硬拉", Arrays.asList(SubMuscle.LOWER_BACK, SubMuscle.GLUTES, SubMuscle.HAMSTRINGS), true, 12f, "exercise_deadlift"),
            new Exercise("山羊挺身", Arrays.asList(SubMuscle.LOWER_BACK), false, 6f, "exercise_romanian_deadlift"),
            new Exercise("早安式", Arrays.asList(SubMuscle.LOWER_BACK, SubMuscle.GLUTES), true, 7f, "exercise_deadlift"),

            // 斜方肌/菱形肌
            new Exercise("耸肩", Arrays.asList(SubMuscle.TRAPS), true, 8f, "exercise_shrug"),
            new Exercise("面拉", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 5f, "exercise_face_pull"),
            new Exercise("反向蝴蝶机", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 4f, "exercise_face_pull")
        );
    }

    /**
     * 肩部动作库
     */
    private static List<Exercise> getShoulderExercises() {
        return Arrays.asList(
            // 前束
            new Exercise("哑铃推举", Arrays.asList(SubMuscle.FRONT_DELTS, SubMuscle.SIDE_DELTS), true, 8f, "exercise_dumbbell_shoulder_press"),
            new Exercise("杠铃推举", Arrays.asList(SubMuscle.FRONT_DELTS, SubMuscle.SIDE_DELTS), true, 9f, "exercise_dumbbell_shoulder_press"),
            new Exercise("阿诺德推举", Arrays.asList(SubMuscle.FRONT_DELTS, SubMuscle.SIDE_DELTS), true, 7f, "exercise_dumbbell_shoulder_press"),
            new Exercise("前平举", Arrays.asList(SubMuscle.FRONT_DELTS), true, 5f, "exercise_front_raise"),

            // 中束
            new Exercise("侧平举", Arrays.asList(SubMuscle.SIDE_DELTS), true, 5f, "exercise_lateral_raise"),
            new Exercise("机器侧平举", Arrays.asList(SubMuscle.SIDE_DELTS), true, 5f, "exercise_lateral_raise"),
            new Exercise("绳索侧平举", Arrays.asList(SubMuscle.SIDE_DELTS), true, 5f, "exercise_lateral_raise"),

            // 后束
            new Exercise("俯身侧平举", Arrays.asList(SubMuscle.REAR_DELTS), true, 4f, "exercise_bent_over_lateral_raise"),
            new Exercise("俯身飞鸟", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 4f, "exercise_bent_over_lateral_raise"),
            new Exercise("面拉", Arrays.asList(SubMuscle.REAR_DELTS, SubMuscle.RHOMBOID), true, 5f, "exercise_face_pull")
        );
    }

    /**
     * 手臂动作库
     */
    private static List<Exercise> getArmExercises() {
        return Arrays.asList(
            // 二头肌
            new Exercise("杠铃弯举", Arrays.asList(SubMuscle.BICEPS), true, 6f, "exercise_barbell_curl"),
            new Exercise("哑铃弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f, "exercise_dumbbell_curl"),
            new Exercise("锤式弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f, "exercise_hammer_curl"),
            new Exercise("集中弯举", Arrays.asList(SubMuscle.BICEPS), true, 4f, "exercise_dumbbell_curl"),
            new Exercise("牧师凳弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f, "exercise_barbell_curl"),
            new Exercise("绳索弯举", Arrays.asList(SubMuscle.BICEPS), true, 5f, "exercise_dumbbell_curl"),

            // 三头肌
            new Exercise("窄距卧推", Arrays.asList(SubMuscle.TRICEPS), true, 8f, "exercise_close_grip_bench"),
            new Exercise("绳索下压", Arrays.asList(SubMuscle.TRICEPS), true, 5f, "exercise_tricep_pushdown"),
            new Exercise("过头臂屈伸", Arrays.asList(SubMuscle.TRICEPS), true, 5f, "exercise_overhead_tricep"),
            new Exercise("哑铃臂屈伸", Arrays.asList(SubMuscle.TRICEPS), true, 5f, "exercise_overhead_tricep"),
            new Exercise("双杠臂屈伸", Arrays.asList(SubMuscle.TRICEPS, SubMuscle.LOWER_CHEST), false, 6f, "exercise_pushup"),
            new Exercise("凳上臂屈伸", Arrays.asList(SubMuscle.TRICEPS), false, 5f, "exercise_overhead_tricep"),

            // 前臂
            new Exercise("腕弯举", Arrays.asList(SubMuscle.FOREARMS), true, 4f, "exercise_dumbbell_curl"),
            new Exercise("反腕弯举", Arrays.asList(SubMuscle.FOREARMS), true, 4f, "exercise_dumbbell_curl"),
            new Exercise("握力器练习", Arrays.asList(SubMuscle.FOREARMS), false, 3f, "exercise_dumbbell_curl")
        );
    }

    /**
     * 腿部动作库
     */
    private static List<Exercise> getLegExercises() {
        return Arrays.asList(
            // 股四头肌
            new Exercise("深蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), true, 10f, "exercise_squat"),
            new Exercise("腿举", Arrays.asList(SubMuscle.QUADS), true, 10f, "exercise_leg_press"),
            new Exercise("哑铃深蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), true, 8f, "exercise_squat"),
            new Exercise("腿屈伸", Arrays.asList(SubMuscle.QUADS), true, 6f, "exercise_leg_curl"),
            new Exercise("箭步蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), false, 6f, "exercise_bulgarian_split_squat"),
            new Exercise("保加利亚深蹲", Arrays.asList(SubMuscle.QUADS, SubMuscle.GLUTES), true, 7f, "exercise_bulgarian_split_squat"),

            // 腘绳肌
            new Exercise("腿弯举", Arrays.asList(SubMuscle.HAMSTRINGS), true, 6f, "exercise_leg_curl"),
            new Exercise("罗马尼亚硬拉", Arrays.asList(SubMuscle.HAMSTRINGS, SubMuscle.GLUTES), true, 8f, "exercise_romanian_deadlift"),
            new Exercise("俯卧腿弯举", Arrays.asList(SubMuscle.HAMSTRINGS), true, 6f, "exercise_leg_curl"),
            new Exercise("北欧腿弯举", Arrays.asList(SubMuscle.HAMSTRINGS), false, 5f, "exercise_leg_curl"),

            // 臀部
            new Exercise("臀桥", Arrays.asList(SubMuscle.GLUTES), false, 6f, "exercise_hip_bridge"),
            new Exercise("臀推", Arrays.asList(SubMuscle.GLUTES), true, 8f, "exercise_hip_bridge"),
            new Exercise("侧卧抬腿", Arrays.asList(SubMuscle.GLUTES), false, 4f, "exercise_hip_bridge"),
            new Exercise("螃蟹步", Arrays.asList(SubMuscle.GLUTES), true, 4f, "exercise_hip_bridge"),
            new Exercise("驴式后踢", Arrays.asList(SubMuscle.GLUTES), false, 5f, "exercise_hip_bridge"),

            // 小腿
            new Exercise("提踵", Arrays.asList(SubMuscle.CALVES), true, 5f, "exercise_calf_raise"),
            new Exercise("坐姿提踵", Arrays.asList(SubMuscle.CALVES), true, 5f, "exercise_calf_raise"),
            new Exercise("骑驴提踵", Arrays.asList(SubMuscle.CALVES), true, 6f, "exercise_calf_raise")
        );
    }

    /**
     * 核心动作库
     */
    private static List<Exercise> getCoreExercises() {
        return Arrays.asList(
            // 上腹
            new Exercise("卷腹", Arrays.asList(SubMuscle.UPPER_ABS), false, 4f, "exercise_crunch"),
            new Exercise("卷腹（器械）", Arrays.asList(SubMuscle.UPPER_ABS), true, 5f, "exercise_crunch"),
            new Exercise("仰卧起坐", Arrays.asList(SubMuscle.UPPER_ABS), false, 4f, "exercise_crunch"),

            // 下腹
            new Exercise("举腿", Arrays.asList(SubMuscle.LOWER_ABS), false, 4f, "exercise_leg_raise"),
            new Exercise("悬垂举腿", Arrays.asList(SubMuscle.LOWER_ABS, SubMuscle.HIP_FLEXOR), false, 5f, "exercise_leg_raise"),
            new Exercise("反向卷腹", Arrays.asList(SubMuscle.LOWER_ABS), false, 4f, "exercise_leg_raise"),

            // 腹斜肌
            new Exercise("俄罗斯转体", Arrays.asList(SubMuscle.OBLIQUES), false, 4f, "exercise_russian_twist"),
            new Exercise("侧卷腹", Arrays.asList(SubMuscle.OBLIQUES), false, 4f, "exercise_crunch"),
            new Exercise("伐木", Arrays.asList(SubMuscle.OBLIQUES), true, 5f, "exercise_russian_twist"),

            // 腹横肌/深层
            new Exercise("平板支撑", Arrays.asList(SubMuscle.TRANSVERSE_ABS, SubMuscle.OBLIQUES), false, 5f, "exercise_plank"),
            new Exercise("死虫式", Arrays.asList(SubMuscle.TRANSVERSE_ABS), false, 4f, "exercise_dead_bug"),
            new Exercise("鸟狗式", Arrays.asList(SubMuscle.TRANSVERSE_ABS), false, 4f, "exercise_dead_bug"),

            // 髂腰肌
            new Exercise("登山跑", Arrays.asList(SubMuscle.HIP_FLEXOR, SubMuscle.LOWER_ABS), false, 6f, "exercise_mountain_climber"),
            new Exercise("悬垂举腿（垂悬）", Arrays.asList(SubMuscle.HIP_FLEXOR, SubMuscle.LOWER_ABS), false, 5f, "exercise_leg_raise")
        );
    }

    /**
     * 获取动作对应的图片资源名
     */
    public static String getImageResourceName(String exerciseName) {
        Map<String, String> imageMap = new HashMap<>();
        // 胸部
        imageMap.put("上斜哑铃卧推", "exercise_incline_dumbbell_press");
        imageMap.put("平板杠铃卧推", "exercise_bench_press");
        imageMap.put("平板哑铃卧推", "exercise_bench_press");
        imageMap.put("下斜哑铃卧推", "exercise_decline_dumbbell_press");
        imageMap.put("蝴蝶机夹胸", "exercise_pec_deck");
        imageMap.put("哑铃飞鸟", "exercise_dumbbell_fly");
        imageMap.put("俯卧撑", "exercise_pushup");

        // 背部
        imageMap.put("引体向上", "exercise_pullup");
        imageMap.put("高位下拉", "exercise_lat_pulldown");
        imageMap.put("直臂下拉", "exercise_straight_arm_pulldown");
        imageMap.put("单臂哑铃划船", "exercise_single_arm_row");
        imageMap.put("杠铃划船", "exercise_barbell_row");
        imageMap.put("硬拉", "exercise_deadlift");

        // 肩部
        imageMap.put("哑铃推举", "exercise_dumbbell_shoulder_press");
        imageMap.put("杠铃推举", "exercise_dumbbell_shoulder_press");
        imageMap.put("侧平举", "exercise_lateral_raise");
        imageMap.put("俯身侧平举", "exercise_bent_over_lateral_raise");
        imageMap.put("前平举", "exercise_front_raise");
        imageMap.put("面拉", "exercise_face_pull");

        // 手臂
        imageMap.put("杠铃弯举", "exercise_barbell_curl");
        imageMap.put("哑铃弯举", "exercise_dumbbell_curl");
        imageMap.put("锤式弯举", "exercise_hammer_curl");
        imageMap.put("绳索下压", "exercise_tricep_pushdown");
        imageMap.put("过头臂屈伸", "exercise_overhead_tricep");
        imageMap.put("窄距卧推", "exercise_close_grip_bench");

        // 腿部
        imageMap.put("深蹲", "exercise_squat");
        imageMap.put("腿举", "exercise_leg_press");
        imageMap.put("腿弯举", "exercise_leg_curl");
        imageMap.put("罗马尼亚硬拉", "exercise_romanian_deadlift");
        imageMap.put("臀桥", "exercise_hip_bridge");
        imageMap.put("保加利亚深蹲", "exercise_bulgarian_split_squat");
        imageMap.put("提踵", "exercise_calf_raise");

        // 核心
        imageMap.put("卷腹", "exercise_crunch");
        imageMap.put("平板支撑", "exercise_plank");
        imageMap.put("俄罗斯转体", "exercise_russian_twist");
        imageMap.put("举腿", "exercise_leg_raise");
        imageMap.put("登山跑", "exercise_mountain_climber");
        imageMap.put("死虫式", "exercise_dead_bug");

        return imageMap.get(exerciseName);
    }

    /**
     * 动作类
     */
    public static class Exercise {
        public final String name;
        public final List<SubMuscle> subMuscles;
        public final boolean needsEquipment;
        public final float baseCaloriesPerRep;
        public final String imageResName; // 图片资源名称，如 "ic_exercise_bench_press"

        public Exercise(String name, List<SubMuscle> subMuscles, boolean needsEquipment, float baseCaloriesPerRep) {
            this(name, subMuscles, needsEquipment, baseCaloriesPerRep, null);
        }

        public Exercise(String name, List<SubMuscle> subMuscles, boolean needsEquipment, float baseCaloriesPerRep, String imageResName) {
            this.name = name;
            this.subMuscles = subMuscles;
            this.needsEquipment = needsEquipment;
            this.baseCaloriesPerRep = baseCaloriesPerRep;
            this.imageResName = imageResName;
        }

        public String getInstructions() {
            StringBuilder sb = new StringBuilder();
            sb.append("【动作要点】\n");

            switch (name) {
                case "平板杠铃卧推":
                    sb.append("1. 平躺在卧推凳上，双脚踩实地面\n");
                    sb.append("2. 双手握杠，握距略宽于肩\n");
                    sb.append("3. 将杠铃从架上取下，移至胸部正上方\n");
                    sb.append("4. 吸气，控制下放至胸部中段\n");
                    sb.append("5. 呼气，发力将杠铃推起至手臂伸直\n");
                    sb.append("6. 保持核心稳定，避免过度弓背");
                    break;
                case "深蹲":
                    sb.append("1. 双脚与肩同宽或略宽，脚尖微微外展\n");
                    sb.append("2. 挺胸收紧核心，目视前方\n");
                    sb.append("3. 臀部向后坐下蹲，重心放在脚后跟\n");
                    sb.append("4. 下蹲至大腿与地面平行或更低\n");
                    sb.append("5. 膝关节方向与脚尖方向一致\n");
                    sb.append("6. 蹬地起身，保持膝盖微屈");
                    break;
                case "引体向上":
                    sb.append("1. 双手正握单杠，握距略宽于肩\n");
                    sb.append("2. 身体悬挂，手臂完全伸直\n");
                    sb.append("3. 肩胛骨下沉收紧\n");
                    sb.append("4. 发力将身体拉起至下巴过杠\n");
                    sb.append("5. 控制下放速度，手臂不完全伸直\n");
                    sb.append("6. 避免身体前后摆动借力");
                    break;
                case "哑铃弯举":
                    sb.append("1. 站立，双手持哑铃，手臂自然下垂\n");
                    sb.append("2. 手心朝前，肘关节贴近身体两侧\n");
                    sb.append("3. 肱二头肌发力，将哑铃弯举至肩部高度\n");
                    sb.append("4. 在顶峰收缩1-2秒，感受肌肉挤压\n");
                    sb.append("5. 控制下放速度，充分伸展\n");
                    sb.append("6. 避免借助身体晃动发力");
                    break;
                case "平板支撑":
                    sb.append("1. 俯卧，用前臂和脚尖支撑身体\n");
                    sb.append("2. 肘关节在肩关节正下方\n");
                    sb.append("3. 收紧腹部、臀部，保持身体呈一条直线\n");
                    sb.append("4. 正常呼吸，保持60秒或更久\n");
                    sb.append("5. 避免塌腰或撅臀\n");
                    sb.append("6. 目视地面，颈部保持自然位置");
                    break;
                case "卷腹":
                    sb.append("1. 平躺在垫子上，膝盖弯曲90度\n");
                    sb.append("2. 双手放在耳侧或交叉置于胸前\n");
                    sb.append("3. 呼气，利用腹肌力量将上背部抬离地面\n");
                    sb.append("4. 上升至肩胛骨离开地面即可\n");
                    sb.append("5. 吸气，有控制地缓缓躺回\n");
                    sb.append("6. 不要完全躺下，保持腹部持续发力");
                    break;
                default:
                    sb.append("1. 做好准备姿势，核心收紧\n");
                    sb.append("2. 控制动作速度，注意肌肉发力感\n");
                    sb.append("3. 保持呼吸均匀\n");
                    sb.append("4. 选择合适的重量\n");
                    sb.append("5. 如有不适立即停止");
                    break;
            }

            return sb.toString();
        }

        public String getTips() {
            switch (name) {
                case "平板杠铃卧推":
                    return "建议配图：杠铃卧推标准姿势\n所需器材：杠铃、卧推凳\n训练组数：4组×8-12次\n组间休息：60-90秒";
                case "深蹲":
                    return "建议配图：深蹲标准姿势\n所需器材：杠铃（可选）、深蹲架\n训练组数：4组×8-12次\n组间休息：90-120秒";
                case "引体向上":
                    return "建议配图：引体向上标准姿势\n所需器材：单杠\n训练组数：4组×6-10次\n组间休息：90秒\n初学者可用弹力带辅助";
                case "哑铃弯举":
                    return "建议配图：哑铃弯举标准姿势\n所需器材：哑铃\n训练组数：3组×10-15次\n组间休息：60秒";
                case "平板支撑":
                    return "建议配图：平板支撑标准姿势\n所需器材：瑜伽垫\n训练组数：3组×60秒\n组间休息：30秒";
                case "卷腹":
                    return "建议配图：卷腹标准姿势\n所需器材：瑜伽垫\n训练组数：3组×15-20次\n组间休息：30秒";
                default:
                    return "建议配图：标准姿势\n所需器材：" + (needsEquipment ? "健身器械" : "徒手") + "\n训练组数：3-4组×8-12次\n组间休息：60-90秒";
            }
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
