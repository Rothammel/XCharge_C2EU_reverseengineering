package com.xcharge.common.utils;

import android.util.Base64;
import android.util.Log;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/* loaded from: classes.dex */
public class AESUtils {
    public static String encrypt128ECB(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        Log.d("AESUtils.encrypt128ECB", "skey: " + sKey + ", sdata: " + sSrc);
        if (sKey.length() == 16) {
            byte[] raw = getRawKey(sKey.getBytes("utf-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
            return Base64.encodeToString(encrypted, 0);
        }
        return null;
    }

    public static byte[] encrypt128ECB(byte[] sSrc, byte[] sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        Log.d("AESUtils.encrypt128ECB", "key: " + FormatUtils.bytesToHexString(sKey) + ", data: " + FormatUtils.bytesToHexString(sSrc));
        if (sKey.length == 16) {
            byte[] raw = getRawKey(sKey);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, skeySpec);
            return cipher.doFinal(sSrc);
        }
        return null;
    }

    public static String decrypt128ECB(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        try {
            Log.d("AESUtils.decrypt128ECB", "skey: " + sKey + ", sdata: " + sSrc);
            if (sKey.length() != 16) {
                return null;
            }
            byte[] raw = getRawKey(sKey.getBytes("utf-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, skeySpec);
            byte[] encrypted1 = Base64.decode(sSrc.getBytes("utf-8"), 0);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                return new String(original, "utf-8");
            } catch (Exception e) {
                Log.e("AESUtils.decrypt128ECB", Log.getStackTraceString(e));
                return null;
            }
        } catch (Exception e2) {
            return null;
        }
    }

    public static byte[] decrypt128ECB(byte[] sSrc, byte[] sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        try {
            Log.d("AESUtils.decrypt128ECB", "key: " + FormatUtils.bytesToHexString(sKey) + ", data: " + FormatUtils.bytesToHexString(sSrc));
            if (sKey.length == 16) {
                byte[] raw = getRawKey(sKey);
                SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(2, skeySpec);
                try {
                    return cipher.doFinal(sSrc);
                } catch (Exception e) {
                    Log.e("AESUtils.decrypt128ECB", Log.getStackTraceString(e));
                    return null;
                }
            }
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }
}