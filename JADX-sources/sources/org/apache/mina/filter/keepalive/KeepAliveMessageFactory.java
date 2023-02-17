package org.apache.mina.filter.keepalive;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface KeepAliveMessageFactory {
    Object getRequest(IoSession ioSession);

    Object getResponse(IoSession ioSession, Object obj);

    boolean isRequest(IoSession ioSession, Object obj);

    boolean isResponse(IoSession ioSession, Object obj);
}
