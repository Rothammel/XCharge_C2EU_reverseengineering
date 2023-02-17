package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface ConnectFuture extends IoFuture {
    @Override // org.apache.mina.core.future.IoFuture
    ConnectFuture addListener(IoFutureListener<?> ioFutureListener);

    @Override // org.apache.mina.core.future.IoFuture
    ConnectFuture await() throws InterruptedException;

    @Override // org.apache.mina.core.future.IoFuture
    ConnectFuture awaitUninterruptibly();

    boolean cancel();

    Throwable getException();

    @Override // org.apache.mina.core.future.IoFuture
    IoSession getSession();

    boolean isCanceled();

    boolean isConnected();

    @Override // org.apache.mina.core.future.IoFuture
    ConnectFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setException(Throwable th);

    void setSession(IoSession ioSession);
}