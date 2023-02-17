package org.apache.mina.core.future;

import java.util.concurrent.TimeUnit;
import org.apache.mina.core.session.IoSession;

public interface IoFuture {
    IoFuture addListener(IoFutureListener<?> ioFutureListener);

    IoFuture await() throws InterruptedException;

    boolean await(long j) throws InterruptedException;

    boolean await(long j, TimeUnit timeUnit) throws InterruptedException;

    IoFuture awaitUninterruptibly();

    boolean awaitUninterruptibly(long j);

    boolean awaitUninterruptibly(long j, TimeUnit timeUnit);

    IoSession getSession();

    boolean isDone();

    @Deprecated
    void join();

    @Deprecated
    boolean join(long j);

    IoFuture removeListener(IoFutureListener<?> ioFutureListener);
}
