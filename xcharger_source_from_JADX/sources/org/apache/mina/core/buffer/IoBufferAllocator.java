package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;

public interface IoBufferAllocator {
    IoBuffer allocate(int i, boolean z);

    ByteBuffer allocateNioBuffer(int i, boolean z);

    void dispose();

    IoBuffer wrap(ByteBuffer byteBuffer);
}
