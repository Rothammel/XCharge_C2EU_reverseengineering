package org.apache.mina.filter.buffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.util.LazyInitializedCacheMap;
import org.apache.mina.util.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public final class BufferedWriteFilter extends IoFilterAdapter {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private int bufferSize;
    private final LazyInitializedCacheMap<IoSession, IoBuffer> buffersMap;
    private final Logger logger;

    public BufferedWriteFilter() {
        this(8192, null);
    }

    public BufferedWriteFilter(int bufferSize) {
        this(bufferSize, null);
    }

    public BufferedWriteFilter(int bufferSize, LazyInitializedCacheMap<IoSession, IoBuffer> buffersMap) {
        this.logger = LoggerFactory.getLogger(BufferedWriteFilter.class);
        this.bufferSize = 8192;
        this.bufferSize = bufferSize;
        if (buffersMap == null) {
            this.buffersMap = new LazyInitializedCacheMap<>();
        } else {
            this.buffersMap = buffersMap;
        }
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object data = writeRequest.getMessage();
        if (data instanceof IoBuffer) {
            write(session, (IoBuffer) data);
            return;
        }
        throw new IllegalArgumentException("This filter should only buffer IoBuffer objects");
    }

    private void write(IoSession session, IoBuffer data) {
        IoBuffer dest = this.buffersMap.putIfAbsent((LazyInitializedCacheMap<IoSession, IoBuffer>) session, (LazyInitializer<IoBuffer>) new IoBufferLazyInitializer(this.bufferSize));
        write(session, data, dest);
    }

    private void write(IoSession session, IoBuffer data, IoBuffer buf) {
        try {
            int len = data.remaining();
            if (len >= buf.capacity()) {
                IoFilter.NextFilter nextFilter = session.getFilterChain().getNextFilter(this);
                internalFlush(nextFilter, session, buf);
                nextFilter.filterWrite(session, new DefaultWriteRequest(data));
                return;
            }
            if (len > buf.limit() - buf.position()) {
                internalFlush(session.getFilterChain().getNextFilter(this), session, buf);
            }
            synchronized (buf) {
                buf.put(data);
            }
        } catch (Exception e) {
            session.getFilterChain().fireExceptionCaught(e);
        }
    }

    private void internalFlush(IoFilter.NextFilter nextFilter, IoSession session, IoBuffer buf) throws Exception {
        IoBuffer tmp;
        synchronized (buf) {
            buf.flip();
            tmp = buf.duplicate();
            buf.clear();
        }
        this.logger.debug("Flushing buffer: {}", tmp);
        nextFilter.filterWrite(session, new DefaultWriteRequest(tmp));
    }

    public void flush(IoSession session) {
        try {
            internalFlush(session.getFilterChain().getNextFilter(this), session, this.buffersMap.get(session));
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

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        free(session);
        nextFilter.exceptionCaught(session, cause);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        free(session);
        nextFilter.sessionClosed(session);
    }
}
