package com.adai.gkdnavi.utils;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;


public class SpUtils {
    private static final String fileName = "tag";

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);

        sp.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        sp.edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        sp.edit().putInt(key, value).commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

    public static void putHashSet(Context context, String key, Set<String> data) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        sp.edit().putStringSet(key, data).commit();
    }

    public static Set<String> getHashSet(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        return sp.getStringSet(key, null);
    }

    public static void installHashSet(Context context, String key, String value) {
        Set<String> set = getHashSet(context, key);
        if (set == null) {
            set = new HashSet<String>();
        }
        set.add(value);
        putHashSet(context, key, set);
    }
}
