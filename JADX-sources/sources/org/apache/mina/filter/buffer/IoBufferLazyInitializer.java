package org.apache.mina.filter.buffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.LazyInitializer;

/* loaded from: classes.dex */
public class IoBufferLazyInitializer extends LazyInitializer<IoBuffer> {
    private int bufferSize;

    public IoBufferLazyInitializer(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override // org.apache.mina.util.LazyInitializer
    public IoBuffer init() {
        return IoBuffer.allocate(this.bufferSize);
    }
}
