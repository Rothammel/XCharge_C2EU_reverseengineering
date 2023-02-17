package org.apache.mina.filter.firewall;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class ConnectionThrottleFilter extends IoFilterAdapter {
    private static final long DEFAULT_TIME = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionThrottleFilter.class);
    private long allowedInterval;
    private final Map<String, Long> clients;
    private Lock lock;

    /* loaded from: classes.dex */
    private class ExpiredSessionThread extends Thread {
        private ExpiredSessionThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Thread.sleep(ConnectionThrottleFilter.this.allowedInterval);
                long currentTime = System.currentTimeMillis();
                ConnectionThrottleFilter.this.lock.lock();
                try {
                    for (String session : ConnectionThrottleFilter.this.clients.keySet()) {
                        long creationTime = ((Long) ConnectionThrottleFilter.this.clients.get(session)).longValue();
                        if (ConnectionThrottleFilter.this.allowedInterval + creationTime < currentTime) {
                            ConnectionThrottleFilter.this.clients.remove(session);
                        }
                    }
                } finally {
                    ConnectionThrottleFilter.this.lock.unlock();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public ConnectionThrottleFilter() {
        this(1000L);
    }

    public ConnectionThrottleFilter(long allowedInterval) {
        this.lock = new ReentrantLock();
        this.allowedInterval = allowedInterval;
        this.clients = new ConcurrentHashMap();
        ExpiredSessionThread cleanupThread = new ExpiredSessionThread();
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public void setAllowedInterval(long allowedInterval) {
        this.lock.lock();
        try {
            this.allowedInterval = allowedInterval;
        } finally {
            this.lock.unlock();
        }
    }

    protected boolean isConnectionOk(IoSession session) {
        boolean z = false;
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) remoteAddress;
            long now = System.currentTimeMillis();
            this.lock.lock();
            try {
                if (this.clients.containsKey(addr.getAddress().getHostAddress())) {
                    LOGGER.debug("This is not a new client");
                    Long lastConnTime = this.clients.get(addr.getAddress().getHostAddress());
                    this.clients.put(addr.getAddress().getHostAddress(), Long.valueOf(now));
                    if (now - lastConnTime.longValue() < this.allowedInterval) {
                        LOGGER.warn("Session connection interval too short");
                    } else {
                        this.lock.unlock();
                        z = true;
                    }
                } else {
                    this.clients.put(addr.getAddress().getHostAddress(), Long.valueOf(now));
                    this.lock.unlock();
                    z = true;
                }
            } finally {
                this.lock.unlock();
            }
        }
        return z;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (!isConnectionOk(session)) {
            LOGGER.warn("Connections coming in too fast; closing.");
            session.closeNow();
        }
        nextFilter.sessionCreated(session);
    }
}
