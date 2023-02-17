package org.apache.mina.filter.ssl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslFilter extends IoFilterAdapter {
    public static final AttributeKey DISABLE_ENCRYPTION_ONCE = new AttributeKey(SslFilter.class, "disableOnce");
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) SslFilter.class);
    private static final AttributeKey NEXT_FILTER = new AttributeKey(SslFilter.class, "nextFilter");
    public static final AttributeKey PEER_ADDRESS = new AttributeKey(SslFilter.class, "peerAddress");
    public static final SslFilterMessage SESSION_SECURED = new SslFilterMessage("SESSION_SECURED");
    public static final SslFilterMessage SESSION_UNSECURED = new SslFilterMessage("SESSION_UNSECURED");
    private static final AttributeKey SSL_HANDLER = new AttributeKey(SslFilter.class, "handler");
    public static final AttributeKey SSL_SESSION = new AttributeKey(SslFilter.class, "session");
    private static final boolean START_HANDSHAKE = true;
    public static final AttributeKey USE_NOTIFICATION = new AttributeKey(SslFilter.class, "useNotification");
    private final boolean autoStart;
    private boolean client;
    private String[] enabledCipherSuites;
    private String[] enabledProtocols;
    private boolean needClientAuth;
    final SSLContext sslContext;
    private boolean wantClientAuth;

    public SslFilter(SSLContext sslContext2) {
        this(sslContext2, true);
    }

    public SslFilter(SSLContext sslContext2, boolean autoStart2) {
        if (sslContext2 == null) {
            throw new IllegalArgumentException("sslContext");
        }
        this.sslContext = sslContext2;
        this.autoStart = autoStart2;
    }

    public SSLSession getSslSession(IoSession session) {
        return (SSLSession) session.getAttribute(SSL_SESSION);
    }

    public boolean startSsl(IoSession session) throws SSLException {
        boolean started;
        SslHandler sslHandler = getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                if (sslHandler.isOutboundDone()) {
                    sslHandler.destroy();
                    sslHandler.init();
                    sslHandler.handshake((IoFilter.NextFilter) session.getAttribute(NEXT_FILTER));
                    started = true;
                } else {
                    started = false;
                }
            }
            sslHandler.flushScheduledEvents();
            return started;
        } catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }

    /* access modifiers changed from: package-private */
    public String getSessionInfo(IoSession session) {
        StringBuilder sb = new StringBuilder();
        if (session.getService() instanceof IoAcceptor) {
            sb.append("Session Server");
        } else {
            sb.append("Session Client");
        }
        sb.append('[').append(session.getId()).append(']');
        SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
        if (sslHandler == null) {
            sb.append("(no sslEngine)");
        } else if (isSslStarted(session)) {
            if (sslHandler.isHandshakeComplete()) {
                sb.append("(SSL)");
            } else {
                sb.append("(ssl...)");
            }
        }
        return sb.toString();
    }

    public boolean isSslStarted(IoSession session) {
        boolean z = false;
        SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
        if (sslHandler != null) {
            synchronized (sslHandler) {
                if (!sslHandler.isOutboundDone()) {
                    z = true;
                }
            }
        }
        return z;
    }

    public WriteFuture stopSsl(IoSession session) throws SSLException {
        WriteFuture future;
        SslHandler sslHandler = getSslSessionHandler(session);
        IoFilter.NextFilter nextFilter = (IoFilter.NextFilter) session.getAttribute(NEXT_FILTER);
        try {
            synchronized (sslHandler) {
                future = initiateClosure(nextFilter, session);
            }
            sslHandler.flushScheduledEvents();
            return future;
        } catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }

    public boolean isUseClientMode() {
        return this.client;
    }

    public void setUseClientMode(boolean clientMode) {
        this.client = clientMode;
    }

    public boolean isNeedClientAuth() {
        return this.needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth2) {
        this.needClientAuth = needClientAuth2;
    }

    public boolean isWantClientAuth() {
        return this.wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth2) {
        this.wantClientAuth = wantClientAuth2;
    }

    public String[] getEnabledCipherSuites() {
        return this.enabledCipherSuites;
    }

    public void setEnabledCipherSuites(String[] cipherSuites) {
        this.enabledCipherSuites = cipherSuites;
    }

    public String[] getEnabledProtocols() {
        return this.enabledProtocols;
    }

    public void setEnabledProtocols(String[] protocols) {
        this.enabledProtocols = protocols;
    }

    public void onPreAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws SSLException {
        if (parent.contains((Class<? extends IoFilter>) SslFilter.class)) {
            LOGGER.error("Only one SSL filter is permitted in a chain.");
            throw new IllegalStateException("Only one SSL filter is permitted in a chain.");
        }
        LOGGER.debug("Adding the SSL Filter {} to the chain", (Object) name);
        IoSession session = parent.getSession();
        session.setAttribute(NEXT_FILTER, nextFilter);
        SslHandler sslHandler = new SslHandler(this, session);
        if (this.enabledCipherSuites == null || this.enabledCipherSuites.length == 0) {
            this.enabledCipherSuites = this.sslContext.getServerSocketFactory().getSupportedCipherSuites();
        }
        sslHandler.init();
        session.setAttribute(SSL_HANDLER, sslHandler);
    }

    public void onPostAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws SSLException {
        if (this.autoStart) {
            initiateHandshake(nextFilter, parent.getSession());
        }
    }

    public void onPreRemove(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws SSLException {
        IoSession session = parent.getSession();
        stopSsl(session);
        session.removeAttribute(NEXT_FILTER);
        session.removeAttribute(SSL_HANDLER);
    }

    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws SSLException {
        SslHandler sslHandler = getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                sslHandler.destroy();
            }
        } finally {
            nextFilter.sessionClosed(session);
        }
    }

    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws SSLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: Message received : {}", (Object) getSessionInfo(session), message);
        }
        SslHandler sslHandler = getSslSessionHandler(session);
        synchronized (sslHandler) {
            if (isSslStarted(session) || !sslHandler.isInboundDone()) {
                IoBuffer buf = (IoBuffer) message;
                try {
                    sslHandler.messageReceived(nextFilter, buf.buf());
                    handleSslData(nextFilter, sslHandler);
                    if (sslHandler.isInboundDone()) {
                        if (sslHandler.isOutboundDone()) {
                            sslHandler.destroy();
                        } else {
                            initiateClosure(nextFilter, session);
                        }
                        if (buf.hasRemaining()) {
                            sslHandler.scheduleMessageReceived(nextFilter, buf);
                        }
                    }
                } catch (SSLException e) {
                    ssle = e;
                    if (!sslHandler.isHandshakeComplete()) {
                        SSLException newSsle = new SSLHandshakeException("SSL handshake failed.");
                        newSsle.initCause(ssle);
                        ssle = newSsle;
                        session.closeNow();
                    } else {
                        sslHandler.release();
                    }
                    throw ssle;
                }
            } else {
                sslHandler.scheduleMessageReceived(nextFilter, message);
            }
        }
        sslHandler.flushScheduledEvents();
    }

    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) {
        if (writeRequest instanceof EncryptedWriteRequest) {
            nextFilter.messageSent(session, ((EncryptedWriteRequest) writeRequest).getParentRequest());
        }
    }

    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        if (cause instanceof WriteToClosedSessionException) {
            List<WriteRequest> failedRequests = ((WriteToClosedSessionException) cause).getRequests();
            boolean containsCloseNotify = false;
            Iterator<WriteRequest> it = failedRequests.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (isCloseNotify(it.next().getMessage())) {
                        containsCloseNotify = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (containsCloseNotify) {
                if (failedRequests.size() != 1) {
                    List<WriteRequest> newFailedRequests = new ArrayList<>(failedRequests.size() - 1);
                    for (WriteRequest r : failedRequests) {
                        if (!isCloseNotify(r.getMessage())) {
                            newFailedRequests.add(r);
                        }
                    }
                    if (!newFailedRequests.isEmpty()) {
                        cause = new WriteToClosedSessionException((Collection<WriteRequest>) newFailedRequests, cause.getMessage(), cause.getCause());
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        nextFilter.exceptionCaught(session, cause);
    }

    private boolean isCloseNotify(Object message) {
        boolean z = true;
        if (!(message instanceof IoBuffer)) {
            return false;
        }
        IoBuffer buf = (IoBuffer) message;
        int offset = buf.position();
        if (!(buf.get(offset + 0) == 21 && buf.get(offset + 1) == 3 && ((buf.get(offset + 2) == 0 || buf.get(offset + 2) == 1 || buf.get(offset + 2) == 2 || buf.get(offset + 2) == 3) && buf.get(offset + 3) == 0))) {
            z = false;
        }
        return z;
    }

    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws SSLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: Writing Message : {}", (Object) getSessionInfo(session), (Object) writeRequest);
        }
        boolean needsFlush = true;
        SslHandler sslHandler = getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                if (!isSslStarted(session)) {
                    sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
                } else if (session.containsAttribute(DISABLE_ENCRYPTION_ONCE)) {
                    session.removeAttribute(DISABLE_ENCRYPTION_ONCE);
                    sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
                } else {
                    IoBuffer buf = (IoBuffer) writeRequest.getMessage();
                    if (sslHandler.isWritingEncryptedData()) {
                        sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
                    } else if (sslHandler.isHandshakeComplete()) {
                        buf.mark();
                        sslHandler.encrypt(buf.buf());
                        sslHandler.scheduleFilterWrite(nextFilter, new EncryptedWriteRequest(writeRequest, sslHandler.fetchOutNetBuffer()));
                    } else {
                        if (session.isConnected()) {
                            sslHandler.schedulePreHandshakeWriteRequest(nextFilter, writeRequest);
                        }
                        needsFlush = false;
                    }
                }
            }
            if (needsFlush) {
                sslHandler.flushScheduledEvents();
            }
        } catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }

    public void filterClose(final IoFilter.NextFilter nextFilter, final IoSession session) throws SSLException {
        SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
        if (sslHandler == null) {
            nextFilter.filterClose(session);
            return;
        }
        WriteFuture future = null;
        try {
            synchronized (sslHandler) {
                if (isSslStarted(session)) {
                    future = initiateClosure(nextFilter, session);
                    future.addListener(new IoFutureListener<IoFuture>() {
                        public void operationComplete(IoFuture future) {
                            nextFilter.filterClose(session);
                        }
                    });
                }
            }
            sslHandler.flushScheduledEvents();
            if (future == null) {
                nextFilter.filterClose(session);
            }
        } catch (SSLException se) {
            try {
                sslHandler.release();
                throw se;
            } catch (Throwable th) {
                if (future == null) {
                    nextFilter.filterClose(session);
                }
                throw th;
            }
        }
    }

    public void initiateHandshake(IoSession session) throws SSLException {
        IoFilterChain filterChain = session.getFilterChain();
        if (filterChain == null) {
            throw new SSLException("No filter chain");
        }
        IoFilter.NextFilter nextFilter = filterChain.getNextFilter((Class<? extends IoFilter>) SslFilter.class);
        if (nextFilter == null) {
            throw new SSLException("No SSL next filter in the chain");
        }
        initiateHandshake(nextFilter, session);
    }

    private void initiateHandshake(IoFilter.NextFilter nextFilter, IoSession session) throws SSLException {
        LOGGER.debug("{} : Starting the first handshake", (Object) getSessionInfo(session));
        SslHandler sslHandler = getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                sslHandler.handshake(nextFilter);
            }
            sslHandler.flushScheduledEvents();
        } catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }

    private WriteFuture initiateClosure(IoFilter.NextFilter nextFilter, IoSession session) throws SSLException {
        SslHandler sslHandler = getSslSessionHandler(session);
        try {
            if (!sslHandler.closeOutbound()) {
                return DefaultWriteFuture.newNotWrittenFuture(session, new IllegalStateException("SSL session is shut down already."));
            }
            WriteFuture future = sslHandler.writeNetBuffer(nextFilter);
            if (future == null) {
                future = DefaultWriteFuture.newWrittenFuture(session);
            }
            if (sslHandler.isInboundDone()) {
                sslHandler.destroy();
            }
            if (session.containsAttribute(USE_NOTIFICATION)) {
                sslHandler.scheduleMessageReceived(nextFilter, SESSION_UNSECURED);
            }
            return future;
        } catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }

    private void handleSslData(IoFilter.NextFilter nextFilter, SslHandler sslHandler) throws SSLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: Processing the SSL Data ", (Object) getSessionInfo(sslHandler.getSession()));
        }
        if (sslHandler.isHandshakeComplete()) {
            sslHandler.flushPreHandshakeEvents();
        }
        sslHandler.writeNetBuffer(nextFilter);
        handleAppDataRead(nextFilter, sslHandler);
    }

    private void handleAppDataRead(IoFilter.NextFilter nextFilter, SslHandler sslHandler) {
        IoBuffer readBuffer = sslHandler.fetchAppBuffer();
        if (readBuffer.hasRemaining()) {
            sslHandler.scheduleMessageReceived(nextFilter, readBuffer);
        }
    }

    private SslHandler getSslSessionHandler(IoSession session) {
        SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
        if (sslHandler == null) {
            throw new IllegalStateException();
        } else if (sslHandler.getSslFilter() == this) {
            return sslHandler;
        } else {
            throw new IllegalArgumentException("Not managed by this filter.");
        }
    }

    public static class SslFilterMessage {
        private final String name;

        private SslFilterMessage(String name2) {
            this.name = name2;
        }

        public String toString() {
            return this.name;
        }
    }

    private static class EncryptedWriteRequest extends WriteRequestWrapper {
        private final IoBuffer encryptedMessage;

        private EncryptedWriteRequest(WriteRequest writeRequest, IoBuffer encryptedMessage2) {
            super(writeRequest);
            this.encryptedMessage = encryptedMessage2;
        }

        public Object getMessage() {
            return this.encryptedMessage;
        }
    }
}
