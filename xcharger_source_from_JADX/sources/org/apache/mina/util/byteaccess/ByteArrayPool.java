package org.apache.mina.util.byteaccess;

import java.util.ArrayList;
import java.util.Stack;
import org.apache.mina.core.buffer.IoBuffer;

public class ByteArrayPool implements ByteArrayFactory {
    private final int MAX_BITS = 32;
    private final boolean direct;
    /* access modifiers changed from: private */
    public int freeBufferCount = 0;
    /* access modifiers changed from: private */
    public ArrayList<Stack<DirectBufferByteArray>> freeBuffers;
    /* access modifiers changed from: private */
    public long freeMemory = 0;
    private boolean freed;
    /* access modifiers changed from: private */
    public final int maxFreeBuffers;
    /* access modifiers changed from: private */
    public final int maxFreeMemory;

    static /* synthetic */ int access$208(ByteArrayPool x0) {
        int i = x0.freeBufferCount;
        x0.freeBufferCount = i + 1;
        return i;
    }

    public ByteArrayPool(boolean direct2, int maxFreeBuffers2, int maxFreeMemory2) {
        this.direct = direct2;
        this.freeBuffers = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            this.freeBuffers.add(new Stack());
        }
        this.maxFreeBuffers = maxFreeBuffers2;
        this.maxFreeMemory = maxFreeMemory2;
        this.freed = false;
    }

    public ByteArray create(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Buffer size must be at least 1: " + size);
        }
        int bits = bits(size);
        synchronized (this) {
            if (!this.freeBuffers.get(bits).isEmpty()) {
                DirectBufferByteArray ba = (DirectBufferByteArray) this.freeBuffers.get(bits).pop();
                ba.setFreed(false);
                ba.getSingleIoBuffer().limit(size);
                return ba;
            }
            IoBuffer bb = IoBuffer.allocate(1 << bits, this.direct);
            bb.limit(size);
            DirectBufferByteArray ba2 = new DirectBufferByteArray(bb);
            ba2.setFreed(false);
            return ba2;
        }
    }

    /* access modifiers changed from: private */
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

    private class DirectBufferByteArray extends BufferByteArray {
        private boolean freed;

        public DirectBufferByteArray(IoBuffer bb) {
            super(bb);
        }

        public void setFreed(boolean freed2) {
            this.freed = freed2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void free() {
            /*
                r8 = this;
                monitor-enter(r8)
                boolean r1 = r8.freed     // Catch:{ all -> 0x000d }
                if (r1 == 0) goto L_0x0010
                java.lang.IllegalStateException r1 = new java.lang.IllegalStateException     // Catch:{ all -> 0x000d }
                java.lang.String r2 = "Already freed."
                r1.<init>(r2)     // Catch:{ all -> 0x000d }
                throw r1     // Catch:{ all -> 0x000d }
            L_0x000d:
                r1 = move-exception
                monitor-exit(r8)     // Catch:{ all -> 0x000d }
                throw r1
            L_0x0010:
                r1 = 1
                r8.freed = r1     // Catch:{ all -> 0x000d }
                monitor-exit(r8)     // Catch:{ all -> 0x000d }
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this
                int r2 = r8.last()
                int r0 = r1.bits(r2)
                org.apache.mina.util.byteaccess.ByteArrayPool r2 = org.apache.mina.util.byteaccess.ByteArrayPool.this
                monitor-enter(r2)
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                java.util.ArrayList r1 = r1.freeBuffers     // Catch:{ all -> 0x0077 }
                if (r1 == 0) goto L_0x0075
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                int r1 = r1.freeBufferCount     // Catch:{ all -> 0x0077 }
                org.apache.mina.util.byteaccess.ByteArrayPool r3 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                int r3 = r3.maxFreeBuffers     // Catch:{ all -> 0x0077 }
                if (r1 >= r3) goto L_0x0075
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                long r4 = r1.freeMemory     // Catch:{ all -> 0x0077 }
                int r1 = r8.last()     // Catch:{ all -> 0x0077 }
                long r6 = (long) r1     // Catch:{ all -> 0x0077 }
                long r4 = r4 + r6
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                int r1 = r1.maxFreeMemory     // Catch:{ all -> 0x0077 }
                long r6 = (long) r1     // Catch:{ all -> 0x0077 }
                int r1 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r1 > 0) goto L_0x0075
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                java.util.ArrayList r1 = r1.freeBuffers     // Catch:{ all -> 0x0077 }
                java.lang.Object r1 = r1.get(r0)     // Catch:{ all -> 0x0077 }
                java.util.Stack r1 = (java.util.Stack) r1     // Catch:{ all -> 0x0077 }
                r1.push(r8)     // Catch:{ all -> 0x0077 }
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                org.apache.mina.util.byteaccess.ByteArrayPool.access$208(r1)     // Catch:{ all -> 0x0077 }
                org.apache.mina.util.byteaccess.ByteArrayPool r1 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                org.apache.mina.util.byteaccess.ByteArrayPool r3 = org.apache.mina.util.byteaccess.ByteArrayPool.this     // Catch:{ all -> 0x0077 }
                long r4 = r3.freeMemory     // Catch:{ all -> 0x0077 }
                int r3 = r8.last()     // Catch:{ all -> 0x0077 }
                long r6 = (long) r3     // Catch:{ all -> 0x0077 }
                long r4 = r4 + r6
                long unused = r1.freeMemory = r4     // Catch:{ all -> 0x0077 }
                monitor-exit(r2)     // Catch:{ all -> 0x0077 }
            L_0x0074:
                return
            L_0x0075:
                monitor-exit(r2)     // Catch:{ all -> 0x0077 }
                goto L_0x0074
            L_0x0077:
                r1 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x0077 }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.util.byteaccess.ByteArrayPool.DirectBufferByteArray.free():void");
        }
    }
}
