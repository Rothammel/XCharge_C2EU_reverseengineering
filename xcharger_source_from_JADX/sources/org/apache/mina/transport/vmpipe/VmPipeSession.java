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

class VmPipeSession extends AbstractIoSession {
    static final TransportMetadata METADATA = new DefaultTransportMetadata("mina", "vmpipe", false, false, VmPipeAddress.class, VmPipeSessionConfig.class, Object.class);
    private final VmPipeFilterChain filterChain;
    private final VmPipeAddress localAddress;
    private final Lock lock;
    final BlockingQueue<Object> receivedMessageQueue;
    private final VmPipeAddress remoteAddress;
    private final VmPipeSession remoteSession;
    private final VmPipeAddress serviceAddress;
    private final IoServiceListenerSupport serviceListeners;

    VmPipeSession(IoService service, IoServiceListenerSupport serviceListeners2, VmPipeAddress localAddress2, IoHandler handler, VmPipe remoteEntry) {
        super(service);
        this.config = new DefaultVmPipeSessionConfig();
        this.serviceListeners = serviceListeners2;
        this.lock = new ReentrantLock();
        this.localAddress = localAddress2;
        VmPipeAddress address = remoteEntry.getAddress();
        this.serviceAddress = address;
        this.remoteAddress = address;
        this.filterChain = new VmPipeFilterChain(this);
        this.receivedMessageQueue = new LinkedBlockingQueue();
        this.remoteSession = new VmPipeSession(this, remoteEntry);
    }

    private VmPipeSession(VmPipeSession remoteSession2, VmPipe entry) {
        super(entry.getAcceptor());
        this.config = new DefaultVmPipeSessionConfig();
        this.serviceListeners = entry.getListeners();
        this.lock = remoteSession2.lock;
        VmPipeAddress vmPipeAddress = remoteSession2.remoteAddress;
        this.serviceAddress = vmPipeAddress;
        this.localAddress = vmPipeAddress;
        this.remoteAddress = remoteSession2.localAddress;
        this.filterChain = new VmPipeFilterChain(this);
        this.remoteSession = remoteSession2;
        this.receivedMessageQueue = new LinkedBlockingQueue();
    }

    public IoProcessor<VmPipeSession> getProcessor() {
        return this.filterChain.getProcessor();
    }

    /* access modifiers changed from: package-private */
    public IoServiceListenerSupport getServiceListeners() {
        return this.serviceListeners;
    }

    public VmPipeSessionConfig getConfig() {
        return (VmPipeSessionConfig) this.config;
    }

    public IoFilterChain getFilterChain() {
        return this.filterChain;
    }

    public VmPipeSession getRemoteSession() {
        return this.remoteSession;
    }

    public TransportMetadata getTransportMetadata() {
        return METADATA;
    }

    public VmPipeAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public VmPipeAddress getLocalAddress() {
        return this.localAddress;
    }

    public VmPipeAddress getServiceAddress() {
        return this.serviceAddress;
    }

    /* access modifiers changed from: package-private */
    public void increaseWrittenBytes0(int increment, long currentTime) {
        super.increaseWrittenBytes(increment, currentTime);
    }

    /* access modifiers changed from: package-private */
    public WriteRequestQueue getWriteRequestQueue0() {
        return super.getWriteRequestQueue();
    }

    /* access modifiers changed from: package-private */
    public Lock getLock() {
        return this.lock;
    }
}
