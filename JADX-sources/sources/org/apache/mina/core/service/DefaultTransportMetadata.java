package org.apache.mina.core.service;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.util.IdentityHashSet;

/* loaded from: classes.dex */
public class DefaultTransportMetadata implements TransportMetadata {
    private final Class<? extends SocketAddress> addressType;
    private final boolean connectionless;
    private final Set<Class<? extends Object>> envelopeTypes;
    private final boolean fragmentation;
    private final String name;
    private final String providerName;
    private final Class<? extends IoSessionConfig> sessionConfigType;

    public DefaultTransportMetadata(String providerName, String name, boolean connectionless, boolean fragmentation, Class<? extends SocketAddress> addressType, Class<? extends IoSessionConfig> sessionConfigType, Class<?>... envelopeTypes) {
        if (providerName == null) {
            throw new IllegalArgumentException("providerName");
        }
        if (name == null) {
            throw new IllegalArgumentException("name");
        }
        String providerName2 = providerName.trim().toLowerCase();
        if (providerName2.length() == 0) {
            throw new IllegalArgumentException("providerName is empty.");
        }
        String name2 = name.trim().toLowerCase();
        if (name2.length() == 0) {
            throw new IllegalArgumentException("name is empty.");
        }
        if (addressType == null) {
            throw new IllegalArgumentException("addressType");
        }
        if (envelopeTypes == null) {
            throw new IllegalArgumentException("envelopeTypes");
        }
        if (envelopeTypes.length == 0) {
            throw new IllegalArgumentException("envelopeTypes is empty.");
        }
        if (sessionConfigType == null) {
            throw new IllegalArgumentException("sessionConfigType");
        }
        this.providerName = providerName2;
        this.name = name2;
        this.connectionless = connectionless;
        this.fragmentation = fragmentation;
        this.addressType = addressType;
        this.sessionConfigType = sessionConfigType;
        IdentityHashSet identityHashSet = new IdentityHashSet();
        for (Class<?> cls : envelopeTypes) {
            identityHashSet.add(cls);
        }
        this.envelopeTypes = Collections.unmodifiableSet(identityHashSet);
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public Class<? extends SocketAddress> getAddressType() {
        return this.addressType;
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public Set<Class<? extends Object>> getEnvelopeTypes() {
        return this.envelopeTypes;
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public Class<? extends IoSessionConfig> getSessionConfigType() {
        return this.sessionConfigType;
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public String getProviderName() {
        return this.providerName;
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public String getName() {
        return this.name;
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public boolean isConnectionless() {
        return this.connectionless;
    }

    @Override // org.apache.mina.core.service.TransportMetadata
    public boolean hasFragmentation() {
        return this.fragmentation;
    }

    public String toString() {
        return this.name;
    }
}
