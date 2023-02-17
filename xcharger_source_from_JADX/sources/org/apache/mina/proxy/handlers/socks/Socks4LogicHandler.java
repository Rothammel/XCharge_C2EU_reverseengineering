package org.apache.mina.proxy.handlers.socks;

import java.util.Arrays;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.ByteUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks4LogicHandler extends AbstractSocksLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) Socks4LogicHandler.class);

    public Socks4LogicHandler(ProxyIoSession proxyIoSession) {
        super(proxyIoSession);
    }

    public void doHandshake(IoFilter.NextFilter nextFilter) {
        logger.debug(" doHandshake()");
        writeRequest(nextFilter, this.request);
    }

    /* access modifiers changed from: protected */
    public void writeRequest(IoFilter.NextFilter nextFilter, SocksProxyRequest request) {
        try {
            boolean isV4ARequest = Arrays.equals(request.getIpAddress(), SocksProxyConstants.FAKE_IP);
            byte[] userID = request.getUserName().getBytes("ASCII");
            byte[] host = isV4ARequest ? request.getHost().getBytes("ASCII") : null;
            int len = userID.length + 9;
            if (isV4ARequest) {
                len += host.length + 1;
            }
            IoBuffer buf = IoBuffer.allocate(len);
            buf.put(request.getProtocolVersion());
            buf.put(request.getCommandCode());
            buf.put(request.getPort());
            buf.put(request.getIpAddress());
            buf.put(userID);
            buf.put((byte) 0);
            if (isV4ARequest) {
                buf.put(host);
                buf.put((byte) 0);
            }
            if (isV4ARequest) {
                logger.debug("  sending SOCKS4a request");
            } else {
                logger.debug("  sending SOCKS4 request");
            }
            buf.flip();
            writeData(nextFilter, buf);
        } catch (Exception ex) {
            closeSession("Unable to send Socks request: ", ex);
        }
    }

    public void messageReceived(IoFilter.NextFilter nextFilter, IoBuffer buf) {
        try {
            if (buf.remaining() >= 8) {
                handleResponse(buf);
            }
        } catch (Exception ex) {
            closeSession("Proxy handshake failed: ", ex);
        }
    }

    /* access modifiers changed from: protected */
    public void handleResponse(IoBuffer buf) throws Exception {
        if (buf.get(0) != 0) {
            throw new Exception("Socks response seems to be malformed");
        }
        byte status = buf.get(1);
        buf.position(buf.position() + 8);
        if (status == 90) {
            setHandshakeComplete();
            return;
        }
        throw new Exception("Proxy handshake failed - Code: 0x" + ByteUtilities.asHex(new byte[]{status}) + " (" + SocksProxyConstants.getReplyCodeAsString(status) + ")");
    }
}
