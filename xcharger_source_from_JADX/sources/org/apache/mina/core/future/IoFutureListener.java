package org.apache.mina.core.future;

import java.util.EventListener;
import org.apache.mina.core.future.IoFuture;

public interface IoFutureListener<F extends IoFuture> extends EventListener {
    public static final IoFutureListener<IoFuture> CLOSE = new IoFutureListener<IoFuture>() {
        public void operationComplete(IoFuture future) {
            future.getSession().closeNow();
        }
    };

    void operationComplete(F f);
}
