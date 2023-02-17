package com.xcharge.common.utils;

import android.support.v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class BCDUtils {
    private static int bcd2dec(byte val) {
        int dec_l = val & HeartBeatRequest.PORT_STATUS_FAULT;
        int dec_h = ((byte) (val >> 4)) & HeartBeatRequest.PORT_STATUS_FAULT;
        return (dec_h * 10) + dec_l;
    }

    public static long bcdBytes2Long(byte[] val) {
        String dec = "";
        for (int i = 0; i < val.length; i++) {
            dec = String.valueOf(dec) + String.format("%02d", Integer.valueOf(bcd2dec(val[i])));
        }
        return Long.parseLong(dec.toString());
    }

    public static byte[] Long2BCDBytes(long val) {
        byte[] bcd = new byte[8];
        byte[] asc = String.format("%016d", Long.valueOf(val)).getBytes(Charset.forName(CharEncoding.UTF_8));
        for (int i = 0; i < 16; i++) {
            asc[i] = (byte) (((asc[i] & 255) - 48) & MotionEventCompat.ACTION_MASK);
        }
        for (int i2 = 0; i2 < 8; i2++) {
            bcd[i2] = (byte) (((asc[i2 * 2] << 4) | asc[(i2 * 2) + 1]) & MotionEventCompat.ACTION_MASK);
        }
        return bcd;
    }
}