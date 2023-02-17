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
import org.apache.http.conn.ssl.TokenParser;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.session.IoSessionConfig;

/* loaded from: classes.dex */
public abstract class AbstractIoAcceptor extends AbstractIoService implements IoAcceptor {
    protected final Object bindLock;
    private final Set<SocketAddress> boundAddresses;
    private final List<SocketAddress> defaultLocalAddresses;
    private boolean disconnectOnUnbind;
    private final List<SocketAddress> unmodifiableDefaultLocalAddresses;

    protected abstract Set<SocketAddress> bindInternal(List<? extends SocketAddress> list) throws Exception;

    protected abstract void unbind0(List<? extends SocketAddress> list) throws Exception;

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractIoAcceptor(IoSessionConfig sessionConfig, Executor executor) {
        super(sessionConfig, executor);
        this.defaultLocalAddresses = new ArrayList();
        this.unmodifiableDefaultLocalAddresses = Collections.unmodifiableList(this.defaultLocalAddresses);
        this.boundAddresses = new HashSet();
        this.disconnectOnUnbind = true;
        this.bindLock = new Object();
        this.defaultLocalAddresses.add(null);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public SocketAddress getLocalAddress() {
        Set<SocketAddress> localAddresses = getLocalAddresses();
        if (localAddresses.isEmpty()) {
            return null;
        }
        return localAddresses.iterator().next();
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final Set<SocketAddress> getLocalAddresses() {
        Set<SocketAddress> localAddresses = new HashSet<>();
        synchronized (this.boundAddresses) {
            localAddresses.addAll(this.boundAddresses);
        }
        return localAddresses;
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public SocketAddress getDefaultLocalAddress() {
        if (this.defaultLocalAddresses.isEmpty()) {
            return null;
        }
        return this.defaultLocalAddresses.iterator().next();
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void setDefaultLocalAddress(SocketAddress localAddress) {
        setDefaultLocalAddresses(localAddress, new SocketAddress[0]);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final List<SocketAddress> getDefaultLocalAddresses() {
        return this.unmodifiableDefaultLocalAddresses;
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void setDefaultLocalAddresses(List<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        setDefaultLocalAddresses((Iterable<? extends SocketAddress>) localAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
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

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void setDefaultLocalAddresses(SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
        if (otherLocalAddresses == null) {
            otherLocalAddresses = new SocketAddress[0];
        }
        Collection<SocketAddress> newLocalAddresses = new ArrayList<>(otherLocalAddresses.length + 1);
        newLocalAddresses.add(firstLocalAddress);
        for (SocketAddress a : otherLocalAddresses) {
            newLocalAddresses.add(a);
        }
        setDefaultLocalAddresses(newLocalAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final boolean isCloseOnDeactivation() {
        return this.disconnectOnUnbind;
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void setCloseOnDeactivation(boolean disconnectClientsOnUnbind) {
        this.disconnectOnUnbind = disconnectClientsOnUnbind;
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void bind() throws IOException {
        bind(getDefaultLocalAddresses());
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void bind(SocketAddress localAddress) throws IOException {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        List<SocketAddress> localAddresses = new ArrayList<>(1);
        localAddresses.add(localAddress);
        bind(localAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void bind(SocketAddress... addresses) throws IOException {
        if (addresses == null || addresses.length == 0) {
            bind(getDefaultLocalAddresses());
            return;
        }
        List<SocketAddress> localAddresses = new ArrayList<>(2);
        for (SocketAddress address : addresses) {
            localAddresses.add(address);
        }
        bind(localAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void bind(SocketAddress firstLocalAddress, SocketAddress... addresses) throws IOException {
        if (firstLocalAddress == null) {
            bind(getDefaultLocalAddresses());
        }
        if (addresses == null || addresses.length == 0) {
            bind(getDefaultLocalAddresses());
            return;
        }
        List<SocketAddress> localAddresses = new ArrayList<>(2);
        localAddresses.add(firstLocalAddress);
        for (SocketAddress address : addresses) {
            localAddresses.add(address);
        }
        bind(localAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void bind(Iterable<? extends SocketAddress> localAddresses) throws IOException {
        if (isDisposing()) {
            throw new IllegalStateException("The Accpetor disposed is being disposed.");
        }
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
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
                try {
                    try {
                        Set<SocketAddress> addresses = bindInternal(localAddressesCopy);
                        synchronized (this.boundAddresses) {
                            this.boundAddresses.addAll(addresses);
                        }
                    } catch (Exception e) {
                        throw new RuntimeIoException("Failed to bind to: " + getLocalAddresses(), e);
                    }
                } catch (IOException e2) {
                    throw e2;
                }
            } catch (RuntimeException e3) {
                throw e3;
            }
        }
        if (activate) {
            getListeners().fireServiceActivated();
        }
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void unbind() {
        unbind(getLocalAddresses());
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void unbind(SocketAddress localAddress) {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        List<SocketAddress> localAddresses = new ArrayList<>(1);
        localAddresses.add(localAddress);
        unbind(localAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void unbind(SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
        if (firstLocalAddress == null) {
            throw new IllegalArgumentException("firstLocalAddress");
        }
        if (otherLocalAddresses == null) {
            throw new IllegalArgumentException("otherLocalAddresses");
        }
        List<SocketAddress> localAddresses = new ArrayList<>();
        localAddresses.add(firstLocalAddress);
        Collections.addAll(localAddresses, otherLocalAddresses);
        unbind(localAddresses);
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final void unbind(Iterable<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        boolean deactivate = false;
        synchronized (this.bindLock) {
            synchronized (this.boundAddresses) {
                if (!this.boundAddresses.isEmpty()) {
                    List<SocketAddress> localAddressesCopy = new ArrayList<>();
                    int specifiedAddressCount = 0;
                    for (SocketAddress a : localAddresses) {
                        specifiedAddressCount++;
                        if (a != null && this.boundAddresses.contains(a)) {
                            localAddressesCopy.add(a);
                        }
                    }
                    if (specifiedAddressCount == 0) {
                        throw new IllegalArgumentException("localAddresses is empty.");
                    }
                    if (!localAddressesCopy.isEmpty()) {
                        try {
                            unbind0(localAddressesCopy);
                            this.boundAddresses.removeAll(localAddressesCopy);
                            if (this.boundAddresses.isEmpty()) {
                                deactivate = true;
                            }
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e2) {
                            throw new RuntimeIoException("Failed to unbind from: " + getLocalAddresses(), e2);
                        }
                    }
                    if (deactivate) {
                        getListeners().fireServiceDeactivated();
                    }
                }
            }
        }
    }

    public String toString() {
        TransportMetadata m = getTransportMetadata();
        return '(' + m.getProviderName() + TokenParser.SP + m.getName() + " acceptor: " + (isActive() ? "localAddress(es): " + getLocalAddresses() + ", managedSessionCount: " + getManagedSessionCount() : "not bound") + ')';
    }

    private void checkAddressType(SocketAddress a) {
        if (a != null && !getTransportMetadata().getAddressType().isAssignableFrom(a.getClass())) {
            throw new IllegalArgumentException("localAddress type: " + a.getClass().getSimpleName() + " (expected: " + getTransportMetadata().getAddressType().getSimpleName() + ")");
        }
    }

    /* loaded from: classes.dex */
    public static class AcceptorOperationFuture extends AbstractIoService.ServiceOperationFuture {
        private final List<SocketAddress> localAddresses;

        public AcceptorOperationFuture(List<? extends SocketAddress> localAddresses) {
            this.localAddresses = new ArrayList(localAddresses);
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
