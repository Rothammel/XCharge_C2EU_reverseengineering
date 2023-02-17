package org.apache.mina.core.future;

/* loaded from: classes.dex */
public interface ReadFuture extends IoFuture {
    @Override // org.apache.mina.core.future.IoFuture
    ReadFuture addListener(IoFutureListener<?> ioFutureListener);

    @Override // org.apache.mina.core.future.IoFuture
    ReadFuture await() throws InterruptedException;

    @Override // org.apache.mina.core.future.IoFuture
    ReadFuture awaitUninterruptibly();

    Throwable getException();

    Object getMessage();

    boolean isClosed();

    boolean isRead();

    @Override // org.apache.mina.core.future.IoFuture
    ReadFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setClosed();

    void setException(Throwable th);

    void setRead(Object obj);
}
