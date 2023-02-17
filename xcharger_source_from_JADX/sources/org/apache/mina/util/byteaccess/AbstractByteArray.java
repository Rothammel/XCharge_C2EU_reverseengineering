package org.apache.mina.util.byteaccess;

import org.apache.mina.util.byteaccess.ByteArray;

abstract class AbstractByteArray implements ByteArray {
    AbstractByteArray() {
    }

    public final int length() {
        return last() - first();
    }

    public final boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ByteArray)) {
            return false;
        }
        ByteArray otherByteArray = (ByteArray) other;
        if (first() != otherByteArray.first() || last() != otherByteArray.last() || !order().equals(otherByteArray.order())) {
            return false;
        }
        ByteArray.Cursor cursor = cursor();
        ByteArray.Cursor otherCursor = otherByteArray.cursor();
        int remaining = cursor.getRemaining();
        while (remaining > 0) {
            if (remaining >= 4) {
                if (cursor.getInt() != otherCursor.getInt()) {
                    return false;
                }
            } else if (cursor.get() != otherCursor.get()) {
                return false;
            }
        }
        return true;
    }
}
