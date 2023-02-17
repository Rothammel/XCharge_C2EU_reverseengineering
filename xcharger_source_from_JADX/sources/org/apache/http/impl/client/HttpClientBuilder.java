package org.apache.http.impl.client;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.BackoffManager;
import org.apache.http.client.ConnectionBackoffStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.TextUtils;
import org.apache.http.util.VersionInfoHC4;

@NotThreadSafe
public class HttpClientBuilder {
    static final String DEFAULT_USER_AGENT;
    private boolean authCachingDisabled;
    private Lookup<AuthSchemeProvider> authSchemeRegistry;
    private boolean automaticRetriesDisabled;
    private BackoffManager backoffManager;
    private List<Closeable> closeables;
    private HttpClientConnectionManager connManager;
    private ConnectionBackoffStrategy connectionBackoffStrategy;
    private boolean connectionStateDisabled;
    private boolean contentCompressionDisabled;
    private boolean cookieManagementDisabled;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private CookieStore cookieStore;
    private CredentialsProvider credentialsProvider;
    private ConnectionConfig defaultConnectionConfig;
    private Collection<? extends Header> defaultHeaders;
    private RequestConfig defaultRequestConfig;
    private SocketConfig defaultSocketConfig;
    private X509HostnameVerifier hostnameVerifier;
    private HttpProcessor httpprocessor;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private int maxConnPerRoute = 0;
    private int maxConnTotal = 0;
    private HttpHost proxy;
    private AuthenticationStrategy proxyAuthStrategy;
    private boolean redirectHandlingDisabled;
    private RedirectStrategy redirectStrategy;
    private HttpRequestExecutor requestExec;
    private LinkedList<HttpRequestInterceptor> requestFirst;
    private LinkedList<HttpRequestInterceptor> requestLast;
    private LinkedList<HttpResponseInterceptor> responseFirst;
    private LinkedList<HttpResponseInterceptor> responseLast;
    private HttpRequestRetryHandler retryHandler;
    private ConnectionReuseStrategy reuseStrategy;
    private HttpRoutePlanner routePlanner;
    private SchemePortResolver schemePortResolver;
    private ServiceUnavailableRetryStrategy serviceUnavailStrategy;
    private LayeredConnectionSocketFactory sslSocketFactory;
    private SSLContext sslcontext;
    private boolean systemProperties;
    private AuthenticationStrategy targetAuthStrategy;
    private String userAgent;
    private UserTokenHandler userTokenHandler;

    static {
        VersionInfoHC4 vi = VersionInfoHC4.loadVersionInfo("org.apache.http.client", HttpClientBuilder.class.getClassLoader());
        DEFAULT_USER_AGENT = "Apache-HttpClient/" + (vi != null ? vi.getRelease() : VersionInfoHC4.UNAVAILABLE) + " (java 1.5)";
    }

    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    protected HttpClientBuilder() {
    }

    public final HttpClientBuilder setRequestExecutor(HttpRequestExecutor requestExec2) {
        this.requestExec = requestExec2;
        return this;
    }

    public final HttpClientBuilder setHostnameVerifier(X509HostnameVerifier hostnameVerifier2) {
        this.hostnameVerifier = hostnameVerifier2;
        return this;
    }

    public final HttpClientBuilder setSslcontext(SSLContext sslcontext2) {
        this.sslcontext = sslcontext2;
        return this;
    }

    public final HttpClientBuilder setSSLSocketFactory(LayeredConnectionSocketFactory sslSocketFactory2) {
        this.sslSocketFactory = sslSocketFactory2;
        return this;
    }

    public final HttpClientBuilder setMaxConnTotal(int maxConnTotal2) {
        this.maxConnTotal = maxConnTotal2;
        return this;
    }

    public final HttpClientBuilder setMaxConnPerRoute(int maxConnPerRoute2) {
        this.maxConnPerRoute = maxConnPerRoute2;
        return this;
    }

    public final HttpClientBuilder setDefaultSocketConfig(SocketConfig config) {
        this.defaultSocketConfig = config;
        return this;
    }

    public final HttpClientBuilder setDefaultConnectionConfig(ConnectionConfig config) {
        this.defaultConnectionConfig = config;
        return this;
    }

    public final HttpClientBuilder setConnectionManager(HttpClientConnectionManager connManager2) {
        this.connManager = connManager2;
        return this;
    }

    public final HttpClientBuilder setConnectionReuseStrategy(ConnectionReuseStrategy reuseStrategy2) {
        this.reuseStrategy = reuseStrategy2;
        return this;
    }

