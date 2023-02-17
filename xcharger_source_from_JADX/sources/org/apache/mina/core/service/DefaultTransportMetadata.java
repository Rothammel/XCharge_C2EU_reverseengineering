package org.apache.mina.core.service;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.util.IdentityHashSet;

public class DefaultTransportMetadata implements TransportMetadata {
    private final Class<? extends SocketAddress> addressType;
    private final boolean connectionless;
    private final Set<Class<? extends Object>> envelopeTypes;
    private final boolean fragmentation;
    private final String name;
    private final String providerName;
    private final Class<? extends IoSessionConfig> sessionConfigType;

    public DefaultTransportMetadata(String providerName2, String name2, boolean connectionless2, boolean fragmentation2, Class<? extends SocketAddress> addressType2, Class<? extends IoSessionConfig> sessionConfigType2, Class<?>... envelopeTypes2) {
        if (providerName2 == null) {
            throw new IllegalArgumentException("providerName");
        } else if (name2 == null) {
            throw new IllegalArgumentException("name");
        } else {
            String providerName3 = providerName2.trim().toLowerCase();
            if (providerName3.length() == 0) {
                throw new IllegalArgumentException("providerName is empty.");
            }
            String name3 = name2.trim().toLowerCase();
            if (name3.length() == 0) {
                throw new IllegalArgumentException("name is empty.");
            } else if (addressType2 == null) {
                throw new IllegalArgumentException("addressType");
            } else if (envelopeTypes2 == null) {
                throw new IllegalArgumentException("envelopeTypes");
            } else if (envelopeTypes2.length == 0) {
                throw new IllegalArgumentException("envelopeTypes is empty.");
            } else if (sessionConfigType2 == null) {
                throw new IllegalArgumentException("sessionConfigType");
            } else {
                this.providerName = providerName3;
                this.name = name3;
                this.connectionless = connectionless2;
                this.fragmentation = fragmentation2;
                this.addressType = addressType2;
                this.sessionConfigType = sessionConfigType2;
                Set<Class<? extends Object>> newEnvelopeTypes = new IdentityHashSet<>();
                for (Class<? extends Object> c : envelopeTypes2) {
                    newEnvelopeTypes.add(c);
                }
                this.envelopeTypes = Collections.unmodifiableSet(newEnvelopeTypes);
            }
        }
    }

    public Class<? extends SocketAddress> getAddressType() {
        return this.addressType;
    }

    public Set<Class<? extends Object>> getEnvelopeTypes() {
        return this.envelopeTypes;
    }

    public Class<? extends IoSessionConfig> getSessionConfigType() {
        return this.sessionConfigType;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public String getName() {
        return this.name;
    }

    public boolean isConnectionless() {
        return this.connectionless;
    }

    public boolean hasFragmentation() {
        return this.fragmentation;
    }

    public String toString() {
        return this.name;
    }
}
