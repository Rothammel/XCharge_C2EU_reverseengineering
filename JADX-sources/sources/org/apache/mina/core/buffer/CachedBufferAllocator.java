package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/* loaded from: classes.dex */
public class CachedBufferAllocator implements IoBufferAllocator {
    private static final int DEFAULT_MAX_CACHED_BUFFER_SIZE = 262144;
    private static final int DEFAULT_MAX_POOL_SIZE = 8;
    private final ThreadLocal<Map<Integer, Queue<CachedBuffer>>> directBuffers;
    private final ThreadLocal<Map<Integer, Queue<CachedBuffer>>> heapBuffers;
    private final int maxCachedBufferSize;
    private final int maxPoolSize;

    public CachedBufferAllocator() {
        this(8, 262144);
    }

    public CachedBufferAllocator(int maxPoolSize, int maxCachedBufferSize) {
        if (maxPoolSize < 0) {
            throw new IllegalArgumentException("maxPoolSize: " + maxPoolSize);
        }
        if (maxCachedBufferSize < 0) {
            throw new IllegalArgumentException("maxCachedBufferSize: " + maxCachedBufferSize);
        }
        this.maxPoolSize = maxPoolSize;
        this.maxCachedBufferSize = maxCachedBufferSize;
        this.heapBuffers = new ThreadLocal<Map<Integer, Queue<CachedBuffer>>>() { // from class: org.apache.mina.core.buffer.CachedBufferAllocator.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public Map<Integer, Queue<CachedBuffer>> initialValue() {
                return CachedBufferAllocator.this.newPoolMap();
            }
        };
        this.directBuffers = new ThreadLocal<Map<Integer, Queue<CachedBuffer>>>() { // from class: org.apache.mina.core.buffer.CachedBufferAllocator.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public Map<Integer, Queue<CachedBuffer>> initialValue() {
                return CachedBufferAllocator.this.newPoolMap();
            }
        };
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public int getMaxCachedBufferSize() {
        return this.maxCachedBufferSize;
    }

    Map<Integer, Queue<CachedBuffer>> newPoolMap() {
        Map<Integer, Queue<CachedBuffer>> poolMap = new HashMap<>();
        for (int i = 0; i < 31; i++) {
            poolMap.put(Integer.valueOf(1 << i), new ConcurrentLinkedQueue<>());
        }
        poolMap.put(0, new ConcurrentLinkedQueue<>());
        poolMap.put(Integer.MAX_VALUE, new ConcurrentLinkedQueue<>());
        return poolMap;
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public IoBuffer allocate(int requestedCapacity, boolean direct) {
        Queue<CachedBuffer> pool;
        CachedBuffer buf;
        int actualCapacity = IoBuffer.normalizeCapacity(requestedCapacity);
        if (this.maxCachedBufferSize != 0 && actualCapacity > this.maxCachedBufferSize) {
            if (direct) {
                buf = wrap(ByteBuffer.allocateDirect(actualCapacity));
            } else {
                buf = wrap(ByteBuffer.allocate(actualCapacity));
            }
        } else {
            if (direct) {
                pool = this.directBuffers.get().get(Integer.valueOf(actualCapacity));
            } else {
                pool = this.heapBuffers.get().get(Integer.valueOf(actualCapacity));
            }
            buf = pool.poll();
            if (buf != null) {
                buf.clear();
                buf.setAutoExpand(false);
                buf.order(ByteOrder.BIG_ENDIAN);
            } else if (direct) {
                buf = wrap(ByteBuffer.allocateDirect(actualCapacity));
            } else {
                buf = wrap(ByteBuffer.allocate(actualCapacity));
            }
        }
        buf.limit(requestedCapacity);
        return buf;
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
        return allocate(capacity, direct).buf();
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public IoBuffer wrap(ByteBuffer nioBuffer) {
        return new CachedBuffer(nioBuffer);
    }

    @Override // org.apache.mina.core.buffer.IoBufferAllocator
    public void dispose() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CachedBuffer extends AbstractIoBuffer {
        private ByteBuffer buf;
        private final Thread ownerThread;

        protected CachedBuffer(ByteBuffer buf) {
            super(CachedBufferAllocator.this, buf.capacity());
            this.ownerThread = Thread.currentThread();
            this.buf = buf;
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        protected CachedBuffer(CachedBuffer parent, ByteBuffer buf) {
            super(parent);
            this.ownerThread = Thread.currentThread();
            this.buf = buf;
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public ByteBuffer buf() {
            if (this.buf == null) {
                throw new IllegalStateException("Buffer has been freed already.");
            }
            return this.buf;
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected void buf(ByteBuffer buf) {
            ByteBuffer oldBuf = this.buf;
            this.buf = buf;
            free(oldBuf);
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected IoBuffer duplicate0() {
            return new CachedBuffer(this, buf().duplicate());
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected IoBuffer slice0() {
            return new CachedBuffer(this, buf().slice());
        }

        @Override // org.apache.mina.core.buffer.AbstractIoBuffer
        protected IoBuffer asReadOnlyBuffer0() {
            return new CachedBuffer(this, buf().asReadOnlyBuffer());
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public byte[] array() {
            return buf().array();
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public int arrayOffset() {
            return buf().arrayOffset();
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public boolean hasArray() {
            return buf().hasArray();
        }

        @Override // org.apache.mina.core.buffer.IoBuffer
        public void free() {
            free(this.buf);
            this.buf = null;
        }

        private void free(ByteBuffer oldBuf) {
            if (oldBuf != null) {
                if ((CachedBufferAllocator.this.maxCachedBufferSize == 0 || oldBuf.capacity() <= CachedBufferAllocator.this.maxCachedBufferSize) && !oldBuf.isReadOnly() && !isDerived() && Thread.currentThread() == this.ownerThread) {
                    Queue<CachedBuffer> pool = oldBuf.isDirect() ? (Queue) ((Map) CachedBufferAllocator.this.directBuffers.get()).get(Integer.valueOf(oldBuf.capacity())) : (Queue) ((Map) CachedBufferAllocator.this.heapBuffers.get()).get(Integer.valueOf(oldBuf.capacity()));
                    if (pool != null) {
                        if (CachedBufferAllocator.this.maxPoolSize == 0 || pool.size() < CachedBufferAllocator.this.maxPoolSize) {
                            pool.offer(new CachedBuffer(oldBuf));
                        }
                    }
                }
            }
        }
    }
}
