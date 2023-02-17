package org.apache.mina.filter.stream;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public abstract class AbstractStreamWriteFilter<T> extends IoFilterAdapter {
    public static final int DEFAULT_STREAM_BUFFER_SIZE = 4096;
    protected final AttributeKey CURRENT_STREAM = new AttributeKey(getClass(), "stream");
    protected final AttributeKey WRITE_REQUEST_QUEUE = new AttributeKey(getClass(), "queue");
    protected final AttributeKey CURRENT_WRITE_REQUEST = new AttributeKey(getClass(), "writeRequest");
    private int writeBufferSize = 4096;

    protected abstract Class<T> getMessageClass();

    protected abstract IoBuffer getNextBuffer(T t) throws IOException;

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPreAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        Class<?> cls = getClass();
        if (parent.contains((Class<? extends IoFilter>) cls)) {
            throw new IllegalStateException("Only one " + cls.getName() + " is permitted.");
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (session.getAttribute(this.CURRENT_STREAM) != null) {
            Queue<WriteRequest> queue = getWriteRequestQueue(session);
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
                session.setAttribute(this.WRITE_REQUEST_QUEUE, queue);
            }
            queue.add(writeRequest);
            return;
        }
        Object message = writeRequest.getMessage();
        if (getMessageClass().isInstance(message)) {
            T stream = getMessageClass().cast(message);
            IoBuffer buffer = getNextBuffer(stream);
            if (buffer == null) {
                writeRequest.getFuture().setWritten();
                nextFilter.messageSent(session, writeRequest);
                return;
            }
            session.setAttribute(this.CURRENT_STREAM, message);
            session.setAttribute(this.CURRENT_WRITE_REQUEST, writeRequest);
            nextFilter.filterWrite(session, new DefaultWriteRequest(buffer));
            return;
        }
        nextFilter.filterWrite(session, writeRequest);
    }

    private Queue<WriteRequest> getWriteRequestQueue(IoSession session) {
        return (Queue) session.getAttribute(this.WRITE_REQUEST_QUEUE);
    }

    private Queue<WriteRequest> removeWriteRequestQueue(IoSession session) {
        return (Queue) session.removeAttribute(this.WRITE_REQUEST_QUEUE);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        T stream = getMessageClass().cast(session.getAttribute(this.CURRENT_STREAM));
        if (stream == null) {
            nextFilter.messageSent(session, writeRequest);
            return;
        }
        IoBuffer buffer = getNextBuffer(stream);
        if (buffer == null) {
            session.removeAttribute(this.CURRENT_STREAM);
            WriteRequest currentWriteRequest = (WriteRequest) session.removeAttribute(this.CURRENT_WRITE_REQUEST);
            Queue<WriteRequest> queue = removeWriteRequestQueue(session);
            if (queue != null) {
                WriteRequest wr = queue.poll();
                while (wr != null) {
                    filterWrite(nextFilter, session, wr);
                    WriteRequest wr2 = queue.poll();
                    wr = wr2;
                }
            }
            currentWriteRequest.getFuture().setWritten();
            nextFilter.messageSent(session, currentWriteRequest);
            return;
        }
        nextFilter.filterWrite(session, new DefaultWriteRequest(buffer));
    }

    public int getWriteBufferSize() {
        return this.writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        if (writeBufferSize < 1) {
            throw new IllegalArgumentException("writeBufferSize must be at least 1");
        }
        this.writeBufferSize = writeBufferSize;
    }
}
