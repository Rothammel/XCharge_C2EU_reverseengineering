package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* loaded from: classes.dex */
public class SimpleBufferAllocator implements IoBufferAllocator {
    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public IoBuffer allocate(int capacity, boolean direct) {
        return wrap(allocateNioBuffer(capacity, direct));
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
        if (direct) {
            ByteBuffer nioBuffer = ByteBuffer.allocateDirect(capacity);
            return nioBuffer;
        }
        ByteBuffer nioBuffer2 = ByteBuffer.allocate(capacity);
        return nioBuffer2;
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public IoBuffer wrap(ByteBuffer nioBuffer) {
        return new SimpleBuffer(nioBuffer);
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public void dispose() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SimpleBuffer extends AbstractIoBuffer {
        private ByteBuffer buf;

        protected SimpleBuffer(ByteBuffer buf) {
            super(SimpleBufferAllocator.this, buf.capacity());
            this.buf = buf;
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        protected SimpleBuffer(SimpleBuffer parent, ByteBuffer buf) {
            super(parent);
            this.buf = buf;
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public ByteBuffer buf() {
            return this.buf;
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected void buf(ByteBuffer buf) {
            this.buf = buf;
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected IoBuffer duplicate0() {
            return new SimpleBuffer(this, this.buf.duplicate());
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected IoBuffer slice0() {
            return new SimpleBuffer(this, this.buf.slice());
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected IoBuffer asReadOnlyBuffer0() {
            return new SimpleBuffer(this, this.buf.asReadOnlyBuffer());
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public byte[] array() {
            return this.buf.array();
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public int arrayOffset() {
            return this.buf.arrayOffset();
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public boolean hasArray() {
            return this.buf.hasArray();
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public void free() {
        }
    }
}
