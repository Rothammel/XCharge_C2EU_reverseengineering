package com.google.zxing.datamatrix.encoder;

import android.support.p000v4.view.MotionEventCompat;
import p010it.sauronsoftware.ftp4j.FTPCodes;

final class Base256Encoder implements Encoder {
    Base256Encoder() {
    }

    public int getEncodingMode() {
        return 5;
    }

    public void encode(EncoderContext context) {
        boolean mustPad;
        StringBuilder buffer = new StringBuilder();
        buffer.append(0);
        while (true) {
            if (!context.hasMoreCharacters()) {
                break;
            }
            buffer.append(context.getCurrentChar());
            context.pos++;
            int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode());
            if (newMode != getEncodingMode()) {
                context.signalEncoderChange(newMode);
                break;
            }
        }
        int dataCount = buffer.length() - 1;
        int currentSize = context.getCodewordCount() + dataCount + 1;
        context.updateSymbolInfo(currentSize);
        if (context.getSymbolInfo().getDataCapacity() - currentSize > 0) {
            mustPad = true;
        } else {
            mustPad = false;
        }
        if (context.hasMoreCharacters() || mustPad) {
            if (dataCount <= 249) {
                buffer.setCharAt(0, (char) dataCount);
            } else if (dataCount <= 249 || dataCount > 1555) {
                throw new IllegalStateException("Message length not in valid ranges: " + dataCount);
            } else {
                buffer.setCharAt(0, (char) ((dataCount / FTPCodes.FILE_ACTION_COMPLETED) + 249));
                buffer.insert(1, (char) (dataCount % FTPCodes.FILE_ACTION_COMPLETED));
            }
        }
        int c = buffer.length();
        for (int i = 0; i < c; i++) {
            context.writeCodeword(randomize255State(buffer.charAt(i), context.getCodewordCount() + 1));
        }
    }

    private static char randomize255State(char ch, int codewordPosition) {
        int tempVariable = ch + ((codewordPosition * 149) % MotionEventCompat.ACTION_MASK) + 1;
        if (tempVariable <= 255) {
            return (char) tempVariable;
        }
        return (char) (tempVariable - 256);
    }
}
