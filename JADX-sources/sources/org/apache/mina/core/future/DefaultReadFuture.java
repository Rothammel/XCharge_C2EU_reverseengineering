package org.apache.mina.core.future;

import java.io.IOException;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public class DefaultReadFuture extends DefaultIoFuture implements ReadFuture {
    private static final Object CLOSED = new Object();

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public /* bridge */ /* synthetic */ IoFuture addListener(IoFutureListener ioFutureListener) {
        return addListener((IoFutureListener<?>) ioFutureListener);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public /* bridge */ /* synthetic */ IoFuture removeListener(IoFutureListener ioFutureListener) {
        return removeListener((IoFutureListener<?>) ioFutureListener);
    }

    public DefaultReadFuture(IoSession session) {
        super(session);
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public Object getMessage() {
        Object v;
        if (isDone() && (v = getValue()) != CLOSED) {
            if (v instanceof RuntimeException) {
                throw ((RuntimeException) v);
            }
            if (v instanceof Error) {
                throw ((Error) v);
            }
            if ((v instanceof IOException) || (v instanceof Exception)) {
                throw new RuntimeIoException((Exception) v);
            }
            return v;
        }
        return null;
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public boolean isRead() {
        Object v;
        return (!isDone() || (v = getValue()) == CLOSED || (v instanceof Throwable)) ? false : true;
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public boolean isClosed() {
        return isDone() && getValue() == CLOSED;
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public Throwable getException() {
        if (isDone()) {
            Object v = getValue();
            if (v instanceof Throwable) {
                return (Throwable) v;
            }
        }
        return null;
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public void setClosed() {
        setValue(CLOSED);
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public void setRead(Object message) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        setValue(message);
    }

    @Override // org.apache.mina.core.future.ReadFuture
    public void setException(Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }
        setValue(exception);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public ReadFuture await() throws InterruptedException {
        return (ReadFuture) super.await();
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public ReadFuture awaitUninterruptibly() {
        return (ReadFuture) super.awaitUninterruptibly();
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public ReadFuture addListener(IoFutureListener<?> listener) {
        return (ReadFuture) super.addListener(listener);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public ReadFuture removeListener(IoFutureListener<?> listener) {
        return (ReadFuture) super.removeListener(listener);
    }
}
