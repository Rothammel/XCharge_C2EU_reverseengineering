package org.apache.http.impl.auth;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BasicHeaderValueFormatterHC4;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.BasicHttpContextHC4;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

@NotThreadSafe
public class DigestSchemeHC4 extends RFC2617SchemeHC4 {
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int QOP_AUTH = 2;
    private static final int QOP_AUTH_INT = 1;
    private static final int QOP_MISSING = 0;
    private static final int QOP_UNKNOWN = -1;

    /* renamed from: a1 */
    private String f170a1;

    /* renamed from: a2 */
    private String f171a2;
    private String cnonce;
    private boolean complete;
    private String lastNonce;
    private long nounceCount;

    public DigestSchemeHC4(Charset credentialsCharset) {
        super(credentialsCharset);
        this.complete = false;
    }

    @Deprecated
    public DigestSchemeHC4(ChallengeState challengeState) {
        super(challengeState);
    }

    public DigestSchemeHC4() {
        this(Consts.ASCII);
    }

    public void processChallenge(Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        this.complete = true;
    }

    public boolean isComplete() {
        if ("true".equalsIgnoreCase(getParameter("stale"))) {
            return false;
        }
        return this.complete;
    }

    public String getSchemeName() {
        return "digest";
    }

    public boolean isConnectionBased() {
        return false;
    }

    public void overrideParamter(String name, String value) {
        getParameters().put(name, value);
    }

