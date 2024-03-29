package com.google.zxing.pdf417.decoder;

import com.google.zxing.pdf417.PDF417Common;
import java.lang.reflect.Array;

/* loaded from: classes.dex */
final class PDF417CodewordDecoder {
    private static final float[][] RATIOS_TABLE = (float[][]) Array.newInstance(Float.TYPE, PDF417Common.SYMBOL_TABLE.length, 8);

    static {
        for (int i = 0; i < PDF417Common.SYMBOL_TABLE.length; i++) {
            int currentSymbol = PDF417Common.SYMBOL_TABLE[i];
            int currentBit = currentSymbol & 1;
            for (int j = 0; j < 8; j++) {
                float size = 0.0f;
                while ((currentSymbol & 1) == currentBit) {
                    size += 1.0f;
                    currentSymbol >>= 1;
                }
                currentBit = currentSymbol & 1;
                RATIOS_TABLE[i][(8 - j) - 1] = size / 17.0f;
            }
        }
    }

    private PDF417CodewordDecoder() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getDecodedValue(int[] moduleBitCount) {
        int decodedValue = getDecodedCodewordValue(sampleBitCounts(moduleBitCount));
        return decodedValue != -1 ? decodedValue : getClosestDecodedValue(moduleBitCount);
    }

    private static int[] sampleBitCounts(int[] moduleBitCount) {
        float bitCountSum = PDF417Common.getBitCountSum(moduleBitCount);
        int[] result = new int[8];
        int bitCountIndex = 0;
        int sumPreviousBits = 0;
        for (int i = 0; i < 17; i++) {
            float sampleIndex = (bitCountSum / 34.0f) + ((i * bitCountSum) / 17.0f);
            if (moduleBitCount[bitCountIndex] + sumPreviousBits <= sampleIndex) {
                sumPreviousBits += moduleBitCount[bitCountIndex];
                bitCountIndex++;
            }
            result[bitCountIndex] = result[bitCountIndex] + 1;
        }
        return result;
    }

    private static int getDecodedCodewordValue(int[] moduleBitCount) {
        int decodedValue = getBitValue(moduleBitCount);
        if (PDF417Common.getCodeword(decodedValue) == -1) {
            return -1;
        }
        return decodedValue;
    }

    private static int getBitValue(int[] moduleBitCount) {
        long result = 0;
        for (int i = 0; i < moduleBitCount.length; i++) {
            for (int bit = 0; bit < moduleBitCount[i]; bit++) {
                result = (result << 1) | (i % 2 == 0 ? 1 : 0);
            }
        }
        return (int) result;
    }

    private static int getClosestDecodedValue(int[] moduleBitCount) {
        int bitCountSum = PDF417Common.getBitCountSum(moduleBitCount);
        float[] bitCountRatios = new float[8];
        for (int i = 0; i < bitCountRatios.length; i++) {
            bitCountRatios[i] = moduleBitCount[i] / bitCountSum;
        }
        float bestMatchError = Float.MAX_VALUE;
        int bestMatch = -1;
        for (int j = 0; j < RATIOS_TABLE.length; j++) {
            float error = 0.0f;
            float[] ratioTableRow = RATIOS_TABLE[j];
            for (int k = 0; k < 8; k++) {
                float diff = ratioTableRow[k] - bitCountRatios[k];
                error += diff * diff;
                if (error >= bestMatchError) {
                    break;
                }
            }
            if (error < bestMatchError) {
                bestMatchError = error;
                bestMatch = PDF417Common.SYMBOL_TABLE[j];
            }
        }
        return bestMatch;
    }
}