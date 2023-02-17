package org.apache.mina.util.byteaccess;

import org.apache.mina.core.buffer.IoBuffer;

public class SimpleByteArrayFactory implements ByteArrayFactory {
    public ByteArray create(int size) {
        if (size >= 0) {
            return new BufferByteArray(IoBuffer.allocate(size)) {
                public void free() {
                }
            };
        }
        throw new IllegalArgumentException("Buffer size must not be negative:" + size);
    }
}
