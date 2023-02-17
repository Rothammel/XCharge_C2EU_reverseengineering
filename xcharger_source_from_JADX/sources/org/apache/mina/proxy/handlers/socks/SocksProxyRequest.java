package org.apache.mina.proxy.handlers.socks;

import java.net.InetSocketAddress;
import org.apache.mina.proxy.handlers.ProxyRequest;

public class SocksProxyRequest extends ProxyRequest {
    private byte commandCode;
    private String host;
    private String password;
    private int port;
    private byte protocolVersion;
    private String serviceKerberosName;
    private String userName;

    public SocksProxyRequest(byte protocolVersion2, byte commandCode2, InetSocketAddress endpointAddress, String userName2) {
        super(endpointAddress);
        this.protocolVersion = protocolVersion2;
        this.commandCode = commandCode2;
        this.userName = userName2;
    }

    public SocksProxyRequest(byte commandCode2, String host2, int port2, String userName2) {
        this.protocolVersion = 4;
        this.commandCode = commandCode2;
        this.userName = userName2;
        this.host = host2;
        this.port = port2;
    }

    public byte[] getIpAddress() {
        if (getEndpointAddress() == null) {
            return SocksProxyConstants.FAKE_IP;
        }
        return getEndpointAddress().getAddress().getAddress();
    }

    public byte[] getPort() {
        byte[] port2 = new byte[2];
        int p = getEndpointAddress() == null ? this.port : getEndpointAddress().getPort();
        port2[1] = (byte) p;
        port2[0] = (byte) (p >> 8);
        return port2;
    }

    public byte getCommandCode() {
        return this.commandCode;
    }

    public byte getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getUserName() {
        return this.userName;
    }

    public final synchronized String getHost() {
        InetSocketAddress adr;
        if (this.host == null && (adr = getEndpointAddress()) != null && !adr.isUnresolved()) {
            this.host = getEndpointAddress().getHostName();
        }
        return this.host;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public String getServiceKerberosName() {
        return this.serviceKerberosName;
    }

    public void setServiceKerberosName(String serviceKerberosName2) {
        this.serviceKerberosName = serviceKerberosName2;
    }
}
