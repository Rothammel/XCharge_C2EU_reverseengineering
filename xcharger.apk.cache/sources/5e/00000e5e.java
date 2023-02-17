package org.apache.mina.transport.vmpipe;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoServiceListenerSupport;

/* loaded from: classes.dex */
class VmPipe {
    private final VmPipeAcceptor acceptor;
    private final VmPipeAddress address;
    private final IoHandler handler;
    private final IoServiceListenerSupport listeners;

    /* JADX INFO: Access modifiers changed from: package-private */
    public VmPipe(VmPipeAcceptor acceptor, VmPipeAddress address, IoHandler handler, IoServiceListenerSupport listeners) {
        this.acceptor = acceptor;
        this.address = address;
        this.handler = handler;
        this.listeners = listeners;
    }

    public VmPipeAcceptor getAcceptor() {
        return this.acceptor;
    }

    public VmPipeAddress getAddress() {
        return this.address;
    }

    public IoHandler getHandler() {
        return this.handler;
    }

    public IoServiceListenerSupport getListeners() {
        return this.listeners;
    }
}