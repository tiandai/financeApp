package com.example.financeapp.utils;

public class InputUtil {
    public static String sanitize(String input) {
        if (input == null) return null;

        // Remove leading/trailing/in-between whitespace
        return input.trim().replaceAll("\\s{2,}", "");
    }

    public static boolean isValidTicker(String input) {
        return input != null && input.matches("^[a-zA-Z]+$");
    }
}
