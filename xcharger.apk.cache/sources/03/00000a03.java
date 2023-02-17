package okhttp3.internal.http;

import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Address;
import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RouteException;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.http2.ConnectionShutdownException;
import org.apache.http.client.methods.HttpHeadHC4;

/* loaded from: classes.dex */
public final class RetryAndFollowUpInterceptor implements Interceptor {
    private static final int MAX_FOLLOW_UPS = 20;
    private Object callStackTrace;
    private volatile boolean canceled;
    private final OkHttpClient client;
    private final boolean forWebSocket;
    private volatile StreamAllocation streamAllocation;

    public RetryAndFollowUpInterceptor(OkHttpClient client, boolean forWebSocket) {
        this.client = client;
        this.forWebSocket = forWebSocket;
    }

    public void cancel() {
        this.canceled = true;
        StreamAllocation streamAllocation = this.streamAllocation;
        if (streamAllocation != null) {
            streamAllocation.cancel();
        }
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCallStackTrace(Object callStackTrace) {
        this.callStackTrace = callStackTrace;
    }

    public StreamAllocation streamAllocation() {
        return this.streamAllocation;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response response;
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();
        StreamAllocation streamAllocation = new StreamAllocation(this.client.connectionPool(), createAddress(request.url()), call, eventListener, this.callStackTrace);
        this.streamAllocation = streamAllocation;
        int followUpCount = 0;
        Response priorResponse = null;
        while (!this.canceled) {
            boolean releaseConnection = true;
            try {
                try {
                    try {
                        response = realChain.proceed(request, streamAllocation, null, null);
                        releaseConnection = false;
                        if (priorResponse != null) {
                            response = response.newBuilder().priorResponse(priorResponse.newBuilder().body(null).build()).build();
                        }
                    } catch (IOException e) {
                        boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                        if (!recover(e, streamAllocation, requestSendStarted, request)) {
                            throw e;
                        }
                        if (0 != 0) {
                            streamAllocation.streamFailed(null);
                            streamAllocation.release();
                        }
                    }
                } catch (RouteException e2) {
                    if (!recover(e2.getLastConnectException(), streamAllocation, false, request)) {
                        throw e2.getFirstConnectException();
                    }
                    if (0 != 0) {
                        streamAllocation.streamFailed(null);
                        streamAllocation.release();
                    }
                }
                try {
                    Request followUp = followUpRequest(response, streamAllocation.route());
                    if (followUp == null) {
                        if (!this.forWebSocket) {
                            streamAllocation.release();
                        }
                        return response;
                    }
                    Util.closeQuietly(response.body());
                    followUpCount++;
                    if (followUpCount > 20) {
                        streamAllocation.release();
                        throw new ProtocolException("Too many follow-up requests: " + followUpCount);
                    } else if (followUp.body() instanceof UnrepeatableRequestBody) {
                        streamAllocation.release();
                        throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
                    } else {
                        if (!sameConnection(response, followUp.url())) {
                            streamAllocation.release();
                            streamAllocation = new StreamAllocation(this.client.connectionPool(), createAddress(followUp.url()), call, eventListener, this.callStackTrace);
                            this.streamAllocation = streamAllocation;
                        } else if (streamAllocation.codec() != null) {
                            throw new IllegalStateException("Closing the body of " + response + " didn't close its backing stream. Bad interceptor?");
                        }
                        request = followUp;
                        priorResponse = response;
                    }
                } catch (IOException e3) {
                    streamAllocation.release();
                    throw e3;
                }
            } finally {
                if (releaseConnection) {
                    streamAllocation.streamFailed(null);
                    streamAllocation.release();
                }
            }
        }
        streamAllocation.release();
        throw new IOException("Canceled");
    }

    private Address createAddress(HttpUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
            sslSocketFactory = this.client.sslSocketFactory();
            hostnameVerifier = this.client.hostnameVerifier();
            certificatePinner = this.client.certificatePinner();
        }
        return new Address(url.host(), url.port(), this.client.dns(), this.client.socketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, this.client.proxyAuthenticator(), this.client.proxy(), this.client.protocols(), this.client.connectionSpecs(), this.client.proxySelector());
    }

