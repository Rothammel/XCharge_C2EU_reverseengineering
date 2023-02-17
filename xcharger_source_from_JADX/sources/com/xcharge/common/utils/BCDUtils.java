package com.xcharge.common.utils;

import android.support.p000v4.view.MotionEventCompat;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

public class BCDUtils {
    private static int bcd2dec(byte val) {
        return ((((byte) (val >> 4)) & 15) * 10) + (val & 15);
    }

    public static long bcdBytes2Long(byte[] val) {
        String dec = "";
        for (int i = 0; i < val.length; i++) {
            dec = String.valueOf(dec) + String.format("%02d", new Object[]{Integer.valueOf(bcd2dec(val[i]))});
        }
        return Long.parseLong(dec.toString());
    }

    public static byte[] Long2BCDBytes(long val) {
        byte[] bcd = new byte[8];
        byte[] asc = String.format("%016d", new Object[]{Long.valueOf(val)}).getBytes(Charset.forName(CharEncoding.UTF_8));
        for (int i = 0; i < 16; i++) {
            asc[i] = (byte) (((asc[i] & 255) - 48) & MotionEventCompat.ACTION_MASK);
        }
        for (int i2 = 0; i2 < 8; i2++) {
            bcd[i2] = (byte) (((asc[i2 * 2] << 4) | asc[(i2 * 2) + 1]) & 255);
        }
        return bcd;
    }
}
