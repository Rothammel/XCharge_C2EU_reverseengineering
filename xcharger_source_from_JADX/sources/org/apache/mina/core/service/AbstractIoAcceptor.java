package org.apache.mina.core.service;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.session.IoSessionConfig;

public abstract class AbstractIoAcceptor extends AbstractIoService implements IoAcceptor {
    protected final Object bindLock = new Object();
    private final Set<SocketAddress> boundAddresses = new HashSet();
    private final List<SocketAddress> defaultLocalAddresses = new ArrayList();
    private boolean disconnectOnUnbind = true;
    private final List<SocketAddress> unmodifiableDefaultLocalAddresses = Collections.unmodifiableList(this.defaultLocalAddresses);

    /* access modifiers changed from: protected */
    public abstract Set<SocketAddress> bindInternal(List<? extends SocketAddress> list) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void unbind0(List<? extends SocketAddress> list) throws Exception;

    protected AbstractIoAcceptor(IoSessionConfig sessionConfig, Executor executor) {
        super(sessionConfig, executor);
        this.defaultLocalAddresses.add((Object) null);
    }

    public SocketAddress getLocalAddress() {
        Set<SocketAddress> localAddresses = getLocalAddresses();
        if (localAddresses.isEmpty()) {
            return null;
        }
        return localAddresses.iterator().next();
    }

    public final Set<SocketAddress> getLocalAddresses() {
        Set<SocketAddress> localAddresses = new HashSet<>();
        synchronized (this.boundAddresses) {
            localAddresses.addAll(this.boundAddresses);
        }
        return localAddresses;
    }

    public SocketAddress getDefaultLocalAddress() {
        if (this.defaultLocalAddresses.isEmpty()) {
            return null;
        }
        return this.defaultLocalAddresses.iterator().next();
    }

    public final void setDefaultLocalAddress(SocketAddress localAddress) {
        setDefaultLocalAddresses(localAddress, new SocketAddress[0]);
    }

    public final List<SocketAddress> getDefaultLocalAddresses() {
        return this.unmodifiableDefaultLocalAddresses;
    }

    public final void setDefaultLocalAddresses(List<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        setDefaultLocalAddresses((Iterable<? extends SocketAddress>) localAddresses);
    }

    public final void setDefaultLocalAddresses(Iterable<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        synchronized (this.bindLock) {
            synchronized (this.boundAddresses) {
                if (!this.boundAddresses.isEmpty()) {
                    throw new IllegalStateException("localAddress can't be set while the acceptor is bound.");
                }
                Collection<SocketAddress> newLocalAddresses = new ArrayList<>();
                for (SocketAddress a : localAddresses) {
                    checkAddressType(a);
                    newLocalAddresses.add(a);
                }
                if (newLocalAddresses.isEmpty()) {
                    throw new IllegalArgumentException("empty localAddresses");
                }
                this.defaultLocalAddresses.clear();
                this.defaultLocalAddresses.addAll(newLocalAddresses);
            }
        }
    }

    public final void setDefaultLocalAddresses(SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
        if (otherLocalAddresses == null) {
            otherLocalAddresses = new SocketAddress[0];
        }
        Collection<SocketAddress> newLocalAddresses = new ArrayList<>(otherLocalAddresses.length + 1);
        newLocalAddresses.add(firstLocalAddress);
        for (SocketAddress a : otherLocalAddresses) {
            newLocalAddresses.add(a);
        }
        setDefaultLocalAddresses((Iterable<? extends SocketAddress>) newLocalAddresses);
    }

    public final boolean isCloseOnDeactivation() {
        return this.disconnectOnUnbind;
    }

    public final void setCloseOnDeactivation(boolean disconnectClientsOnUnbind) {
        this.disconnectOnUnbind = disconnectClientsOnUnbind;
    }

    public final void bind() throws IOException {
        bind((Iterable<? extends SocketAddress>) getDefaultLocalAddresses());
    }

    public final void bind(SocketAddress localAddress) throws IOException {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        List<SocketAddress> localAddresses = new ArrayList<>(1);
        localAddresses.add(localAddress);
        bind((Iterable<? extends SocketAddress>) localAddresses);
    }

    public final void bind(SocketAddress... addresses) throws IOException {
        if (addresses == null || addresses.length == 0) {
            bind((Iterable<? extends SocketAddress>) getDefaultLocalAddresses());
            return;
        }
        List<SocketAddress> localAddresses = new ArrayList<>(2);
        for (SocketAddress address : addresses) {
            localAddresses.add(address);
        }
        bind((Iterable<? extends SocketAddress>) localAddresses);
    }

