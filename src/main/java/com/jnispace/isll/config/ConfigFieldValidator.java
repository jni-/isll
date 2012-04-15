package com.jnispace.isll.config;

public class ConfigFieldValidator {

    private ConfigFieldValidator() {
    }

    public static boolean shouldInclude(String field) {
        return field != null && field.length() > 0;
    }
}
