package org.apache.mina.transport.vmpipe;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoServiceListenerSupport;

class VmPipe {
    private final VmPipeAcceptor acceptor;
    private final VmPipeAddress address;
    private final IoHandler handler;
    private final IoServiceListenerSupport listeners;

    VmPipe(VmPipeAcceptor acceptor2, VmPipeAddress address2, IoHandler handler2, IoServiceListenerSupport listeners2) {
        this.acceptor = acceptor2;
        this.address = address2;
        this.handler = handler2;
        this.listeners = listeners2;
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
