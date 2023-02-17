package com.xcharge.common.utils;

/* loaded from: classes.dex */
public class FormatUtils {
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 255;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static byte[] hexStringToBytes(String src) {
        if (src == null || src.equals("")) {
            return null;
        }
        String src2 = src.toUpperCase();
        int length = src2.length() / 2;
        char[] hexChars = src2.toCharArray();
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            b[i] = (byte) ((hexCharToByte(hexChars[pos]) << 4) | hexCharToByte(hexChars[pos + 1]));
        }
        return b;
    }

    public static byte hexCharToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String getRandomString(int length) {
        StringBuffer sb = new StringBuffer();
        int len = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".length();
        for (int i = 0; i < length; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt((int) Math.round(Math.random() * (len - 1))));
        }
        return sb.toString();
    }

    public static boolean isHexString(String src) {
        try {
            Integer.parseInt(src, 16);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}