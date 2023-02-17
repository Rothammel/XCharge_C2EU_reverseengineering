package org.apache.mina.proxy.handlers.http.digest;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.AuthSchemes;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDigestAuthLogicHandler extends AbstractAuthLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) HttpDigestAuthLogicHandler.class);
    private static SecureRandom rnd;
    private HashMap<String, String> directives = null;
    private HttpProxyResponse response;

    static {
        try {
            rnd = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpDigestAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        super(proxyIoSession);
        ((HttpProxyRequest) this.request).checkRequiredProperties(HttpProxyConstants.USER_PROPERTY, HttpProxyConstants.PWD_PROPERTY);
    }

    /* JADX WARNING: type inference failed for: r3v16, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r3v38, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r3v42, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r3v53, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r3v55, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    public void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException {
        Map<String, List<String>> headers;
        logger.debug(" doHandshake()");
        if (this.step <= 0 || this.directives != null) {
            HttpProxyRequest req = (HttpProxyRequest) this.request;
            if (req.getHeaders() != null) {
                headers = req.getHeaders();
            } else {
                headers = new HashMap<>();
            }
            if (this.step > 0) {
                logger.debug("  sending DIGEST challenge response");
                HashMap<String, String> map = new HashMap<>();
                map.put("username", req.getProperties().get(HttpProxyConstants.USER_PROPERTY));
                StringUtilities.copyDirective(this.directives, map, "realm");
                StringUtilities.copyDirective(this.directives, map, "uri");
                StringUtilities.copyDirective(this.directives, map, "opaque");
                StringUtilities.copyDirective(this.directives, map, "nonce");
                String algorithm = StringUtilities.copyDirective(this.directives, map, "algorithm");
                if (algorithm == null || "md5".equalsIgnoreCase(algorithm) || "md5-sess".equalsIgnoreCase(algorithm)) {
                    String qop = this.directives.get("qop");
                    if (qop != null) {
                        StringTokenizer stringTokenizer = new StringTokenizer(qop, ",");
                        String token = null;
                        while (stringTokenizer.hasMoreTokens()) {
                            String tk = stringTokenizer.nextToken();
                            if ("auth".equalsIgnoreCase(token)) {
                                break;
                            } else if (Arrays.binarySearch(DigestUtilities.SUPPORTED_QOPS, tk) > -1) {
                                token = tk;
                            }
                        }
                        if (token != null) {
                            map.put("qop", token);
                            byte[] nonce = new byte[8];
                            rnd.nextBytes(nonce);
                            try {
                                map.put("cnonce", new String(Base64.encodeBase64(nonce), this.proxyIoSession.getCharsetName()));
                            } catch (UnsupportedEncodingException e) {
                                throw new ProxyAuthException("Unable to encode cnonce", e);
                            }
                        } else {
                            throw new ProxyAuthException("No supported qop option available");
                        }
                    }
                    map.put("nc", "00000001");
                    map.put("uri", req.getHttpURI());
                    try {
                        map.put("response", DigestUtilities.computeResponseValue(this.proxyIoSession.getSession(), map, req.getHttpVerb().toUpperCase(), req.getProperties().get(HttpProxyConstants.PWD_PROPERTY), this.proxyIoSession.getCharsetName(), this.response.getBody()));
                        StringBuilder sb = new StringBuilder("Digest ");
                        boolean addSeparator = false;
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            String key = entry.getKey();
                            if (addSeparator) {
                                sb.append(", ");
                            } else {
                                addSeparator = true;
                            }
                            boolean quotedValue = !"qop".equals(key) && !"nc".equals(key);
                            sb.append(key);
                            if (quotedValue) {
                                sb.append("=\"").append(entry.getValue()).append(TokenParser.DQUOTE);
                            } else {
                                sb.append('=').append(entry.getValue());
                            }
                        }
                        StringUtilities.addValueToHeader(headers, HttpHeaders.PROXY_AUTHORIZATION, sb.toString(), true);
                    } catch (Exception e2) {
                        throw new ProxyAuthException("Digest response computing failed", e2);
                    }
                } else {
                    throw new ProxyAuthException("Unknown algorithm required by server");
                }
            }
            addKeepAliveHeaders(headers);
            req.setHeaders(headers);
            writeRequest(nextFilter, req);
            this.step++;
            return;
        }
        throw new ProxyAuthException("Authentication challenge not received");
    }

    /* JADX WARNING: type inference failed for: r4v1, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r4v6, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r4v11, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r4v13, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    public void handleResponse(HttpProxyResponse response2) throws ProxyAuthException {
        this.response = response2;
        if (this.step != 0) {
            throw new ProxyAuthException("Received unexpected response code (" + response2.getStatusLine() + ").");
        } else if (response2.getStatusCode() == 401 || response2.getStatusCode() == 407) {
            String challengeResponse = null;
            Iterator<String> it = response2.getHeaders().get(HttpHeaders.PROXY_AUTHENTICATE).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String s = it.next();
                if (s.startsWith(AuthSchemes.DIGEST)) {
                    challengeResponse = s;
                    break;
                }
            }
            if (challengeResponse == null) {
                throw new ProxyAuthException("Server doesn't support digest authentication method !");
            }
            try {
                this.directives = StringUtilities.parseDirectives(challengeResponse.substring(7).getBytes(this.proxyIoSession.getCharsetName()));
                this.step = 1;
            } catch (Exception e) {
                throw new ProxyAuthException("Parsing of server digest directives failed", e);
            }
        } else {
            throw new ProxyAuthException("Received unexpected response code (" + response2.getStatusLine() + ").");
        }
    }
}
