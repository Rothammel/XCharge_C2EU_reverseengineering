package org.apache.commons.lang3;

import android.support.p000v4.internal.view.SupportMenu;
import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import java.util.UUID;

public class Conversion {
    static final /* synthetic */ boolean $assertionsDisabled = (!Conversion.class.desiredAssertionStatus());
    private static final boolean[] FFFF = {false, false, false, false};
    private static final boolean[] FFFT = {false, false, false, true};
    private static final boolean[] FFTF = {false, false, true, false};
    private static final boolean[] FFTT = {false, false, true, true};
    private static final boolean[] FTFF = {false, true, false, false};
    private static final boolean[] FTFT = {false, true, false, true};
    private static final boolean[] FTTF = {false, true, true, false};
    private static final boolean[] FTTT = {false, true, true, true};
    private static final boolean[] TFFF = {true, false, false, false};
    private static final boolean[] TFFT = {true, false, false, true};
    private static final boolean[] TFTF = {true, false, true, false};
    private static final boolean[] TFTT = {true, false, true, true};
    private static final boolean[] TTFF = {true, true, false, false};
    private static final boolean[] TTFT = {true, true, false, true};
    private static final boolean[] TTTF = {true, true, true, false};
    private static final boolean[] TTTT = {true, true, true, true};

    public static int hexDigitToInt(char hexDigit) {
        int digit = Character.digit(hexDigit, 16);
        if (digit >= 0) {
            return digit;
        }
        throw new IllegalArgumentException("Cannot interpret '" + hexDigit + "' as a hexadecimal digit");
    }

    public static int hexDigitMsb0ToInt(char hexDigit) {
        switch (hexDigit) {
            case '0':
                return 0;
            case '1':
                return 8;
            case '2':
                return 4;
            case '3':
                return 12;
            case '4':
                return 2;
            case '5':
                return 10;
            case '6':
                return 6;
            case '7':
                return 14;
            case '8':
                return 1;
            case '9':
                return 9;
            case 'A':
            case 'a':
                return 5;
            case 'B':
            case 'b':
                return 13;
            case 'C':
            case 'c':
                return 3;
            case 'D':
            case 'd':
                return 11;
            case 'E':
            case 'e':
                return 7;
            case 'F':
            case 'f':
                return 15;
            default:
                throw new IllegalArgumentException("Cannot interpret '" + hexDigit + "' as a hexadecimal digit");
        }
    }

    public static boolean[] hexDigitToBinary(char hexDigit) {
        switch (hexDigit) {
            case '0':
                return (boolean[]) FFFF.clone();
            case '1':
                return (boolean[]) TFFF.clone();
            case '2':
                return (boolean[]) FTFF.clone();
            case '3':
                return (boolean[]) TTFF.clone();
            case '4':
                return (boolean[]) FFTF.clone();
            case '5':
                return (boolean[]) TFTF.clone();
            case '6':
                return (boolean[]) FTTF.clone();
            case '7':
                return (boolean[]) TTTF.clone();
            case '8':
                return (boolean[]) FFFT.clone();
            case '9':
                return (boolean[]) TFFT.clone();
            case 'A':
            case 'a':
                return (boolean[]) FTFT.clone();
            case 'B':
            case 'b':
                return (boolean[]) TTFT.clone();
            case 'C':
            case 'c':
                return (boolean[]) FFTT.clone();
            case 'D':
            case 'd':
                return (boolean[]) TFTT.clone();
            case 'E':
            case 'e':
                return (boolean[]) FTTT.clone();
            case 'F':
            case 'f':
                return (boolean[]) TTTT.clone();
            default:
                throw new IllegalArgumentException("Cannot interpret '" + hexDigit + "' as a hexadecimal digit");
        }
    }

