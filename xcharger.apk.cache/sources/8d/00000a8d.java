package org.apache.commons.lang3;

import org.apache.http.conn.ssl.TokenParser;

/* loaded from: classes.dex */
public class CharSetUtils {
    public static String squeeze(String str, String... set) {
        if (!StringUtils.isEmpty(str) && !deepEmpty(set)) {
            CharSet chars = CharSet.getInstance(set);
            StringBuilder buffer = new StringBuilder(str.length());
            char[] chrs = str.toCharArray();
            int sz = chrs.length;
            char lastChar = TokenParser.SP;
            for (int i = 0; i < sz; i++) {
                char ch = chrs[i];
                if (ch != lastChar || i == 0 || !chars.contains(ch)) {
                    buffer.append(ch);
                    lastChar = ch;
                }
            }
            return buffer.toString();
        }
        return str;
    }

    public static boolean containsAny(String str, String... set) {
        if (StringUtils.isEmpty(str) || deepEmpty(set)) {
            return false;
        }
        CharSet chars = CharSet.getInstance(set);
        char[] arr$ = str.toCharArray();
        for (char c : arr$) {
            if (chars.contains(c)) {
                return true;
            }
        }
        return false;
    }

    public static int count(String str, String... set) {
        if (StringUtils.isEmpty(str) || deepEmpty(set)) {
            return 0;
        }
        CharSet chars = CharSet.getInstance(set);
        int count = 0;
        char[] arr$ = str.toCharArray();
        for (char c : arr$) {
            if (chars.contains(c)) {
                count++;
            }
        }
        return count;
    }

    public static String keep(String str, String... set) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty() || deepEmpty(set)) {
            return "";
        }
        return modify(str, set, true);
    }

    public static String delete(String str, String... set) {
        return (StringUtils.isEmpty(str) || deepEmpty(set)) ? str : modify(str, set, false);
    }

    private static String modify(String str, String[] set, boolean expect) {
        CharSet chars = CharSet.getInstance(set);
        StringBuilder buffer = new StringBuilder(str.length());
        char[] chrs = str.toCharArray();
        int sz = chrs.length;
        for (int i = 0; i < sz; i++) {
            if (chars.contains(chrs[i]) == expect) {
                buffer.append(chrs[i]);
            }
        }
        return buffer.toString();
    }

    private static boolean deepEmpty(String[] strings) {
        if (strings != null) {
            for (String s : strings) {
                if (StringUtils.isNotEmpty(s)) {
                    return false;
                }
            }
        }
        return true;
    }
}