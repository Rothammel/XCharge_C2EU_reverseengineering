package org.apache.http.conn.ssl;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500Principal;
import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.TextUtils;

@ThreadSafe
/* loaded from: classes.dex */
public class SSLConnectionSocketFactory implements LayeredConnectionSocketFactory {
    public static final String SSL = "SSL";
    public static final String SSLV2 = "SSLv2";
    private static final String TAG = "HttpClient";
    public static final String TLS = "TLS";
    private final X509HostnameVerifier hostnameVerifier;
    private final javax.net.ssl.SSLSocketFactory socketfactory;
    private final String[] supportedCipherSuites;
    private final String[] supportedProtocols;
    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifierHC4();
    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER = new BrowserCompatHostnameVerifierHC4();
    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER = new StrictHostnameVerifierHC4();

    public static SSLConnectionSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory((javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault(), BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    private static String[] split(String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public static SSLConnectionSocketFactory getSystemSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory((javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault(), split(System.getProperty("https.protocols")), split(System.getProperty("https.cipherSuites")), BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLConnectionSocketFactory(SSLContext sslContext) {
        this(sslContext, BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLConnectionSocketFactory(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
        this(((SSLContext) Args.notNull(sslContext, "SSL context")).getSocketFactory(), (String[]) null, (String[]) null, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, X509HostnameVerifier hostnameVerifier) {
        this(((SSLContext) Args.notNull(sslContext, "SSL context")).getSocketFactory(), supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(javax.net.ssl.SSLSocketFactory socketfactory, X509HostnameVerifier hostnameVerifier) {
        this(socketfactory, (String[]) null, (String[]) null, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(javax.net.ssl.SSLSocketFactory socketfactory, String[] supportedProtocols, String[] supportedCipherSuites, X509HostnameVerifier hostnameVerifier) {
        this.socketfactory = (javax.net.ssl.SSLSocketFactory) Args.notNull(socketfactory, "SSL socket factory");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.hostnameVerifier = hostnameVerifier == null ? BROWSER_COMPATIBLE_HOSTNAME_VERIFIER : hostnameVerifier;
    }

    protected void prepareSocket(SSLSocket socket) throws IOException {
    }

    @Override // org.apache.http.conn.socket.ConnectionSocketFactory
    public Socket createSocket(HttpContext context) throws IOException {
        return SocketFactory.getDefault().createSocket();
    }

    @Override // org.apache.http.conn.socket.ConnectionSocketFactory
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        Socket sock = socket != null ? socket : createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        if (connectTimeout > 0) {
            try {
                if (sock.getSoTimeout() == 0) {
                    sock.setSoTimeout(connectTimeout);
                }
            } catch (IOException ex) {
                try {
                    sock.close();
                } catch (IOException e) {
                }
                throw ex;
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Connecting socket to " + remoteAddress + " with timeout " + connectTimeout);
        }
        sock.connect(remoteAddress, connectTimeout);
        if (sock instanceof SSLSocket) {
            SSLSocket sslsock = (SSLSocket) sock;
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Starting handshake");
            }
            sslsock.startHandshake();
            verifyHostname(sslsock, host.getHostName());
            return sock;
        }
        return createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
    }

    @Override // org.apache.http.conn.socket.LayeredConnectionSocketFactory
    @TargetApi(17)
    public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context) throws IOException {
        SSLSocket sslsock = (SSLSocket) this.socketfactory.createSocket(socket, target, port, true);
        if (this.supportedProtocols != null) {
            sslsock.setEnabledProtocols(this.supportedProtocols);
        } else {
            String[] allProtocols = sslsock.getEnabledProtocols();
            List<String> enabledProtocols = new ArrayList<>(allProtocols.length);
            for (String protocol : allProtocols) {
                if (!protocol.startsWith(SSL)) {
                    enabledProtocols.add(protocol);
                }
            }
            if (!enabledProtocols.isEmpty()) {
                sslsock.setEnabledProtocols((String[]) enabledProtocols.toArray(new String[enabledProtocols.size()]));
            }
        }
        if (this.supportedCipherSuites != null) {
            sslsock.setEnabledCipherSuites(this.supportedCipherSuites);
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Enabled protocols: " + Arrays.asList(sslsock.getEnabledProtocols()));
            Log.d(TAG, "Enabled cipher suites:" + Arrays.asList(sslsock.getEnabledCipherSuites()));
        }
        prepareSocket(sslsock);
        if (Build.VERSION.SDK_INT >= 17) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Enabling SNI for " + target);
            }
            try {
                Method method = sslsock.getClass().getMethod("setHostname", String.class);
                method.invoke(sslsock, target);
            } catch (Exception ex) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "SNI configuration failed", ex);
                }
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Starting handshake");
        }
        sslsock.startHandshake();
        verifyHostname(sslsock, target);
        return sslsock;
    }

    X509HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    private void verifyHostname(SSLSocket sslsock, String hostname) throws IOException {
        try {
            if (Log.isLoggable(TAG, 3)) {
                try {
                    SSLSession session = sslsock.getSession();
                    Log.d(TAG, "Secure session established");
                    Log.d(TAG, " negotiated protocol: " + session.getProtocol());
                    Log.d(TAG, " negotiated cipher suite: " + session.getCipherSuite());
                    Certificate[] certs = session.getPeerCertificates();
                    X509Certificate x509 = (X509Certificate) certs[0];
                    X500Principal peer = x509.getSubjectX500Principal();
                    Log.d(TAG, " peer principal: " + peer.toString());
                    Collection<List<?>> altNames1 = x509.getSubjectAlternativeNames();
                    if (altNames1 != null) {
                        List<String> altNames = new ArrayList<>();
                        for (List<?> aC : altNames1) {
                            if (!aC.isEmpty()) {
                                altNames.add((String) aC.get(1));
                            }
                        }
                        Log.d(TAG, " peer alternative names: " + altNames);
                    }
                    X500Principal issuer = x509.getIssuerX500Principal();
                    Log.d(TAG, " issuer principal: " + issuer.toString());
                    Collection<List<?>> altNames2 = x509.getIssuerAlternativeNames();
                    if (altNames2 != null) {
                        List<String> altNames3 = new ArrayList<>();
                        for (List<?> aC2 : altNames2) {
                            if (!aC2.isEmpty()) {
                                altNames3.add((String) aC2.get(1));
                            }
                        }
                        Log.d(TAG, " issuer alternative names: " + altNames3);
                    }
                } catch (Exception e) {
                }
            }
            this.hostnameVerifier.verify(hostname, sslsock);
        } catch (IOException iox) {
            try {
                sslsock.close();
            } catch (Exception e2) {
            }
            throw iox;
        }
    }
}
