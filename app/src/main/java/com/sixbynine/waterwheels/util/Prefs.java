package com.sixbynine.waterwheels.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public final class Prefs {

    private static SharedPreferences prefs;

    public static void initialize(Context context) {
        prefs = context.getSharedPreferences(context.getPackageName() + "_prefs",
                Context.MODE_MULTI_PROCESS);
    }

    private static void checkInit() {
        if (prefs == null) {
            throw new IllegalStateException("Prefs not initialized!");
        }
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String fallback) {
        checkInit();
        return prefs.getString(key, fallback);
    }

    public static void putString(String key, String string) {
        checkInit();
        prefs.edit().putString(key, string).apply();
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int fallback) {
        checkInit();
        return prefs.getInt(key, fallback);
    }

    public static void putInt(String key, int i) {
        checkInit();
        prefs.edit().putInt(key, i).apply();
    }

    public static float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public static float getFloat(String key, float fallback) {
        checkInit();
        return prefs.getFloat(key, fallback);
    }

    public static void putFloat(String key, float f) {
        checkInit();
        prefs.edit().putFloat(key, f).apply();
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }

    public static long getLong(String key, long fallback) {
        checkInit();
        return prefs.getLong(key, fallback);
    }

    public static void putLong(String key, long l) {
        checkInit();
        prefs.edit().putLong(key, l).apply();
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean fallback) {
        checkInit();
        return prefs.getBoolean(key, fallback);
    }

    public static void putBoolean(String key, boolean b) {
        checkInit();
        prefs.edit().putBoolean(key, b).apply();
    }

    public static Set<String> getStringSet(String key) {
        return getStringSet(key, new HashSet<String>());
    }

    public static Set<String> getStringSet(String key, Set<String> fallback) {
        checkInit();
        return prefs.getStringSet(key, fallback);
    }

    public static void putStringSet(String key, Set<String> set) {
        checkInit();
        prefs.edit().putStringSet(key, set).apply();
    }
}