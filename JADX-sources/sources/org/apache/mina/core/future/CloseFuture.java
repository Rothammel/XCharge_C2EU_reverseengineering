package org.apache.mina.core.future;

/* loaded from: classes.dex */
public interface CloseFuture extends IoFuture {
    @Override // org.apache.mina.core.future.IoFuture
    CloseFuture addListener(IoFutureListener<?> ioFutureListener);

    @Override // org.apache.mina.core.future.IoFuture
    CloseFuture await() throws InterruptedException;

    @Override // org.apache.mina.core.future.IoFuture
    CloseFuture awaitUninterruptibly();

    boolean isClosed();

    @Override // org.apache.mina.core.future.IoFuture
    CloseFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setClosed();
}
