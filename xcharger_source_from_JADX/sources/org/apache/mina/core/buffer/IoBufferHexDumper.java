package org.apache.mina.core.buffer;

import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

class IoBufferHexDumper {
    private static final byte[] highDigits;
    private static final byte[] lowDigits;

    IoBufferHexDumper() {
    }

    static {
        byte[] digits = {AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, 50, 51, 52, AnyoMessage.CMD_BIND_USER, AnyoMessage.CMD_QUERY_SYS_INFO, AnyoMessage.CMD_UPDATE_SYS_INFO, AnyoMessage.CMD_QUERY_FEE_POLICY, AnyoMessage.CMD_UPDATE_FEE_POLICY, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, AnyoMessage.CMD_QUERY_DEVICE_FAULT, AnyoMessage.CMD_QUERY_BATTERY_CHARGE_INFO, 68, 69, 70};
        byte[] high = new byte[256];
        byte[] low = new byte[256];
        for (int i = 0; i < 256; i++) {
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 15];
        }
        highDigits = high;
        lowDigits = low;
    }

    public static String getHexdump(IoBuffer in, int lengthLimit) {
        int size;
        if (lengthLimit == 0) {
            throw new IllegalArgumentException("lengthLimit: " + lengthLimit + " (expected: 1+)");
        }
        boolean truncate = in.remaining() > lengthLimit;
        if (truncate) {
            size = lengthLimit;
        } else {
            size = in.remaining();
        }
        if (size == 0) {
            return "empty";
        }
        StringBuilder out = new StringBuilder((size * 3) + 3);
        int mark = in.position();
        int byteValue = in.get() & MotionEventCompat.ACTION_MASK;
        out.append((char) highDigits[byteValue]);
        out.append((char) lowDigits[byteValue]);
        for (int size2 = size - 1; size2 > 0; size2--) {
            out.append(TokenParser.f168SP);
            int byteValue2 = in.get() & MotionEventCompat.ACTION_MASK;
            out.append((char) highDigits[byteValue2]);
            out.append((char) lowDigits[byteValue2]);
        }
        in.position(mark);
        if (truncate) {
            out.append("...");
        }
        return out.toString();
    }
}
