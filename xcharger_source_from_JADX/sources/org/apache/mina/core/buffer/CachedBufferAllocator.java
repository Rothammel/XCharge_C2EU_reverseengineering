package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CachedBufferAllocator implements IoBufferAllocator {
    private static final int DEFAULT_MAX_CACHED_BUFFER_SIZE = 262144;
    private static final int DEFAULT_MAX_POOL_SIZE = 8;
    /* access modifiers changed from: private */
    public final ThreadLocal<Map<Integer, Queue<CachedBuffer>>> directBuffers;
    /* access modifiers changed from: private */
    public final ThreadLocal<Map<Integer, Queue<CachedBuffer>>> heapBuffers;
    /* access modifiers changed from: private */
    public final int maxCachedBufferSize;
    /* access modifiers changed from: private */
    public final int maxPoolSize;

    public CachedBufferAllocator() {
        this(8, 262144);
    }

    public CachedBufferAllocator(int maxPoolSize2, int maxCachedBufferSize2) {
        if (maxPoolSize2 < 0) {
            throw new IllegalArgumentException("maxPoolSize: " + maxPoolSize2);
        } else if (maxCachedBufferSize2 < 0) {
            throw new IllegalArgumentException("maxCachedBufferSize: " + maxCachedBufferSize2);
        } else {
            this.maxPoolSize = maxPoolSize2;
            this.maxCachedBufferSize = maxCachedBufferSize2;
            this.heapBuffers = new ThreadLocal<Map<Integer, Queue<CachedBuffer>>>() {
                /* access modifiers changed from: protected */
                public Map<Integer, Queue<CachedBuffer>> initialValue() {
                    return CachedBufferAllocator.this.newPoolMap();
                }
            };
            this.directBuffers = new ThreadLocal<Map<Integer, Queue<CachedBuffer>>>() {
                /* access modifiers changed from: protected */
                public Map<Integer, Queue<CachedBuffer>> initialValue() {
                    return CachedBufferAllocator.this.newPoolMap();
                }
            };
        }
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public int getMaxCachedBufferSize() {
        return this.maxCachedBufferSize;
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, Queue<CachedBuffer>> newPoolMap() {
        Map<Integer, Queue<CachedBuffer>> poolMap = new HashMap<>();
        for (int i = 0; i < 31; i++) {
            poolMap.put(Integer.valueOf(1 << i), new ConcurrentLinkedQueue());
        }
        poolMap.put(0, new ConcurrentLinkedQueue());
        poolMap.put(Integer.MAX_VALUE, new ConcurrentLinkedQueue());
        return poolMap;
    }

    public IoBuffer allocate(int requestedCapacity, boolean direct) {
        Queue<CachedBuffer> pool;
        IoBuffer buf;
        int actualCapacity = IoBuffer.normalizeCapacity(requestedCapacity);
        if (this.maxCachedBufferSize == 0 || actualCapacity <= this.maxCachedBufferSize) {
            if (direct) {
                pool = (Queue) this.directBuffers.get().get(Integer.valueOf(actualCapacity));
            } else {
                pool = (Queue) this.heapBuffers.get().get(Integer.valueOf(actualCapacity));
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
        } else if (direct) {
            buf = wrap(ByteBuffer.allocateDirect(actualCapacity));
        } else {
            buf = wrap(ByteBuffer.allocate(actualCapacity));
        }
        buf.limit(requestedCapacity);
        return buf;
    }

    public ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
        return allocate(capacity, direct).buf();
    }

    public IoBuffer wrap(ByteBuffer nioBuffer) {
        return new CachedBuffer(nioBuffer);
    }

    public void dispose() {
    }

    private class CachedBuffer extends AbstractIoBuffer {
        private ByteBuffer buf;
        private final Thread ownerThread = Thread.currentThread();

        protected CachedBuffer(ByteBuffer buf2) {
            super(CachedBufferAllocator.this, buf2.capacity());
            this.buf = buf2;
            buf2.order(ByteOrder.BIG_ENDIAN);
        }

        protected CachedBuffer(CachedBuffer parent, ByteBuffer buf2) {
            super(parent);
            this.buf = buf2;
        }

        public ByteBuffer buf() {
            if (this.buf != null) {
                return this.buf;
            }
            throw new IllegalStateException("Buffer has been freed already.");
        }

        /* access modifiers changed from: protected */
        public void buf(ByteBuffer buf2) {
            ByteBuffer oldBuf = this.buf;
            this.buf = buf2;
            free(oldBuf);
        }

        /* access modifiers changed from: protected */
        public IoBuffer duplicate0() {
            return new CachedBuffer(this, buf().duplicate());
        }

        /* access modifiers changed from: protected */
        public IoBuffer slice0() {
            return new CachedBuffer(this, buf().slice());
        }

        /* access modifiers changed from: protected */
        public IoBuffer asReadOnlyBuffer0() {
            return new CachedBuffer(this, buf().asReadOnlyBuffer());
        }

        public byte[] array() {
            return buf().array();
        }

        public int arrayOffset() {
            return buf().arrayOffset();
        }

        public boolean hasArray() {
            return buf().hasArray();
        }

        public void free() {
            free(this.buf);
            this.buf = null;
        }

        private void free(ByteBuffer oldBuf) {
            Queue<CachedBuffer> pool;
            if (oldBuf == null) {
                return;
            }
            if ((CachedBufferAllocator.this.maxCachedBufferSize == 0 || oldBuf.capacity() <= CachedBufferAllocator.this.maxCachedBufferSize) && !oldBuf.isReadOnly() && !isDerived() && Thread.currentThread() == this.ownerThread) {
                if (oldBuf.isDirect()) {
                    pool = (Queue) ((Map) CachedBufferAllocator.this.directBuffers.get()).get(Integer.valueOf(oldBuf.capacity()));
                } else {
                    pool = (Queue) ((Map) CachedBufferAllocator.this.heapBuffers.get()).get(Integer.valueOf(oldBuf.capacity()));
                }
                if (pool == null) {
                    return;
                }
                if (CachedBufferAllocator.this.maxPoolSize == 0 || pool.size() < CachedBufferAllocator.this.maxPoolSize) {
                    pool.offer(new CachedBuffer(oldBuf));
                }
            }
        }
    }
}
