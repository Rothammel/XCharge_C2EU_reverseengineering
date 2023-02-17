package org.apache.mina.core.future;

public interface WriteFuture extends IoFuture {
    WriteFuture addListener(IoFutureListener<?> ioFutureListener);

    WriteFuture await() throws InterruptedException;

    WriteFuture awaitUninterruptibly();

    Throwable getException();

    boolean isWritten();

    WriteFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setException(Throwable th);

    void setWritten();
}
