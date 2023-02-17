package org.apache.mina.filter.keepalive;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public interface KeepAliveRequestTimeoutHandler {
    public static final KeepAliveRequestTimeoutHandler NOOP = new KeepAliveRequestTimeoutHandler() { // from class: org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler.1
        @Override // org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler
        public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
        }
    };
    public static final KeepAliveRequestTimeoutHandler LOG = new KeepAliveRequestTimeoutHandler() { // from class: org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler.2
        private final Logger LOGGER = LoggerFactory.getLogger(KeepAliveFilter.class);

        @Override // org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler
        public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
            this.LOGGER.warn("A keep-alive response message was not received within {} second(s).", Integer.valueOf(filter.getRequestTimeout()));
        }
    };
    public static final KeepAliveRequestTimeoutHandler EXCEPTION = new KeepAliveRequestTimeoutHandler() { // from class: org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler.3
        @Override // org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler
        public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
            throw new KeepAliveRequestTimeoutException("A keep-alive response message was not received within " + filter.getRequestTimeout() + " second(s).");
        }
    };
    public static final KeepAliveRequestTimeoutHandler CLOSE = new KeepAliveRequestTimeoutHandler() { // from class: org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler.4
        private final Logger LOGGER = LoggerFactory.getLogger(KeepAliveFilter.class);

        @Override // org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler
        public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
            this.LOGGER.warn("Closing the session because a keep-alive response message was not received within {} second(s).", Integer.valueOf(filter.getRequestTimeout()));
            session.closeNow();
        }
    };
    public static final KeepAliveRequestTimeoutHandler DEAF_SPEAKER = new KeepAliveRequestTimeoutHandler() { // from class: org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler.5
        @Override // org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler
        public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
            throw new Error("Shouldn't be invoked.  Please file a bug report.");
        }
    };

    void keepAliveRequestTimedOut(KeepAliveFilter keepAliveFilter, IoSession ioSession) throws Exception;
}
