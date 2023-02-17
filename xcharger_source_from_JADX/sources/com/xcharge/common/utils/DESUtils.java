package com.xcharge.common.utils;

import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESUtils {
    private static final String DES = "DES";

    public static String encrypt(String data, String key) throws Exception {
        return Base64.encodeToString(encrypt(data.getBytes(), key.getBytes()), 0);
    }

    public static String decrypt(String data, String key) throws Exception {
        return new String(decrypt(Base64.decode(data, 0), key.getBytes()));
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        SecretKey securekey = SecretKeyFactory.getInstance(DES).generateSecret(new DESKeySpec(key));
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(1, securekey, sr);
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        SecretKey securekey = SecretKeyFactory.getInstance(DES).generateSecret(new DESKeySpec(key));
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(2, securekey, sr);
        return cipher.doFinal(data);
    }
}
