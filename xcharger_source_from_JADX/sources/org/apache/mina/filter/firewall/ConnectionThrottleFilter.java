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

public class ConnectionThrottleFilter extends IoFilterAdapter {
    private static final long DEFAULT_TIME = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) ConnectionThrottleFilter.class);
    /* access modifiers changed from: private */
    public long allowedInterval;
    /* access modifiers changed from: private */
    public final Map<String, Long> clients;
    /* access modifiers changed from: private */
    public Lock lock;

    private class ExpiredSessionThread extends Thread {
        private ExpiredSessionThread() {
        }

        public void run() {
            try {
                Thread.sleep(ConnectionThrottleFilter.this.allowedInterval);
                long currentTime = System.currentTimeMillis();
                ConnectionThrottleFilter.this.lock.lock();
                try {
                    for (String session : ConnectionThrottleFilter.this.clients.keySet()) {
                        if (ConnectionThrottleFilter.this.allowedInterval + ((Long) ConnectionThrottleFilter.this.clients.get(session)).longValue() < currentTime) {
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
        this(1000);
    }

    public ConnectionThrottleFilter(long allowedInterval2) {
        this.lock = new ReentrantLock();
        this.allowedInterval = allowedInterval2;
        this.clients = new ConcurrentHashMap();
        ExpiredSessionThread cleanupThread = new ExpiredSessionThread();
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public void setAllowedInterval(long allowedInterval2) {
        this.lock.lock();
        try {
            this.allowedInterval = allowedInterval2;
        } finally {
            this.lock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isConnectionOk(IoSession session) {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (!(remoteAddress instanceof InetSocketAddress)) {
            return false;
        }
        InetSocketAddress addr = (InetSocketAddress) remoteAddress;
        long now = System.currentTimeMillis();
        this.lock.lock();
        try {
            if (this.clients.containsKey(addr.getAddress().getHostAddress())) {
                LOGGER.debug("This is not a new client");
                this.clients.put(addr.getAddress().getHostAddress(), Long.valueOf(now));
                if (now - this.clients.get(addr.getAddress().getHostAddress()).longValue() < this.allowedInterval) {
                    LOGGER.warn("Session connection interval too short");
                    return false;
                }
                this.lock.unlock();
                return true;
            }
            this.clients.put(addr.getAddress().getHostAddress(), Long.valueOf(now));
            this.lock.unlock();
            return true;
        } finally {
            this.lock.unlock();
        }
    }

    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (!isConnectionOk(session)) {
            LOGGER.warn("Connections coming in too fast; closing.");
            session.closeNow();
        }
        nextFilter.sessionCreated(session);
    }
}
