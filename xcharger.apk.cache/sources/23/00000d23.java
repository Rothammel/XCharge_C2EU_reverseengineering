package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public class DefaultCloseFuture extends DefaultIoFuture implements CloseFuture {
    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public /* bridge */ /* synthetic */ IoFuture addListener(IoFutureListener ioFutureListener) {
        return addListener((IoFutureListener<?>) ioFutureListener);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public /* bridge */ /* synthetic */ IoFuture removeListener(IoFutureListener ioFutureListener) {
        return removeListener((IoFutureListener<?>) ioFutureListener);
    }

    public DefaultCloseFuture(IoSession session) {
        super(session);
    }

    @Override // org.apache.mina.core.future.CloseFuture
    public boolean isClosed() {
        if (isDone()) {
            return ((Boolean) getValue()).booleanValue();
        }
        return false;
    }

    @Override // org.apache.mina.core.future.CloseFuture
    public void setClosed() {
        setValue(Boolean.TRUE);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public CloseFuture await() throws InterruptedException {
        return (CloseFuture) super.await();
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public CloseFuture awaitUninterruptibly() {
        return (CloseFuture) super.awaitUninterruptibly();
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public CloseFuture addListener(IoFutureListener<?> listener) {
        return (CloseFuture) super.addListener(listener);
    }

    @Override // org.apache.mina.core.future.DefaultIoFuture, org.apache.mina.core.future.IoFuture
    public CloseFuture removeListener(IoFutureListener<?> listener) {
        return (CloseFuture) super.removeListener(listener);
    }
}