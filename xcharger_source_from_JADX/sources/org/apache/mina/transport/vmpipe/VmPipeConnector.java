package org.apache.mina.transport.vmpipe;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.AbstractIoConnector;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.util.ExceptionMonitor;

public final class VmPipeConnector extends AbstractIoConnector {
    private static final IoFutureListener<IoFuture> LOCAL_ADDRESS_RECLAIMER = new LocalAddressReclaimer();
    /* access modifiers changed from: private */
    public static final Set<VmPipeAddress> TAKEN_LOCAL_ADDRESSES = new HashSet();
    private static int nextLocalPort = -1;
    private IdleStatusChecker idleChecker;

    public VmPipeConnector() {
        this((Executor) null);
    }

    public VmPipeConnector(Executor executor) {
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

    /* access modifiers changed from: protected */
    public ConnectFuture connect0(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        VmPipe entry = VmPipeAcceptor.boundHandlers.get(remoteAddress);
        if (entry == null) {
            return DefaultConnectFuture.newFailedFuture(new IOException("Endpoint unavailable: " + remoteAddress));
        }
        DefaultConnectFuture future = new DefaultConnectFuture();
        try {
            VmPipeSession localSession = new VmPipeSession(this, getListeners(), nextLocalAddress(), getHandler(), entry);
            initSession(localSession, future, sessionInitializer);
            localSession.getCloseFuture().addListener(LOCAL_ADDRESS_RECLAIMER);
            try {
                getFilterChainBuilder().buildFilterChain(localSession.getFilterChain());
                getListeners().fireSessionCreated(localSession);
                this.idleChecker.addSession(localSession);
                VmPipeSession remoteSession = localSession.getRemoteSession();
                ((VmPipeAcceptor) remoteSession.getService()).doFinishSessionInitialization(remoteSession, (IoFuture) null);
                try {
                    entry.getAcceptor().getFilterChainBuilder().buildFilterChain(remoteSession.getFilterChain());
                    entry.getListeners().fireSessionCreated(remoteSession);
                    this.idleChecker.addSession(remoteSession);
                } catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                    remoteSession.closeNow();
                }
                ((VmPipeFilterChain) localSession.getFilterChain()).start();
                ((VmPipeFilterChain) remoteSession.getFilterChain()).start();
                return future;
            } catch (Exception e2) {
                future.setException(e2);
                return future;
            }
        } catch (IOException e3) {
            return DefaultConnectFuture.newFailedFuture(e3);
        }
    }

    /* access modifiers changed from: protected */
    public void dispose0() throws Exception {
        this.idleChecker.getNotifyingTask().cancel();
    }

    private static VmPipeAddress nextLocalAddress() throws IOException {
        synchronized (TAKEN_LOCAL_ADDRESSES) {
            if (nextLocalPort >= 0) {
                nextLocalPort = -1;
            }
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                int i2 = nextLocalPort;
                nextLocalPort = i2 - 1;
                VmPipeAddress answer = new VmPipeAddress(i2);
                if (!TAKEN_LOCAL_ADDRESSES.contains(answer)) {
                    TAKEN_LOCAL_ADDRESSES.add(answer);
                    return answer;
                }
            }
            throw new IOException("Can't assign a local VM pipe port.");
        }
    }

    private static class LocalAddressReclaimer implements IoFutureListener<IoFuture> {
        private LocalAddressReclaimer() {
        }

        public void operationComplete(IoFuture future) {
            synchronized (VmPipeConnector.TAKEN_LOCAL_ADDRESSES) {
                VmPipeConnector.TAKEN_LOCAL_ADDRESSES.remove(future.getSession().getLocalAddress());
            }
        }
    }
}
