package com.google.zxing.datamatrix.encoder;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import it.sauronsoftware.ftp4j.FTPCodes;

/* loaded from: classes.dex */
public final class ErrorCorrection {
    private static final int MODULO_VALUE = 301;
    private static final int[] FACTOR_SETS = {5, 7, 10, 11, 12, 14, 18, 20, 24, 28, 36, 42, 48, 56, 62, 68};
    private static final int[][] FACTORS = {new int[]{228, 48, 15, 111, 62}, new int[]{23, 68, 144, 134, 240, 92, 254}, new int[]{28, 24, 185, 166, 223, 248, 116, MotionEventCompat.ACTION_MASK, FTPCodes.RESTART_MARKER, 61}, new int[]{175, 138, 205, 12, 194, 168, 39, 245, 60, 97, 120}, new int[]{41, 153, 158, 91, 61, 42, 142, FTPCodes.FILE_STATUS, 97, 178, 100, 242}, new int[]{156, 97, 192, 252, 95, 9, 157, 119, 138, 45, 18, 186, 83, 185}, new int[]{83, 195, 100, 39, 188, 75, 66, 61, 241, FTPCodes.FILE_STATUS, 109, 129, 94, 254, FTPCodes.DATA_CONNECTION_OPEN, 48, 90, 188}, new int[]{15, 195, 244, 9, 233, 71, 168, 2, 188, 160, 153, 145, 253, 79, 108, 82, 27, 174, 186, 172}, new int[]{52, 190, 88, 205, 109, 39, 176, 21, 155, 197, 251, 223, 155, 21, 5, 172, 254, 124, 12, 181, 184, 96, 50, 193}, new int[]{FTPCodes.STATUS_MESSAGE, 231, 43, 97, 71, 96, 103, 174, 37, 151, 170, 53, 75, 34, 249, 121, 17, 138, FTPCodes.RESTART_MARKER, FTPCodes.FILE_STATUS, 141, 136, 120, 151, 233, 168, 93, MotionEventCompat.ACTION_MASK}, new int[]{245, TransportMediator.KEYCODE_MEDIA_PAUSE, 242, 218, TransportMediator.KEYCODE_MEDIA_RECORD, FTPCodes.FILE_ACTION_COMPLETED, 162, 181, 102, 120, 84, 179, FTPCodes.SERVICE_READY_FOR_NEW_USER, 251, 80, 182, 229, 18, 2, 4, 68, 33, 101, 137, 95, 119, 115, 44, 175, 184, 59, 25, FTPCodes.DATA_CONNECTION_OPEN, 98, 81, 112}, new int[]{77, 193, 137, 31, 19, 38, 22, 153, 247, 105, 122, 2, 245, 133, 242, 8, 175, 95, 100, 9, 167, 105, FTPCodes.HELP_MESSAGE, 111, 57, 121, 21, 1, 253, 57, 54, 101, 248, FTPCodes.SUPERFLOUS_COMMAND, 69, 50, FTPCodes.FILE_STATUS_OK, 177, FTPCodes.DATA_CONNECTION_CLOSING, 5, 9, 5}, new int[]{245, 132, 172, 223, 96, 32, 117, 22, 238, 133, 238, 231, 205, 188, 237, 87, 191, 106, 16, 147, 118, 23, 37, 90, 170, 205, 131, 88, 120, 100, 66, 138, 186, 240, 82, 44, 176, 87, 187, 147, 160, 175, 69, FTPCodes.FILE_STATUS, 92, 253, FTPCodes.DATA_CONNECTION_OPEN, 19}, new int[]{175, 9, 223, 238, 12, 17, FTPCodes.SERVICE_READY_FOR_NEW_USER, 208, 100, 29, 175, 170, FTPCodes.USER_LOGGED_IN, 192, FTPCodes.NAME_SYSTEM_TIME, 235, FTPCodes.FILE_STATUS_OK, 159, 36, 223, 38, 200, 132, 54, 228, 146, 218, 234, 117, 203, 29, 232, 144, 238, 22, FTPCodes.FILE_STATUS_OK, 201, 117, 62, 207, 164, 13, 137, 245, TransportMediator.KEYCODE_MEDIA_PAUSE, 67, 247, 28, 155, 43, 203, 107, 233, 53, 143, 46}, new int[]{242, 93, 169, 50, 144, 210, 39, 118, FTPCodes.SUPERFLOUS_COMMAND, 188, 201, 189, 143, 108, 196, 37, 185, 112, 134, FTPCodes.USER_LOGGED_IN, 245, 63, 197, 190, FTPCodes.FILE_ACTION_COMPLETED, 106, 185, FTPCodes.SERVICE_CLOSING_CONTROL_CONNECTION, 175, 64, 114, 71, 161, 44, 147, 6, 27, 218, 51, 63, 87, 10, 40, TransportMediator.KEYCODE_MEDIA_RECORD, 188, 17, 163, 31, 176, 170, 4, 107, 232, 7, 94, 166, 224, 124, 86, 47, 11, 204}, new int[]{FTPCodes.SERVICE_READY_FOR_NEW_USER, 228, 173, 89, 251, 149, 159, 56, 89, 33, 147, 244, 154, 36, 73, TransportMediator.KEYCODE_MEDIA_PAUSE, FTPCodes.FILE_STATUS, 136, 248, 180, 234, 197, 158, 177, 68, 122, 93, FTPCodes.FILE_STATUS, 15, 160, FTPCodes.ENTER_PASSIVE_MODE, 236, 66, 139, 153, 185, FTPCodes.SUPERFLOUS_COMMAND, 167, 179, 25, FTPCodes.SERVICE_READY_FOR_NEW_USER, 232, 96, 210, 231, 136, 223, 239, 181, 241, 59, 52, 172, 25, 49, 232, FTPCodes.STATUS_MESSAGE, 189, 64, 54, 108, 153, 132, 63, 96, 103, 82, 186}};
    private static final int[] LOG = new int[256];
    private static final int[] ALOG = new int[MotionEventCompat.ACTION_MASK];

