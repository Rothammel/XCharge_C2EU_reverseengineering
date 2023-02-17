package org.apache.mina.proxy.session;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;

/* loaded from: classes.dex */
public class ProxyIoSessionInitializer<T extends ConnectFuture> implements IoSessionInitializer<T> {
    private final ProxyIoSession proxyIoSession;
    private final IoSessionInitializer<T> wrappedSessionInitializer;

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.session.IoSessionInitializer
    public /* bridge */ /* synthetic */ void initializeSession(IoSession ioSession, IoFuture ioFuture) {
        initializeSession(ioSession, (IoSession) ((ConnectFuture) ioFuture));
    }

    public ProxyIoSessionInitializer(IoSessionInitializer<T> wrappedSessionInitializer, ProxyIoSession proxyIoSession) {
        this.wrappedSessionInitializer = wrappedSessionInitializer;
        this.proxyIoSession = proxyIoSession;
    }

    public ProxyIoSession getProxySession() {
        return this.proxyIoSession;
    }

    public void initializeSession(IoSession session, T future) {
        if (this.wrappedSessionInitializer != null) {
            this.wrappedSessionInitializer.initializeSession(session, future);
        }
        if (this.proxyIoSession != null) {
            this.proxyIoSession.setSession(session);
            session.setAttribute(ProxyIoSession.PROXY_SESSION, this.proxyIoSession);
        }
    }
}