    @Deprecated
    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        return authenticate(credentials, request, new BasicHttpContextHC4());
    }

    public Header authenticate(Credentials credentials, HttpRequest request, HttpContext context) throws AuthenticationException {
        Args.notNull(credentials, "Credentials");
        Args.notNull(request, "HTTP request");
        if (getParameter("realm") == null) {
            throw new AuthenticationException("missing realm in challenge");
        } else if (getParameter("nonce") == null) {
            throw new AuthenticationException("missing nonce in challenge");
        } else {
            getParameters().put("methodname", request.getRequestLine().getMethod());
            getParameters().put("uri", request.getRequestLine().getUri());
            if (getParameter("charset") == null) {
                getParameters().put("charset", getCredentialsCharset(request));
            }
            return createDigestHeader(credentials, request);
        }
    }

    private static MessageDigest createMessageDigest(String digAlg) throws UnsupportedDigestAlgorithmException {
        try {
            return MessageDigest.getInstance(digAlg);
        } catch (Exception e) {
            throw new UnsupportedDigestAlgorithmException("Unsupported algorithm in HTTP Digest authentication: " + digAlg);
        }
    }

    private Header createDigestHeader(Credentials credentials, HttpRequest request) throws AuthenticationException {
        String digestValue;
        boolean z;
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String opaque = getParameter("opaque");
        String method = getParameter("methodname");
        String algorithm = getParameter("algorithm");
        if (algorithm == null) {
            algorithm = "MD5";
        }
        HashSet hashSet = new HashSet(8);
        int qop = -1;
        String qoplist = getParameter("qop");
        if (qoplist != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(qoplist, ",");
            while (stringTokenizer.hasMoreTokens()) {
                hashSet.add(stringTokenizer.nextToken().trim().toLowerCase(Locale.ENGLISH));
            }
            if ((request instanceof HttpEntityEnclosingRequest) && hashSet.contains("auth-int")) {
                qop = 1;
            } else if (hashSet.contains("auth")) {
                qop = 2;
            }
        } else {
            qop = 0;
        }
        if (qop == -1) {
            throw new AuthenticationException("None of the qop methods is supported: " + qoplist);
        }
        String charset = getParameter("charset");
        if (charset == null) {
            charset = CharEncoding.ISO_8859_1;
        }
        String digAlg = algorithm;
        if (digAlg.equalsIgnoreCase("MD5-sess")) {
            digAlg = "MD5";
        }
        try {
            MessageDigest digester = createMessageDigest(digAlg);
            String uname = credentials.getUserPrincipal().getName();
            String pwd = credentials.getPassword();
            if (nonce.equals(this.lastNonce)) {
                this.nounceCount++;
            } else {
                this.nounceCount = 1;
                this.cnonce = null;
                this.lastNonce = nonce;
            }
            StringBuilder sb = new StringBuilder(256);
            Formatter formatter = new Formatter(sb, Locale.US);
            formatter.format("%08x", new Object[]{Long.valueOf(this.nounceCount)});
            formatter.close();
            String nc = sb.toString();
            if (this.cnonce == null) {
                this.cnonce = createCnonce();
            }
            this.f170a1 = null;
            this.f171a2 = null;
            if (algorithm.equalsIgnoreCase("MD5-sess")) {
                sb.setLength(0);
                sb.append(uname).append(':').append(realm).append(':').append(pwd);
                String checksum = encode(digester.digest(EncodingUtils.getBytes(sb.toString(), charset)));
                sb.setLength(0);
                sb.append(checksum).append(':').append(nonce).append(':').append(this.cnonce);
                this.f170a1 = sb.toString();
            } else {
                sb.setLength(0);
                sb.append(uname).append(':').append(realm).append(':').append(pwd);
                this.f170a1 = sb.toString();
            }
            String hasha1 = encode(digester.digest(EncodingUtils.getBytes(this.f170a1, charset)));
            if (qop == 2) {
                this.f171a2 = String.valueOf(method) + ':' + uri;
            } else if (qop == 1) {
                HttpEntity entity = null;
                if (request instanceof HttpEntityEnclosingRequest) {
                    entity = ((HttpEntityEnclosingRequest) request).getEntity();
                }
                if (entity == null || entity.isRepeatable()) {
                    HttpEntityDigester entityDigester = new HttpEntityDigester(digester);
                    if (entity != null) {
                        try {
                            entity.writeTo(entityDigester);
                        } catch (IOException ex) {
                            throw new AuthenticationException("I/O error reading entity content", ex);
                        }
                    }
                    entityDigester.close();
                    this.f171a2 = String.valueOf(method) + ':' + uri + ':' + encode(entityDigester.getDigest());
                } else if (hashSet.contains("auth")) {
                    qop = 2;
                    this.f171a2 = String.valueOf(method) + ':' + uri;
                } else {
                    throw new AuthenticationException("Qop auth-int cannot be used with a non-repeatable entity");
                }
            } else {
                this.f171a2 = String.valueOf(method) + ':' + uri;
            }
            String hasha2 = encode(digester.digest(EncodingUtils.getBytes(this.f171a2, charset)));
            if (qop == 0) {
                sb.setLength(0);
                sb.append(hasha1).append(':').append(nonce).append(':').append(hasha2);
                digestValue = sb.toString();
            } else {
                sb.setLength(0);
                sb.append(hasha1).append(':').append(nonce).append(':').append(nc).append(':').append(this.cnonce).append(':').append(qop == 1 ? "auth-int" : "auth").append(':').append(hasha2);
                digestValue = sb.toString();
            }
            String digest = encode(digester.digest(EncodingUtils.getAsciiBytes(digestValue)));
            CharArrayBuffer buffer = new CharArrayBuffer(128);
            if (isProxy()) {
                buffer.append(HttpHeaders.PROXY_AUTHORIZATION);
            } else {
                buffer.append("Authorization");
            }
            buffer.append(": Digest ");
            ArrayList arrayList = new ArrayList(20);
            arrayList.add(new BasicNameValuePair("username", uname));
            arrayList.add(new BasicNameValuePair("realm", realm));
            arrayList.add(new BasicNameValuePair("nonce", nonce));
            arrayList.add(new BasicNameValuePair("uri", uri));
            arrayList.add(new BasicNameValuePair("response", digest));
            if (qop != 0) {
                arrayList.add(new BasicNameValuePair("qop", qop == 1 ? "auth-int" : "auth"));
                arrayList.add(new BasicNameValuePair("nc", nc));
                arrayList.add(new BasicNameValuePair("cnonce", this.cnonce));
            }
            arrayList.add(new BasicNameValuePair("algorithm", algorithm));
            if (opaque != null) {
                arrayList.add(new BasicNameValuePair("opaque", opaque));
            }
            for (int i = 0; i < arrayList.size(); i++) {
                BasicNameValuePair param = (BasicNameValuePair) arrayList.get(i);
                if (i > 0) {
                    buffer.append(", ");
                }
                String name = param.getName();
                boolean noQuotes = "nc".equals(name) || "qop".equals(name) || "algorithm".equals(name);
                BasicHeaderValueFormatterHC4 basicHeaderValueFormatterHC4 = BasicHeaderValueFormatterHC4.INSTANCE;
                if (noQuotes) {
                    z = false;
                } else {
                    z = true;
                }
                basicHeaderValueFormatterHC4.formatNameValuePair(buffer, (NameValuePair) param, z);
            }
            return new BufferedHeader(buffer);
        } catch (UnsupportedDigestAlgorithmException e) {
            throw new AuthenticationException("Unsuppported digest algorithm: " + digAlg);
        }
    }

    /* access modifiers changed from: package-private */
    public String getCnonce() {
        return this.cnonce;
    }

    /* access modifiers changed from: package-private */
    public String getA1() {
        return this.f170a1;
    }

    /* access modifiers changed from: package-private */
    public String getA2() {
        return this.f171a2;
    }

    static String encode(byte[] binaryData) {
        int n = binaryData.length;
        char[] buffer = new char[(n * 2)];
        for (int i = 0; i < n; i++) {
            int low = binaryData[i] & 15;
            buffer[i * 2] = HEXADECIMAL[(binaryData[i] & 240) >> 4];
            buffer[(i * 2) + 1] = HEXADECIMAL[low];
        }
        return new String(buffer);
    }

    public static String createCnonce() {
        byte[] tmp = new byte[8];
        new SecureRandom().nextBytes(tmp);
        return encode(tmp);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DIGEST [complete=").append(this.complete).append(", nonce=").append(this.lastNonce).append(", nc=").append(this.nounceCount).append("]");
        return builder.toString();
    }
}