    static {
        int p = 1;
        for (int i = 0; i < 255; i++) {
            ALOG[i] = p;
            LOG[p] = i;
            p *= 2;
            if (p >= 256) {
                p ^= MODULO_VALUE;
            }
        }
    }

    private ErrorCorrection() {
    }

    public static String encodeECC200(String codewords, SymbolInfo symbolInfo) {
        if (codewords.length() != symbolInfo.getDataCapacity()) {
            throw new IllegalArgumentException("The number of codewords does not match the selected symbol");
        }
        StringBuilder sb = new StringBuilder(symbolInfo.getDataCapacity() + symbolInfo.getErrorCodewords());
        sb.append(codewords);
        int blockCount = symbolInfo.getInterleavedBlockCount();
        if (blockCount == 1) {
            String ecc = createECCBlock(codewords, symbolInfo.getErrorCodewords());
            sb.append(ecc);
        } else {
            sb.setLength(sb.capacity());
            int[] dataSizes = new int[blockCount];
            int[] errorSizes = new int[blockCount];
            int[] startPos = new int[blockCount];
            for (int i = 0; i < blockCount; i++) {
                dataSizes[i] = symbolInfo.getDataLengthForInterleavedBlock(i + 1);
                errorSizes[i] = symbolInfo.getErrorLengthForInterleavedBlock(i + 1);
                startPos[i] = 0;
                if (i > 0) {
                    startPos[i] = startPos[i - 1] + dataSizes[i];
                }
            }
            for (int block = 0; block < blockCount; block++) {
                StringBuilder temp = new StringBuilder(dataSizes[block]);
                for (int d = block; d < symbolInfo.getDataCapacity(); d += blockCount) {
                    temp.append(codewords.charAt(d));
                }
                String ecc2 = createECCBlock(temp.toString(), errorSizes[block]);
                int pos = 0;
                int e = block;
                while (e < errorSizes[block] * blockCount) {
                    sb.setCharAt(symbolInfo.getDataCapacity() + e, ecc2.charAt(pos));
                    e += blockCount;
                    pos++;
                }
            }
        }
        return sb.toString();
    }

    private static String createECCBlock(CharSequence codewords, int numECWords) {
        return createECCBlock(codewords, 0, codewords.length(), numECWords);
    }

    private static String createECCBlock(CharSequence codewords, int start, int len, int numECWords) {
        int table = -1;
        int i = 0;
        while (true) {
            if (i < FACTOR_SETS.length) {
                if (FACTOR_SETS[i] != numECWords) {
                    i++;
                } else {
                    table = i;
                    break;
                }
            } else {
                break;
            }
        }
        if (table < 0) {
            throw new IllegalArgumentException("Illegal number of error correction codewords specified: " + numECWords);
        }
        int[] poly = FACTORS[table];
        char[] ecc = new char[numECWords];
        for (int i2 = 0; i2 < numECWords; i2++) {
            ecc[i2] = 0;
        }
        for (int i3 = start; i3 < start + len; i3++) {
            int m = ecc[numECWords - 1] ^ codewords.charAt(i3);
            for (int k = numECWords - 1; k > 0; k--) {
                if (m != 0 && poly[k] != 0) {
                    ecc[k] = (char) (ecc[k - 1] ^ ALOG[(LOG[m] + LOG[poly[k]]) % MotionEventCompat.ACTION_MASK]);
                } else {
                    ecc[k] = ecc[k - 1];
                }
            }
            if (m != 0 && poly[0] != 0) {
                ecc[0] = (char) ALOG[(LOG[m] + LOG[poly[0]]) % MotionEventCompat.ACTION_MASK];
            } else {
                ecc[0] = 0;
            }
        }
        char[] eccReversed = new char[numECWords];
        for (int i4 = 0; i4 < numECWords; i4++) {
            eccReversed[i4] = ecc[(numECWords - i4) - 1];
        }
        return String.valueOf(eccReversed);
    }
}
