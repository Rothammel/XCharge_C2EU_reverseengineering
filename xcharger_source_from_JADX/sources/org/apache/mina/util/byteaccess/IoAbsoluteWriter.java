package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

public interface IoAbsoluteWriter {
    int first();

    int last();

    ByteOrder order();

    void put(int i, byte b);

    void put(int i, IoBuffer ioBuffer);

    void putChar(int i, char c);

    void putDouble(int i, double d);

    void putFloat(int i, float f);

    void putInt(int i, int i2);

    void putLong(int i, long j);

    void putShort(int i, short s);
}
