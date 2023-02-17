package net.xcharger.util;

public class StrKit {
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar < 'A' || firstChar > 'Z') {
            return str;
        }
        char[] arr = str.toCharArray();
        arr[0] = (char) (arr[0] + TokenParser.f168SP);
        return new String(arr);
    }

    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar < 'a' || firstChar > 'z') {
            return str;
        }
        char[] arr = str.toCharArray();
        arr[0] = (char) (arr[0] - ' ');
        return new String(arr);
    }

    public static boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean notBlank(String str) {
        return str != null && !"".equals(str.trim());
    }

    public static boolean notBlank(String... strings) {
        if (strings == null) {
            return false;
        }
        for (String str : strings) {
            if (str == null || "".equals(str.trim())) {
                return false;
            }
        }
        return true;
    }

    public static boolean notNull(Object... paras) {
        if (paras == null) {
            return false;
        }
        for (Object obj : paras) {
            if (obj == null) {
                return false;
            }
        }
        return true;
    }
}
