package com.xcharge.common.utils;

import android.content.Context;
import java.lang.reflect.Method;

/* loaded from: classes.dex */
public class SystemPropertiesProxy {
    public static String get(Context context, String key) throws IllegalArgumentException {
        try {
            ClassLoader cl = context.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = {String.class};
            Method get = SystemProperties.getMethod("get", paramTypes);
            Object[] params = {new String(key)};
            String ret = (String) get.invoke(SystemProperties, params);
            return ret;
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return "";
        }
    }

    public static String get(Context context, String key, String def) throws IllegalArgumentException {
        try {
            ClassLoader cl = context.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = {String.class, String.class};
            Method get = SystemProperties.getMethod("get", paramTypes);
            Object[] params = {new String(key), new String(def)};
            String ret = (String) get.invoke(SystemProperties, params);
            return ret;
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return def;
        }
    }

    public static Integer getInt(Context context, String key, int def) throws IllegalArgumentException {
        Integer.valueOf(def);
        try {
            ClassLoader cl = context.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = {String.class, Integer.TYPE};
            Method getInt = SystemProperties.getMethod("getInt", paramTypes);
            Object[] params = {new String(key), new Integer(def)};
            Integer ret = (Integer) getInt.invoke(SystemProperties, params);
            return ret;
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            Integer ret2 = Integer.valueOf(def);
            return ret2;
        }
    }

    public static Long getLong(Context context, String key, long def) throws IllegalArgumentException {
        Long.valueOf(def);
        try {
            ClassLoader cl = context.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = {String.class, Long.TYPE};
            Method getLong = SystemProperties.getMethod("getLong", paramTypes);
            Object[] params = {new String(key), new Long(def)};
            Long ret = (Long) getLong.invoke(SystemProperties, params);
            return ret;
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            Long ret2 = Long.valueOf(def);
            return ret2;
        }
    }

    public static Boolean getBoolean(Context context, String key, boolean def) throws IllegalArgumentException {
        Boolean.valueOf(def);
        try {
            ClassLoader cl = context.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = {String.class, Boolean.TYPE};
            Method getBoolean = SystemProperties.getMethod("getBoolean", paramTypes);
            Object[] params = {new String(key), new Boolean(def)};
            Boolean ret = (Boolean) getBoolean.invoke(SystemProperties, params);
            return ret;
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            Boolean ret2 = Boolean.valueOf(def);
            return ret2;
        }
    }

    public static void set(Context context, String key, String val) throws Exception {
        try {
            context.getClassLoader();
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Class[] paramTypes = {String.class, String.class};
            Method set = SystemProperties.getMethod("set", paramTypes);
            Object[] params = {new String(key), new String(val)};
            set.invoke(SystemProperties, params);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        }
    }
}
