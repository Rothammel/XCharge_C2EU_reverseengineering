package org.apache.mina.filter.buffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.util.LazyInitializedCacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BufferedWriteFilter extends IoFilterAdapter {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private int bufferSize;
    private final LazyInitializedCacheMap<IoSession, IoBuffer> buffersMap;
    private final Logger logger;

    public BufferedWriteFilter() {
        this(8192, (LazyInitializedCacheMap<IoSession, IoBuffer>) null);
    }

    public BufferedWriteFilter(int bufferSize2) {
        this(bufferSize2, (LazyInitializedCacheMap<IoSession, IoBuffer>) null);
    }

    public BufferedWriteFilter(int bufferSize2, LazyInitializedCacheMap<IoSession, IoBuffer> buffersMap2) {
        this.logger = LoggerFactory.getLogger((Class<?>) BufferedWriteFilter.class);
        this.bufferSize = 8192;
        this.bufferSize = bufferSize2;
        if (buffersMap2 == null) {
            this.buffersMap = new LazyInitializedCacheMap<>();
        } else {
            this.buffersMap = buffersMap2;
        }
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize2) {
        this.bufferSize = bufferSize2;
    }

    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object data = writeRequest.getMessage();
        if (data instanceof IoBuffer) {
            write(session, (IoBuffer) data);
            return;
        }
        throw new IllegalArgumentException("This filter should only buffer IoBuffer objects");
    }

    private void write(IoSession session, IoBuffer data) {
        write(session, data, this.buffersMap.putIfAbsent(session, new IoBufferLazyInitializer(this.bufferSize)));
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void write(org.apache.mina.core.session.IoSession r6, org.apache.mina.core.buffer.IoBuffer r7, org.apache.mina.core.buffer.IoBuffer r8) {
        /*
            r5 = this;
            int r1 = r7.remaining()     // Catch:{ Exception -> 0x003d }
            int r3 = r8.capacity()     // Catch:{ Exception -> 0x003d }
            if (r1 < r3) goto L_0x001e
            org.apache.mina.core.filterchain.IoFilterChain r3 = r6.getFilterChain()     // Catch:{ Exception -> 0x003d }
            org.apache.mina.core.filterchain.IoFilter$NextFilter r2 = r3.getNextFilter((org.apache.mina.core.filterchain.IoFilter) r5)     // Catch:{ Exception -> 0x003d }
            r5.internalFlush(r2, r6, r8)     // Catch:{ Exception -> 0x003d }
            org.apache.mina.core.write.DefaultWriteRequest r3 = new org.apache.mina.core.write.DefaultWriteRequest     // Catch:{ Exception -> 0x003d }
            r3.<init>(r7)     // Catch:{ Exception -> 0x003d }
            r2.filterWrite(r6, r3)     // Catch:{ Exception -> 0x003d }
        L_0x001d:
            return
        L_0x001e:
            int r3 = r8.limit()     // Catch:{ Exception -> 0x003d }
            int r4 = r8.position()     // Catch:{ Exception -> 0x003d }
            int r3 = r3 - r4
            if (r1 <= r3) goto L_0x0034
            org.apache.mina.core.filterchain.IoFilterChain r3 = r6.getFilterChain()     // Catch:{ Exception -> 0x003d }
            org.apache.mina.core.filterchain.IoFilter$NextFilter r3 = r3.getNextFilter((org.apache.mina.core.filterchain.IoFilter) r5)     // Catch:{ Exception -> 0x003d }
            r5.internalFlush(r3, r6, r8)     // Catch:{ Exception -> 0x003d }
        L_0x0034:
            monitor-enter(r8)     // Catch:{ Exception -> 0x003d }
            r8.put((org.apache.mina.core.buffer.IoBuffer) r7)     // Catch:{ all -> 0x003a }
            monitor-exit(r8)     // Catch:{ all -> 0x003a }
            goto L_0x001d
        L_0x003a:
            r3 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x003a }
            throw r3     // Catch:{ Exception -> 0x003d }
        L_0x003d:
            r0 = move-exception
            org.apache.mina.core.filterchain.IoFilterChain r3 = r6.getFilterChain()
            r3.fireExceptionCaught(r0)
            goto L_0x001d
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.buffer.BufferedWriteFilter.write(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.core.buffer.IoBuffer):void");
    }

    private void internalFlush(IoFilter.NextFilter nextFilter, IoSession session, IoBuffer buf) throws Exception {
        IoBuffer tmp;
        synchronized (buf) {
            buf.flip();
            tmp = buf.duplicate();
            buf.clear();
        }
        this.logger.debug("Flushing buffer: {}", (Object) tmp);
        nextFilter.filterWrite(session, new DefaultWriteRequest(tmp));
    }

    public void flush(IoSession session) {
        try {
            internalFlush(session.getFilterChain().getNextFilter((IoFilter) this), session, this.buffersMap.get(session));
        } catch (Exception e) {
            session.getFilterChain().fireExceptionCaught(e);
        }
    }

    private void free(IoSession session) {
        IoBuffer buf = this.buffersMap.remove(session);
        if (buf != null) {
            buf.free();
        }
    }

    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        free(session);
        nextFilter.exceptionCaught(session, cause);
    }

    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        free(session);
        nextFilter.sessionClosed(session);
    }
}
