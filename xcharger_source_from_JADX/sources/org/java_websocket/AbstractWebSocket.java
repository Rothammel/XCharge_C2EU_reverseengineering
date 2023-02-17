package org.java_websocket;

import com.xcharge.charger.p006ui.p009c2.activity.advert.AutoScrollViewPager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.framing.CloseFrame;

public abstract class AbstractWebSocket extends WebSocketAdapter {
    /* access modifiers changed from: private */
    public int connectionLostTimeout = 60;
    private Timer connectionLostTimer;
    private TimerTask connectionLostTimerTask;
    private boolean reuseAddr;
    private boolean tcpNoDelay;

    /* access modifiers changed from: protected */
    public abstract Collection<WebSocket> getConnections();

    public int getConnectionLostTimeout() {
        return this.connectionLostTimeout;
    }

    public void setConnectionLostTimeout(int connectionLostTimeout2) {
        this.connectionLostTimeout = connectionLostTimeout2;
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

    /* access modifiers changed from: protected */
    public void stopConnectionLostTimer() {
        if (this.connectionLostTimer != null || this.connectionLostTimerTask != null) {
            if (WebSocketImpl.DEBUG) {
                System.out.println("Connection lost timer stopped");
            }
            cancelConnectionLostTimer();
        }
    }

    /* access modifiers changed from: protected */
    public void startConnectionLostTimer() {
        if (this.connectionLostTimeout > 0) {
            if (WebSocketImpl.DEBUG) {
                System.out.println("Connection lost timer started");
            }
            restartConnectionLostTimer();
        } else if (WebSocketImpl.DEBUG) {
            System.out.println("Connection lost timer deactivated");
        }
    }

    private void restartConnectionLostTimer() {
        cancelConnectionLostTimer();
        this.connectionLostTimer = new Timer("WebSocketTimer");
        this.connectionLostTimerTask = new TimerTask() {
            private ArrayList<WebSocket> connections = new ArrayList<>();

            public void run() {
                this.connections.clear();
                this.connections.addAll(AbstractWebSocket.this.getConnections());
                long current = System.currentTimeMillis() - ((long) (AbstractWebSocket.this.connectionLostTimeout * AutoScrollViewPager.DEFAULT_INTERVAL));
                Iterator<WebSocket> it = this.connections.iterator();
                while (it.hasNext()) {
                    WebSocket conn = it.next();
                    if (conn instanceof WebSocketImpl) {
                        WebSocketImpl webSocketImpl = (WebSocketImpl) conn;
                        if (webSocketImpl.getLastPong() < current) {
                            if (WebSocketImpl.DEBUG) {
                                System.out.println("Closing connection due to no pong received: " + conn.toString());
                            }
                            webSocketImpl.closeConnection((int) CloseFrame.ABNORMAL_CLOSE, "The connection was closed because the other endpoint did not respond with a pong in time. For more information check: https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection");
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
        this.connectionLostTimer.scheduleAtFixedRate(this.connectionLostTimerTask, (long) (this.connectionLostTimeout * 1000), (long) (this.connectionLostTimeout * 1000));
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

    public void setTcpNoDelay(boolean tcpNoDelay2) {
        this.tcpNoDelay = tcpNoDelay2;
    }

    public boolean isReuseAddr() {
        return this.reuseAddr;
    }

    public void setReuseAddr(boolean reuseAddr2) {
        this.reuseAddr = reuseAddr2;
    }
}
