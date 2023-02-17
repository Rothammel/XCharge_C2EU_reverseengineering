package org.apache.mina.core.session;

import org.apache.mina.core.future.IoFuture;

public interface IoSessionInitializer<T extends IoFuture> {
    void initializeSession(IoSession ioSession, T t);
}
