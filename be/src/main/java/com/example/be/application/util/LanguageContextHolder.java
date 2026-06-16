package com.example.be.application.util;

public class LanguageContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setLanguage(String language) {
        contextHolder.set(language);
    }

    public static String getLanguage() {
        String lan = contextHolder.get();
        return (lan != null && !lan.trim().isEmpty()) ? lan.toUpperCase() : "VI";
    }

    public static void clear() {
        contextHolder.remove();
    }
}
