package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

public interface IoAbsoluteReader {
    int first();

    byte get(int i);

    void get(int i, IoBuffer ioBuffer);

    char getChar(int i);

    double getDouble(int i);

    float getFloat(int i);

    int getInt(int i);

    long getLong(int i);

    short getShort(int i);

    int last();

    int length();

    ByteOrder order();

    ByteArray slice(int i, int i2);
}
