package com.feldsher.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;

    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    private static final List<String> COMMON_WEAK_PASSWORDS = Arrays.asList(
            "123456", "123456789", "password", "12345678", "111111",
            "123123", "1234567890", "1234567", "qwerty", "000000",
            "12345678910", "1234", "12345", "abc123", "123",
            "password1", "admin", "letmein", "welcome", "monkey",
            "123456a", "654321", "superman", "123321", "qwerty123"
    );

    private PasswordValidator() {
    }

    public static PasswordValidationResult validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            return PasswordValidationResult.failed("密码不能为空");
        }

        String trimmedPassword = password.trim();

        if (trimmedPassword.length() < MIN_LENGTH) {
            return PasswordValidationResult.failed("密码长度不能少于" + MIN_LENGTH + "位");
        }

        if (trimmedPassword.length() > MAX_LENGTH) {
            return PasswordValidationResult.failed("密码长度不能超过" + MAX_LENGTH + "位");
        }

        int score = 0;
        StringBuilder suggestions = new StringBuilder();

        if (LOWERCASE_PATTERN.matcher(trimmedPassword).find()) {
            score++;
        } else {
            suggestions.append("需要包含小写字母；");
        }

        if (UPPERCASE_PATTERN.matcher(trimmedPassword).find()) {
            score++;
        } else {
            suggestions.append("需要包含大写字母；");
        }

        if (DIGIT_PATTERN.matcher(trimmedPassword).find()) {
            score++;
        } else {
            suggestions.append("需要包含数字；");
        }

        if (SPECIAL_CHAR_PATTERN.matcher(trimmedPassword).find()) {
            score++;
        } else {
            suggestions.append("需要包含特殊字符(!@#$%^&*(),.?\":{}|<>)；");
        }

        if (score < 3) {
            return PasswordValidationResult.failed("密码强度不足，" + suggestions.toString());
        }

        if (COMMON_WEAK_PASSWORDS.stream().anyMatch(weak -> trimmedPassword.toLowerCase().contains(weak))) {
            return PasswordValidationResult.failed("密码过于简单，请勿使用常见的弱密码组合");
        }

        String phonePattern = "1[3-9]\\d{9}";
        if (trimmedPassword.matches(phonePattern)) {
            return PasswordValidationResult.failed("密码不能是手机号");
        }

        if (hasSequentialChars(trimmedPassword, 4)) {
            return PasswordValidationResult.failed("密码不能包含连续4个以上的顺序字符");
        }

        if (hasRepeatedChars(trimmedPassword, 4)) {
            return PasswordValidationResult.failed("密码不能包含连续4个以上的重复字符");
        }

        String strength;
        if (score == 4) {
            strength = "强";
        } else if (score == 3) {
            strength = "中";
        } else {
            strength = "弱";
        }

        return PasswordValidationResult.success(strength);
    }

    private static boolean hasSequentialChars(String password, int minLength) {
        if (password.length() < minLength) {
            return false;
        }

        for (int i = 0; i <= password.length() - minLength; i++) {
            boolean sequential = true;
            for (int j = 0; j < minLength - 1; j++) {
                if (password.charAt(i + j + 1) != password.charAt(i + j) + 1) {
                    sequential = false;
                    break;
                }
            }
            if (sequential) {
                return true;
            }

            sequential = true;
            for (int j = 0; j < minLength - 1; j++) {
                if (password.charAt(i + j + 1) != password.charAt(i + j) - 1) {
                    sequential = false;
                    break;
                }
            }
            if (sequential) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasRepeatedChars(String password, int minLength) {
        if (password.length() < minLength) {
            return false;
        }

        for (int i = 0; i <= password.length() - minLength; i++) {
            boolean repeated = true;
            for (int j = 0; j < minLength - 1; j++) {
                if (password.charAt(i + j + 1) != password.charAt(i + j)) {
                    repeated = false;
                    break;
                }
            }
            if (repeated) {
                return true;
            }
        }

        return false;
    }

    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;
        private final String strength;

        private PasswordValidationResult(boolean valid, String message, String strength) {
            this.valid = valid;
            this.message = message;
            this.strength = strength;
        }

        public static PasswordValidationResult success(String strength) {
            return new PasswordValidationResult(true, "密码验证通过", strength);
        }

        public static PasswordValidationResult failed(String message) {
            return new PasswordValidationResult(false, message, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getStrength() {
            return strength;
        }
    }
}
