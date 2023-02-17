package org.apache.mina.transport.vmpipe;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public final class VmPipeAcceptor extends AbstractIoAcceptor {
    static final Map<VmPipeAddress, VmPipe> boundHandlers = new HashMap();
    private IdleStatusChecker idleChecker;

    public VmPipeAcceptor() {
        this(null);
    }

    public VmPipeAcceptor(Executor executor) {
        super(new DefaultVmPipeSessionConfig(), executor);
        this.idleChecker = new IdleStatusChecker();
        executeWorker(this.idleChecker.getNotifyingTask(), "idleStatusChecker");
    }

    @Override // org.apache.mina.core.service.IoService
    public TransportMetadata getTransportMetadata() {
        return VmPipeSession.METADATA;
    }

    @Override // org.apache.mina.core.service.IoService
    public VmPipeSessionConfig getSessionConfig() {
        return (VmPipeSessionConfig) this.sessionConfig;
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor, org.apache.mina.core.service.IoAcceptor
    public VmPipeAddress getLocalAddress() {
        return (VmPipeAddress) super.getLocalAddress();
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor, org.apache.mina.core.service.IoAcceptor
    public VmPipeAddress getDefaultLocalAddress() {
        return (VmPipeAddress) super.getDefaultLocalAddress();
    }

    public void setDefaultLocalAddress(VmPipeAddress localAddress) {
        super.setDefaultLocalAddress((SocketAddress) localAddress);
    }

    @Override // org.apache.mina.core.service.AbstractIoService
    protected void dispose0() throws Exception {
        this.idleChecker.getNotifyingTask().cancel();
        unbind();
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor
    protected Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws IOException {
        Set<SocketAddress> newLocalAddresses = new HashSet<>();
        synchronized (boundHandlers) {
            Iterator<? extends SocketAddress> it2 = localAddresses.iterator();
            while (it2.hasNext()) {
                VmPipeAddress localAddress = (VmPipeAddress) it2.next();
                if (localAddress == null || localAddress.getPort() == 0) {
                    localAddress = null;
                    int i = 10000;
                    while (true) {
                        if (i >= Integer.MAX_VALUE) {
                            break;
                        }
                        VmPipeAddress newLocalAddress = new VmPipeAddress(i);
                        if (boundHandlers.containsKey(newLocalAddress) || newLocalAddresses.contains(newLocalAddress)) {
                            i++;
                        } else {
                            localAddress = newLocalAddress;
                            break;
                        }
                    }
                    if (localAddress == null) {
                        throw new IOException("No port available.");
                    }
                } else if (localAddress.getPort() < 0) {
                    throw new IOException("Bind port number must be 0 or above.");
                } else {
                    if (boundHandlers.containsKey(localAddress)) {
                        throw new IOException("Address already bound: " + localAddress);
                    }
                }
                newLocalAddresses.add(localAddress);
            }
            for (SocketAddress a : newLocalAddresses) {
                VmPipeAddress localAddress2 = (VmPipeAddress) a;
                if (!boundHandlers.containsKey(localAddress2)) {
                    boundHandlers.put(localAddress2, new VmPipe(this, localAddress2, getHandler(), getListeners()));
                } else {
                    for (SocketAddress a2 : newLocalAddresses) {
                        boundHandlers.remove(a2);
                    }
                    throw new IOException("Duplicate local address: " + a);
                }
            }
        }
        return newLocalAddresses;
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor
    protected void unbind0(List<? extends SocketAddress> localAddresses) {
        synchronized (boundHandlers) {
            for (SocketAddress a : localAddresses) {
                boundHandlers.remove(a);
            }
        }
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void doFinishSessionInitialization(IoSession session, IoFuture future) {
        initSession(session, future, null);
    }
}