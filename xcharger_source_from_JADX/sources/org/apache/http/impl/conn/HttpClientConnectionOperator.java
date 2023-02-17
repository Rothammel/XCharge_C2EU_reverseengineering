package org.apache.http.impl.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@Immutable
class HttpClientConnectionOperator {
    static final String SOCKET_FACTORY_REGISTRY = "http.socket-factory-registry";
    private static final String TAG = "HttpClient";
    private final DnsResolver dnsResolver;
    private final SchemePortResolver schemePortResolver;
    private final Lookup<ConnectionSocketFactory> socketFactoryRegistry;

    HttpClientConnectionOperator(Lookup<ConnectionSocketFactory> socketFactoryRegistry2, SchemePortResolver schemePortResolver2, DnsResolver dnsResolver2) {
        Args.notNull(socketFactoryRegistry2, "Socket factory registry");
        this.socketFactoryRegistry = socketFactoryRegistry2;
        this.schemePortResolver = schemePortResolver2 == null ? DefaultSchemePortResolver.INSTANCE : schemePortResolver2;
        this.dnsResolver = dnsResolver2 == null ? SystemDefaultDnsResolver.INSTANCE : dnsResolver2;
    }

    private Lookup<ConnectionSocketFactory> getSocketFactoryRegistry(HttpContext context) {
        Lookup<ConnectionSocketFactory> reg = (Lookup) context.getAttribute(SOCKET_FACTORY_REGISTRY);
        if (reg == null) {
            return this.socketFactoryRegistry;
        }
        return reg;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0119 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void connect(org.apache.http.conn.ManagedHttpClientConnection r20, org.apache.http.HttpHost r21, java.net.InetSocketAddress r22, int r23, org.apache.http.config.SocketConfig r24, org.apache.http.protocol.HttpContext r25) throws java.io.IOException {
        /*
            r19 = this;
            r0 = r19
            r1 = r25
            org.apache.http.config.Lookup r18 = r0.getSocketFactoryRegistry(r1)
            java.lang.String r3 = r21.getSchemeName()
            r0 = r18
            java.lang.Object r2 = r0.lookup(r3)
            org.apache.http.conn.socket.ConnectionSocketFactory r2 = (org.apache.http.conn.socket.ConnectionSocketFactory) r2
            if (r2 != 0) goto L_0x0033
            org.apache.http.conn.UnsupportedSchemeException r3 = new org.apache.http.conn.UnsupportedSchemeException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r7 = r21.getSchemeName()
            java.lang.String r7 = java.lang.String.valueOf(r7)
            r5.<init>(r7)
            java.lang.String r7 = " protocol is not supported"
            java.lang.StringBuilder r5 = r5.append(r7)
            java.lang.String r5 = r5.toString()
            r3.<init>(r5)
            throw r3
        L_0x0033:
            r0 = r19
            org.apache.http.conn.DnsResolver r3 = r0.dnsResolver
            java.lang.String r5 = r21.getHostName()
            java.net.InetAddress[] r10 = r3.resolve(r5)
            r0 = r19
            org.apache.http.conn.SchemePortResolver r3 = r0.schemePortResolver
            r0 = r21
            int r17 = r3.resolve(r0)
            r13 = 0
        L_0x004a:
            int r3 = r10.length
            if (r13 < r3) goto L_0x004e
        L_0x004d:
            return
        L_0x004e:
            r9 = r10[r13]
            int r3 = r10.length
            int r3 = r3 + -1
            if (r13 != r3) goto L_0x011d
            r14 = 1
        L_0x0056:
            r0 = r25
            java.net.Socket r4 = r2.createSocket(r0)
            int r3 = r24.getSoTimeout()
            r4.setSoTimeout(r3)
            boolean r3 = r24.isSoReuseAddress()
            r4.setReuseAddress(r3)
            boolean r3 = r24.isTcpNoDelay()
            r4.setTcpNoDelay(r3)
            boolean r3 = r24.isSoKeepAlive()
            r4.setKeepAlive(r3)
            int r15 = r24.getSoLinger()
            if (r15 < 0) goto L_0x0084
            if (r15 <= 0) goto L_0x0120
            r3 = 1
        L_0x0081:
            r4.setSoLinger(r3, r15)
        L_0x0084:
            r0 = r20
            r0.bind(r4)
            java.net.InetSocketAddress r6 = new java.net.InetSocketAddress
            r0 = r17
            r6.<init>(r9, r0)
            java.lang.String r3 = "HttpClient"
            r5 = 3
            boolean r3 = android.util.Log.isLoggable(r3, r5)
            if (r3 == 0) goto L_0x00ad
            java.lang.String r3 = "HttpClient"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r7 = "Connecting to "
            r5.<init>(r7)
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r3, r5)
        L_0x00ad:
            r3 = r23
            r5 = r21
            r7 = r22
            r8 = r25
            java.net.Socket r4 = r2.connectSocket(r3, r4, r5, r6, r7, r8)     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            r0 = r20
            r0.bind(r4)     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            java.lang.String r3 = "HttpClient"
            r5 = 3
            boolean r3 = android.util.Log.isLoggable(r3, r5)     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            if (r3 == 0) goto L_0x004d
            java.lang.String r3 = "HttpClient"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            java.lang.String r7 = "Connection established "
            r5.<init>(r7)     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            r0 = r20
            java.lang.StringBuilder r5 = r5.append(r0)     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            java.lang.String r5 = r5.toString()     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            android.util.Log.d(r3, r5)     // Catch:{ SocketTimeoutException -> 0x00df, ConnectException -> 0x0123 }
            goto L_0x004d
        L_0x00df:
            r12 = move-exception
            if (r14 == 0) goto L_0x00f0
            org.apache.http.conn.ConnectTimeoutException r11 = new org.apache.http.conn.ConnectTimeoutException
            r0 = r21
            java.lang.String r3 = getConnectTimeoutMessage(r12, r0, r10)
            r11.<init>(r3)
            r11.initCause(r12)
        L_0x00f0:
            java.lang.String r3 = "HttpClient"
            r5 = 3
            boolean r3 = android.util.Log.isLoggable(r3, r5)
            if (r3 == 0) goto L_0x0119
            java.lang.String r3 = "HttpClient"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r7 = "Connect to "
            r5.<init>(r7)
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r7 = " timed out. "
            java.lang.StringBuilder r5 = r5.append(r7)
            java.lang.String r7 = "Connection will be retried using another IP address"
            java.lang.StringBuilder r5 = r5.append(r7)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r3, r5)
        L_0x0119:
            int r13 = r13 + 1
            goto L_0x004a
        L_0x011d:
            r14 = 0
            goto L_0x0056
        L_0x0120:
            r3 = 0
            goto L_0x0081
        L_0x0123:
            r12 = move-exception
            if (r14 == 0) goto L_0x00f0
            java.lang.String r16 = r12.getMessage()
            java.lang.String r3 = "Connection timed out"
            r0 = r16
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0143
            org.apache.http.conn.ConnectTimeoutException r11 = new org.apache.http.conn.ConnectTimeoutException
            r0 = r21
            java.lang.String r3 = getConnectTimeoutMessage(r12, r0, r10)
            r11.<init>(r3)
            r11.initCause(r12)
            throw r11
        L_0x0143:
            org.apache.http.conn.HttpHostConnectException r3 = new org.apache.http.conn.HttpHostConnectException
            r0 = r21
            r3.<init>(r0, r12)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.conn.HttpClientConnectionOperator.connect(org.apache.http.conn.ManagedHttpClientConnection, org.apache.http.HttpHost, java.net.InetSocketAddress, int, org.apache.http.config.SocketConfig, org.apache.http.protocol.HttpContext):void");
    }

    private static String getConnectTimeoutMessage(IOException cause, HttpHost host, InetAddress... remoteAddresses) {
        return "Connect to " + (host != null ? host.toHostString() : "remote host") + ((remoteAddresses == null || remoteAddresses.length <= 0) ? "" : StringUtils.SPACE + Arrays.asList(remoteAddresses)) + ((cause == null || cause.getMessage() == null) ? " timed out" : " failed: " + cause.getMessage());
    }

    public void upgrade(ManagedHttpClientConnection conn, HttpHost host, HttpContext context) throws IOException {
        ConnectionSocketFactory sf = getSocketFactoryRegistry(HttpClientContext.adapt(context)).lookup(host.getSchemeName());
        if (sf == null) {
            throw new UnsupportedSchemeException(String.valueOf(host.getSchemeName()) + " protocol is not supported");
        } else if (!(sf instanceof LayeredConnectionSocketFactory)) {
            throw new UnsupportedSchemeException(String.valueOf(host.getSchemeName()) + " protocol does not support connection upgrade");
        } else {
            conn.bind(((LayeredConnectionSocketFactory) sf).createLayeredSocket(conn.getSocket(), host.getHostName(), this.schemePortResolver.resolve(host), context));
        }
    }
}
