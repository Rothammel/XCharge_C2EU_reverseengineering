package org.apache.mina.proxy;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.proxy.session.ProxyIoSession;

public interface ProxyLogicHandler {
    void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException;

    void enqueueWriteRequest(IoFilter.NextFilter nextFilter, WriteRequest writeRequest);

    ProxyIoSession getProxyIoSession();

    boolean isHandshakeComplete();

    void messageReceived(IoFilter.NextFilter nextFilter, IoBuffer ioBuffer) throws ProxyAuthException;
}
