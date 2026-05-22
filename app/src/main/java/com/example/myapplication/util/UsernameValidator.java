package com.example.myapplication.util;

import android.content.Context;
import java.util.Set;
import java.util.regex.Pattern;

public class UsernameValidator {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");

    public static class ValidationResult {
        public final boolean valid;
        public final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
    }

    public static ValidationResult validate(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "用户名不能为空");
        }

        String trimmed = username.trim();

        if (trimmed.length() < MIN_LENGTH) {
            return new ValidationResult(false, "用户名至少需要" + MIN_LENGTH + "个字符");
        }

        if (trimmed.length() > MAX_LENGTH) {
            return new ValidationResult(false, "用户名不能超过" + MAX_LENGTH + "个字符");
        }

        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            return new ValidationResult(false, "用户名只能包含字母、数字、下划线和中文");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateWithAvailability(Context context, String username) {
        ValidationResult basicResult = validate(username);
        if (!basicResult.valid) {
            return basicResult;
        }

        SessionManager sessionManager = SessionManager.getInstance(context);
        if (!sessionManager.isNicknameAvailable(username.trim())) {
            return new ValidationResult(false, "该用户名已被使用");
        }

        return new ValidationResult(true, null);
    }

    public static boolean isUsernameTaken(Context context, String username) {
        SessionManager sessionManager = SessionManager.getInstance(context);
        return !sessionManager.isNicknameAvailable(username.trim());
    }
}
