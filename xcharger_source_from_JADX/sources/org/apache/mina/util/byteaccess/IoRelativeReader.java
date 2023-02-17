package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

public interface IoRelativeReader {
    byte get();

    void get(IoBuffer ioBuffer);

    char getChar();

    double getDouble();

    float getFloat();

    int getInt();

    long getLong();

    int getRemaining();

    short getShort();

    boolean hasRemaining();

    ByteOrder order();

    void skip(int i);

    ByteArray slice(int i);
}
