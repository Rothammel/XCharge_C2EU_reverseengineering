package org.eclipse.paho.client.mqttv3.util;

public final class Strings {
    private static final int INDEX_NOT_FOUND = -1;

    public static boolean equalsAny(CharSequence cs, CharSequence[] strs) {
        boolean eq;
        boolean eq2 = false;
        if (cs == null) {
            eq2 = strs == null;
        }
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                if (eq || strs[i].equals(cs)) {
                    eq = true;
                } else {
                    eq = false;
                }
            }
        }
        return eq;
    }

    public static boolean containsAny(CharSequence cs, CharSequence searchChars) {
        if (searchChars == null) {
            return false;
        }
        return containsAny(cs, toCharArray(searchChars));
    }

    public static boolean containsAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || isEmpty(searchChars)) {
            return false;
        }
        int csLength = cs.length();
        int searchLength = searchChars.length;
        int csLast = csLength - 1;
        int searchLast = searchLength - 1;
        for (int i = 0; i < csLength; i++) {
            char ch = cs.charAt(i);
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    if (!Character.isHighSurrogate(ch) || j == searchLast) {
                        return true;
                    }
                    if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    private static char[] toCharArray(CharSequence cs) {
        if (cs instanceof String) {
            return ((String) cs).toCharArray();
        }
        int sz = cs.length();
        char[] array = new char[cs.length()];
        for (int i = 0; i < sz; i++) {
            array[i] = cs.charAt(i);
        }
        return array;
    }

    public static int countMatches(CharSequence str, CharSequence sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while (true) {
            int idx2 = indexOf(str, sub, idx);
            if (idx2 == -1) {
                return count;
            }
            count++;
            idx = idx2 + sub.length();
        }
    }

    private static int indexOf(CharSequence cs, CharSequence searchChar, int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
    }

    private Strings() {
    }
}