    public final void bind(SocketAddress firstLocalAddress, SocketAddress... addresses) throws IOException {
        if (firstLocalAddress == null) {
            bind((Iterable<? extends SocketAddress>) getDefaultLocalAddresses());
        }
        if (addresses == null || addresses.length == 0) {
            bind((Iterable<? extends SocketAddress>) getDefaultLocalAddresses());
            return;
        }
        List<SocketAddress> localAddresses = new ArrayList<>(2);
        localAddresses.add(firstLocalAddress);
        for (SocketAddress address : addresses) {
            localAddresses.add(address);
        }
        bind((Iterable<? extends SocketAddress>) localAddresses);
    }

    public final void bind(Iterable<? extends SocketAddress> localAddresses) throws IOException {
        if (isDisposing()) {
            throw new IllegalStateException("The Accpetor disposed is being disposed.");
        } else if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        } else {
            List<SocketAddress> localAddressesCopy = new ArrayList<>();
            for (SocketAddress a : localAddresses) {
                checkAddressType(a);
                localAddressesCopy.add(a);
            }
            if (localAddressesCopy.isEmpty()) {
                throw new IllegalArgumentException("localAddresses is empty.");
            }
            boolean activate = false;
            synchronized (this.bindLock) {
                synchronized (this.boundAddresses) {
                    if (this.boundAddresses.isEmpty()) {
                        activate = true;
                    }
                }
                if (getHandler() == null) {
                    throw new IllegalStateException("handler is not set.");
                }
                try {
                    Set<SocketAddress> addresses = bindInternal(localAddressesCopy);
                    synchronized (this.boundAddresses) {
                        this.boundAddresses.addAll(addresses);
                    }
                } catch (IOException e) {
                    throw e;
                } catch (RuntimeException e2) {
                    throw e2;
                } catch (Exception e3) {
                    throw new RuntimeIoException("Failed to bind to: " + getLocalAddresses(), e3);
                }
            }
            if (activate) {
                getListeners().fireServiceActivated();
            }
        }
    }

    public final void unbind() {
        unbind((Iterable<? extends SocketAddress>) getLocalAddresses());
    }

    public final void unbind(SocketAddress localAddress) {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        List<SocketAddress> localAddresses = new ArrayList<>(1);
        localAddresses.add(localAddress);
        unbind((Iterable<? extends SocketAddress>) localAddresses);
    }

    public final void unbind(SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
        if (firstLocalAddress == null) {
            throw new IllegalArgumentException("firstLocalAddress");
        } else if (otherLocalAddresses == null) {
            throw new IllegalArgumentException("otherLocalAddresses");
        } else {
            List<SocketAddress> localAddresses = new ArrayList<>();
            localAddresses.add(firstLocalAddress);
            Collections.addAll(localAddresses, otherLocalAddresses);
            unbind((Iterable<? extends SocketAddress>) localAddresses);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x006b, code lost:
        if (r1 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x006d, code lost:
        getListeners().fireServiceDeactivated();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void unbind(java.lang.Iterable<? extends java.net.SocketAddress> r11) {
        /*
            r10 = this;
            if (r11 != 0) goto L_0x000a
            java.lang.IllegalArgumentException r5 = new java.lang.IllegalArgumentException
            java.lang.String r6 = "localAddresses"
            r5.<init>(r6)
            throw r5
        L_0x000a:
            r1 = 0
            java.lang.Object r6 = r10.bindLock
            monitor-enter(r6)
            java.util.Set<java.net.SocketAddress> r7 = r10.boundAddresses     // Catch:{ all -> 0x0045 }
            monitor-enter(r7)     // Catch:{ all -> 0x0045 }
            java.util.Set<java.net.SocketAddress> r5 = r10.boundAddresses     // Catch:{ all -> 0x0042 }
            boolean r5 = r5.isEmpty()     // Catch:{ all -> 0x0042 }
            if (r5 == 0) goto L_0x001c
            monitor-exit(r7)     // Catch:{ all -> 0x0042 }
            monitor-exit(r6)     // Catch:{ all -> 0x0045 }
        L_0x001b:
            return
        L_0x001c:
            java.util.ArrayList r3 = new java.util.ArrayList     // Catch:{ all -> 0x0042 }
            r3.<init>()     // Catch:{ all -> 0x0042 }
            r4 = 0
            java.util.Iterator r5 = r11.iterator()     // Catch:{ all -> 0x0042 }
        L_0x0026:
            boolean r8 = r5.hasNext()     // Catch:{ all -> 0x0042 }
            if (r8 == 0) goto L_0x0048
            java.lang.Object r0 = r5.next()     // Catch:{ all -> 0x0042 }
            java.net.SocketAddress r0 = (java.net.SocketAddress) r0     // Catch:{ all -> 0x0042 }
            int r4 = r4 + 1
            if (r0 == 0) goto L_0x0026
            java.util.Set<java.net.SocketAddress> r8 = r10.boundAddresses     // Catch:{ all -> 0x0042 }
            boolean r8 = r8.contains(r0)     // Catch:{ all -> 0x0042 }
            if (r8 == 0) goto L_0x0026
            r3.add(r0)     // Catch:{ all -> 0x0042 }
            goto L_0x0026
        L_0x0042:
            r5 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x0042 }
            throw r5     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x0045 }
            throw r5
        L_0x0048:
            if (r4 != 0) goto L_0x0052
            java.lang.IllegalArgumentException r5 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x0042 }
            java.lang.String r8 = "localAddresses is empty."
            r5.<init>(r8)     // Catch:{ all -> 0x0042 }
            throw r5     // Catch:{ all -> 0x0042 }
        L_0x0052:
            boolean r5 = r3.isEmpty()     // Catch:{ all -> 0x0042 }
            if (r5 != 0) goto L_0x0069
            r10.unbind0(r3)     // Catch:{ RuntimeException -> 0x0075, Exception -> 0x0077 }
            java.util.Set<java.net.SocketAddress> r5 = r10.boundAddresses     // Catch:{ all -> 0x0042 }
            r5.removeAll(r3)     // Catch:{ all -> 0x0042 }
            java.util.Set<java.net.SocketAddress> r5 = r10.boundAddresses     // Catch:{ all -> 0x0042 }
            boolean r5 = r5.isEmpty()     // Catch:{ all -> 0x0042 }
            if (r5 == 0) goto L_0x0069
            r1 = 1
        L_0x0069:
            monitor-exit(r7)     // Catch:{ all -> 0x0042 }
            monitor-exit(r6)     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x001b
            org.apache.mina.core.service.IoServiceListenerSupport r5 = r10.getListeners()
            r5.fireServiceDeactivated()
            goto L_0x001b
        L_0x0075:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x0042 }
        L_0x0077:
            r2 = move-exception
            org.apache.mina.core.RuntimeIoException r5 = new org.apache.mina.core.RuntimeIoException     // Catch:{ all -> 0x0042 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0042 }
            r8.<init>()     // Catch:{ all -> 0x0042 }
            java.lang.String r9 = "Failed to unbind from: "
            java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ all -> 0x0042 }
            java.util.Set r9 = r10.getLocalAddresses()     // Catch:{ all -> 0x0042 }
            java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ all -> 0x0042 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0042 }
            r5.<init>(r8, r2)     // Catch:{ all -> 0x0042 }
            throw r5     // Catch:{ all -> 0x0042 }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.service.AbstractIoAcceptor.unbind(java.lang.Iterable):void");
    }

    public String toString() {
        TransportMetadata m = getTransportMetadata();
        return '(' + m.getProviderName() + TokenParser.f168SP + m.getName() + " acceptor: " + (isActive() ? "localAddress(es): " + getLocalAddresses() + ", managedSessionCount: " + getManagedSessionCount() : "not bound") + ')';
    }

    private void checkAddressType(SocketAddress a) {
        if (a != null && !getTransportMetadata().getAddressType().isAssignableFrom(a.getClass())) {
            throw new IllegalArgumentException("localAddress type: " + a.getClass().getSimpleName() + " (expected: " + getTransportMetadata().getAddressType().getSimpleName() + ")");
        }
    }

    public static class AcceptorOperationFuture extends AbstractIoService.ServiceOperationFuture {
        private final List<SocketAddress> localAddresses;

        public AcceptorOperationFuture(List<? extends SocketAddress> localAddresses2) {
            this.localAddresses = new ArrayList(localAddresses2);
        }

        public final List<SocketAddress> getLocalAddresses() {
            return Collections.unmodifiableList(this.localAddresses);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Acceptor operation : ");
            if (this.localAddresses != null) {
                boolean isFirst = true;
                for (SocketAddress address : this.localAddresses) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(address);
                }
            }
            return sb.toString();
        }
    }
}
