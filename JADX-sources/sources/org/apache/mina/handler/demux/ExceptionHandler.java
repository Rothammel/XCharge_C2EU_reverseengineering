package org.apache.mina.handler.demux;

import java.lang.Throwable;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface ExceptionHandler<E extends Throwable> {
    public static final ExceptionHandler<Throwable> NOOP = new ExceptionHandler<Throwable>() { // from class: org.apache.mina.handler.demux.ExceptionHandler.1
        @Override // org.apache.mina.handler.demux.ExceptionHandler
        public void exceptionCaught(IoSession session, Throwable cause) {
        }
    };
    public static final ExceptionHandler<Throwable> CLOSE = new ExceptionHandler<Throwable>() { // from class: org.apache.mina.handler.demux.ExceptionHandler.2
        @Override // org.apache.mina.handler.demux.ExceptionHandler
        public void exceptionCaught(IoSession session, Throwable cause) {
            session.closeNow();
        }
    };

    void exceptionCaught(IoSession ioSession, E e) throws Exception;
}
