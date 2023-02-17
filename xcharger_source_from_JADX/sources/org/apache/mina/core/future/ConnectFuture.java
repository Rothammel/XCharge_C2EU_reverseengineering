package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

public interface ConnectFuture extends IoFuture {
    ConnectFuture addListener(IoFutureListener<?> ioFutureListener);

    ConnectFuture await() throws InterruptedException;

    ConnectFuture awaitUninterruptibly();

    boolean cancel();

    Throwable getException();

    IoSession getSession();

    boolean isCanceled();

    boolean isConnected();

    ConnectFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setException(Throwable th);

    void setSession(IoSession ioSession);
}
