package org.apache.mina.filter.executor;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class WriteRequestFilter extends IoFilterAdapter {
    /* access modifiers changed from: private */
    public final IoEventQueueHandler queueHandler;

    public WriteRequestFilter() {
        this(new IoEventQueueThrottle());
    }

    public WriteRequestFilter(IoEventQueueHandler queueHandler2) {
        if (queueHandler2 == null) {
            throw new IllegalArgumentException("queueHandler");
        }
        this.queueHandler = queueHandler2;
    }

    public IoEventQueueHandler getQueueHandler() {
        return this.queueHandler;
    }

    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        final IoEvent e = new IoEvent(IoEventType.WRITE, session, writeRequest);
        if (this.queueHandler.accept(this, e)) {
            nextFilter.filterWrite(session, writeRequest);
            WriteFuture writeFuture = writeRequest.getFuture();
            if (writeFuture != null) {
                this.queueHandler.offered(this, e);
                writeFuture.addListener(new IoFutureListener<WriteFuture>() {
                    public void operationComplete(WriteFuture future) {
                        WriteRequestFilter.this.queueHandler.polled(WriteRequestFilter.this, e);
                    }
                });
            }
        }
    }
}
