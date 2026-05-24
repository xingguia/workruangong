package com.example.myapplication.api;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AICalorieService {

    private static final String API_URL = "https://api.dmxapi.com/v1/chat/completions";
    private static final String API_KEY = "sk-Dtu7642mkC9by9V8Bu9RiomjSPu5TSe8efqfI9mae2pyTS1R";
    private static final String MODEL = "qwen-flash-search";

    private static AICalorieService instance;
    private final OkHttpClient client;
    private final Handler mainHandler;

    // 跑步机计算相关常量
    private static final float BASE_MET = 3.5f;  // 基础MET值（步行）
    private static final float SPEED_MET_FACTOR = 0.2f;  // 速度对MET的影响因子
    private static final float INCLINE_MET_FACTOR = 0.1f;  // 坡度对MET的影响因子

    public interface AICalorieCallback {
        void onSuccess(float calories);
        void onError(String error);
    }

    private AICalorieService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized AICalorieService getInstance() {
        if (instance == null) {
            instance = new AICalorieService();
        }
        return instance;
    }

    /**
     * 计算跑步机卡路里
     * 使用修正的MET公式：考虑速度、坡度和体重
     *
     * @param speed 速度（公里/小时）
     * @param incline 坡度（%）
     * @param duration 时长（分钟）
     * @param userWeight 用户体重（公斤）
     * @param userAge 用户年龄
     * @param gender 用户性别
     * @param callback 回调
     */
    public void calculateTreadmillCalories(float speed, float incline, int duration,
                                          float userWeight, int userAge, String gender,
                                          AICalorieCallback callback) {
        // 计算MET值
        // MET = 基础MET + 速度影响 + 坡度影响
        // 速度<6km/h: 主要是走路，6-8: 轻松跑，>8: 快速跑
        float met;
        if (speed < 4) {
            // 慢走
            met = 3.0f + speed * 0.2f;
        } else if (speed < 6) {
            // 快走
            met = 4.0f + (speed - 4) * 0.5f;
        } else if (speed < 8) {
            // 慢跑
            met = 6.0f + (speed - 6) * 0.8f;
        } else if (speed < 10) {
            // 中速跑
            met = 8.0f + (speed - 8) * 0.7f;
        } else if (speed < 12) {
            // 快跑
            met = 9.5f + (speed - 10) * 0.6f;
        } else {
            // 冲刺
            met = 11.0f + (speed - 12) * 0.5f;
        }

        // 添加坡度影响：每1%坡度增加约10%的MET
        float inclineFactor = 1.0f + (incline / 100.0f) * 0.1f * incline;
        met *= inclineFactor;

        // 性别调整：男性通常代谢略高
        if ("男".equals(gender) || "male".equalsIgnoreCase(gender)) {
            met *= 1.05f;
        }

        // 年龄调整：每10年MET下降约5%
        float ageFactor = 1.0f - ((userAge - 25) / 100.0f);
        ageFactor = Math.max(0.8f, ageFactor); // 最低80%
        met *= ageFactor;

        // 计算卡路里: Calories = MET × weight(kg) × time(hours)
        float calories = met * userWeight * (duration / 60.0f);

        callback.onSuccess(calories);
    }

    /**
     * 计算跑步机卡路里（简化版，使用默认参数）
     */
    public void calculateTreadmillCaloriesSimple(float speed, float incline, int duration, float userWeight, AICalorieCallback callback) {
        calculateTreadmillCalories(speed, incline, duration, userWeight, 25, "未知", callback);
    }

    public void calculateCalories(String exerciseName, int duration, int reps, float weight,
                                   float userWeight, float userHeight, int userAge, String gender,
                                   AICalorieCallback callback) {
        String prompt = buildCaloriePrompt(exerciseName, duration, reps, weight, userWeight, userHeight, userAge, gender);

        String jsonBody = "{"
                + "\"model\": \"" + MODEL + "\","
                + "\"messages\": ["
                + "{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}"
                + "],"
                + "\"stream\": false"
                + "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    float calories = parseCaloriesFromResponse(responseBody);
                    mainHandler.post(() -> callback.onSuccess(calories));
                } else {
                    mainHandler.post(() -> callback.onError("API请求失败: " + response.code()));
                }
            }
        });
    }

    public void calculateCaloriesSimple(String exerciseName, int duration, AICalorieCallback callback) {
        calculateCalories(exerciseName, duration, 0, 0, 70, 170, 25, "未知", callback);
    }

    private String buildCaloriePrompt(String exerciseName, int duration, int reps, float weight,
                                       float userWeight, float userHeight, int userAge, String gender) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个健身卡路里计算助手。根据以下信息计算消耗的卡路里（千卡）：\n\n");
        prompt.append("运动名称：").append(exerciseName).append("\n");
        prompt.append("运动时长：").append(duration).append("分钟\n");
        if (reps > 0) {
            prompt.append("动作次数：").append(reps).append("次\n");
        }
        if (weight > 0) {
            prompt.append("负重重量：").append(weight).append("公斤\n");
        }
        prompt.append("用户体重：").append(userWeight).append("公斤\n");
        prompt.append("用户身高：").append(userHeight).append("厘米\n");
        prompt.append("用户年龄：").append(userAge).append("岁\n");
        prompt.append("用户性别：").append(gender).append("\n\n");
        prompt.append("请根据这些信息计算消耗的卡路里（千卡），只返回一个数字，不要解释。");
        return prompt.toString();
    }

    private float parseCaloriesFromResponse(String response) {
        try {
            // Parse JSON response and extract content
            // Expected format: {"choices":[{"message":{"content":"123"}}]}
            int contentIndex = response.indexOf("\"content\":\"");
            if (contentIndex != -1) {
                int start = contentIndex + 10;
                int end = response.indexOf("\"", start);
                if (end != -1) {
                    String content = response.substring(start, end);
                    // Extract number from content
                    StringBuilder number = new StringBuilder();
                    for (char c : content.toCharArray()) {
                        if (Character.isDigit(c) || c == '.') {
                            number.append(c);
                        } else if (number.length() > 0) {
                            break;
                        }
                    }
                    if (number.length() > 0) {
                        return Float.parseFloat(number.toString());
                    }
                }
            }

            // Fallback: try to find any number in response
            StringBuilder number = new StringBuilder();
            for (char c : response.toCharArray()) {
                if (Character.isDigit(c) || c == '.') {
                    number.append(c);
                } else if (number.length() > 0) {
                    break;
                }
            }
            if (number.length() > 0) {
                return Float.parseFloat(number.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    public void estimateCalories(String exerciseName, int duration, int reps) {
        estimateCalories(exerciseName, duration, reps, 0);
    }

    public void estimateCalories(String exerciseName, int duration, int reps, float weight) {
        // Fallback estimation based on exercise type
        float baseCalories = getBaseCaloriesPerMinute(exerciseName);
        float calories = baseCalories * duration;

        if (reps > 0) {
            // Adjust for reps
            calories += (reps / 10.0f) * baseCalories * 0.5f;
        }

        if (weight > 0) {
            // Adjust for added weight
            calories *= (1 + weight / 50.0f);
        }
    }

    private float getBaseCaloriesPerMinute(String exerciseName) {
        String name = exerciseName.toLowerCase();
        if (name.contains("俯卧撑")) return 7f;
        if (name.contains("仰卧起坐") || name.contains("卷腹")) return 5f;
        if (name.contains("深蹲")) return 6f;
        if (name.contains("跑步") || name.contains("慢跑")) return 10f;
        if (name.contains("跳绳")) return 12f;
        if (name.contains("游泳")) return 11f;
        if (name.contains("骑行") || name.contains("自行车")) return 8f;
        if (name.contains("瑜伽")) return 4f;
        if (name.contains("拉伸")) return 3f;
        if (name.contains("平板支撑")) return 5f;
        if (name.contains("波比")) return 10f;
        if (name.contains("引体向上")) return 6f;
        if (name.contains("硬拉")) return 8f;
        if (name.contains("卧推")) return 7f;
        if (name.contains("哑铃")) return 6f;
        if (name.contains("开合跳")) return 8f;
        if (name.contains("高抬腿")) return 9f;
        return 5f; // Default
    }
}
