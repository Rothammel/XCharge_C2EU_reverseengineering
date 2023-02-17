package org.apache.mina.transport.vmpipe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListenerSupport;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.write.WriteRequestQueue;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class VmPipeSession extends AbstractIoSession {
    static final TransportMetadata METADATA = new DefaultTransportMetadata("mina", "vmpipe", false, false, VmPipeAddress.class, VmPipeSessionConfig.class, Object.class);
    private final VmPipeFilterChain filterChain;
    private final VmPipeAddress localAddress;
    private final Lock lock;
    final BlockingQueue<Object> receivedMessageQueue;
    private final VmPipeAddress remoteAddress;
    private final VmPipeSession remoteSession;
    private final VmPipeAddress serviceAddress;
    private final IoServiceListenerSupport serviceListeners;

    /* JADX INFO: Access modifiers changed from: package-private */
    public VmPipeSession(IoService service, IoServiceListenerSupport serviceListeners, VmPipeAddress localAddress, IoHandler handler, VmPipe remoteEntry) {
        super(service);
        this.config = new DefaultVmPipeSessionConfig();
        this.serviceListeners = serviceListeners;
        this.lock = new ReentrantLock();
        this.localAddress = localAddress;
        VmPipeAddress address = remoteEntry.getAddress();
        this.serviceAddress = address;
        this.remoteAddress = address;
        this.filterChain = new VmPipeFilterChain(this);
        this.receivedMessageQueue = new LinkedBlockingQueue();
        this.remoteSession = new VmPipeSession(this, remoteEntry);
    }

    private VmPipeSession(VmPipeSession remoteSession, VmPipe entry) {
        super(entry.getAcceptor());
        this.config = new DefaultVmPipeSessionConfig();
        this.serviceListeners = entry.getListeners();
        this.lock = remoteSession.lock;
        VmPipeAddress vmPipeAddress = remoteSession.remoteAddress;
        this.serviceAddress = vmPipeAddress;
        this.localAddress = vmPipeAddress;
        this.remoteAddress = remoteSession.localAddress;
        this.filterChain = new VmPipeFilterChain(this);
        this.remoteSession = remoteSession;
        this.receivedMessageQueue = new LinkedBlockingQueue();
    }

    @Override // org.apache.mina.core.session.AbstractIoSession
    public IoProcessor<VmPipeSession> getProcessor() {
        return this.filterChain.getProcessor();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public IoServiceListenerSupport getServiceListeners() {
        return this.serviceListeners;
    }

    @Override // org.apache.mina.core.session.AbstractIoSession, org.apache.mina.core.session.IoSession
    public VmPipeSessionConfig getConfig() {
        return (VmPipeSessionConfig) this.config;
    }

    @Override // org.apache.mina.core.session.IoSession
    public IoFilterChain getFilterChain() {
        return this.filterChain;
    }

    public VmPipeSession getRemoteSession() {
        return this.remoteSession;
    }

    @Override // org.apache.mina.core.session.IoSession
    public TransportMetadata getTransportMetadata() {
        return METADATA;
    }

    @Override // org.apache.mina.core.session.IoSession
    public VmPipeAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override // org.apache.mina.core.session.IoSession
    public VmPipeAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override // org.apache.mina.core.session.AbstractIoSession, org.apache.mina.core.session.IoSession
    public VmPipeAddress getServiceAddress() {
        return this.serviceAddress;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void increaseWrittenBytes0(int increment, long currentTime) {
        super.increaseWrittenBytes(increment, currentTime);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WriteRequestQueue getWriteRequestQueue0() {
        return super.getWriteRequestQueue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Lock getLock() {
        return this.lock;
    }
}
