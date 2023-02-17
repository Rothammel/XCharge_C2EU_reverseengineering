package org.java_websocket;

import com.xcharge.charger.ui.c2.activity.advert.AutoScrollViewPager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.framing.CloseFrame;

/* loaded from: classes.dex */
public abstract class AbstractWebSocket extends WebSocketAdapter {
    private int connectionLostTimeout = 60;
    private Timer connectionLostTimer;
    private TimerTask connectionLostTimerTask;
    private boolean reuseAddr;
    private boolean tcpNoDelay;

    protected abstract Collection<WebSocket> getConnections();

    public int getConnectionLostTimeout() {
        return this.connectionLostTimeout;
    }

    public void setConnectionLostTimeout(int connectionLostTimeout) {
        this.connectionLostTimeout = connectionLostTimeout;
        if (this.connectionLostTimeout <= 0) {
            stopConnectionLostTimer();
        }
        if (this.connectionLostTimer != null || this.connectionLostTimerTask != null) {
            if (WebSocketImpl.DEBUG) {
                System.out.println("Connection lost timer restarted");
            }
            restartConnectionLostTimer();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void stopConnectionLostTimer() {
        if (this.connectionLostTimer != null || this.connectionLostTimerTask != null) {
            if (WebSocketImpl.DEBUG) {
                System.out.println("Connection lost timer stopped");
            }
            cancelConnectionLostTimer();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startConnectionLostTimer() {
        if (this.connectionLostTimeout <= 0) {
            if (WebSocketImpl.DEBUG) {
                System.out.println("Connection lost timer deactivated");
                return;
            }
            return;
        }
        if (WebSocketImpl.DEBUG) {
            System.out.println("Connection lost timer started");
        }
        restartConnectionLostTimer();
    }

    private void restartConnectionLostTimer() {
        cancelConnectionLostTimer();
        this.connectionLostTimer = new Timer("WebSocketTimer");
        this.connectionLostTimerTask = new TimerTask() { // from class: org.java_websocket.AbstractWebSocket.1
            private ArrayList<WebSocket> connections = new ArrayList<>();

            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                this.connections.clear();
                this.connections.addAll(AbstractWebSocket.this.getConnections());
                long current = System.currentTimeMillis() - (AbstractWebSocket.this.connectionLostTimeout * AutoScrollViewPager.DEFAULT_INTERVAL);
                Iterator<WebSocket> it2 = this.connections.iterator();
                while (it2.hasNext()) {
                    WebSocket conn = it2.next();
                    if (conn instanceof WebSocketImpl) {
                        WebSocketImpl webSocketImpl = (WebSocketImpl) conn;
                        if (webSocketImpl.getLastPong() < current) {
                            if (WebSocketImpl.DEBUG) {
                                System.out.println("Closing connection due to no pong received: " + conn.toString());
                            }
                            webSocketImpl.closeConnection(CloseFrame.ABNORMAL_CLOSE, "The connection was closed because the other endpoint did not respond with a pong in time. For more information check: https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection");
                        } else if (webSocketImpl.isOpen()) {
                            webSocketImpl.sendPing();
                        } else if (WebSocketImpl.DEBUG) {
                            System.out.println("Trying to ping a non open connection: " + conn.toString());
                        }
                    }
                }
                this.connections.clear();
            }
        };
        this.connectionLostTimer.scheduleAtFixedRate(this.connectionLostTimerTask, this.connectionLostTimeout * 1000, this.connectionLostTimeout * 1000);
    }

    private void cancelConnectionLostTimer() {
        if (this.connectionLostTimer != null) {
            this.connectionLostTimer.cancel();
            this.connectionLostTimer = null;
        }
        if (this.connectionLostTimerTask != null) {
            this.connectionLostTimerTask.cancel();
            this.connectionLostTimerTask = null;
        }
    }

    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isReuseAddr() {
        return this.reuseAddr;
    }

    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }
}