    public static boolean[] hexDigitMsb0ToBinary(char hexDigit) {
        switch (hexDigit) {
            case '0':
                return (boolean[]) FFFF.clone();
            case '1':
                return (boolean[]) FFFT.clone();
            case '2':
                return (boolean[]) FFTF.clone();
            case '3':
                return (boolean[]) FFTT.clone();
            case '4':
                return (boolean[]) FTFF.clone();
            case '5':
                return (boolean[]) FTFT.clone();
            case '6':
                return (boolean[]) FTTF.clone();
            case '7':
                return (boolean[]) FTTT.clone();
            case '8':
                return (boolean[]) TFFF.clone();
            case '9':
                return (boolean[]) TFFT.clone();
            case 'A':
            case 'a':
                return (boolean[]) TFTF.clone();
            case 'B':
            case 'b':
                return (boolean[]) TFTT.clone();
            case 'C':
            case 'c':
                return (boolean[]) TTFF.clone();
            case 'D':
            case 'd':
                return (boolean[]) TTFT.clone();
            case 'E':
            case 'e':
                return (boolean[]) TTTF.clone();
            case 'F':
            case 'f':
                return (boolean[]) TTTT.clone();
            default:
                throw new IllegalArgumentException("Cannot interpret '" + hexDigit + "' as a hexadecimal digit");
        }
    }

    public static char binaryToHexDigit(boolean[] src) {
        return binaryToHexDigit(src, 0);
    }

    public static char binaryToHexDigit(boolean[] src, int srcPos) {
        if (src.length == 0) {
            throw new IllegalArgumentException("Cannot convert an empty array.");
        } else if (src.length <= srcPos + 3 || !src[srcPos + 3]) {
            return (src.length <= srcPos + 2 || !src[srcPos + 2]) ? (src.length <= srcPos + 1 || !src[srcPos + 1]) ? src[srcPos] ? '1' : '0' : src[srcPos] ? '3' : '2' : (src.length <= srcPos + 1 || !src[srcPos + 1]) ? src[srcPos] ? '5' : '4' : src[srcPos] ? '7' : '6';
        } else {
            if (src.length <= srcPos + 2 || !src[srcPos + 2]) {
                return (src.length <= srcPos + 1 || !src[srcPos + 1]) ? src[srcPos] ? '9' : '8' : src[srcPos] ? 'b' : 'a';
            }
            if (src.length <= srcPos + 1 || !src[srcPos + 1]) {
                return src[srcPos] ? 'd' : 'c';
            }
            if (src[srcPos]) {
                return 'f';
            }
            return 'e';
        }
    }

    public static char binaryToHexDigitMsb0_4bits(boolean[] src) {
        return binaryToHexDigitMsb0_4bits(src, 0);
    }

    public static char binaryToHexDigitMsb0_4bits(boolean[] src, int srcPos) {
        if (src.length > 8) {
            throw new IllegalArgumentException("src.length>8: src.length=" + src.length);
        } else if (src.length - srcPos < 4) {
            throw new IllegalArgumentException("src.length-srcPos<4: src.length=" + src.length + ", srcPos=" + srcPos);
        } else if (!src[srcPos + 3]) {
            return src[srcPos + 2] ? src[srcPos + 1] ? src[srcPos] ? 'e' : '6' : src[srcPos] ? 'a' : '2' : src[srcPos + 1] ? src[srcPos] ? 'c' : '4' : src[srcPos] ? '8' : '0';
        } else {
            if (!src[srcPos + 2]) {
                return src[srcPos + 1] ? src[srcPos] ? 'd' : '5' : src[srcPos] ? '9' : '1';
            }
            if (!src[srcPos + 1]) {
                return src[srcPos] ? 'b' : '3';
            }
            if (src[srcPos]) {
                return 'f';
            }
            return '7';
        }
    }

    public static char binaryBeMsb0ToHexDigit(boolean[] src) {
        return binaryBeMsb0ToHexDigit(src, 0);
    }

