package org.java_websocket.util;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.mina.proxy.handlers.socks.SocksProxyConstants;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

/* loaded from: classes.dex */
public class Base64 {
    static final /* synthetic */ boolean $assertionsDisabled;
    public static final int DECODE = 0;
    public static final int DONT_GUNZIP = 4;
    public static final int DO_BREAK_LINES = 8;
    public static final int ENCODE = 1;
    private static final byte EQUALS_SIGN = 61;
    private static final byte EQUALS_SIGN_ENC = -1;
    public static final int GZIP = 2;
    private static final int MAX_LINE_LENGTH = 76;
    private static final byte NEW_LINE = 10;
    public static final int NO_OPTIONS = 0;
    public static final int ORDERED = 32;
    private static final String PREFERRED_ENCODING = "US-ASCII";
    public static final int URL_SAFE = 16;
    private static final byte WHITE_SPACE_ENC = -5;
    private static final byte[] _ORDERED_ALPHABET;
    private static final byte[] _ORDERED_DECODABET;
    private static final byte[] _STANDARD_ALPHABET;
    private static final byte[] _STANDARD_DECODABET;
    private static final byte[] _URL_SAFE_ALPHABET;
    private static final byte[] _URL_SAFE_DECODABET;

    static {
        $assertionsDisabled = !Base64.class.desiredAssertionStatus();
        _STANDARD_ALPHABET = new byte[]{AnyoMessage.CMD_UPDATE_CHARGE_SETTING, AnyoMessage.CMD_QUERY_DEVICE_FAULT, AnyoMessage.CMD_QUERY_BATTERY_CHARGE_INFO, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, AnyoMessage.STATUS_CODE_INTERNAL_ERROR, 81, AnyoMessage.CMD_RESERVE_PORT, AnyoMessage.CMD_CANCEL_RESERVE_PORT, 84, 85, 86, 87, 88, 89, SocksProxyConstants.V4_REPLY_REQUEST_GRANTED, 97, 98, 99, 100, 101, 102, 103, AnyoMessage.START_CODE_REQUEST, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, 43, 47};
        _STANDARD_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, AnyoMessage.CMD_START_UPGRADE, -9, -9, -9, 63, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, AnyoMessage.CMD_SET_TIMING_CHARGE, AnyoMessage.CMD_CANCEL_TIMING_CHARGE, AnyoMessage.CMD_START_CHARGE, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, MqttWireMessage.MESSAGE_TYPE_UNSUBACK, 12, MqttWireMessage.MESSAGE_TYPE_PINGRESP, MqttWireMessage.MESSAGE_TYPE_DISCONNECT, HeartBeatRequest.PORT_STATUS_FAULT, 16, 17, 18, 19, 20, 21, 22, 23, AnyoMessage.CMD_REPORT_CHARGE_STOPPED, AnyoMessage.CMD_REPORT_EVENT, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, AnyoMessage.CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE, AnyoMessage.CMD_REPORT_NETWORK_INFO, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
        _URL_SAFE_ALPHABET = new byte[]{AnyoMessage.CMD_UPDATE_CHARGE_SETTING, AnyoMessage.CMD_QUERY_DEVICE_FAULT, AnyoMessage.CMD_QUERY_BATTERY_CHARGE_INFO, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, AnyoMessage.STATUS_CODE_INTERNAL_ERROR, 81, AnyoMessage.CMD_RESERVE_PORT, AnyoMessage.CMD_CANCEL_RESERVE_PORT, 84, 85, 86, 87, 88, 89, SocksProxyConstants.V4_REPLY_REQUEST_GRANTED, 97, 98, 99, 100, 101, 102, 103, AnyoMessage.START_CODE_REQUEST, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, 45, 95};
        _URL_SAFE_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, AnyoMessage.CMD_START_UPGRADE, -9, -9, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, AnyoMessage.CMD_SET_TIMING_CHARGE, AnyoMessage.CMD_CANCEL_TIMING_CHARGE, AnyoMessage.CMD_START_CHARGE, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, MqttWireMessage.MESSAGE_TYPE_UNSUBACK, 12, MqttWireMessage.MESSAGE_TYPE_PINGRESP, MqttWireMessage.MESSAGE_TYPE_DISCONNECT, HeartBeatRequest.PORT_STATUS_FAULT, 16, 17, 18, 19, 20, 21, 22, 23, AnyoMessage.CMD_REPORT_CHARGE_STOPPED, AnyoMessage.CMD_REPORT_EVENT, -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, 32, AnyoMessage.CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE, AnyoMessage.CMD_REPORT_NETWORK_INFO, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
        _ORDERED_ALPHABET = new byte[]{45, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, AnyoMessage.CMD_QUERY_DEVICE_FAULT, AnyoMessage.CMD_QUERY_BATTERY_CHARGE_INFO, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, AnyoMessage.STATUS_CODE_INTERNAL_ERROR, 81, AnyoMessage.CMD_RESERVE_PORT, AnyoMessage.CMD_CANCEL_RESERVE_PORT, 84, 85, 86, 87, 88, 89, SocksProxyConstants.V4_REPLY_REQUEST_GRANTED, 95, 97, 98, 99, 100, 101, 102, 103, AnyoMessage.START_CODE_REQUEST, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};
        _ORDERED_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 0, -9, -9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -9, -9, -9, -1, -9, -9, -9, MqttWireMessage.MESSAGE_TYPE_UNSUBACK, 12, MqttWireMessage.MESSAGE_TYPE_PINGRESP, MqttWireMessage.MESSAGE_TYPE_DISCONNECT, HeartBeatRequest.PORT_STATUS_FAULT, 16, 17, 18, 19, 20, 21, 22, 23, AnyoMessage.CMD_REPORT_CHARGE_STOPPED, AnyoMessage.CMD_REPORT_EVENT, 26, 27, 28, 29, 30, 31, 32, AnyoMessage.CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE, AnyoMessage.CMD_REPORT_NETWORK_INFO, 35, 36, -9, -9, -9, -9, 37, -9, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, AnyoMessage.CMD_SET_TIMING_CHARGE, AnyoMessage.CMD_CANCEL_TIMING_CHARGE, AnyoMessage.CMD_START_CHARGE, 61, AnyoMessage.CMD_START_UPGRADE, 63, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
    }

