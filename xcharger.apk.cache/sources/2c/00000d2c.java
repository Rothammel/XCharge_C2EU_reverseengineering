package org.apache.mina.core.future;

/* loaded from: classes.dex */
public interface WriteFuture extends IoFuture {
    @Override // org.apache.mina.core.future.IoFuture
    WriteFuture addListener(IoFutureListener<?> ioFutureListener);

    @Override // org.apache.mina.core.future.IoFuture
    WriteFuture await() throws InterruptedException;

    @Override // org.apache.mina.core.future.IoFuture
    WriteFuture awaitUninterruptibly();

    Throwable getException();

    boolean isWritten();

    @Override // org.apache.mina.core.future.IoFuture
    WriteFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setException(Throwable th);

    void setWritten();
}