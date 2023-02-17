package org.apache.mina.handler.demux;

import java.lang.Throwable;
import org.apache.mina.core.session.IoSession;

public interface ExceptionHandler<E extends Throwable> {
    public static final ExceptionHandler<Throwable> CLOSE = new ExceptionHandler<Throwable>() {
        public void exceptionCaught(IoSession session, Throwable cause) {
            session.closeNow();
        }
    };
    public static final ExceptionHandler<Throwable> NOOP = new ExceptionHandler<Throwable>() {
        public void exceptionCaught(IoSession session, Throwable cause) {
        }
    };

    void exceptionCaught(IoSession ioSession, E e) throws Exception;
}
