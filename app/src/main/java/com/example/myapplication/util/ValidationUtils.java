package com.example.myapplication.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,20}$");

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidVerificationCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return code.length() == 6 && code.matches("\\d+");
    }

    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int strength = 0;

        if (password.length() >= 6) strength++;
        if (password.length() >= 10) strength++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[^a-zA-Z0-9].*")) strength++;

        return Math.min(strength, 3);
    }

    public static float calculateBMI(float weight, float height) {
        if (weight <= 0 || height <= 0) {
            return 0;
        }
        float heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    public static String getBMIStatus(float bmi) {
        if (bmi < 18.5f) {
            return "偏瘦";
        } else if (bmi < 24) {
            return "正常";
        } else if (bmi < 28) {
            return "偏胖";
        } else {
            return "肥胖";
        }
    }
}
