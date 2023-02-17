package org.apache.mina.filter.executor;

import java.util.EventListener;
import org.apache.mina.core.session.IoEvent;

/* loaded from: classes.dex */
public interface IoEventQueueHandler extends EventListener {
    public static final IoEventQueueHandler NOOP = new IoEventQueueHandler() { // from class: org.apache.mina.filter.executor.IoEventQueueHandler.1
        @Override // org.apache.mina.filter.executor.IoEventQueueHandler
        public boolean accept(Object source, IoEvent event) {
            return true;
        }

        @Override // org.apache.mina.filter.executor.IoEventQueueHandler
        public void offered(Object source, IoEvent event) {
        }

        @Override // org.apache.mina.filter.executor.IoEventQueueHandler
        public void polled(Object source, IoEvent event) {
        }
    };

    boolean accept(Object obj, IoEvent ioEvent);

    void offered(Object obj, IoEvent ioEvent);

    void polled(Object obj, IoEvent ioEvent);
}