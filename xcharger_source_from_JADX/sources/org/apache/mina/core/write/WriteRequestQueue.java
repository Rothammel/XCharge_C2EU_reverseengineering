package org.apache.mina.core.write;

import org.apache.mina.core.session.IoSession;

public interface WriteRequestQueue {
    void clear(IoSession ioSession);

    void dispose(IoSession ioSession);

    boolean isEmpty(IoSession ioSession);

    void offer(IoSession ioSession, WriteRequest writeRequest);

    WriteRequest poll(IoSession ioSession);

    int size();
}
