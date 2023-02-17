package org.apache.mina.proxy.utils;

import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.CharEncoding;

public class ByteUtilities {
    public static int networkByteOrderToInt(byte[] buf, int start, int count) {
        if (count > 4) {
            throw new IllegalArgumentException("Cannot handle more than 4 bytes");
        }
        int result = 0;
        for (int i = 0; i < count; i++) {
            result = (result << 8) | (buf[start + i] & MotionEventCompat.ACTION_MASK);
        }
        return result;
    }

    public static byte[] intToNetworkByteOrder(int num, int count) {
        byte[] buf = new byte[count];
        intToNetworkByteOrder(num, buf, 0, count);
        return buf;
    }

    public static void intToNetworkByteOrder(int num, byte[] buf, int start, int count) {
        if (count > 4) {
            throw new IllegalArgumentException("Cannot handle more than 4 bytes");
        }
        for (int i = count - 1; i >= 0; i--) {
            buf[start + i] = (byte) (num & MotionEventCompat.ACTION_MASK);
            num >>>= 8;
        }
    }

    public static final byte[] writeShort(short v) {
        return writeShort(v, new byte[2], 0);
    }

    public static final byte[] writeShort(short v, byte[] b, int offset) {
        b[offset] = (byte) v;
        b[offset + 1] = (byte) (v >> 8);
        return b;
    }

    public static final byte[] writeInt(int v) {
        return writeInt(v, new byte[4], 0);
    }

    public static final byte[] writeInt(int v, byte[] b, int offset) {
        b[offset] = (byte) v;
        b[offset + 1] = (byte) (v >> 8);
        b[offset + 2] = (byte) (v >> 16);
        b[offset + 3] = (byte) (v >> 24);
        return b;
    }

    public static final void changeWordEndianess(byte[] b, int offset, int length) {
        for (int i = offset; i < offset + length; i += 4) {
            byte tmp = b[i];
            b[i] = b[i + 3];
            b[i + 3] = tmp;
            byte tmp2 = b[i + 1];
            b[i + 1] = b[i + 2];
            b[i + 2] = tmp2;
        }
    }

    public static final void changeByteEndianess(byte[] b, int offset, int length) {
        for (int i = offset; i < offset + length; i += 2) {
            byte tmp = b[i];
            b[i] = b[i + 1];
            b[i + 1] = tmp;
        }
    }

    public static final byte[] getOEMStringAsByteArray(String s) throws UnsupportedEncodingException {
        return s.getBytes("ASCII");
    }

    public static final byte[] getUTFStringAsByteArray(String s) throws UnsupportedEncodingException {
        return s.getBytes(CharEncoding.UTF_16LE);
    }

    public static final byte[] encodeString(String s, boolean useUnicode) throws UnsupportedEncodingException {
        if (useUnicode) {
            return getUTFStringAsByteArray(s);
        }
        return getOEMStringAsByteArray(s);
    }

    public static String asHex(byte[] bytes) {
        return asHex(bytes, (String) null);
    }

    public static String asHex(byte[] bytes, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String code = Integer.toHexString(bytes[i] & 255);
            if ((bytes[i] & 255) < 16) {
                sb.append('0');
            }
            sb.append(code);
            if (separator != null && i < bytes.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static byte[] asByteArray(String hex) {
        byte[] bts = new byte[(hex.length() / 2)];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hex.substring(i * 2, (i * 2) + 2), 16);
        }
        return bts;
    }

    public static final int makeIntFromByte4(byte[] b) {
        return makeIntFromByte4(b, 0);
    }

    public static final int makeIntFromByte4(byte[] b, int offset) {
        return (b[offset] << AnyoMessage.CMD_REPORT_CHARGE_STOPPED) | ((b[offset + 1] & 255) << 16) | ((b[offset + 2] & 255) << 8) | (b[offset + 3] & 255);
    }

    public static final int makeIntFromByte2(byte[] b) {
        return makeIntFromByte2(b, 0);
    }

    public static final int makeIntFromByte2(byte[] b, int offset) {
        return ((b[offset] & 255) << 8) | (b[offset + 1] & 255);
    }

    public static final boolean isFlagSet(int flagSet, int testFlag) {
        return (flagSet & testFlag) > 0;
    }
}
