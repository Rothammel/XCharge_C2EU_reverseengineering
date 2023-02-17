package org.apache.mina.core.future;

import java.io.IOException;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.session.IoSession;

public class DefaultReadFuture extends DefaultIoFuture implements ReadFuture {
    private static final Object CLOSED = new Object();

    public DefaultReadFuture(IoSession session) {
        super(session);
    }

    public Object getMessage() {
        if (!isDone()) {
            return null;
        }
        Object v = getValue();
        if (v == CLOSED) {
            return null;
        }
        if (v instanceof RuntimeException) {
            throw ((RuntimeException) v);
        } else if (v instanceof Error) {
            throw ((Error) v);
        } else if (!(v instanceof IOException) && !(v instanceof Exception)) {
            return v;
        } else {
            throw new RuntimeIoException((Throwable) (Exception) v);
        }
    }

    public boolean isRead() {
        Object v;
        if (!isDone() || (v = getValue()) == CLOSED || (v instanceof Throwable)) {
            return false;
        }
        return true;
    }

    public boolean isClosed() {
        if (!isDone() || getValue() != CLOSED) {
            return false;
        }
        return true;
    }

    public Throwable getException() {
        if (isDone()) {
            Object v = getValue();
            if (v instanceof Throwable) {
                return (Throwable) v;
            }
        }
        return null;
    }

    public void setClosed() {
        setValue(CLOSED);
    }

    public void setRead(Object message) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        setValue(message);
    }

    public void setException(Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }
        setValue(exception);
    }

    public ReadFuture await() throws InterruptedException {
        return (ReadFuture) super.await();
    }

    public ReadFuture awaitUninterruptibly() {
        return (ReadFuture) super.awaitUninterruptibly();
    }

    public ReadFuture addListener(IoFutureListener<?> listener) {
        return (ReadFuture) super.addListener(listener);
    }

    public ReadFuture removeListener(IoFutureListener<?> listener) {
        return (ReadFuture) super.removeListener(listener);
    }
}
