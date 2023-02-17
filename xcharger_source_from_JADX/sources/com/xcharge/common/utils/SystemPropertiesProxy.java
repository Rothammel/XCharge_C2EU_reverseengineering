package com.xcharge.common.utils;

import android.content.Context;

public class SystemPropertiesProxy {
    public static String get(Context context, String key) throws IllegalArgumentException {
        try {
            Class SystemProperties = context.getClassLoader().loadClass("android.os.SystemProperties");
            return (String) SystemProperties.getMethod("get", new Class[]{String.class}).invoke(SystemProperties, new Object[]{new String(key)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return "";
        }
    }

    public static String get(Context context, String key, String def) throws IllegalArgumentException {
        String str = def;
        try {
            Class SystemProperties = context.getClassLoader().loadClass("android.os.SystemProperties");
            return (String) SystemProperties.getMethod("get", new Class[]{String.class, String.class}).invoke(SystemProperties, new Object[]{new String(key), new String(def)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return def;
        }
    }

    public static Integer getInt(Context context, String key, int def) throws IllegalArgumentException {
        Integer valueOf = Integer.valueOf(def);
        try {
            Class SystemProperties = context.getClassLoader().loadClass("android.os.SystemProperties");
            return (Integer) SystemProperties.getMethod("getInt", new Class[]{String.class, Integer.TYPE}).invoke(SystemProperties, new Object[]{new String(key), new Integer(def)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return Integer.valueOf(def);
        }
    }

    public static Long getLong(Context context, String key, long def) throws IllegalArgumentException {
        Long valueOf = Long.valueOf(def);
        try {
            Class SystemProperties = context.getClassLoader().loadClass("android.os.SystemProperties");
            return (Long) SystemProperties.getMethod("getLong", new Class[]{String.class, Long.TYPE}).invoke(SystemProperties, new Object[]{new String(key), new Long(def)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return Long.valueOf(def);
        }
    }

    public static Boolean getBoolean(Context context, String key, boolean def) throws IllegalArgumentException {
        Boolean valueOf = Boolean.valueOf(def);
        try {
            Class SystemProperties = context.getClassLoader().loadClass("android.os.SystemProperties");
            return (Boolean) SystemProperties.getMethod("getBoolean", new Class[]{String.class, Boolean.TYPE}).invoke(SystemProperties, new Object[]{new String(key), new Boolean(def)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return Boolean.valueOf(def);
        }
    }

    public static void set(Context context, String key, String val) throws Exception {
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            SystemProperties.getMethod("set", new Class[]{String.class, String.class}).invoke(SystemProperties, new Object[]{new String(key), new String(val)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        }
    }
}