    public static char binaryBeMsb0ToHexDigit(boolean[] src, int srcPos) {
        if (src.length == 0) {
            throw new IllegalArgumentException("Cannot convert an empty array.");
        }
        int beSrcPos = (src.length - 1) - srcPos;
        int srcLen = Math.min(4, beSrcPos + 1);
        boolean[] paddedSrc = new boolean[4];
        System.arraycopy(src, (beSrcPos + 1) - srcLen, paddedSrc, 4 - srcLen, srcLen);
        boolean[] src2 = paddedSrc;
        if (!src2[0]) {
            return (src2.length <= 1 || !src2[1]) ? (src2.length <= 2 || !src2[2]) ? (src2.length <= 3 || !src2[3]) ? '0' : '1' : (src2.length <= 3 || !src2[3]) ? '2' : '3' : (src2.length <= 2 || !src2[2]) ? (src2.length <= 3 || !src2[3]) ? '4' : '5' : (src2.length <= 3 || !src2[3]) ? '6' : '7';
        }
        if (src2.length <= 1 || !src2[1]) {
            return (src2.length <= 2 || !src2[2]) ? (src2.length <= 3 || !src2[3]) ? '8' : '9' : (src2.length <= 3 || !src2[3]) ? 'a' : 'b';
        }
        if (src2.length <= 2 || !src2[2]) {
            return (src2.length <= 3 || !src2[3]) ? 'c' : 'd';
        }
        if (src2.length <= 3 || !src2[3]) {
            return 'e';
        }
        return 'f';
    }

    public static char intToHexDigit(int nibble) {
        char c = Character.forDigit(nibble, 16);
        if (c != 0) {
            return c;
        }
        throw new IllegalArgumentException("nibble value not between 0 and 15: " + nibble);
    }

    public static char intToHexDigitMsb0(int nibble) {
        switch (nibble) {
            case 0:
                return '0';
            case 1:
                return '8';
            case 2:
                return '4';
            case 3:
                return 'c';
            case 4:
                return '2';
            case 5:
                return 'a';
            case 6:
                return '6';
            case 7:
                return 'e';
            case 8:
                return '1';
            case 9:
                return '9';
            case 10:
                return '5';
            case PortRuntimeData.STATUS_EX_11:
                return 'd';
            case PortRuntimeData.STATUS_EX_12:
                return '3';
            case 13:
                return 'b';
            case 14:
                return '7';
            case 15:
                return 'f';
            default:
                throw new IllegalArgumentException("nibble value not between 0 and 15: " + nibble);
        }
    }

    public static long intArrayToLong(int[] src, int srcPos, long dstInit, int dstPos, int nInts) {
        if ((src.length == 0 && srcPos == 0) || nInts == 0) {
            return dstInit;
        }
        if (((nInts - 1) * 32) + dstPos >= 64) {
            throw new IllegalArgumentException("(nInts-1)*32+dstPos is greather or equal to than 64");
        }
        long out = dstInit;
        for (int i = 0; i < nInts; i++) {
            int shift = (i * 32) + dstPos;
            out = ((-1 ^ (XMSZHead.ID_BROADCAST << shift)) & out) | ((XMSZHead.ID_BROADCAST & ((long) src[i + srcPos])) << shift);
        }
        return out;
    }

    public static long shortArrayToLong(short[] src, int srcPos, long dstInit, int dstPos, int nShorts) {
        if ((src.length == 0 && srcPos == 0) || nShorts == 0) {
            return dstInit;
        }
        if (((nShorts - 1) * 16) + dstPos >= 64) {
            throw new IllegalArgumentException("(nShorts-1)*16+dstPos is greather or equal to than 64");
        }
        long out = dstInit;
        for (int i = 0; i < nShorts; i++) {
            int shift = (i * 16) + dstPos;
            out = ((-1 ^ (65535 << shift)) & out) | ((65535 & ((long) src[i + srcPos])) << shift);
        }
        return out;
    }

