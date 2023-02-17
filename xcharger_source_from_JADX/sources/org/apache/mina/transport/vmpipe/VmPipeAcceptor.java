package org.apache.mina.transport.vmpipe;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;

public final class VmPipeAcceptor extends AbstractIoAcceptor {
    static final Map<VmPipeAddress, VmPipe> boundHandlers = new HashMap();
    private IdleStatusChecker idleChecker;

    public VmPipeAcceptor() {
        this((Executor) null);
    }

    public VmPipeAcceptor(Executor executor) {
        super(new DefaultVmPipeSessionConfig(), executor);
        this.idleChecker = new IdleStatusChecker();
        executeWorker(this.idleChecker.getNotifyingTask(), "idleStatusChecker");
    }

    public TransportMetadata getTransportMetadata() {
        return VmPipeSession.METADATA;
    }

    public VmPipeSessionConfig getSessionConfig() {
        return (VmPipeSessionConfig) this.sessionConfig;
    }

    public VmPipeAddress getLocalAddress() {
        return (VmPipeAddress) super.getLocalAddress();
    }

    public VmPipeAddress getDefaultLocalAddress() {
        return (VmPipeAddress) super.getDefaultLocalAddress();
    }

    public void setDefaultLocalAddress(VmPipeAddress localAddress) {
        super.setDefaultLocalAddress(localAddress);
    }

    /* access modifiers changed from: protected */
    public void dispose0() throws Exception {
        this.idleChecker.getNotifyingTask().cancel();
        unbind();
    }

    /* access modifiers changed from: protected */
    public Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws IOException {
        Set<SocketAddress> newLocalAddresses = new HashSet<>();
        synchronized (boundHandlers) {
            for (SocketAddress a : localAddresses) {
                VmPipeAddress localAddress = (VmPipeAddress) a;
                if (localAddress == null || localAddress.getPort() == 0) {
                    localAddress = null;
                    int i = 10000;
                    while (true) {
                        if (i >= Integer.MAX_VALUE) {
                            break;
                        }
                        VmPipeAddress newLocalAddress = new VmPipeAddress(i);
                        if (!boundHandlers.containsKey(newLocalAddress) && !newLocalAddresses.contains(newLocalAddress)) {
                            localAddress = newLocalAddress;
                            break;
                        }
                        i++;
                    }
                    if (localAddress == null) {
                        throw new IOException("No port available.");
                    }
                } else if (localAddress.getPort() < 0) {
                    throw new IOException("Bind port number must be 0 or above.");
                } else if (boundHandlers.containsKey(localAddress)) {
                    throw new IOException("Address already bound: " + localAddress);
                }
                newLocalAddresses.add(localAddress);
            }
            for (SocketAddress a2 : newLocalAddresses) {
                VmPipeAddress localAddress2 = (VmPipeAddress) a2;
                if (!boundHandlers.containsKey(localAddress2)) {
                    boundHandlers.put(localAddress2, new VmPipe(this, localAddress2, getHandler(), getListeners()));
                } else {
                    for (SocketAddress a22 : newLocalAddresses) {
                        boundHandlers.remove(a22);
                    }
                    throw new IOException("Duplicate local address: " + a2);
                }
            }
        }
        return newLocalAddresses;
    }

    /* access modifiers changed from: protected */
    public void unbind0(List<? extends SocketAddress> localAddresses) {
        synchronized (boundHandlers) {
            for (SocketAddress a : localAddresses) {
                boundHandlers.remove(a);
            }
        }
    }

    public IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void doFinishSessionInitialization(IoSession session, IoFuture future) {
        initSession(session, future, (IoSessionInitializer) null);
    }
}
