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

/* loaded from: classes.dex */
public class HttpNTLMAuthLogicHandler extends AbstractAuthLogicHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpNTLMAuthLogicHandler.class);
    private byte[] challengePacket;

    public HttpNTLMAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        super(proxyIoSession);
        this.challengePacket = null;
        ((HttpProxyRequest) this.request).checkRequiredProperties(HttpProxyConstants.USER_PROPERTY, HttpProxyConstants.PWD_PROPERTY, HttpProxyConstants.DOMAIN_PROPERTY, HttpProxyConstants.WORKSTATION_PROPERTY);
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler
    public void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException {
        LOGGER.debug(" doHandshake()");
        if (this.step > 0 && this.challengePacket == null) {
            throw new IllegalStateException("NTLM Challenge packet not received");
        }
        HttpProxyRequest req = (HttpProxyRequest) this.request;
        Map<String, List<String>> headers = req.getHeaders() != null ? req.getHeaders() : new HashMap<>();
        String domain = req.getProperties().get(HttpProxyConstants.DOMAIN_PROPERTY);
        String workstation = req.getProperties().get(HttpProxyConstants.WORKSTATION_PROPERTY);
        if (this.step > 0) {
            LOGGER.debug("  sending NTLM challenge response");
            byte[] challenge = NTLMUtilities.extractChallengeFromType2Message(this.challengePacket);
            int serverFlags = NTLMUtilities.extractFlagsFromType2Message(this.challengePacket);
            String username = req.getProperties().get(HttpProxyConstants.USER_PROPERTY);
            String password = req.getProperties().get(HttpProxyConstants.PWD_PROPERTY);
            byte[] authenticationPacket = NTLMUtilities.createType3Message(username, password, challenge, domain, workstation, Integer.valueOf(serverFlags), null);
            StringUtilities.addValueToHeader(headers, HttpHeaders.PROXY_AUTHORIZATION, "NTLM " + new String(Base64.encodeBase64(authenticationPacket)), true);
        } else {
            LOGGER.debug("  sending NTLM negotiation packet");
            byte[] negotiationPacket = NTLMUtilities.createType1Message(workstation, domain, null, null);
            StringUtilities.addValueToHeader(headers, HttpHeaders.PROXY_AUTHORIZATION, "NTLM " + new String(Base64.encodeBase64(negotiationPacket)), true);
        }
        addKeepAliveHeaders(headers);
        req.setHeaders(headers);
        writeRequest(nextFilter, req);
        this.step++;
    }

    private String getNTLMHeader(HttpProxyResponse response) {
        List<String> values = response.getHeaders().get(HttpHeaders.PROXY_AUTHENTICATE);
        for (String s : values) {
            if (s.startsWith(AuthSchemes.NTLM)) {
                return s;
            }
        }
        return null;
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler
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
                return;
            } catch (IOException e) {
                throw new ProxyAuthException("Unable to decode the base64 encoded NTLM challenge", e);
            }
        }
        throw new ProxyAuthException("Received unexpected response code (" + response.getStatusLine() + ").");
    }
}
