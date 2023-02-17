package org.apache.mina.util.byteaccess;

import java.util.ArrayList;
import java.util.Stack;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public class ByteArrayPool implements ByteArrayFactory {
    private final boolean direct;
    private boolean freed;
    private final int maxFreeBuffers;
    private final int maxFreeMemory;
    private final int MAX_BITS = 32;
    private int freeBufferCount = 0;
    private long freeMemory = 0;
    private ArrayList<Stack<DirectBufferByteArray>> freeBuffers = new ArrayList<>();

    static /* synthetic */ int access$208(ByteArrayPool x0) {
        int i = x0.freeBufferCount;
        x0.freeBufferCount = i + 1;
        return i;
    }

    public ByteArrayPool(boolean direct, int maxFreeBuffers, int maxFreeMemory) {
        this.direct = direct;
        for (int i = 0; i < 32; i++) {
            this.freeBuffers.add(new Stack<>());
        }
        this.maxFreeBuffers = maxFreeBuffers;
        this.maxFreeMemory = maxFreeMemory;
        this.freed = false;
    }

    @Override // org.apache.mina.util.byteaccess.ByteArrayFactory
    public ByteArray create(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Buffer size must be at least 1: " + size);
        }
        int bits = bits(size);
        synchronized (this) {
            if (!this.freeBuffers.get(bits).isEmpty()) {
                DirectBufferByteArray ba = this.freeBuffers.get(bits).pop();
                ba.setFreed(false);
                ba.getSingleIoBuffer().limit(size);
                return ba;
            }
            int bbSize = 1 << bits;
            IoBuffer bb = IoBuffer.allocate(bbSize, this.direct);
            bb.limit(size);
            DirectBufferByteArray ba2 = new DirectBufferByteArray(bb);
            ba2.setFreed(false);
            return ba2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int bits(int index) {
        int bits = 0;
        while ((1 << bits) < index) {
            bits++;
        }
        return bits;
    }

    public void free() {
        synchronized (this) {
            if (this.freed) {
                throw new IllegalStateException("Already freed.");
            }
            this.freed = true;
            this.freeBuffers.clear();
            this.freeBuffers = null;
        }
    }

    /* loaded from: classes.dex */
    private class DirectBufferByteArray extends BufferByteArray {
        private boolean freed;

        public DirectBufferByteArray(IoBuffer bb) {
            super(bb);
        }

        public void setFreed(boolean freed) {
            this.freed = freed;
        }

        @Override // org.apache.mina.util.byteaccess.BufferByteArray, org.apache.mina.util.byteaccess.ByteArray
        public void free() {
            synchronized (this) {
                if (this.freed) {
                    throw new IllegalStateException("Already freed.");
                }
                this.freed = true;
            }
            int bits = ByteArrayPool.this.bits(last());
            synchronized (ByteArrayPool.this) {
                if (ByteArrayPool.this.freeBuffers != null && ByteArrayPool.this.freeBufferCount < ByteArrayPool.this.maxFreeBuffers && ByteArrayPool.this.freeMemory + last() <= ByteArrayPool.this.maxFreeMemory) {
                    ((Stack) ByteArrayPool.this.freeBuffers.get(bits)).push(this);
                    ByteArrayPool.access$208(ByteArrayPool.this);
                    ByteArrayPool.this.freeMemory += last();
                }
            }
        }
    }
}