package com.alibaba.sdk.android.oss.common.utils;

import android.support.v4.view.MotionEventCompat;
import java.lang.reflect.Array;
import java.util.zip.Checksum;

/* loaded from: classes.dex */
public class CRC64 implements Checksum {
    private static final int GF2_DIM = 64;
    private static final long POLY = -3932672073523589310L;
    private static final long[][] table = (long[][]) Array.newInstance(Long.TYPE, 8, 256);
    private long value = 0;

    static {
        for (int n = 0; n < 256; n++) {
            long crc = n;
            for (int k = 0; k < 8; k++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ POLY;
                } else {
                    crc >>>= 1;
                }
            }
            table[0][n] = crc;
        }
        for (int n2 = 0; n2 < 256; n2++) {
            long crc2 = table[0][n2];
            for (int k2 = 1; k2 < 8; k2++) {
                crc2 = table[0][(int) (255 & crc2)] ^ (crc2 >>> 8);
                table[k2][n2] = crc2;
            }
        }
    }

    @Override // java.util.zip.Checksum
    public long getValue() {
        return this.value;
    }

    @Override // java.util.zip.Checksum
    public void reset() {
        this.value = 0L;
    }

    @Override // java.util.zip.Checksum
    public void update(int val) {
        byte[] b = {(byte) (val & MotionEventCompat.ACTION_MASK)};
        update(b, b.length);
    }

    public void update(byte[] b, int len) {
        update(b, 0, len);
    }

    @Override // java.util.zip.Checksum
    public void update(byte[] b, int off, int len) {
        this.value ^= -1;
        int idx = off;
        while (len >= 8) {
            this.value = ((((((table[7][(int) ((this.value & 255) ^ (b[idx] & 255))] ^ table[6][(int) (((this.value >>> 8) & 255) ^ (b[idx + 1] & 255))]) ^ table[5][(int) (((this.value >>> 16) & 255) ^ (b[idx + 2] & 255))]) ^ table[4][(int) (((this.value >>> 24) & 255) ^ (b[idx + 3] & 255))]) ^ table[3][(int) (((this.value >>> 32) & 255) ^ (b[idx + 4] & 255))]) ^ table[2][(int) (((this.value >>> 40) & 255) ^ (b[idx + 5] & 255))]) ^ table[1][(int) (((this.value >>> 48) & 255) ^ (b[idx + 6] & 255))]) ^ table[0][(int) ((this.value >>> 56) ^ (b[idx + 7] & 255))];
            idx += 8;
            len -= 8;
        }
        while (len > 0) {
            this.value = table[0][(int) ((this.value ^ b[idx]) & 255)] ^ (this.value >>> 8);
            idx++;
            len--;
        }
        this.value ^= -1;
    }

    private static long gf2MatrixTimes(long[] mat, long vec) {
        long sum = 0;
        int idx = 0;
        while (vec != 0) {
            if ((vec & 1) == 1) {
                sum ^= mat[idx];
            }
            vec >>>= 1;
            idx++;
        }
        return sum;
    }

    private static void gf2MatrixSquare(long[] square, long[] mat) {
        for (int n = 0; n < 64; n++) {
            square[n] = gf2MatrixTimes(mat, mat[n]);
        }
    }

    public static long combine(long crcLast, long crcNext, long len2) {
        if (len2 != 0) {
            long[] even = new long[64];
            long[] odd = new long[64];
            odd[0] = -3932672073523589310L;
            long row = 1;
            for (int n = 1; n < 64; n++) {
                odd[n] = row;
                row <<= 1;
            }
            gf2MatrixSquare(even, odd);
            gf2MatrixSquare(odd, even);
            long crc1 = crcLast;
            do {
                gf2MatrixSquare(even, odd);
                if ((1 & len2) == 1) {
                    crc1 = gf2MatrixTimes(even, crc1);
                }
                long len22 = len2 >>> 1;
                if (len22 == 0) {
                    break;
                }
                gf2MatrixSquare(odd, even);
                if ((1 & len22) == 1) {
                    crc1 = gf2MatrixTimes(odd, crc1);
                }
                len2 = len22 >>> 1;
            } while (len2 != 0);
            return crc1 ^ crcNext;
        }
        return crcLast;
    }
}