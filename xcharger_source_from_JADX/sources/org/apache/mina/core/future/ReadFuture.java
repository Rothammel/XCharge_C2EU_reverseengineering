package org.apache.mina.core.future;

public interface ReadFuture extends IoFuture {
    ReadFuture addListener(IoFutureListener<?> ioFutureListener);

    ReadFuture await() throws InterruptedException;

    ReadFuture awaitUninterruptibly();

    Throwable getException();

    Object getMessage();

    boolean isClosed();

    boolean isRead();

    ReadFuture removeListener(IoFutureListener<?> ioFutureListener);

    void setClosed();

    void setException(Throwable th);

    void setRead(Object obj);
}
