package org.apache.mina.core.future;

public interface CloseFuture extends IoFuture {
    CloseFuture addListener(IoFutureListener<?> ioFutureListener);

    CloseFuture await() throws InterruptedException;

    CloseFuture awaitUninterruptibly();

    boolean isClosed();

    CloseFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setClosed();
}
