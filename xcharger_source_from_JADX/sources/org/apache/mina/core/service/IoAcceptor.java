package org.apache.mina.core.service;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import org.apache.mina.core.session.IoSession;

public interface IoAcceptor extends IoService {
    void bind() throws IOException;

    void bind(Iterable<? extends SocketAddress> iterable) throws IOException;

    void bind(SocketAddress socketAddress) throws IOException;

    void bind(SocketAddress socketAddress, SocketAddress... socketAddressArr) throws IOException;

    void bind(SocketAddress... socketAddressArr) throws IOException;

    SocketAddress getDefaultLocalAddress();

    List<SocketAddress> getDefaultLocalAddresses();

    SocketAddress getLocalAddress();

    Set<SocketAddress> getLocalAddresses();

    boolean isCloseOnDeactivation();

    IoSession newSession(SocketAddress socketAddress, SocketAddress socketAddress2);

    void setCloseOnDeactivation(boolean z);

    void setDefaultLocalAddress(SocketAddress socketAddress);

    void setDefaultLocalAddresses(Iterable<? extends SocketAddress> iterable);

    void setDefaultLocalAddresses(SocketAddress socketAddress, SocketAddress... socketAddressArr);

    void setDefaultLocalAddresses(List<? extends SocketAddress> list);

    void unbind();

    void unbind(Iterable<? extends SocketAddress> iterable);

    void unbind(SocketAddress socketAddress);

    void unbind(SocketAddress socketAddress, SocketAddress... socketAddressArr);
}
