package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public class DefaultWriteFuture extends DefaultIoFuture implements WriteFuture {
    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public /* bridge */ /* synthetic */ IoFuture addListener(IoFutureListener ioFutureListener) {
        return addListener((IoFutureListener<?>) ioFutureListener);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public /* bridge */ /* synthetic */ IoFuture removeListener(IoFutureListener ioFutureListener) {
        return removeListener((IoFutureListener<?>) ioFutureListener);
    }

    public static WriteFuture newWrittenFuture(IoSession session) {
        DefaultWriteFuture writtenFuture = new DefaultWriteFuture(session);
        writtenFuture.setWritten();
        return writtenFuture;
    }

    public static WriteFuture newNotWrittenFuture(IoSession session, Throwable cause) {
        DefaultWriteFuture unwrittenFuture = new DefaultWriteFuture(session);
        unwrittenFuture.setException(cause);
        return unwrittenFuture;
    }

    public DefaultWriteFuture(IoSession session) {
        super(session);
    }

    @Override // org.apache.mina.core.future.WriteFuture
    public boolean isWritten() {
        if (isDone()) {
            Object v = getValue();
            if (v instanceof Boolean) {
                return ((Boolean) v).booleanValue();
            }
        }
        return false;
    }

    @Override // org.apache.mina.core.future.WriteFuture
    public Throwable getException() {
        if (isDone()) {
            Object v = getValue();
            if (v instanceof Throwable) {
                return (Throwable) v;
            }
        }
        return null;
    }

    @Override // org.apache.mina.core.future.WriteFuture
    public void setWritten() {
        setValue(Boolean.TRUE);
    }

    @Override // org.apache.mina.core.future.WriteFuture
    public void setException(Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }
        setValue(exception);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public WriteFuture await() throws InterruptedException {
        return (WriteFuture) super.await();
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public WriteFuture awaitUninterruptibly() {
        return (WriteFuture) super.awaitUninterruptibly();
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public WriteFuture addListener(IoFutureListener<?> listener) {
        return (WriteFuture) super.addListener(listener);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public WriteFuture removeListener(IoFutureListener<?> listener) {
        return (WriteFuture) super.removeListener(listener);
    }
}