    public final HttpClientBuilder setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy2) {
        this.keepAliveStrategy = keepAliveStrategy2;
        return this;
    }

    public final HttpClientBuilder setTargetAuthenticationStrategy(AuthenticationStrategy targetAuthStrategy2) {
        this.targetAuthStrategy = targetAuthStrategy2;
        return this;
    }

    public final HttpClientBuilder setProxyAuthenticationStrategy(AuthenticationStrategy proxyAuthStrategy2) {
        this.proxyAuthStrategy = proxyAuthStrategy2;
        return this;
    }

    public final HttpClientBuilder setUserTokenHandler(UserTokenHandler userTokenHandler2) {
        this.userTokenHandler = userTokenHandler2;
        return this;
    }

    public final HttpClientBuilder disableConnectionState() {
        this.connectionStateDisabled = true;
        return this;
    }

    public final HttpClientBuilder setSchemePortResolver(SchemePortResolver schemePortResolver2) {
        this.schemePortResolver = schemePortResolver2;
        return this;
    }

    public final HttpClientBuilder setUserAgent(String userAgent2) {
        this.userAgent = userAgent2;
        return this;
    }

    public final HttpClientBuilder setDefaultHeaders(Collection<? extends Header> defaultHeaders2) {
        this.defaultHeaders = defaultHeaders2;
        return this;
    }

    public final HttpClientBuilder addInterceptorFirst(HttpResponseInterceptor itcp) {
        if (itcp != null) {
            if (this.responseFirst == null) {
                this.responseFirst = new LinkedList<>();
            }
            this.responseFirst.addFirst(itcp);
        }
        return this;
    }

    public final HttpClientBuilder addInterceptorLast(HttpResponseInterceptor itcp) {
        if (itcp != null) {
            if (this.responseLast == null) {
                this.responseLast = new LinkedList<>();
            }
            this.responseLast.addLast(itcp);
        }
        return this;
    }

    public final HttpClientBuilder addInterceptorFirst(HttpRequestInterceptor itcp) {
        if (itcp != null) {
            if (this.requestFirst == null) {
                this.requestFirst = new LinkedList<>();
            }
            this.requestFirst.addFirst(itcp);
        }
        return this;
    }

    public final HttpClientBuilder addInterceptorLast(HttpRequestInterceptor itcp) {
        if (itcp != null) {
            if (this.requestLast == null) {
                this.requestLast = new LinkedList<>();
            }
            this.requestLast.addLast(itcp);
        }
        return this;
    }

    public final HttpClientBuilder disableCookieManagement() {
        this.cookieManagementDisabled = true;
        return this;
    }

    public final HttpClientBuilder disableContentCompression() {
        this.contentCompressionDisabled = true;
        return this;
    }

    public final HttpClientBuilder disableAuthCaching() {
        this.authCachingDisabled = true;
        return this;
    }

    public final HttpClientBuilder setHttpProcessor(HttpProcessor httpprocessor2) {
        this.httpprocessor = httpprocessor2;
        return this;
    }

    public final HttpClientBuilder setRetryHandler(HttpRequestRetryHandler retryHandler2) {
        this.retryHandler = retryHandler2;
        return this;
    }

    public final HttpClientBuilder disableAutomaticRetries() {
        this.automaticRetriesDisabled = true;
        return this;
    }

    public final HttpClientBuilder setProxy(HttpHost proxy2) {
        this.proxy = proxy2;
        return this;
    }

    public final HttpClientBuilder setRoutePlanner(HttpRoutePlanner routePlanner2) {
        this.routePlanner = routePlanner2;
        return this;
    }

    public final HttpClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy2) {
        this.redirectStrategy = redirectStrategy2;
        return this;
    }

    public final HttpClientBuilder disableRedirectHandling() {
        this.redirectHandlingDisabled = true;
        return this;
    }

    public final HttpClientBuilder setConnectionBackoffStrategy(ConnectionBackoffStrategy connectionBackoffStrategy2) {
        this.connectionBackoffStrategy = connectionBackoffStrategy2;
        return this;
    }

    public final HttpClientBuilder setBackoffManager(BackoffManager backoffManager2) {
        this.backoffManager = backoffManager2;
        return this;
    }

    public final HttpClientBuilder setServiceUnavailableRetryStrategy(ServiceUnavailableRetryStrategy serviceUnavailStrategy2) {
        this.serviceUnavailStrategy = serviceUnavailStrategy2;
        return this;
    }

    public final HttpClientBuilder setDefaultCookieStore(CookieStore cookieStore2) {
        this.cookieStore = cookieStore2;
        return this;
    }

    public final HttpClientBuilder setDefaultCredentialsProvider(CredentialsProvider credentialsProvider2) {
        this.credentialsProvider = credentialsProvider2;
        return this;
    }

    public final HttpClientBuilder setDefaultAuthSchemeRegistry(Lookup<AuthSchemeProvider> authSchemeRegistry2) {
        this.authSchemeRegistry = authSchemeRegistry2;
        return this;
    }

    public final HttpClientBuilder setDefaultCookieSpecRegistry(Lookup<CookieSpecProvider> cookieSpecRegistry2) {
        this.cookieSpecRegistry = cookieSpecRegistry2;
        return this;
    }

    public final HttpClientBuilder setDefaultRequestConfig(RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    public final HttpClientBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    /* access modifiers changed from: protected */
    public ClientExecChain decorateMainExec(ClientExecChain mainExec) {
        return mainExec;
    }

    /* access modifiers changed from: protected */
    public ClientExecChain decorateProtocolExec(ClientExecChain protocolExec) {
        return protocolExec;
    }

    /* access modifiers changed from: protected */
    public void addCloseable(Closeable closeable) {
        if (closeable != null) {
            if (this.closeables == null) {
                this.closeables = new ArrayList();
            }
            this.closeables.add(closeable);
        }
    }

    private static String[] split(String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v7, resolved type: org.apache.http.impl.execchain.BackoffStrategyExec} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v8, resolved type: org.apache.http.impl.execchain.ServiceUnavailableRetryExec} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v9, resolved type: org.apache.http.impl.execchain.RedirectExec} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r36v0, resolved type: org.apache.http.conn.socket.LayeredConnectionSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r36v2, resolved type: org.apache.http.conn.ssl.SSLConnectionSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v115, resolved type: org.apache.http.conn.ssl.SSLConnectionSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v116, resolved type: org.apache.http.conn.ssl.SSLConnectionSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v117, resolved type: org.apache.http.conn.ssl.SSLConnectionSocketFactory} */
    /* JADX WARNING: type inference failed for: r36v1 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.apache.http.impl.client.CloseableHttpClient build() {
        /*
            r40 = this;
            r0 = r40
            org.apache.http.protocol.HttpRequestExecutor r5 = r0.requestExec
            if (r5 != 0) goto L_0x000b
            org.apache.http.protocol.HttpRequestExecutor r5 = new org.apache.http.protocol.HttpRequestExecutor
            r5.<init>()
        L_0x000b:
            r0 = r40
            org.apache.http.conn.HttpClientConnectionManager r6 = r0.connManager
            if (r6 != 0) goto L_0x00eb
            r0 = r40
            org.apache.http.conn.socket.LayeredConnectionSocketFactory r0 = r0.sslSocketFactory
            r36 = r0
            if (r36 != 0) goto L_0x005a
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x03b1
            java.lang.String r12 = "https.protocols"
            java.lang.String r12 = java.lang.System.getProperty(r12)
            java.lang.String[] r38 = split(r12)
        L_0x0029:
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x03b5
            java.lang.String r12 = "https.cipherSuites"
            java.lang.String r12 = java.lang.System.getProperty(r12)
            java.lang.String[] r37 = split(r12)
        L_0x0039:
            r0 = r40
            org.apache.http.conn.ssl.X509HostnameVerifier r0 = r0.hostnameVerifier
            r26 = r0
            if (r26 != 0) goto L_0x0043
            org.apache.http.conn.ssl.X509HostnameVerifier r26 = org.apache.http.conn.ssl.SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
        L_0x0043:
            r0 = r40
            javax.net.ssl.SSLContext r12 = r0.sslcontext
            if (r12 == 0) goto L_0x03b9
            org.apache.http.conn.ssl.SSLConnectionSocketFactory r36 = new org.apache.http.conn.ssl.SSLConnectionSocketFactory
            r0 = r40
            javax.net.ssl.SSLContext r12 = r0.sslcontext
            r0 = r36
            r1 = r38
            r2 = r37
            r3 = r26
            r0.<init>((javax.net.ssl.SSLContext) r12, (java.lang.String[]) r1, (java.lang.String[]) r2, (org.apache.http.conn.ssl.X509HostnameVerifier) r3)
        L_0x005a:
            org.apache.http.impl.conn.PoolingHttpClientConnectionManager r30 = new org.apache.http.impl.conn.PoolingHttpClientConnectionManager
            org.apache.http.config.RegistryBuilder r12 = org.apache.http.config.RegistryBuilder.create()
            java.lang.String r13 = "http"
            org.apache.http.conn.socket.PlainConnectionSocketFactory r14 = org.apache.http.conn.socket.PlainConnectionSocketFactory.getSocketFactory()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "https"
            r0 = r36
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r0)
            org.apache.http.config.Registry r12 = r12.build()
            r0 = r30
            r0.<init>((org.apache.http.config.Registry<org.apache.http.conn.socket.ConnectionSocketFactory>) r12)
            r0 = r40
            org.apache.http.config.SocketConfig r12 = r0.defaultSocketConfig
            if (r12 == 0) goto L_0x008a
            r0 = r40
            org.apache.http.config.SocketConfig r12 = r0.defaultSocketConfig
            r0 = r30
            r0.setDefaultSocketConfig(r12)
        L_0x008a:
            r0 = r40
            org.apache.http.config.ConnectionConfig r12 = r0.defaultConnectionConfig
            if (r12 == 0) goto L_0x0099
            r0 = r40
            org.apache.http.config.ConnectionConfig r12 = r0.defaultConnectionConfig
            r0 = r30
            r0.setDefaultConnectionConfig(r12)
        L_0x0099:
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x00cb
            java.lang.String r12 = "http.keepAlive"
            java.lang.String r13 = "true"
            java.lang.String r33 = java.lang.System.getProperty(r12, r13)
            java.lang.String r12 = "true"
            r0 = r33
            boolean r12 = r12.equalsIgnoreCase(r0)
            if (r12 == 0) goto L_0x00cb
            java.lang.String r12 = "http.maxConnections"
            java.lang.String r13 = "5"
            java.lang.String r33 = java.lang.System.getProperty(r12, r13)
            int r29 = java.lang.Integer.parseInt(r33)
            r0 = r30
            r1 = r29
            r0.setDefaultMaxPerRoute(r1)
            int r12 = r29 * 2
            r0 = r30
            r0.setMaxTotal(r12)
        L_0x00cb:
            r0 = r40
            int r12 = r0.maxConnTotal
            if (r12 <= 0) goto L_0x00da
            r0 = r40
            int r12 = r0.maxConnTotal
            r0 = r30
            r0.setMaxTotal(r12)
        L_0x00da:
            r0 = r40
            int r12 = r0.maxConnPerRoute
            if (r12 <= 0) goto L_0x00e9
            r0 = r40
            int r12 = r0.maxConnPerRoute
            r0 = r30
            r0.setDefaultMaxPerRoute(r12)
        L_0x00e9:
            r6 = r30
        L_0x00eb:
            r0 = r40
            org.apache.http.ConnectionReuseStrategy r7 = r0.reuseStrategy
            if (r7 != 0) goto L_0x010b
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x03eb
            java.lang.String r12 = "http.keepAlive"
            java.lang.String r13 = "true"
            java.lang.String r33 = java.lang.System.getProperty(r12, r13)
            java.lang.String r12 = "true"
            r0 = r33
            boolean r12 = r12.equalsIgnoreCase(r0)
            if (r12 == 0) goto L_0x03e7
            org.apache.http.impl.DefaultConnectionReuseStrategyHC4 r7 = org.apache.http.impl.DefaultConnectionReuseStrategyHC4.INSTANCE
        L_0x010b:
            r0 = r40
            org.apache.http.conn.ConnectionKeepAliveStrategy r8 = r0.keepAliveStrategy
            if (r8 != 0) goto L_0x0113
            org.apache.http.impl.client.DefaultConnectionKeepAliveStrategyHC4 r8 = org.apache.http.impl.client.DefaultConnectionKeepAliveStrategyHC4.INSTANCE
        L_0x0113:
            r0 = r40
            org.apache.http.client.AuthenticationStrategy r9 = r0.targetAuthStrategy
            if (r9 != 0) goto L_0x011b
            org.apache.http.impl.client.TargetAuthenticationStrategy r9 = org.apache.http.impl.client.TargetAuthenticationStrategy.INSTANCE
        L_0x011b:
            r0 = r40
            org.apache.http.client.AuthenticationStrategy r10 = r0.proxyAuthStrategy
            if (r10 != 0) goto L_0x0123
            org.apache.http.impl.client.ProxyAuthenticationStrategy r10 = org.apache.http.impl.client.ProxyAuthenticationStrategy.INSTANCE
        L_0x0123:
            r0 = r40
            org.apache.http.client.UserTokenHandler r11 = r0.userTokenHandler
            if (r11 != 0) goto L_0x0131
            r0 = r40
            boolean r12 = r0.connectionStateDisabled
            if (r12 != 0) goto L_0x03ef
            org.apache.http.impl.client.DefaultUserTokenHandlerHC4 r11 = org.apache.http.impl.client.DefaultUserTokenHandlerHC4.INSTANCE
        L_0x0131:
            org.apache.http.impl.execchain.MainClientExec r4 = new org.apache.http.impl.execchain.MainClientExec
            r4.<init>(r5, r6, r7, r8, r9, r10, r11)
            r0 = r40
            org.apache.http.impl.execchain.ClientExecChain r4 = r0.decorateMainExec(r4)
            r0 = r40
            org.apache.http.protocol.HttpProcessor r0 = r0.httpprocessor
            r27 = r0
            if (r27 != 0) goto L_0x0246
            r0 = r40
            java.lang.String r0 = r0.userAgent
            r39 = r0
            if (r39 != 0) goto L_0x015c
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x0158
            java.lang.String r12 = "http.agent"
            java.lang.String r39 = java.lang.System.getProperty(r12)
        L_0x0158:
            if (r39 != 0) goto L_0x015c
            java.lang.String r39 = DEFAULT_USER_AGENT
        L_0x015c:
            org.apache.http.protocol.HttpProcessorBuilder r22 = org.apache.http.protocol.HttpProcessorBuilder.create()
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpRequestInterceptor> r12 = r0.requestFirst
            if (r12 == 0) goto L_0x0174
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpRequestInterceptor> r12 = r0.requestFirst
            java.util.Iterator r12 = r12.iterator()
        L_0x016e:
            boolean r13 = r12.hasNext()
            if (r13 != 0) goto L_0x03f3
        L_0x0174:
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpResponseInterceptor> r12 = r0.responseFirst
            if (r12 == 0) goto L_0x0188
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpResponseInterceptor> r12 = r0.responseFirst
            java.util.Iterator r12 = r12.iterator()
        L_0x0182:
            boolean r13 = r12.hasNext()
            if (r13 != 0) goto L_0x0402
        L_0x0188:
            r12 = 6
            org.apache.http.HttpRequestInterceptor[] r12 = new org.apache.http.HttpRequestInterceptor[r12]
            r13 = 0
            org.apache.http.client.protocol.RequestDefaultHeadersHC4 r14 = new org.apache.http.client.protocol.RequestDefaultHeadersHC4
            r0 = r40
            java.util.Collection<? extends org.apache.http.Header> r0 = r0.defaultHeaders
            r20 = r0
            r0 = r20
            r14.<init>(r0)
            r12[r13] = r14
            r13 = 1
            org.apache.http.protocol.RequestContentHC4 r14 = new org.apache.http.protocol.RequestContentHC4
            r14.<init>()
            r12[r13] = r14
            r13 = 2
            org.apache.http.protocol.RequestTargetHostHC4 r14 = new org.apache.http.protocol.RequestTargetHostHC4
            r14.<init>()
            r12[r13] = r14
            r13 = 3
            org.apache.http.client.protocol.RequestClientConnControl r14 = new org.apache.http.client.protocol.RequestClientConnControl
            r14.<init>()
            r12[r13] = r14
            r13 = 4
            org.apache.http.protocol.RequestUserAgentHC4 r14 = new org.apache.http.protocol.RequestUserAgentHC4
            r0 = r39
            r14.<init>(r0)
            r12[r13] = r14
            r13 = 5
            org.apache.http.client.protocol.RequestExpectContinue r14 = new org.apache.http.client.protocol.RequestExpectContinue
            r14.<init>()
            r12[r13] = r14
            r0 = r22
            r0.addAll((org.apache.http.HttpRequestInterceptor[]) r12)
            r0 = r40
            boolean r12 = r0.cookieManagementDisabled
            if (r12 != 0) goto L_0x01da
            org.apache.http.client.protocol.RequestAddCookiesHC4 r12 = new org.apache.http.client.protocol.RequestAddCookiesHC4
            r12.<init>()
            r0 = r22
            r0.add((org.apache.http.HttpRequestInterceptor) r12)
        L_0x01da:
            r0 = r40
            boolean r12 = r0.contentCompressionDisabled
            if (r12 != 0) goto L_0x01ea
            org.apache.http.client.protocol.RequestAcceptEncoding r12 = new org.apache.http.client.protocol.RequestAcceptEncoding
            r12.<init>()
            r0 = r22
            r0.add((org.apache.http.HttpRequestInterceptor) r12)
        L_0x01ea:
            r0 = r40
            boolean r12 = r0.authCachingDisabled
            if (r12 != 0) goto L_0x01fa
            org.apache.http.client.protocol.RequestAuthCache r12 = new org.apache.http.client.protocol.RequestAuthCache
            r12.<init>()
            r0 = r22
            r0.add((org.apache.http.HttpRequestInterceptor) r12)
        L_0x01fa:
            r0 = r40
            boolean r12 = r0.cookieManagementDisabled
            if (r12 != 0) goto L_0x020a
            org.apache.http.client.protocol.ResponseProcessCookiesHC4 r12 = new org.apache.http.client.protocol.ResponseProcessCookiesHC4
            r12.<init>()
            r0 = r22
            r0.add((org.apache.http.HttpResponseInterceptor) r12)
        L_0x020a:
            r0 = r40
            boolean r12 = r0.contentCompressionDisabled
            if (r12 != 0) goto L_0x021a
            org.apache.http.client.protocol.ResponseContentEncoding r12 = new org.apache.http.client.protocol.ResponseContentEncoding
            r12.<init>()
            r0 = r22
            r0.add((org.apache.http.HttpResponseInterceptor) r12)
        L_0x021a:
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpRequestInterceptor> r12 = r0.requestLast
            if (r12 == 0) goto L_0x022e
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpRequestInterceptor> r12 = r0.requestLast
            java.util.Iterator r12 = r12.iterator()
        L_0x0228:
            boolean r13 = r12.hasNext()
            if (r13 != 0) goto L_0x0411
        L_0x022e:
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpResponseInterceptor> r12 = r0.responseLast
            if (r12 == 0) goto L_0x0242
            r0 = r40
            java.util.LinkedList<org.apache.http.HttpResponseInterceptor> r12 = r0.responseLast
            java.util.Iterator r12 = r12.iterator()
        L_0x023c:
            boolean r13 = r12.hasNext()
            if (r13 != 0) goto L_0x0420
        L_0x0242:
            org.apache.http.protocol.HttpProcessor r27 = r22.build()
        L_0x0246:
            org.apache.http.impl.execchain.ProtocolExec r25 = new org.apache.http.impl.execchain.ProtocolExec
            r0 = r25
            r1 = r27
            r0.<init>(r4, r1)
            r0 = r40
            r1 = r25
            org.apache.http.impl.execchain.ClientExecChain r4 = r0.decorateProtocolExec(r1)
            r0 = r40
            boolean r12 = r0.automaticRetriesDisabled
            if (r12 != 0) goto L_0x0272
            r0 = r40
            org.apache.http.client.HttpRequestRetryHandler r0 = r0.retryHandler
            r32 = r0
            if (r32 != 0) goto L_0x0267
            org.apache.http.impl.client.DefaultHttpRequestRetryHandlerHC4 r32 = org.apache.http.impl.client.DefaultHttpRequestRetryHandlerHC4.INSTANCE
        L_0x0267:
            org.apache.http.impl.execchain.RetryExec r25 = new org.apache.http.impl.execchain.RetryExec
            r0 = r25
            r1 = r32
            r0.<init>(r4, r1)
            r4 = r25
        L_0x0272:
            r0 = r40
            org.apache.http.conn.routing.HttpRoutePlanner r15 = r0.routePlanner
            if (r15 != 0) goto L_0x0293
            r0 = r40
            org.apache.http.conn.SchemePortResolver r0 = r0.schemePortResolver
            r34 = r0
            if (r34 != 0) goto L_0x0282
            org.apache.http.impl.conn.DefaultSchemePortResolver r34 = org.apache.http.impl.conn.DefaultSchemePortResolver.INSTANCE
        L_0x0282:
            r0 = r40
            org.apache.http.HttpHost r12 = r0.proxy
            if (r12 == 0) goto L_0x042f
            org.apache.http.impl.conn.DefaultProxyRoutePlanner r15 = new org.apache.http.impl.conn.DefaultProxyRoutePlanner
            r0 = r40
            org.apache.http.HttpHost r12 = r0.proxy
            r0 = r34
            r15.<init>(r12, r0)
        L_0x0293:
            r0 = r40
            boolean r12 = r0.redirectHandlingDisabled
            if (r12 != 0) goto L_0x02ae
            r0 = r40
            org.apache.http.client.RedirectStrategy r0 = r0.redirectStrategy
            r31 = r0
            if (r31 != 0) goto L_0x02a3
            org.apache.http.impl.client.DefaultRedirectStrategy r31 = org.apache.http.impl.client.DefaultRedirectStrategy.INSTANCE
        L_0x02a3:
            org.apache.http.impl.execchain.RedirectExec r25 = new org.apache.http.impl.execchain.RedirectExec
            r0 = r25
            r1 = r31
            r0.<init>(r4, r15, r1)
            r4 = r25
        L_0x02ae:
            r0 = r40
            org.apache.http.client.ServiceUnavailableRetryStrategy r0 = r0.serviceUnavailStrategy
            r35 = r0
            if (r35 == 0) goto L_0x02c1
            org.apache.http.impl.execchain.ServiceUnavailableRetryExec r25 = new org.apache.http.impl.execchain.ServiceUnavailableRetryExec
            r0 = r25
            r1 = r35
            r0.<init>(r4, r1)
            r4 = r25
        L_0x02c1:
            r0 = r40
            org.apache.http.client.BackoffManager r0 = r0.backoffManager
            r23 = r0
            r0 = r40
            org.apache.http.client.ConnectionBackoffStrategy r0 = r0.connectionBackoffStrategy
            r24 = r0
            if (r23 == 0) goto L_0x02de
            if (r24 == 0) goto L_0x02de
            org.apache.http.impl.execchain.BackoffStrategyExec r25 = new org.apache.http.impl.execchain.BackoffStrategyExec
            r0 = r25
            r1 = r24
            r2 = r23
            r0.<init>(r4, r1, r2)
            r4 = r25
        L_0x02de:
            r0 = r40
            org.apache.http.config.Lookup<org.apache.http.auth.AuthSchemeProvider> r0 = r0.authSchemeRegistry
            r17 = r0
            if (r17 != 0) goto L_0x030f
            org.apache.http.config.RegistryBuilder r12 = org.apache.http.config.RegistryBuilder.create()
            java.lang.String r13 = "Basic"
            org.apache.http.impl.auth.BasicSchemeFactoryHC4 r14 = new org.apache.http.impl.auth.BasicSchemeFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "Digest"
            org.apache.http.impl.auth.DigestSchemeFactoryHC4 r14 = new org.apache.http.impl.auth.DigestSchemeFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "NTLM"
            org.apache.http.impl.auth.NTLMSchemeFactory r14 = new org.apache.http.impl.auth.NTLMSchemeFactory
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            org.apache.http.config.Registry r17 = r12.build()
        L_0x030f:
            r0 = r40
            org.apache.http.config.Lookup<org.apache.http.cookie.CookieSpecProvider> r0 = r0.cookieSpecRegistry
            r16 = r0
            if (r16 != 0) goto L_0x036c
            org.apache.http.config.RegistryBuilder r12 = org.apache.http.config.RegistryBuilder.create()
            java.lang.String r13 = "best-match"
            org.apache.http.impl.cookie.BestMatchSpecFactoryHC4 r14 = new org.apache.http.impl.cookie.BestMatchSpecFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "standard"
            org.apache.http.impl.cookie.RFC2965SpecFactoryHC4 r14 = new org.apache.http.impl.cookie.RFC2965SpecFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "compatibility"
            org.apache.http.impl.cookie.BrowserCompatSpecFactoryHC4 r14 = new org.apache.http.impl.cookie.BrowserCompatSpecFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "netscape"
            org.apache.http.impl.cookie.NetscapeDraftSpecFactoryHC4 r14 = new org.apache.http.impl.cookie.NetscapeDraftSpecFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "ignoreCookies"
            org.apache.http.impl.cookie.IgnoreSpecFactory r14 = new org.apache.http.impl.cookie.IgnoreSpecFactory
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "rfc2109"
            org.apache.http.impl.cookie.RFC2109SpecFactoryHC4 r14 = new org.apache.http.impl.cookie.RFC2109SpecFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            java.lang.String r13 = "rfc2965"
            org.apache.http.impl.cookie.RFC2965SpecFactoryHC4 r14 = new org.apache.http.impl.cookie.RFC2965SpecFactoryHC4
            r14.<init>()
            org.apache.http.config.RegistryBuilder r12 = r12.register(r13, r14)
            org.apache.http.config.Registry r16 = r12.build()
        L_0x036c:
            r0 = r40
            org.apache.http.client.CookieStore r0 = r0.cookieStore
            r18 = r0
            if (r18 != 0) goto L_0x0379
            org.apache.http.impl.client.BasicCookieStoreHC4 r18 = new org.apache.http.impl.client.BasicCookieStoreHC4
            r18.<init>()
        L_0x0379:
            r0 = r40
            org.apache.http.client.CredentialsProvider r0 = r0.credentialsProvider
            r19 = r0
            if (r19 != 0) goto L_0x038c
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x044b
            org.apache.http.impl.client.SystemDefaultCredentialsProvider r19 = new org.apache.http.impl.client.SystemDefaultCredentialsProvider
            r19.<init>()
        L_0x038c:
            org.apache.http.impl.client.InternalHttpClient r12 = new org.apache.http.impl.client.InternalHttpClient
            r0 = r40
            org.apache.http.client.config.RequestConfig r13 = r0.defaultRequestConfig
            if (r13 == 0) goto L_0x0452
            r0 = r40
            org.apache.http.client.config.RequestConfig r0 = r0.defaultRequestConfig
            r20 = r0
        L_0x039a:
            r0 = r40
            java.util.List<java.io.Closeable> r13 = r0.closeables
            if (r13 == 0) goto L_0x0456
            java.util.ArrayList r21 = new java.util.ArrayList
            r0 = r40
            java.util.List<java.io.Closeable> r13 = r0.closeables
            r0 = r21
            r0.<init>(r13)
        L_0x03ab:
            r13 = r4
            r14 = r6
            r12.<init>(r13, r14, r15, r16, r17, r18, r19, r20, r21)
            return r12
        L_0x03b1:
            r38 = 0
            goto L_0x0029
        L_0x03b5:
            r37 = 0
            goto L_0x0039
        L_0x03b9:
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x03d5
            org.apache.http.conn.ssl.SSLConnectionSocketFactory r36 = new org.apache.http.conn.ssl.SSLConnectionSocketFactory
            r12 = 0
            javax.net.SocketFactory r12 = android.net.SSLCertificateSocketFactory.getDefault(r12)
            javax.net.ssl.SSLSocketFactory r12 = (javax.net.ssl.SSLSocketFactory) r12
            r0 = r36
            r1 = r38
            r2 = r37
            r3 = r26
            r0.<init>((javax.net.ssl.SSLSocketFactory) r12, (java.lang.String[]) r1, (java.lang.String[]) r2, (org.apache.http.conn.ssl.X509HostnameVerifier) r3)
            goto L_0x005a
        L_0x03d5:
            org.apache.http.conn.ssl.SSLConnectionSocketFactory r36 = new org.apache.http.conn.ssl.SSLConnectionSocketFactory
            r12 = 0
            javax.net.SocketFactory r12 = android.net.SSLCertificateSocketFactory.getDefault(r12)
            javax.net.ssl.SSLSocketFactory r12 = (javax.net.ssl.SSLSocketFactory) r12
            r0 = r36
            r1 = r26
            r0.<init>((javax.net.ssl.SSLSocketFactory) r12, (org.apache.http.conn.ssl.X509HostnameVerifier) r1)
            goto L_0x005a
        L_0x03e7:
            org.apache.http.impl.NoConnectionReuseStrategyHC4 r7 = org.apache.http.impl.NoConnectionReuseStrategyHC4.INSTANCE
            goto L_0x010b
        L_0x03eb:
            org.apache.http.impl.DefaultConnectionReuseStrategyHC4 r7 = org.apache.http.impl.DefaultConnectionReuseStrategyHC4.INSTANCE
            goto L_0x010b
        L_0x03ef:
            org.apache.http.impl.client.NoopUserTokenHandler r11 = org.apache.http.impl.client.NoopUserTokenHandler.INSTANCE
            goto L_0x0131
        L_0x03f3:
            java.lang.Object r28 = r12.next()
            org.apache.http.HttpRequestInterceptor r28 = (org.apache.http.HttpRequestInterceptor) r28
            r0 = r22
            r1 = r28
            r0.addFirst((org.apache.http.HttpRequestInterceptor) r1)
            goto L_0x016e
        L_0x0402:
            java.lang.Object r28 = r12.next()
            org.apache.http.HttpResponseInterceptor r28 = (org.apache.http.HttpResponseInterceptor) r28
            r0 = r22
            r1 = r28
            r0.addFirst((org.apache.http.HttpResponseInterceptor) r1)
            goto L_0x0182
        L_0x0411:
            java.lang.Object r28 = r12.next()
            org.apache.http.HttpRequestInterceptor r28 = (org.apache.http.HttpRequestInterceptor) r28
            r0 = r22
            r1 = r28
            r0.addLast((org.apache.http.HttpRequestInterceptor) r1)
            goto L_0x0228
        L_0x0420:
            java.lang.Object r28 = r12.next()
            org.apache.http.HttpResponseInterceptor r28 = (org.apache.http.HttpResponseInterceptor) r28
            r0 = r22
            r1 = r28
            r0.addLast((org.apache.http.HttpResponseInterceptor) r1)
            goto L_0x023c
        L_0x042f:
            r0 = r40
            boolean r12 = r0.systemProperties
            if (r12 == 0) goto L_0x0442
            org.apache.http.impl.conn.SystemDefaultRoutePlanner r15 = new org.apache.http.impl.conn.SystemDefaultRoutePlanner
            java.net.ProxySelector r12 = java.net.ProxySelector.getDefault()
            r0 = r34
            r15.<init>(r0, r12)
            goto L_0x0293
        L_0x0442:
            org.apache.http.impl.conn.DefaultRoutePlanner r15 = new org.apache.http.impl.conn.DefaultRoutePlanner
            r0 = r34
            r15.<init>(r0)
            goto L_0x0293
        L_0x044b:
            org.apache.http.impl.client.BasicCredentialsProviderHC4 r19 = new org.apache.http.impl.client.BasicCredentialsProviderHC4
            r19.<init>()
            goto L_0x038c
        L_0x0452:
            org.apache.http.client.config.RequestConfig r20 = org.apache.http.client.config.RequestConfig.DEFAULT
            goto L_0x039a
        L_0x0456:
            r21 = 0
            goto L_0x03ab
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.client.HttpClientBuilder.build():org.apache.http.impl.client.CloseableHttpClient");
    }
}
