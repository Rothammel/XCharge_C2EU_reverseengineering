package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

public class DefaultWriteFuture extends DefaultIoFuture implements WriteFuture {
    public static WriteFuture newWrittenFuture(IoSession session) {
        DefaultWriteFuture writtenFuture = new DefaultWriteFuture(session);
        writtenFuture.setWritten();
        return writtenFuture;
    }

    public static WriteFuture newNotWrittenFuture(IoSession session, Throwable cause) {
        DefaultWriteFuture unwrittenFuture = new DefaultWriteFuture(session);
        unwrittenFuture.setException(cause);
        return unwrittenFuture;
    }

    public DefaultWriteFuture(IoSession session) {
        super(session);
    }

    public boolean isWritten() {
        if (isDone()) {
            Object v = getValue();
            if (v instanceof Boolean) {
                return ((Boolean) v).booleanValue();
            }
        }
        return false;
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

    public void setWritten() {
        setValue(Boolean.TRUE);
    }

    public void setException(Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }
        setValue(exception);
    }

    public WriteFuture await() throws InterruptedException {
        return (WriteFuture) super.await();
    }

    public WriteFuture awaitUninterruptibly() {
        return (WriteFuture) super.awaitUninterruptibly();
    }

    public WriteFuture addListener(IoFutureListener<?> listener) {
        return (WriteFuture) super.addListener(listener);
    }

    public WriteFuture removeListener(IoFutureListener<?> listener) {
        return (WriteFuture) super.removeListener(listener);
    }
}
