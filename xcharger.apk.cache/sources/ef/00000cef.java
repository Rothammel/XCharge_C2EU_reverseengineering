package org.apache.http.util;

/* loaded from: classes.dex */
public class Asserts {
    public static void check(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void check(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalStateException(String.format(message, args));
        }
    }

    public static void notNull(Object object, String name) {
        if (object == null) {
            throw new IllegalStateException(String.valueOf(name) + " is null");
        }
    }

    public static void notEmpty(CharSequence s, String name) {
        if (TextUtils.isEmpty(s)) {
            throw new IllegalStateException(String.valueOf(name) + " is empty");
        }
    }

    public static void notBlank(CharSequence s, String name) {
        if (TextUtils.isBlank(s)) {
            throw new IllegalStateException(String.valueOf(name) + " is blank");
        }
    }
}