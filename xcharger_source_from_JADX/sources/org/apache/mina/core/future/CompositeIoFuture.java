package org.apache.mina.core.future;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.session.IoSession;

public class CompositeIoFuture<E extends IoFuture> extends DefaultIoFuture {
    /* access modifiers changed from: private */
    public volatile boolean constructionFinished;
    private final CompositeIoFuture<E>.NotifyingListener listener = new NotifyingListener();
    /* access modifiers changed from: private */
    public final AtomicInteger unnotified = new AtomicInteger();

    public CompositeIoFuture(Iterable<E> children) {
        super((IoSession) null);
        for (E f : children) {
            f.addListener(this.listener);
            this.unnotified.incrementAndGet();
        }
        this.constructionFinished = true;
        if (this.unnotified.get() == 0) {
            setValue(true);
        }
    }

    private class NotifyingListener implements IoFutureListener<IoFuture> {
        private NotifyingListener() {
        }

        public void operationComplete(IoFuture future) {
            if (CompositeIoFuture.this.unnotified.decrementAndGet() == 0 && CompositeIoFuture.this.constructionFinished) {
                CompositeIoFuture.this.setValue(true);
            }
        }
    }
}
