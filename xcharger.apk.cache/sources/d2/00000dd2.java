package org.apache.mina.filter.executor;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public class WriteRequestFilter extends IoFilterAdapter {
    private final IoEventQueueHandler queueHandler;

    public WriteRequestFilter() {
        this(new IoEventQueueThrottle());
    }

    public WriteRequestFilter(IoEventQueueHandler queueHandler) {
        if (queueHandler == null) {
            throw new IllegalArgumentException("queueHandler");
        }
        this.queueHandler = queueHandler;
    }

    public IoEventQueueHandler getQueueHandler() {
        return this.queueHandler;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        final IoEvent e = new IoEvent(IoEventType.WRITE, session, writeRequest);
        if (this.queueHandler.accept(this, e)) {
            nextFilter.filterWrite(session, writeRequest);
            WriteFuture writeFuture = writeRequest.getFuture();
            if (writeFuture != null) {
                this.queueHandler.offered(this, e);
                writeFuture.addListener((IoFutureListener<?>) new IoFutureListener<WriteFuture>() { // from class: org.apache.mina.filter.executor.WriteRequestFilter.1
                    @Override // org.apache.mina.core.future.IoFutureListener
                    public void operationComplete(WriteFuture future) {
                        WriteRequestFilter.this.queueHandler.polled(WriteRequestFilter.this, e);
                    }
                });
            }
        }
    }
}