    public static int shortArrayToInt(short[] src, int srcPos, int dstInit, int dstPos, int nShorts) {
        if ((src.length == 0 && srcPos == 0) || nShorts == 0) {
            return dstInit;
        }
        if (((nShorts - 1) * 16) + dstPos >= 32) {
            throw new IllegalArgumentException("(nShorts-1)*16+dstPos is greather or equal to than 32");
        }
        int out = dstInit;
        for (int i = 0; i < nShorts; i++) {
            int shift = (i * 16) + dstPos;
            out = (((SupportMenu.USER_MASK << shift) ^ -1) & out) | ((src[i + srcPos] & 65535) << shift);
        }
        return out;
    }

    public static long byteArrayToLong(byte[] src, int srcPos, long dstInit, int dstPos, int nBytes) {
        if ((src.length == 0 && srcPos == 0) || nBytes == 0) {
            return dstInit;
        }
        if (((nBytes - 1) * 8) + dstPos >= 64) {
            throw new IllegalArgumentException("(nBytes-1)*8+dstPos is greather or equal to than 64");
        }
        long out = dstInit;
        for (int i = 0; i < nBytes; i++) {
            int shift = (i * 8) + dstPos;
            out = ((-1 ^ (255 << shift)) & out) | ((255 & ((long) src[i + srcPos])) << shift);
        }
        return out;
    }

    public static int byteArrayToInt(byte[] src, int srcPos, int dstInit, int dstPos, int nBytes) {
        if ((src.length == 0 && srcPos == 0) || nBytes == 0) {
            return dstInit;
        }
        if (((nBytes - 1) * 8) + dstPos >= 32) {
            throw new IllegalArgumentException("(nBytes-1)*8+dstPos is greather or equal to than 32");
        }
        int out = dstInit;
        for (int i = 0; i < nBytes; i++) {
            int shift = (i * 8) + dstPos;
            out = (((MotionEventCompat.ACTION_MASK << shift) ^ -1) & out) | ((src[i + srcPos] & 255) << shift);
        }
        return out;
    }

    public static short byteArrayToShort(byte[] src, int srcPos, short dstInit, int dstPos, int nBytes) {
        if ((src.length == 0 && srcPos == 0) || nBytes == 0) {
            return dstInit;
        }
        if (((nBytes - 1) * 8) + dstPos >= 16) {
            throw new IllegalArgumentException("(nBytes-1)*8+dstPos is greather or equal to than 16");
        }
        short out = dstInit;
        for (int i = 0; i < nBytes; i++) {
            int shift = (i * 8) + dstPos;
            out = (short) ((((MotionEventCompat.ACTION_MASK << shift) ^ -1) & out) | ((src[i + srcPos] & 255) << shift));
        }
        return out;
    }

