package com.xcharge.common.utils;

import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

public class EndianUtils {
    public static byte[] shortToLittleBytes(short s) {
        return new byte[]{(byte) (s & 255), (byte) ((s >> 8) & MotionEventCompat.ACTION_MASK)};
    }

    public static short littleBytesToShort(byte[] b) {
        return (short) ((b[0] & 255) | ((b[1] & 255) << 8));
    }

    public static byte[] intToLittleBytes(int i) {
        return new byte[]{(byte) (i & MotionEventCompat.ACTION_MASK), (byte) ((i >> 8) & MotionEventCompat.ACTION_MASK), (byte) ((i >> 16) & MotionEventCompat.ACTION_MASK), (byte) ((i >> 24) & MotionEventCompat.ACTION_MASK)};
    }

    public static int littleBytesToInt(byte[] b) {
        return (b[0] & 255) | ((b[1] & 255) << 8) | ((b[2] & 255) << 16) | ((b[3] & 255) << AnyoMessage.CMD_REPORT_CHARGE_STOPPED);
    }

    public static byte[] longToLittleBytes(long l) {
        long temp = l;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(255 & temp).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static byte[] longToLittleBytes(long l, int len) {
        long temp = l;
        byte[] b = new byte[len];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(255 & temp).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static long littleBytesToLong(byte[] b) {
        return ((((long) b[7]) & 255) << 56) | ((((long) b[6]) & 255) << 48) | ((((long) b[5]) & 255) << 40) | ((((long) b[4]) & 255) << 32) | ((((long) b[3]) & 255) << 24) | ((((long) b[2]) & 255) << 16) | ((((long) b[1]) & 255) << 8) | ((((long) b[0]) & 255) << 0);
    }

    public static byte[] shortToBigBytes(short s) {
        return new byte[]{(byte) ((s >> 8) & MotionEventCompat.ACTION_MASK), (byte) (s & 255)};
    }

    public static short bigBytesToShort(byte[] b) {
        return (short) ((b[1] & 255) | ((b[0] & 255) << 8));
    }

    public static byte[] intToBigBytes(int i) {
        return new byte[]{(byte) ((i >> 24) & MotionEventCompat.ACTION_MASK), (byte) ((i >> 16) & MotionEventCompat.ACTION_MASK), (byte) ((i >> 8) & MotionEventCompat.ACTION_MASK), (byte) (i & MotionEventCompat.ACTION_MASK)};
    }

    public static int bigBytesToInt(byte[] b) {
        return (b[3] & 255) | ((b[2] & 255) << 8) | ((b[1] & 255) << 16) | ((b[0] & 255) << AnyoMessage.CMD_REPORT_CHARGE_STOPPED);
    }

    public static byte[] longToBigBytes(long l) {
        long temp = l;
        byte[] b = new byte[8];
        for (int i = b.length; i > 0; i--) {
            b[i - 1] = Long.valueOf(255 & temp).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static byte[] longToBigBytes(long l, int len) {
        long temp = l;
        byte[] b = new byte[len];
        for (int i = b.length; i > 0; i--) {
            b[i - 1] = Long.valueOf(255 & temp).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static long bigBytesToLong(byte[] b) {
        return ((((long) b[0]) & 255) << 56) | ((((long) b[1]) & 255) << 48) | ((((long) b[2]) & 255) << 40) | ((((long) b[3]) & 255) << 32) | ((((long) b[4]) & 255) << 24) | ((((long) b[5]) & 255) << 16) | ((((long) b[6]) & 255) << 8) | ((((long) b[7]) & 255) << 0);
    }
}
