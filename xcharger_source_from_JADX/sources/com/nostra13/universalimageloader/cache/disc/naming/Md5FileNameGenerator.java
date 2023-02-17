package com.nostra13.universalimageloader.cache.disc.naming;

import com.nostra13.universalimageloader.utils.C0219L;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5FileNameGenerator implements FileNameGenerator {
    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 36;

    public String generate(String imageUri) {
        return new BigInteger(getMD5(imageUri.getBytes())).abs().toString(RADIX);
    }

    private byte[] getMD5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            C0219L.m14e(e);
            return null;
        }
    }
}
