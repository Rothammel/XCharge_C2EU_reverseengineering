package org.apache.mina.proxy.handlers.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.proxy.AbstractProxyLogicHandler;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.IoBufferDecoder;
import org.apache.mina.proxy.utils.StringUtilities;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpLogicHandler extends AbstractProxyLogicHandler {
    private static final byte[] CRLF_DELIMITER = {MqttWireMessage.MESSAGE_TYPE_PINGRESP, 10};
    private static final String DECODER = (AbstractHttpLogicHandler.class.getName() + ".Decoder");
    private static final byte[] HTTP_DELIMITER = {MqttWireMessage.MESSAGE_TYPE_PINGRESP, 10, MqttWireMessage.MESSAGE_TYPE_PINGRESP, 10};
    /* access modifiers changed from: private */
    public static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) AbstractHttpLogicHandler.class);
    private int contentLength = -1;
    private int entityBodyLimitPosition;
    private int entityBodyStartPosition;
    private boolean hasChunkedData;
    private HttpProxyResponse parsedResponse = null;
    private IoBuffer responseData = null;
    private boolean waitingChunkedData;
    private boolean waitingFooters;

    public abstract void handleResponse(HttpProxyResponse httpProxyResponse) throws ProxyAuthException;

    public AbstractHttpLogicHandler(ProxyIoSession proxyIoSession) {
        super(proxyIoSession);
    }

    /* JADX WARNING: type inference failed for: r10v4, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    /* JADX WARNING: type inference failed for: r4v1, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    public synchronized void messageReceived(IoFilter.NextFilter nextFilter, IoBuffer buf) throws ProxyAuthException {
        String chunkSize;
        LOGGER.debug(" messageReceived()");
        IoBufferDecoder decoder = (IoBufferDecoder) getSession().getAttribute(DECODER);
        if (decoder == null) {
            decoder = new IoBufferDecoder(HTTP_DELIMITER);
            getSession().setAttribute(DECODER, decoder);
        }
        try {
            if (this.parsedResponse == null) {
                this.responseData = decoder.decodeFully(buf);
                if (this.responseData != null) {
                    String responseHeader = this.responseData.getString(getProxyIoSession().getCharset().newDecoder());
                    this.entityBodyStartPosition = this.responseData.position();
                    LOGGER.debug("  response header received:\n{}", (Object) responseHeader.replace(StringUtils.f145CR, "\\r").replace(StringUtils.f146LF, "\\n\n"));
                    this.parsedResponse = decodeResponse(responseHeader);
                    if (this.parsedResponse.getStatusCode() == 200 || (this.parsedResponse.getStatusCode() >= 300 && this.parsedResponse.getStatusCode() <= 307)) {
                        buf.position(0);
                        setHandshakeComplete();
                    } else {
                        String contentLengthHeader = StringUtilities.getSingleValuedHeader(this.parsedResponse.getHeaders(), "Content-Length");
                        if (contentLengthHeader == null) {
                            this.contentLength = 0;
                        } else {
                            this.contentLength = Integer.parseInt(contentLengthHeader.trim());
                            decoder.setContentLength(this.contentLength, true);
                        }
                    }
                }
            }
            if (!this.hasChunkedData) {
                if (this.contentLength > 0) {
                    IoBuffer tmp = decoder.decodeFully(buf);
                    if (tmp != null) {
                        this.responseData.setAutoExpand(true);
                        this.responseData.put(tmp);
                        this.contentLength = 0;
                    }
                }
                if ("chunked".equalsIgnoreCase(StringUtilities.getSingleValuedHeader(this.parsedResponse.getHeaders(), HttpHeaders.TRANSFER_ENCODING))) {
                    LOGGER.debug("Retrieving additional http response chunks");
                    this.hasChunkedData = true;
                    this.waitingChunkedData = true;
                }
            }
            if (this.hasChunkedData) {
                while (true) {
                    if (this.waitingChunkedData) {
                        if (this.contentLength == 0) {
                            decoder.setDelimiter(CRLF_DELIMITER, false);
                            IoBuffer tmp2 = decoder.decodeFully(buf);
                            if (tmp2 == null) {
                                break;
                            }
                            String chunkSize2 = tmp2.getString(getProxyIoSession().getCharset().newDecoder());
                            int pos = chunkSize2.indexOf(59);
                            if (pos >= 0) {
                                chunkSize = chunkSize2.substring(0, pos);
                            } else {
                                chunkSize = chunkSize2.substring(0, chunkSize2.length() - 2);
                            }
                            this.contentLength = Integer.decode("0x" + chunkSize).intValue();
                            if (this.contentLength > 0) {
                                this.contentLength += 2;
                                decoder.setContentLength(this.contentLength, true);
                            }
                        }
                        if (this.contentLength != 0) {
                            IoBuffer tmp3 = decoder.decodeFully(buf);
                            if (tmp3 == null) {
                                break;
                            }
                            this.contentLength = 0;
                            this.responseData.put(tmp3);
                            buf.position(buf.position());
                        } else {
                            this.waitingChunkedData = false;
                            this.waitingFooters = true;
                            this.entityBodyLimitPosition = this.responseData.position();
                            break;
                        }
                    } else {
                        break;
                    }
                }
                while (true) {
                    if (!this.waitingFooters) {
                        break;
                    }
                    decoder.setDelimiter(CRLF_DELIMITER, false);
                    IoBuffer tmp4 = decoder.decodeFully(buf);
                    if (tmp4 == null) {
                        break;
                    } else if (tmp4.remaining() == 2) {
                        this.waitingFooters = false;
                        break;
                    } else {
                        String[] f = tmp4.getString(getProxyIoSession().getCharset().newDecoder()).split(":\\s?", 2);
                        StringUtilities.addValueToHeader(this.parsedResponse.getHeaders(), f[0], f[1], false);
                        this.responseData.put(tmp4);
                        this.responseData.put(CRLF_DELIMITER);
                    }
                }
            }
            this.responseData.flip();
            LOGGER.debug("  end of response received:\n{}", (Object) this.responseData.getString(getProxyIoSession().getCharset().newDecoder()));
            this.responseData.position(this.entityBodyStartPosition);
            this.responseData.limit(this.entityBodyLimitPosition);
            this.parsedResponse.setBody(this.responseData.getString(getProxyIoSession().getCharset().newDecoder()));
            this.responseData.free();
            this.responseData = null;
            handleResponse(this.parsedResponse);
            this.parsedResponse = null;
            this.hasChunkedData = false;
            this.contentLength = -1;
            decoder.setDelimiter(HTTP_DELIMITER, true);
            if (!isHandshakeComplete()) {
                doHandshake(nextFilter);
            }
        } catch (Exception ex) {
            if (ex instanceof ProxyAuthException) {
                throw ((ProxyAuthException) ex);
            }
            throw new ProxyAuthException("Handshake failed", ex);
        }
    }

    public void writeRequest(IoFilter.NextFilter nextFilter, HttpProxyRequest request) {
        if (getProxyIoSession().isReconnectionNeeded()) {
            reconnect(nextFilter, request);
        } else {
            writeRequest0(nextFilter, request);
        }
    }

    /* access modifiers changed from: private */
    public void writeRequest0(IoFilter.NextFilter nextFilter, HttpProxyRequest request) {
        try {
            String data = request.toHttpString();
            IoBuffer buf = IoBuffer.wrap(data.getBytes(getProxyIoSession().getCharsetName()));
            LOGGER.debug("   write:\n{}", (Object) data.replace(StringUtils.f145CR, "\\r").replace(StringUtils.f146LF, "\\n\n"));
            writeData(nextFilter, buf);
        } catch (UnsupportedEncodingException ex) {
            closeSession("Unable to send HTTP request: ", ex);
        }
    }

    private void reconnect(final IoFilter.NextFilter nextFilter, final HttpProxyRequest request) {
        LOGGER.debug("Reconnecting to proxy ...");
        final ProxyIoSession proxyIoSession = getProxyIoSession();
        proxyIoSession.getConnector().connect((IoSessionInitializer<? extends ConnectFuture>) new IoSessionInitializer<ConnectFuture>() {
            public void initializeSession(IoSession session, ConnectFuture future) {
                AbstractHttpLogicHandler.LOGGER.debug("Initializing new session: {}", (Object) session);
                session.setAttribute(ProxyIoSession.PROXY_SESSION, proxyIoSession);
                proxyIoSession.setSession(session);
                AbstractHttpLogicHandler.LOGGER.debug("  setting up proxyIoSession: {}", (Object) proxyIoSession);
                future.addListener(new IoFutureListener<ConnectFuture>() {
                    public void operationComplete(ConnectFuture future) {
                        proxyIoSession.setReconnectionNeeded(false);
                        AbstractHttpLogicHandler.this.writeRequest0(nextFilter, request);
                    }
                });
            }
        });
    }

    /* access modifiers changed from: protected */
    public HttpProxyResponse decodeResponse(String response) throws Exception {
        LOGGER.debug("  parseResponse()");
        String[] responseLines = response.split(HttpProxyConstants.CRLF);
        String[] statusLine = responseLines[0].trim().split(StringUtils.SPACE, 2);
        if (statusLine.length < 2) {
            throw new Exception("Invalid response status line (" + statusLine + "). Response: " + response);
        } else if (!statusLine[1].matches("^\\d\\d\\d")) {
            throw new Exception("Invalid response code (" + statusLine[1] + "). Response: " + response);
        } else {
            Map<String, List<String>> headers = new HashMap<>();
            for (int i = 1; i < responseLines.length; i++) {
                String[] args = responseLines[i].split(":\\s?", 2);
                StringUtilities.addValueToHeader(headers, args[0], args[1], false);
            }
            return new HttpProxyResponse(statusLine[0], statusLine[1], headers);
        }
    }
}
