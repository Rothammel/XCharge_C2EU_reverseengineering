package org.apache.mina.core.service;

import java.net.SocketAddress;
import java.util.Set;
import org.apache.mina.core.session.IoSessionConfig;

/* loaded from: classes.dex */
public interface TransportMetadata {
    Class<? extends SocketAddress> getAddressType();

    Set<Class<? extends Object>> getEnvelopeTypes();

    String getName();

    String getProviderName();

    Class<? extends IoSessionConfig> getSessionConfigType();

    boolean hasFragmentation();

    boolean isConnectionless();
}