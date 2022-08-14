package com.ds.app.pricereading.db.entity.util;

public class StringUtil {

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String getOrDefault(String value, String defaultValue) {
        if (isNullOrEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

}
