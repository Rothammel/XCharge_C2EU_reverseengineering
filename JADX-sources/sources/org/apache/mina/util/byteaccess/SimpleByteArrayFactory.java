package org.apache.mina.util.byteaccess;

import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public class SimpleByteArrayFactory implements ByteArrayFactory {
    @Override // org.apache.mina.util.byteaccess.ByteArrayFactory
    public ByteArray create(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Buffer size must not be negative:" + size);
        }
        IoBuffer bb = IoBuffer.allocate(size);
        ByteArray ba = new BufferByteArray(bb) { // from class: org.apache.mina.util.byteaccess.SimpleByteArrayFactory.1
            @Override // org.apache.mina.util.byteaccess.BufferByteArray, org.apache.mina.util.byteaccess.ByteArray
            public void free() {
            }
        };
        return ba;
    }
}
