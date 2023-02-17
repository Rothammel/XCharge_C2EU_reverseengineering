package org.apache.mina.util.byteaccess;

import org.apache.mina.util.byteaccess.ByteArray;

/* loaded from: classes.dex */
abstract class AbstractByteArray implements ByteArray {
    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public final int length() {
        return last() - first();
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray
    public final boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ByteArray) {
            ByteArray otherByteArray = (ByteArray) other;
            if (first() == otherByteArray.first() && last() == otherByteArray.last() && order().equals(otherByteArray.order())) {
                ByteArray.Cursor cursor = cursor();
                ByteArray.Cursor otherCursor = otherByteArray.cursor();
                int remaining = cursor.getRemaining();
                while (remaining > 0) {
                    if (remaining >= 4) {
                        int i = cursor.getInt();
                        int otherI = otherCursor.getInt();
                        if (i != otherI) {
                            return false;
                        }
                    } else {
                        byte b = cursor.get();
                        byte otherB = otherCursor.get();
                        if (b != otherB) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }
}