    private boolean recover(IOException e, StreamAllocation streamAllocation, boolean requestSendStarted, Request userRequest) {
        streamAllocation.streamFailed(e);
        if (this.client.retryOnConnectionFailure()) {
            return !(requestSendStarted && (userRequest.body() instanceof UnrepeatableRequestBody)) && isRecoverable(e, requestSendStarted) && streamAllocation.hasMoreRoutes();
        }
        return false;
    }

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        boolean z = true;
        if (e instanceof ProtocolException) {
            return false;
        }
        if (!(e instanceof InterruptedIOException)) {
            return (((e instanceof SSLHandshakeException) && (e.getCause() instanceof CertificateException)) || (e instanceof SSLPeerUnverifiedException)) ? false : true;
        }
        if (!(e instanceof SocketTimeoutException) || requestSendStarted) {
            z = false;
        }
        return z;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        String location;
        HttpUrl url;
        Proxy selectedProxy;
        if (userResponse == null) {
            throw new IllegalStateException();
        }
        int responseCode = userResponse.code();
        String method = userResponse.request().method();
        switch (responseCode) {
            case XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED /* 300 */:
            case 301:
            case 302:
            case 303:
                break;
            case StatusLine.HTTP_TEMP_REDIRECT /* 307 */:
            case StatusLine.HTTP_PERM_REDIRECT /* 308 */:
                if (!method.equals("GET") && !method.equals(HttpHeadHC4.METHOD_NAME)) {
                    return null;
                }
                break;
            case 401:
                return this.client.authenticator().authenticate(route, userResponse);
            case 407:
                if (route != null) {
                    selectedProxy = route.proxy();
                } else {
                    selectedProxy = this.client.proxy();
                }
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                return this.client.proxyAuthenticator().authenticate(route, userResponse);
            case 408:
                if (!this.client.retryOnConnectionFailure() || (userResponse.request().body() instanceof UnrepeatableRequestBody)) {
                    return null;
                }
                if ((userResponse.priorResponse() == null || userResponse.priorResponse().code() != 408) && retryAfter(userResponse, 0) <= 0) {
                    return userResponse.request();
                }
                return null;
            case FTPCodes.BAD_SEQUENCE_OF_COMMANDS /* 503 */:
                if ((userResponse.priorResponse() == null || userResponse.priorResponse().code() != 503) && retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                    return userResponse.request();
                }
                return null;
            default:
                return null;
        }
        if (!this.client.followRedirects() || (location = userResponse.header("Location")) == null || (url = userResponse.request().url().resolve(location)) == null) {
            return null;
        }
        boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
        if (sameScheme || this.client.followSslRedirects()) {
            Request.Builder requestBuilder = userResponse.request().newBuilder();
            if (HttpMethod.permitsRequestBody(method)) {
                boolean maintainBody = HttpMethod.redirectsWithBody(method);
                if (HttpMethod.redirectsToGet(method)) {
                    requestBuilder.method("GET", null);
                } else {
                    RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
                    requestBuilder.method(method, requestBody);
                }
                if (!maintainBody) {
                    requestBuilder.removeHeader(org.apache.http.HttpHeaders.TRANSFER_ENCODING);
                    requestBuilder.removeHeader("Content-Length");
                    requestBuilder.removeHeader("Content-Type");
                }
            }
            if (!sameConnection(userResponse, url)) {
                requestBuilder.removeHeader("Authorization");
            }
            return requestBuilder.url(url).build();
        }
        return null;
    }

    private int retryAfter(Response userResponse, int defaultDelay) {
        String header = userResponse.header(org.apache.http.HttpHeaders.RETRY_AFTER);
        if (header != null) {
            if (header.matches("\\d+")) {
                int defaultDelay2 = Integer.valueOf(header).intValue();
                return defaultDelay2;
            }
            return Integer.MAX_VALUE;
        }
        return defaultDelay;
    }

    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host()) && url.port() == followUp.port() && url.scheme().equals(followUp.scheme());
    }
}