    public static long hexToLong(String src, int srcPos, long dstInit, int dstPos, int nHex) {
        if (nHex == 0) {
            return dstInit;
        }
        if (((nHex - 1) * 4) + dstPos >= 64) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 64");
        }
        long out = dstInit;
        for (int i = 0; i < nHex; i++) {
            int shift = (i * 4) + dstPos;
            out = ((-1 ^ (15 << shift)) & out) | ((15 & ((long) hexDigitToInt(src.charAt(i + srcPos)))) << shift);
        }
        return out;
    }

    public static int hexToInt(String src, int srcPos, int dstInit, int dstPos, int nHex) {
        if (nHex == 0) {
            return dstInit;
        }
        if (((nHex - 1) * 4) + dstPos >= 32) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 32");
        }
        int out = dstInit;
        for (int i = 0; i < nHex; i++) {
            int shift = (i * 4) + dstPos;
            out = (((15 << shift) ^ -1) & out) | ((hexDigitToInt(src.charAt(i + srcPos)) & 15) << shift);
        }
        return out;
    }

    public static short hexToShort(String src, int srcPos, short dstInit, int dstPos, int nHex) {
        if (nHex == 0) {
            return dstInit;
        }
        if (((nHex - 1) * 4) + dstPos >= 16) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 16");
        }
        short out = dstInit;
        for (int i = 0; i < nHex; i++) {
            int shift = (i * 4) + dstPos;
            out = (short) ((((15 << shift) ^ -1) & out) | ((hexDigitToInt(src.charAt(i + srcPos)) & 15) << shift));
        }
        return out;
    }

    public static byte hexToByte(String src, int srcPos, byte dstInit, int dstPos, int nHex) {
        if (nHex == 0) {
            return dstInit;
        }
        if (((nHex - 1) * 4) + dstPos >= 8) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 8");
        }
        byte out = dstInit;
        for (int i = 0; i < nHex; i++) {
            int shift = (i * 4) + dstPos;
            out = (byte) ((((15 << shift) ^ -1) & out) | ((hexDigitToInt(src.charAt(i + srcPos)) & 15) << shift));
        }
        return out;
    }

    public static long binaryToLong(boolean[] src, int srcPos, long dstInit, int dstPos, int nBools) {
        if ((src.length == 0 && srcPos == 0) || nBools == 0) {
            return dstInit;
        }
        if ((nBools - 1) + dstPos >= 64) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 64");
        }
        long out = dstInit;
        for (int i = 0; i < nBools; i++) {
            int shift = i + dstPos;
            out = ((-1 ^ (1 << shift)) & out) | ((src[i + srcPos] ? 1 : 0) << shift);
        }
        return out;
    }

    public static int binaryToInt(boolean[] src, int srcPos, int dstInit, int dstPos, int nBools) {
        if ((src.length == 0 && srcPos == 0) || nBools == 0) {
            return dstInit;
        }
        if ((nBools - 1) + dstPos >= 32) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 32");
        }
        int out = dstInit;
        for (int i = 0; i < nBools; i++) {
            int shift = i + dstPos;
            out = (((1 << shift) ^ -1) & out) | ((src[i + srcPos] ? 1 : 0) << shift);
        }
        return out;
    }

    public static short binaryToShort(boolean[] src, int srcPos, short dstInit, int dstPos, int nBools) {
        if ((src.length == 0 && srcPos == 0) || nBools == 0) {
            return dstInit;
        }
        if ((nBools - 1) + dstPos >= 16) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 16");
        }
        short out = dstInit;
        for (int i = 0; i < nBools; i++) {
            int shift = i + dstPos;
            out = (short) ((((1 << shift) ^ -1) & out) | ((src[i + srcPos] ? 1 : 0) << shift));
        }
        return out;
    }

    public static byte binaryToByte(boolean[] src, int srcPos, byte dstInit, int dstPos, int nBools) {
        if ((src.length == 0 && srcPos == 0) || nBools == 0) {
            return dstInit;
        }
        if ((nBools - 1) + dstPos >= 8) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 8");
        }
        byte out = dstInit;
        for (int i = 0; i < nBools; i++) {
            int shift = i + dstPos;
            out = (byte) ((((1 << shift) ^ -1) & out) | ((src[i + srcPos] ? 1 : 0) << shift));
        }
        return out;
    }

    public static int[] longToIntArray(long src, int srcPos, int[] dst, int dstPos, int nInts) {
        if (nInts != 0) {
            if (((nInts - 1) * 32) + srcPos >= 64) {
                throw new IllegalArgumentException("(nInts-1)*32+srcPos is greather or equal to than 64");
            }
            for (int i = 0; i < nInts; i++) {
                dst[dstPos + i] = (int) (-1 & (src >> ((i * 32) + srcPos)));
            }
        }
        return dst;
    }

    public static short[] longToShortArray(long src, int srcPos, short[] dst, int dstPos, int nShorts) {
        if (nShorts != 0) {
            if (((nShorts - 1) * 16) + srcPos >= 64) {
                throw new IllegalArgumentException("(nShorts-1)*16+srcPos is greather or equal to than 64");
            }
            for (int i = 0; i < nShorts; i++) {
                dst[dstPos + i] = (short) ((int) (65535 & (src >> ((i * 16) + srcPos))));
            }
        }
        return dst;
    }

    public static short[] intToShortArray(int src, int srcPos, short[] dst, int dstPos, int nShorts) {
        if (nShorts != 0) {
            if (((nShorts - 1) * 16) + srcPos >= 32) {
                throw new IllegalArgumentException("(nShorts-1)*16+srcPos is greather or equal to than 32");
            }
            for (int i = 0; i < nShorts; i++) {
                dst[dstPos + i] = (short) (65535 & (src >> ((i * 16) + srcPos)));
            }
        }
        return dst;
    }

    public static byte[] longToByteArray(long src, int srcPos, byte[] dst, int dstPos, int nBytes) {
        if (nBytes != 0) {
            if (((nBytes - 1) * 8) + srcPos >= 64) {
                throw new IllegalArgumentException("(nBytes-1)*8+srcPos is greather or equal to than 64");
            }
            for (int i = 0; i < nBytes; i++) {
                dst[dstPos + i] = (byte) ((int) (255 & (src >> ((i * 8) + srcPos))));
            }
        }
        return dst;
    }

    public static byte[] intToByteArray(int src, int srcPos, byte[] dst, int dstPos, int nBytes) {
        if (nBytes != 0) {
            if (((nBytes - 1) * 8) + srcPos >= 32) {
                throw new IllegalArgumentException("(nBytes-1)*8+srcPos is greather or equal to than 32");
            }
            for (int i = 0; i < nBytes; i++) {
                dst[dstPos + i] = (byte) ((src >> ((i * 8) + srcPos)) & MotionEventCompat.ACTION_MASK);
            }
        }
        return dst;
    }

    public static byte[] shortToByteArray(short src, int srcPos, byte[] dst, int dstPos, int nBytes) {
        if (nBytes != 0) {
            if (((nBytes - 1) * 8) + srcPos >= 16) {
                throw new IllegalArgumentException("(nBytes-1)*8+srcPos is greather or equal to than 16");
            }
            for (int i = 0; i < nBytes; i++) {
                dst[dstPos + i] = (byte) ((src >> ((i * 8) + srcPos)) & MotionEventCompat.ACTION_MASK);
            }
        }
        return dst;
    }

    public static String longToHex(long src, int srcPos, String dstInit, int dstPos, int nHexs) {
        if (nHexs == 0) {
            return dstInit;
        }
        if (((nHexs - 1) * 4) + srcPos >= 64) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 64");
        }
        StringBuilder sb = new StringBuilder(dstInit);
        int append = sb.length();
        for (int i = 0; i < nHexs; i++) {
            int bits = (int) (15 & (src >> ((i * 4) + srcPos)));
            if (dstPos + i == append) {
                append++;
                sb.append(intToHexDigit(bits));
            } else {
                sb.setCharAt(dstPos + i, intToHexDigit(bits));
            }
        }
        return sb.toString();
    }

    public static String intToHex(int src, int srcPos, String dstInit, int dstPos, int nHexs) {
        if (nHexs == 0) {
            return dstInit;
        }
        if (((nHexs - 1) * 4) + srcPos >= 32) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 32");
        }
        StringBuilder sb = new StringBuilder(dstInit);
        int append = sb.length();
        for (int i = 0; i < nHexs; i++) {
            int bits = (src >> ((i * 4) + srcPos)) & 15;
            if (dstPos + i == append) {
                append++;
                sb.append(intToHexDigit(bits));
            } else {
                sb.setCharAt(dstPos + i, intToHexDigit(bits));
            }
        }
        return sb.toString();
    }

    public static String shortToHex(short src, int srcPos, String dstInit, int dstPos, int nHexs) {
        if (nHexs == 0) {
            return dstInit;
        }
        if (((nHexs - 1) * 4) + srcPos >= 16) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 16");
        }
        StringBuilder sb = new StringBuilder(dstInit);
        int append = sb.length();
        for (int i = 0; i < nHexs; i++) {
            int bits = (src >> ((i * 4) + srcPos)) & 15;
            if (dstPos + i == append) {
                append++;
                sb.append(intToHexDigit(bits));
            } else {
                sb.setCharAt(dstPos + i, intToHexDigit(bits));
            }
        }
        return sb.toString();
    }

    public static String byteToHex(byte src, int srcPos, String dstInit, int dstPos, int nHexs) {
        if (nHexs == 0) {
            return dstInit;
        }
        if (((nHexs - 1) * 4) + srcPos >= 8) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 8");
        }
        StringBuilder sb = new StringBuilder(dstInit);
        int append = sb.length();
        for (int i = 0; i < nHexs; i++) {
            int bits = (src >> ((i * 4) + srcPos)) & 15;
            if (dstPos + i == append) {
                append++;
                sb.append(intToHexDigit(bits));
            } else {
                sb.setCharAt(dstPos + i, intToHexDigit(bits));
            }
        }
        return sb.toString();
    }

    public static boolean[] longToBinary(long src, int srcPos, boolean[] dst, int dstPos, int nBools) {
        if (nBools != 0) {
            if ((nBools - 1) + srcPos >= 64) {
                throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 64");
            }
            for (int i = 0; i < nBools; i++) {
                dst[dstPos + i] = (1 & (src >> (i + srcPos))) != 0;
            }
        }
        return dst;
    }

    public static boolean[] intToBinary(int src, int srcPos, boolean[] dst, int dstPos, int nBools) {
        if (nBools != 0) {
            if ((nBools - 1) + srcPos >= 32) {
                throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 32");
            }
            for (int i = 0; i < nBools; i++) {
                dst[dstPos + i] = ((src >> (i + srcPos)) & 1) != 0;
            }
        }
        return dst;
    }

    public static boolean[] shortToBinary(short src, int srcPos, boolean[] dst, int dstPos, int nBools) {
        if (nBools != 0) {
            if ((nBools - 1) + srcPos >= 16) {
                throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 16");
            } else if ($assertionsDisabled || nBools - 1 < 16 - srcPos) {
                for (int i = 0; i < nBools; i++) {
                    dst[dstPos + i] = ((src >> (i + srcPos)) & 1) != 0;
                }
            } else {
                throw new AssertionError();
            }
        }
        return dst;
    }

    public static boolean[] byteToBinary(byte src, int srcPos, boolean[] dst, int dstPos, int nBools) {
        if (nBools != 0) {
            if ((nBools - 1) + srcPos >= 8) {
                throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 8");
            }
            for (int i = 0; i < nBools; i++) {
                dst[dstPos + i] = ((src >> (i + srcPos)) & 1) != 0;
            }
        }
        return dst;
    }

    public static byte[] uuidToByteArray(UUID src, byte[] dst, int dstPos, int nBytes) {
        int i;
        if (nBytes != 0) {
            if (nBytes > 16) {
                throw new IllegalArgumentException("nBytes is greather than 16");
            }
            long mostSignificantBits = src.getMostSignificantBits();
            if (nBytes > 8) {
                i = 8;
            } else {
                i = nBytes;
            }
            longToByteArray(mostSignificantBits, 0, dst, dstPos, i);
            if (nBytes >= 8) {
                longToByteArray(src.getLeastSignificantBits(), 0, dst, dstPos + 8, nBytes - 8);
            }
        }
        return dst;
    }

    public static UUID byteArrayToUuid(byte[] src, int srcPos) {
        if (src.length - srcPos < 16) {
            throw new IllegalArgumentException("Need at least 16 bytes for UUID");
        }
        return new UUID(byteArrayToLong(src, srcPos, 0, 0, 8), byteArrayToLong(src, srcPos + 8, 0, 0, 8));
    }
}
