package com.xcharge.common.utils;

import android.util.Base64;
import android.util.Log;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    public static String encrypt128ECB(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        Log.d("AESUtils.encrypt128ECB", "skey: " + sKey + ", sdata: " + sSrc);
        if (sKey.length() != 16) {
            return null;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(sKey.getBytes("utf-8")), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return Base64.encodeToString(cipher.doFinal(sSrc.getBytes("utf-8")), 0);
    }

    public static byte[] encrypt128ECB(byte[] sSrc, byte[] sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        Log.d("AESUtils.encrypt128ECB", "key: " + FormatUtils.bytesToHexString(sKey) + ", data: " + FormatUtils.bytesToHexString(sSrc));
        if (sKey.length != 16) {
            return null;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(sKey), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(sSrc);
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
            SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(sKey.getBytes("utf-8")), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, skeySpec);
            try {
                return new String(cipher.doFinal(Base64.decode(sSrc.getBytes("utf-8"), 0)), "utf-8");
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
            if (sKey.length != 16) {
                return null;
            }
            SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(sKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, skeySpec);
            try {
                return cipher.doFinal(sSrc);
            } catch (Exception e) {
                Log.e("AESUtils.decrypt128ECB", Log.getStackTraceString(e));
                return null;
            }
        } catch (Exception e2) {
            return null;
        }
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, sr);
        return kgen.generateKey().getEncoded();
    }
}