    private static final byte[] getAlphabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_ALPHABET;
        }
        if ((options & 32) == 32) {
            return _ORDERED_ALPHABET;
        }
        return _STANDARD_ALPHABET;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final byte[] getDecodabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_DECODABET;
        }
        if ((options & 32) == 32) {
            return _ORDERED_DECODABET;
        }
        return _STANDARD_DECODABET;
    }

    private Base64() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options) {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
        return b4;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options) {
        byte[] ALPHABET = getAlphabet(options);
        int inBuff = (numSigBytes > 1 ? (source[srcOffset + 1] << AnyoMessage.CMD_REPORT_CHARGE_STOPPED) >>> 16 : 0) | (numSigBytes > 0 ? (source[srcOffset] << AnyoMessage.CMD_REPORT_CHARGE_STOPPED) >>> 8 : 0) | (numSigBytes > 2 ? (source[srcOffset + 2] << AnyoMessage.CMD_REPORT_CHARGE_STOPPED) >>> 24 : 0);
        switch (numSigBytes) {
            case 1:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = 61;
                destination[destOffset + 3] = 61;
                break;
            case 2:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = 61;
                break;
            case 3:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = ALPHABET[inBuff & 63];
                break;
        }
        return destination;
    }

    public static String encodeBytes(byte[] source) {
        String encoded = null;
        try {
            encoded = encodeBytes(source, 0, source.length, 0);
        } catch (IOException ex) {
            if (!$assertionsDisabled) {
                throw new AssertionError(ex.getMessage());
            }
        }
        if ($assertionsDisabled || encoded != null) {
            return encoded;
        }
        throw new AssertionError();
    }

    public static String encodeBytes(byte[] source, int off, int len, int options) throws IOException {
        byte[] encoded = encodeBytesToBytes(source, off, len, options);
        try {
            return new String(encoded, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return new String(encoded);
        }
    }

    public static byte[] encodeBytesToBytes(byte[] source, int off, int len, int options) throws IOException {
        OutputStream b64os;
        GZIPOutputStream gzos;
        if (source == null) {
            throw new IllegalArgumentException("Cannot serialize a null array.");
        }
        if (off < 0) {
            throw new IllegalArgumentException("Cannot have negative offset: " + off);
        }
        if (len < 0) {
            throw new IllegalArgumentException("Cannot have length offset: " + len);
        }
        if (off + len > source.length) {
            throw new IllegalArgumentException(String.format("Cannot have offset of %d and length of %d with array of length %d", Integer.valueOf(off), Integer.valueOf(len), Integer.valueOf(source.length)));
        }
        if ((options & 2) == 0) {
            boolean breakLines = (options & 8) != 0;
            int encLen = ((len / 3) * 4) + (len % 3 > 0 ? 4 : 0);
            if (breakLines) {
                encLen += encLen / MAX_LINE_LENGTH;
            }
            byte[] outBuff = new byte[encLen];
            int d = 0;
            int e = 0;
            int len2 = len - 2;
            int lineLength = 0;
            while (d < len2) {
                encode3to4(source, d + off, 3, outBuff, e, options);
                lineLength += 4;
                if (breakLines && lineLength >= MAX_LINE_LENGTH) {
                    outBuff[e + 4] = 10;
                    e++;
                    lineLength = 0;
                }
                d += 3;
                e += 4;
            }
            if (d < len) {
                encode3to4(source, d + off, len - d, outBuff, e, options);
                e += 4;
            }
            if (e <= outBuff.length - 1) {
                byte[] finalOut = new byte[e];
                System.arraycopy(outBuff, 0, finalOut, 0, e);
                return finalOut;
            }
            return outBuff;
        }
        ByteArrayOutputStream baos = null;
        GZIPOutputStream gzos2 = null;
        OutputStream b64os2 = null;
        try {
            try {
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                try {
                    b64os = new OutputStream(baos2, options | 1);
                    try {
                        gzos = new GZIPOutputStream(b64os);
                    } catch (IOException e2) {
                        throw e2;
                    } catch (Throwable th) {
                        th = th;
                        b64os2 = b64os;
                        baos = baos2;
                    }
                } catch (IOException e3) {
                    throw e3;
                } catch (Throwable th2) {
                    th = th2;
                    baos = baos2;
                }
                try {
                    gzos.write(source, off, len);
                    gzos.close();
                    try {
                        gzos.close();
                    } catch (Exception e4) {
                    }
                    try {
                        b64os.close();
                    } catch (Exception e5) {
                    }
                    try {
                        baos2.close();
                    } catch (Exception e6) {
                    }
                    return baos2.toByteArray();
                } catch (IOException e7) {
                    throw e7;
                } catch (Throwable th3) {
                    th = th3;
                    b64os2 = b64os;
                    gzos2 = gzos;
                    baos = baos2;
                    try {
                        gzos2.close();
                    } catch (Exception e8) {
                    }
                    try {
                        b64os2.close();
                    } catch (Exception e9) {
                    }
                    try {
                        baos.close();
                    } catch (Exception e10) {
                    }
                    throw th;
                }
            } catch (IOException e11) {
                throw e11;
            }
        } catch (Throwable th4) {
            th = th4;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options) {
        if (source == null) {
            throw new IllegalArgumentException("Source array was null.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination array was null.");
        }
        if (srcOffset < 0 || srcOffset + 3 >= source.length) {
            throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and still process four bytes.", Integer.valueOf(source.length), Integer.valueOf(srcOffset)));
        }
        if (destOffset < 0 || destOffset + 2 >= destination.length) {
            throw new IllegalArgumentException(String.format("Destination array with length %d cannot have offset of %d and still store three bytes.", Integer.valueOf(destination.length), Integer.valueOf(destOffset)));
        }
        byte[] DECODABET = getDecodabet(options);
        if (source[srcOffset + 2] == 61) {
            destination[destOffset] = (byte) ((((DECODABET[source[srcOffset]] & 255) << 18) | ((DECODABET[source[srcOffset + 1]] & 255) << 12)) >>> 16);
            return 1;
        } else if (source[srcOffset + 3] == 61) {
            int outBuff = ((DECODABET[source[srcOffset]] & 255) << 18) | ((DECODABET[source[srcOffset + 1]] & 255) << 12) | ((DECODABET[source[srcOffset + 2]] & 255) << 6);
            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);
            return 2;
        } else {
            int outBuff2 = ((DECODABET[source[srcOffset]] & 255) << 18) | ((DECODABET[source[srcOffset + 1]] & 255) << 12) | ((DECODABET[source[srcOffset + 2]] & 255) << 6) | (DECODABET[source[srcOffset + 3]] & 255);
            destination[destOffset] = (byte) (outBuff2 >> 16);
            destination[destOffset + 1] = (byte) (outBuff2 >> 8);
            destination[destOffset + 2] = (byte) outBuff2;
            return 3;
        }
    }

    public static byte[] decode(byte[] source) throws IOException {
        return decode(source, 0, source.length, 0);
    }

    /* JADX WARN: Code restructure failed: missing block: B:28:0x009a, code lost:
        r7 = new byte[r9];
        java.lang.System.arraycopy(r8, 0, r7, 0, r9);
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:?, code lost:
        return r7;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static byte[] decode(byte[] r16, int r17, int r18, int r19) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 202
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: org.java_websocket.util.Base64.decode(byte[], int, int, int):byte[]");
    }

    public static byte[] decode(String s) throws IOException {
        return decode(s, 0);
    }

    public static byte[] decode(String s, int options) throws IOException {
        byte[] bytes;
        if (s == null) {
            throw new IllegalArgumentException("Input string was null.");
        }
        try {
            bytes = s.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            bytes = s.getBytes();
        }
        byte[] bytes2 = decode(bytes, 0, bytes.length, options);
        boolean dontGunzip = (options & 4) != 0;
        if (bytes2 != null && bytes2.length >= 4 && !dontGunzip) {
            int head = (bytes2[0] & 255) | ((bytes2[1] << 8) & 65280);
            if (35615 == head) {
                ByteArrayInputStream bais = null;
                GZIPInputStream gzis = null;
                ByteArrayOutputStream baos = null;
                byte[] buffer = new byte[2048];
                try {
                    try {
                        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                        try {
                            ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes2);
                            try {
                                GZIPInputStream gzis2 = new GZIPInputStream(bais2);
                                while (true) {
                                    try {
                                        int length = gzis2.read(buffer);
                                        if (length < 0) {
                                            break;
                                        }
                                        baos2.write(buffer, 0, length);
                                    } catch (IOException e2) {
                                        e = e2;
                                        baos = baos2;
                                        gzis = gzis2;
                                        bais = bais2;
                                        e.printStackTrace();
                                        try {
                                            baos.close();
                                        } catch (Exception e3) {
                                        }
                                        try {
                                            gzis.close();
                                        } catch (Exception e4) {
                                        }
                                        try {
                                            bais.close();
                                        } catch (Exception e5) {
                                        }
                                        return bytes2;
                                    } catch (Throwable th) {
                                        th = th;
                                        baos = baos2;
                                        gzis = gzis2;
                                        bais = bais2;
                                        try {
                                            baos.close();
                                        } catch (Exception e6) {
                                        }
                                        try {
                                            gzis.close();
                                        } catch (Exception e7) {
                                        }
                                        try {
                                            bais.close();
                                        } catch (Exception e8) {
                                        }
                                        throw th;
                                    }
                                }
                                bytes2 = baos2.toByteArray();
                                try {
                                    baos2.close();
                                } catch (Exception e9) {
                                }
                                try {
                                    gzis2.close();
                                } catch (Exception e10) {
                                }
                                try {
                                    bais2.close();
                                } catch (Exception e11) {
                                }
                            } catch (IOException e12) {
                                e = e12;
                                baos = baos2;
                                bais = bais2;
                            } catch (Throwable th2) {
                                th = th2;
                                baos = baos2;
                                bais = bais2;
                            }
                        } catch (IOException e13) {
                            e = e13;
                            baos = baos2;
                        } catch (Throwable th3) {
                            th = th3;
                            baos = baos2;
                        }
                    } catch (IOException e14) {
                        e = e14;
                    }
                } catch (Throwable th4) {
                    th = th4;
                }
            }
        }
        return bytes2;
    }

    public static byte[] decodeFromFile(String filename) throws IOException {
        InputStream bis = null;
        try {
            try {
                File file = new File(filename);
                int length = 0;
                if (file.length() > 2147483647L) {
                    throw new IOException("File is too big for this convenience method (" + file.length() + " bytes).");
                }
                byte[] buffer = new byte[(int) file.length()];
                InputStream bis2 = new InputStream(new BufferedInputStream(new FileInputStream(file)), 0);
                while (true) {
                    try {
                        int numBytes = bis2.read(buffer, length, 4096);
                        if (numBytes < 0) {
                            break;
                        }
                        length += numBytes;
                    } catch (IOException e) {
                        throw e;
                    } catch (Throwable th) {
                        th = th;
                        bis = bis2;
                        try {
                            bis.close();
                        } catch (Exception e2) {
                        }
                        throw th;
                    }
                }
                byte[] decodedData = new byte[length];
                System.arraycopy(buffer, 0, decodedData, 0, length);
                try {
                    bis2.close();
                } catch (Exception e3) {
                }
                return decodedData;
            } catch (IOException e4) {
                throw e4;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static String encodeFromFile(String filename) throws IOException {
        InputStream bis = null;
        try {
            try {
                File file = new File(filename);
                byte[] buffer = new byte[Math.max((int) ((file.length() * 1.4d) + 1.0d), 40)];
                int length = 0;
                InputStream bis2 = new InputStream(new BufferedInputStream(new FileInputStream(file)), 1);
                while (true) {
                    try {
                        int numBytes = bis2.read(buffer, length, 4096);
                        if (numBytes < 0) {
                            break;
                        }
                        length += numBytes;
                    } catch (IOException e) {
                        throw e;
                    } catch (Throwable th) {
                        th = th;
                        bis = bis2;
                        try {
                            bis.close();
                        } catch (Exception e2) {
                        }
                        throw th;
                    }
                }
                String encodedData = new String(buffer, 0, length, "US-ASCII");
                try {
                    bis2.close();
                } catch (Exception e3) {
                }
                return encodedData;
            } catch (IOException e4) {
                throw e4;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* loaded from: classes.dex */
    public static class InputStream extends FilterInputStream {
        private boolean breakLines;
        private byte[] buffer;
        private int bufferLength;
        private byte[] decodabet;
        private boolean encode;
        private int lineLength;
        private int numSigBytes;
        private int options;
        private int position;

        public InputStream(java.io.InputStream in) {
            this(in, 0);
        }

        public InputStream(java.io.InputStream in, int options) {
            super(in);
            this.options = options;
            this.breakLines = (options & 8) > 0;
            this.encode = (options & 1) > 0;
            this.bufferLength = this.encode ? 4 : 3;
            this.buffer = new byte[this.bufferLength];
            this.position = -1;
            this.lineLength = 0;
            this.decodabet = Base64.getDecodabet(options);
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read() throws IOException {
            int b;
            if (this.position < 0) {
                if (this.encode) {
                    byte[] b3 = new byte[3];
                    int numBinaryBytes = 0;
                    for (int i = 0; i < 3; i++) {
                        int b2 = this.in.read();
                        if (b2 < 0) {
                            break;
                        }
                        b3[i] = (byte) b2;
                        numBinaryBytes++;
                    }
                    if (numBinaryBytes <= 0) {
                        return -1;
                    }
                    Base64.encode3to4(b3, 0, numBinaryBytes, this.buffer, 0, this.options);
                    this.position = 0;
                    this.numSigBytes = 4;
                } else {
                    byte[] b4 = new byte[4];
                    int i2 = 0;
                    while (i2 < 4) {
                        do {
                            b = this.in.read();
                            if (b < 0) {
                                break;
                            }
                        } while (this.decodabet[b & TransportMediator.KEYCODE_MEDIA_PAUSE] <= -5);
                        if (b < 0) {
                            break;
                        }
                        b4[i2] = (byte) b;
                        i2++;
                    }
                    if (i2 == 4) {
                        this.numSigBytes = Base64.decode4to3(b4, 0, this.buffer, 0, this.options);
                        this.position = 0;
                    } else if (i2 == 0) {
                        return -1;
                    } else {
                        throw new IOException("Improperly padded Base64 input.");
                    }
                }
            }
            if (this.position >= 0) {
                if (this.position >= this.numSigBytes) {
                    return -1;
                }
                if (this.encode && this.breakLines && this.lineLength >= Base64.MAX_LINE_LENGTH) {
                    this.lineLength = 0;
                    return 10;
                }
                this.lineLength++;
                byte[] bArr = this.buffer;
                int i3 = this.position;
                this.position = i3 + 1;
                int b5 = bArr[i3];
                if (this.position >= this.bufferLength) {
                    this.position = -1;
                }
                return b5 & MotionEventCompat.ACTION_MASK;
            }
            throw new IOException("Error in Base64 code reading stream.");
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read(byte[] dest, int off, int len) throws IOException {
            int i = 0;
            while (i < len) {
                int b = read();
                if (b >= 0) {
                    dest[off + i] = (byte) b;
                    i++;
                } else if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }
            return i;
        }
    }

    /* loaded from: classes.dex */
    public static class OutputStream extends FilterOutputStream {
        private byte[] b4;
        private boolean breakLines;
        private byte[] buffer;
        private int bufferLength;
        private byte[] decodabet;
        private boolean encode;
        private int lineLength;
        private int options;
        private int position;
        private boolean suspendEncoding;

        public OutputStream(java.io.OutputStream out) {
            this(out, 1);
        }

        public OutputStream(java.io.OutputStream out, int options) {
            super(out);
            this.breakLines = (options & 8) != 0;
            this.encode = (options & 1) != 0;
            this.bufferLength = this.encode ? 3 : 4;
            this.buffer = new byte[this.bufferLength];
            this.position = 0;
            this.lineLength = 0;
            this.suspendEncoding = false;
            this.b4 = new byte[4];
            this.options = options;
            this.decodabet = Base64.getDecodabet(options);
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream
        public void write(int theByte) throws IOException {
            if (this.suspendEncoding) {
                this.out.write(theByte);
            } else if (this.encode) {
                byte[] bArr = this.buffer;
                int i = this.position;
                this.position = i + 1;
                bArr[i] = (byte) theByte;
                if (this.position >= this.bufferLength) {
                    this.out.write(Base64.encode3to4(this.b4, this.buffer, this.bufferLength, this.options));
                    this.lineLength += 4;
                    if (this.breakLines && this.lineLength >= Base64.MAX_LINE_LENGTH) {
                        this.out.write(10);
                        this.lineLength = 0;
                    }
                    this.position = 0;
                }
            } else if (this.decodabet[theByte & TransportMediator.KEYCODE_MEDIA_PAUSE] > -5) {
                byte[] bArr2 = this.buffer;
                int i2 = this.position;
                this.position = i2 + 1;
                bArr2[i2] = (byte) theByte;
                if (this.position >= this.bufferLength) {
                    int len = Base64.decode4to3(this.buffer, 0, this.b4, 0, this.options);
                    this.out.write(this.b4, 0, len);
                    this.position = 0;
                }
            } else if (this.decodabet[theByte & TransportMediator.KEYCODE_MEDIA_PAUSE] != -5) {
                throw new IOException("Invalid character in Base64 data.");
            }
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream
        public void write(byte[] theBytes, int off, int len) throws IOException {
            if (this.suspendEncoding) {
                this.out.write(theBytes, off, len);
                return;
            }
            for (int i = 0; i < len; i++) {
                write(theBytes[off + i]);
            }
        }

        public void flushBase64() throws IOException {
            if (this.position > 0) {
                if (this.encode) {
                    this.out.write(Base64.encode3to4(this.b4, this.buffer, this.position, this.options));
                    this.position = 0;
                    return;
                }
                throw new IOException("Base64 input not properly padded.");
            }
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            flushBase64();
            super.close();
            this.buffer = null;
            this.out = null;
        }

        public void suspendEncoding() throws IOException {
            flushBase64();
            this.suspendEncoding = true;
        }

        public void resumeEncoding() {
            this.suspendEncoding = false;
        }
    }
}
