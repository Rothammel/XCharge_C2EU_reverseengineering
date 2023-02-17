package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SimpleBufferAllocator implements IoBufferAllocator {
    public IoBuffer allocate(int capacity, boolean direct) {
        return wrap(allocateNioBuffer(capacity, direct));
    }

    public ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
        if (direct) {
            return ByteBuffer.allocateDirect(capacity);
        }
        return ByteBuffer.allocate(capacity);
    }

    public IoBuffer wrap(ByteBuffer nioBuffer) {
        return new SimpleBuffer(nioBuffer);
    }

    public void dispose() {
    }

    private class SimpleBuffer extends AbstractIoBuffer {
        private ByteBuffer buf;

        protected SimpleBuffer(ByteBuffer buf2) {
            super(SimpleBufferAllocator.this, buf2.capacity());
            this.buf = buf2;
            buf2.order(ByteOrder.BIG_ENDIAN);
        }

        protected SimpleBuffer(SimpleBuffer parent, ByteBuffer buf2) {
            super(parent);
            this.buf = buf2;
        }

        public ByteBuffer buf() {
            return this.buf;
        }

        /* access modifiers changed from: protected */
        public void buf(ByteBuffer buf2) {
            this.buf = buf2;
        }

        /* access modifiers changed from: protected */
        public IoBuffer duplicate0() {
            return new SimpleBuffer(this, this.buf.duplicate());
        }

        /* access modifiers changed from: protected */
        public IoBuffer slice0() {
            return new SimpleBuffer(this, this.buf.slice());
        }

        /* access modifiers changed from: protected */
        public IoBuffer asReadOnlyBuffer0() {
            return new SimpleBuffer(this, this.buf.asReadOnlyBuffer());
        }

        public byte[] array() {
            return this.buf.array();
        }

        public int arrayOffset() {
            return this.buf.arrayOffset();
        }

        public boolean hasArray() {
            return this.buf.hasArray();
        }

        public void free() {
        }
    }
}
