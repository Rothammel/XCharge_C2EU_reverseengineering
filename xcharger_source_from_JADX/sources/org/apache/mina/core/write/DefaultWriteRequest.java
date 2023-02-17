package org.apache.mina.core.write;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

public class DefaultWriteRequest implements WriteRequest {
    public static final byte[] EMPTY_MESSAGE = new byte[0];
    private static final WriteFuture UNUSED_FUTURE = new WriteFuture() {
        public boolean isWritten() {
            return false;
        }

        public void setWritten() {
        }

        public IoSession getSession() {
            return null;
        }

        public void join() {
        }

        public boolean join(long timeoutInMillis) {
            return true;
        }

        public boolean isDone() {
            return true;
        }

        public WriteFuture addListener(IoFutureListener<?> ioFutureListener) {
            throw new IllegalStateException("You can't add a listener to a dummy future.");
        }

        public WriteFuture removeListener(IoFutureListener<?> ioFutureListener) {
            throw new IllegalStateException("You can't add a listener to a dummy future.");
        }

        public WriteFuture await() throws InterruptedException {
            return this;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        public boolean await(long timeoutMillis) throws InterruptedException {
            return true;
        }

        public WriteFuture awaitUninterruptibly() {
            return this;
        }

        public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
            return true;
        }

        public boolean awaitUninterruptibly(long timeoutMillis) {
            return true;
        }

        public Throwable getException() {
            return null;
        }

        public void setException(Throwable cause) {
        }
    };
    private final SocketAddress destination;
    private final WriteFuture future;
    private final Object message;

    public DefaultWriteRequest(Object message2) {
        this(message2, (WriteFuture) null, (SocketAddress) null);
    }

    public DefaultWriteRequest(Object message2, WriteFuture future2) {
        this(message2, future2, (SocketAddress) null);
    }

    public DefaultWriteRequest(Object message2, WriteFuture future2, SocketAddress destination2) {
        if (message2 == null) {
            throw new IllegalArgumentException("message");
        }
        future2 = future2 == null ? UNUSED_FUTURE : future2;
        this.message = message2;
        this.future = future2;
        this.destination = destination2;
    }

    public WriteFuture getFuture() {
        return this.future;
    }

    public Object getMessage() {
        return this.message;
    }

    public WriteRequest getOriginalRequest() {
        return this;
    }

    public SocketAddress getDestination() {
        return this.destination;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WriteRequest: ");
        if (this.message.getClass().getName().equals(Object.class.getName())) {
            sb.append("CLOSE_REQUEST");
        } else if (getDestination() == null) {
            sb.append(this.message);
        } else {
            sb.append(this.message);
            sb.append(" => ");
            sb.append(getDestination());
        }
        return sb.toString();
    }

    public boolean isEncoded() {
        return false;
    }
}
