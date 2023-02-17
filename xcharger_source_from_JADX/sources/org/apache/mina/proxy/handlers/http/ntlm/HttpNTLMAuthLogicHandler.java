package org.apache.mina.proxy.handlers.http.ntlm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class HttpNTLMAuthLogicHandler extends AbstractAuthLogicHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) HttpNTLMAuthLogicHandler.class);
    private byte[] challengePacket = null;

    public HttpNTLMAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        super(proxyIoSession);
        ((HttpProxyRequest) this.request).checkRequiredProperties(HttpProxyConstants.USER_PROPERTY, HttpProxyConstants.PWD_PROPERTY, HttpProxyConstants.DOMAIN_PROPERTY, HttpProxyConstants.WORKSTATION_PROPERTY);
    }

    public void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException {
        Map<String, List<String>> headers;
        LOGGER.debug(" doHandshake()");
        if (this.step <= 0 || this.challengePacket != null) {
            HttpProxyRequest req = (HttpProxyRequest) this.request;
            if (req.getHeaders() != null) {
                headers = req.getHeaders();
            } else {
                headers = new HashMap<>();
            }
            String domain = req.getProperties().get(HttpProxyConstants.DOMAIN_PROPERTY);
            String workstation = req.getProperties().get(HttpProxyConstants.WORKSTATION_PROPERTY);
            if (this.step > 0) {
                LOGGER.debug("  sending NTLM challenge response");
                StringUtilities.addValueToHeader(headers, HttpHeaders.PROXY_AUTHORIZATION, "NTLM " + new String(Base64.encodeBase64(NTLMUtilities.createType3Message(req.getProperties().get(HttpProxyConstants.USER_PROPERTY), req.getProperties().get(HttpProxyConstants.PWD_PROPERTY), NTLMUtilities.extractChallengeFromType2Message(this.challengePacket), domain, workstation, Integer.valueOf(NTLMUtilities.extractFlagsFromType2Message(this.challengePacket)), (byte[]) null))), true);
            } else {
                LOGGER.debug("  sending NTLM negotiation packet");
                StringUtilities.addValueToHeader(headers, HttpHeaders.PROXY_AUTHORIZATION, "NTLM " + new String(Base64.encodeBase64(NTLMUtilities.createType1Message(workstation, domain, (Integer) null, (byte[]) null))), true);
            }
            addKeepAliveHeaders(headers);
            req.setHeaders(headers);
            writeRequest(nextFilter, req);
            this.step++;
            return;
        }
        throw new IllegalStateException("NTLM Challenge packet not received");
    }

    private String getNTLMHeader(HttpProxyResponse response) {
        for (String s : response.getHeaders().get(HttpHeaders.PROXY_AUTHENTICATE)) {
            if (s.startsWith(AuthSchemes.NTLM)) {
                return s;
            }
        }
        return null;
    }

    /* JADX WARNING: type inference failed for: r2v2, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r2v3, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r2v6, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
        if (this.step == 0) {
            String challengeResponse = getNTLMHeader(response);
            this.step = 1;
            if (challengeResponse == null || challengeResponse.length() < 5) {
                return;
            }
        }
        if (this.step == 1) {
            String challengeResponse2 = getNTLMHeader(response);
            if (challengeResponse2 == null || challengeResponse2.length() < 5) {
                throw new ProxyAuthException("Unexpected error while reading server challenge !");
            }
            try {
                this.challengePacket = Base64.decodeBase64(challengeResponse2.substring(5).getBytes(this.proxyIoSession.getCharsetName()));
                this.step = 2;
            } catch (IOException e) {
                throw new ProxyAuthException("Unable to decode the base64 encoded NTLM challenge", e);
            }
        } else {
            throw new ProxyAuthException("Received unexpected response code (" + response.getStatusLine() + ").");
        }
    }
}
