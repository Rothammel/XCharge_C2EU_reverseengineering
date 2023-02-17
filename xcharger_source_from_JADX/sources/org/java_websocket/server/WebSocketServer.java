package org.java_websocket.server;

import java.io.IOException;
import java.lang.Thread;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.java_websocket.AbstractWebSocket;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketFactory;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public abstract class WebSocketServer extends AbstractWebSocket implements Runnable {
    static final /* synthetic */ boolean $assertionsDisabled = (!WebSocketServer.class.desiredAssertionStatus());
    public static int DECODERS = Runtime.getRuntime().availableProcessors();
    private final InetSocketAddress address;
    private BlockingQueue<ByteBuffer> buffers;
    private final Collection<WebSocket> connections;
    protected List<WebSocketWorker> decoders;
    private List<Draft> drafts;
    private List<WebSocketImpl> iqueue;
    private final AtomicBoolean isclosed;
    private int queueinvokes;
    private final AtomicInteger queuesize;
    private Selector selector;
    private Thread selectorthread;
    private ServerSocketChannel server;
    private WebSocketServerFactory wsf;

    public abstract void onClose(WebSocket webSocket, int i, String str, boolean z);

    public abstract void onError(WebSocket webSocket, Exception exc);

    public abstract void onMessage(WebSocket webSocket, String str);

    public abstract void onOpen(WebSocket webSocket, ClientHandshake clientHandshake);

    public abstract void onStart();

    public WebSocketServer() {
        this(new InetSocketAddress(80), DECODERS, (List<Draft>) null);
    }

    public WebSocketServer(InetSocketAddress address2) {
        this(address2, DECODERS, (List<Draft>) null);
    }

    public WebSocketServer(InetSocketAddress address2, int decodercount) {
        this(address2, decodercount, (List<Draft>) null);
    }

    public WebSocketServer(InetSocketAddress address2, List<Draft> drafts2) {
        this(address2, DECODERS, drafts2);
    }

    public WebSocketServer(InetSocketAddress address2, int decodercount, List<Draft> drafts2) {
        this(address2, decodercount, drafts2, new HashSet());
    }

    public WebSocketServer(InetSocketAddress address2, int decodercount, List<Draft> drafts2, Collection<WebSocket> connectionscontainer) {
        this.isclosed = new AtomicBoolean(false);
        this.queueinvokes = 0;
        this.queuesize = new AtomicInteger(0);
        this.wsf = new DefaultWebSocketServerFactory();
        if (address2 == null || decodercount < 1 || connectionscontainer == null) {
            throw new IllegalArgumentException("address and connectionscontainer must not be null and you need at least 1 decoder");
        }
        if (drafts2 == null) {
            this.drafts = Collections.emptyList();
        } else {
            this.drafts = drafts2;
        }
        this.address = address2;
        this.connections = connectionscontainer;
        setTcpNoDelay(false);
        setReuseAddr(false);
        this.iqueue = new LinkedList();
        this.decoders = new ArrayList(decodercount);
        this.buffers = new LinkedBlockingQueue();
        for (int i = 0; i < decodercount; i++) {
            WebSocketWorker ex = new WebSocketWorker();
            this.decoders.add(ex);
            ex.start();
        }
    }

    public void start() {
        if (this.selectorthread != null) {
            throw new IllegalStateException(getClass().getName() + " can only be started once.");
        }
        new Thread(this).start();
    }

    public void stop(int timeout) throws InterruptedException {
        List<WebSocket> socketsToClose;
        if (this.isclosed.compareAndSet(false, true)) {
            synchronized (this.connections) {
                socketsToClose = new ArrayList<>(this.connections);
            }
            for (WebSocket ws : socketsToClose) {
                ws.close(1001);
            }
            this.wsf.close();
            synchronized (this) {
                if (!(this.selectorthread == null || this.selector == null)) {
                    this.selector.wakeup();
                    this.selectorthread.join((long) timeout);
                }
            }
        }
    }

    public void stop() throws IOException, InterruptedException {
        stop(0);
    }

    @Deprecated
    public Collection<WebSocket> connections() {
        return getConnections();
    }

    public Collection<WebSocket> getConnections() {
        return Collections.unmodifiableCollection(new ArrayList(this.connections));
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        int port = getAddress().getPort();
        if (port != 0 || this.server == null) {
            return port;
        }
        return this.server.socket().getLocalPort();
    }

    public List<Draft> getDraft() {
        return Collections.unmodifiableList(this.drafts);
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public void run() {
        /*
            r22 = this;
            monitor-enter(r22)
            r0 = r22
            java.lang.Thread r0 = r0.selectorthread     // Catch:{ all -> 0x002a }
            r18 = r0
            if (r18 == 0) goto L_0x002d
            java.lang.IllegalStateException r18 = new java.lang.IllegalStateException     // Catch:{ all -> 0x002a }
            java.lang.StringBuilder r19 = new java.lang.StringBuilder     // Catch:{ all -> 0x002a }
            r19.<init>()     // Catch:{ all -> 0x002a }
            java.lang.Class r20 = r22.getClass()     // Catch:{ all -> 0x002a }
            java.lang.String r20 = r20.getName()     // Catch:{ all -> 0x002a }
            java.lang.StringBuilder r19 = r19.append(r20)     // Catch:{ all -> 0x002a }
            java.lang.String r20 = " can only be started once."
            java.lang.StringBuilder r19 = r19.append(r20)     // Catch:{ all -> 0x002a }
            java.lang.String r19 = r19.toString()     // Catch:{ all -> 0x002a }
            r18.<init>(r19)     // Catch:{ all -> 0x002a }
            throw r18     // Catch:{ all -> 0x002a }
        L_0x002a:
            r18 = move-exception
            monitor-exit(r22)     // Catch:{ all -> 0x002a }
            throw r18
        L_0x002d:
            java.lang.Thread r18 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x002a }
            r0 = r18
            r1 = r22
            r1.selectorthread = r0     // Catch:{ all -> 0x002a }
            r0 = r22
            java.util.concurrent.atomic.AtomicBoolean r0 = r0.isclosed     // Catch:{ all -> 0x002a }
            r18 = r0
            boolean r18 = r18.get()     // Catch:{ all -> 0x002a }
            if (r18 == 0) goto L_0x0045
            monitor-exit(r22)     // Catch:{ all -> 0x002a }
        L_0x0044:
            return
        L_0x0045:
            monitor-exit(r22)     // Catch:{ all -> 0x002a }
            r0 = r22
            java.lang.Thread r0 = r0.selectorthread
            r18 = r0
            java.lang.StringBuilder r19 = new java.lang.StringBuilder
            r19.<init>()
            java.lang.String r20 = "WebSocketSelector-"
            java.lang.StringBuilder r19 = r19.append(r20)
            r0 = r22
            java.lang.Thread r0 = r0.selectorthread
            r20 = r0
            long r20 = r20.getId()
            java.lang.StringBuilder r19 = r19.append(r20)
            java.lang.String r19 = r19.toString()
            r18.setName(r19)
            java.nio.channels.ServerSocketChannel r18 = java.nio.channels.ServerSocketChannel.open()     // Catch:{ IOException -> 0x014d }
            r0 = r18
            r1 = r22
            r1.server = r0     // Catch:{ IOException -> 0x014d }
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x014d }
            r18 = r0
            r19 = 0
            r18.configureBlocking(r19)     // Catch:{ IOException -> 0x014d }
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x014d }
            r18 = r0
            java.net.ServerSocket r16 = r18.socket()     // Catch:{ IOException -> 0x014d }
            int r18 = org.java_websocket.WebSocketImpl.RCVBUF     // Catch:{ IOException -> 0x014d }
            r0 = r16
            r1 = r18
            r0.setReceiveBufferSize(r1)     // Catch:{ IOException -> 0x014d }
            boolean r18 = r22.isReuseAddr()     // Catch:{ IOException -> 0x014d }
            r0 = r16
            r1 = r18
            r0.setReuseAddress(r1)     // Catch:{ IOException -> 0x014d }
            r0 = r22
            java.net.InetSocketAddress r0 = r0.address     // Catch:{ IOException -> 0x014d }
            r18 = r0
            r0 = r16
            r1 = r18
            r0.bind(r1)     // Catch:{ IOException -> 0x014d }
            java.nio.channels.Selector r18 = java.nio.channels.Selector.open()     // Catch:{ IOException -> 0x014d }
            r0 = r18
            r1 = r22
            r1.selector = r0     // Catch:{ IOException -> 0x014d }
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x014d }
            r18 = r0
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ IOException -> 0x014d }
            r19 = r0
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x014d }
            r20 = r0
            int r20 = r20.validOps()     // Catch:{ IOException -> 0x014d }
            r18.register(r19, r20)     // Catch:{ IOException -> 0x014d }
            r22.startConnectionLostTimer()     // Catch:{ IOException -> 0x014d }
            r22.onStart()     // Catch:{ IOException -> 0x014d }
            r11 = 5
            r15 = 0
        L_0x00d7:
            r0 = r22
            java.lang.Thread r0 = r0.selectorthread     // Catch:{ RuntimeException -> 0x025c }
            r18 = r0
            boolean r18 = r18.isInterrupted()     // Catch:{ RuntimeException -> 0x025c }
            if (r18 != 0) goto L_0x0429
            if (r11 == 0) goto L_0x0429
            r12 = 0
            r7 = 0
            r0 = r22
            java.util.concurrent.atomic.AtomicBoolean r0 = r0.isclosed     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            boolean r18 = r18.get()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x00f4
            r15 = 5
        L_0x00f4:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            long r0 = (long) r15     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r20 = r0
            r0 = r18
            r1 = r20
            int r13 = r0.select(r1)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r13 != 0) goto L_0x0115
            r0 = r22
            java.util.concurrent.atomic.AtomicBoolean r0 = r0.isclosed     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            boolean r18 = r18.get()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0115
            int r11 = r11 + -1
        L_0x0115:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            java.util.Set r14 = r18.selectedKeys()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            java.util.Iterator r10 = r14.iterator()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
        L_0x0123:
            boolean r18 = r10.hasNext()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x035b
            java.lang.Object r18 = r10.next()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r18
            java.nio.channels.SelectionKey r0 = (java.nio.channels.SelectionKey) r0     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r12 = r0
            r7 = 0
            boolean r18 = r12.isValid()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0123
            boolean r18 = r12.isAcceptable()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0223
            r0 = r22
            boolean r18 = r0.onConnect(r12)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 != 0) goto L_0x0159
            r12.cancel()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            goto L_0x0123
        L_0x014b:
            r18 = move-exception
            goto L_0x00d7
        L_0x014d:
            r9 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.handleFatal(r1, r9)
            goto L_0x0044
        L_0x0159:
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            java.nio.channels.SocketChannel r6 = r18.accept()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r6 == 0) goto L_0x0123
            r18 = 0
            r0 = r18
            r6.configureBlocking(r0)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            java.net.Socket r16 = r6.socket()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            boolean r18 = r22.isTcpNoDelay()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r16
            r1 = r18
            r0.setTcpNoDelay(r1)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = 1
            r0 = r16
            r1 = r18
            r0.setKeepAlive(r1)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r22
            org.java_websocket.WebSocketServerFactory r0 = r0.wsf     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r22
            java.util.List<org.java_websocket.drafts.Draft> r0 = r0.drafts     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r19 = r0
            r0 = r18
            r1 = r22
            r2 = r19
            org.java_websocket.WebSocketImpl r17 = r0.createWebSocket((org.java_websocket.WebSocketAdapter) r1, (java.util.List<org.java_websocket.drafts.Draft>) r2)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            r19 = 1
            r0 = r18
            r1 = r19
            r2 = r17
            java.nio.channels.SelectionKey r18 = r6.register(r0, r1, r2)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r18
            r1 = r17
            r1.key = r0     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r22
            org.java_websocket.WebSocketServerFactory r0 = r0.wsf     // Catch:{ IOException -> 0x01d8, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r17
            java.nio.channels.SelectionKey r0 = r0.key     // Catch:{ IOException -> 0x01d8, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r19 = r0
            r0 = r18
            r1 = r19
            java.nio.channels.ByteChannel r18 = r0.wrapChannel(r6, r1)     // Catch:{ IOException -> 0x01d8, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r0 = r18
            r1 = r17
            r1.channel = r0     // Catch:{ IOException -> 0x01d8, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r10.remove()     // Catch:{ IOException -> 0x01d8, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r0 = r22
            r1 = r17
            r0.allocateBuffers(r1)     // Catch:{ IOException -> 0x01d8, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            goto L_0x0123
        L_0x01d8:
            r9 = move-exception
            r0 = r17
            java.nio.channels.SelectionKey r0 = r0.key     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            if (r18 == 0) goto L_0x01ea
            r0 = r17
            java.nio.channels.SelectionKey r0 = r0.key     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            r18.cancel()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
        L_0x01ea:
            r0 = r17
            java.nio.channels.SelectionKey r0 = r0.key     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            r19 = 0
            r0 = r22
            r1 = r18
            r2 = r19
            r0.handleIOException(r1, r2, r9)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            goto L_0x0123
        L_0x01fd:
            r8 = move-exception
            r22.stopConnectionLostTimer()
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            if (r18 == 0) goto L_0x03b3
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            java.util.Iterator r18 = r18.iterator()
        L_0x0213:
            boolean r19 = r18.hasNext()
            if (r19 == 0) goto L_0x03b3
            java.lang.Object r17 = r18.next()
            org.java_websocket.server.WebSocketServer$WebSocketWorker r17 = (org.java_websocket.server.WebSocketServer.WebSocketWorker) r17
            r17.interrupt()
            goto L_0x0213
        L_0x0223:
            boolean r18 = r12.isReadable()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x02d1
            java.lang.Object r18 = r12.attachment()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r18
            org.java_websocket.WebSocketImpl r0 = (org.java_websocket.WebSocketImpl) r0     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r7 = r0
            java.nio.ByteBuffer r4 = r22.takeBuffer()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            java.nio.channels.ByteChannel r0 = r7.channel     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            if (r18 != 0) goto L_0x028b
            if (r12 == 0) goto L_0x0241
            r12.cancel()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
        L_0x0241:
            java.io.IOException r18 = new java.io.IOException     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18.<init>()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r22
            r1 = r18
            r0.handleIOException(r12, r7, r1)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            goto L_0x0123
        L_0x024f:
            r9 = move-exception
            if (r12 == 0) goto L_0x0255
            r12.cancel()     // Catch:{ RuntimeException -> 0x025c }
        L_0x0255:
            r0 = r22
            r0.handleIOException(r12, r7, r9)     // Catch:{ RuntimeException -> 0x025c }
            goto L_0x00d7
        L_0x025c:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.handleFatal(r1, r8)     // Catch:{ all -> 0x032e }
            r22.stopConnectionLostTimer()
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            if (r18 == 0) goto L_0x0489
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            java.util.Iterator r18 = r18.iterator()
        L_0x027b:
            boolean r19 = r18.hasNext()
            if (r19 == 0) goto L_0x0489
            java.lang.Object r17 = r18.next()
            org.java_websocket.server.WebSocketServer$WebSocketWorker r17 = (org.java_websocket.server.WebSocketServer.WebSocketWorker) r17
            r17.interrupt()
            goto L_0x027b
        L_0x028b:
            java.nio.channels.ByteChannel r0 = r7.channel     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            boolean r18 = org.java_websocket.SocketChannelIOHelper.read(r4, r7, r0)     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0354
            boolean r18 = r4.hasRemaining()     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0321
            java.util.concurrent.BlockingQueue<java.nio.ByteBuffer> r0 = r7.inQueue     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            r0.put(r4)     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r0 = r22
            r0.queue(r7)     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r10.remove()     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            java.nio.channels.ByteChannel r0 = r7.channel     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            boolean r0 = r0 instanceof org.java_websocket.WrappedByteChannel     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            if (r18 == 0) goto L_0x02d1
            java.nio.channels.ByteChannel r0 = r7.channel     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            org.java_websocket.WrappedByteChannel r18 = (org.java_websocket.WrappedByteChannel) r18     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            boolean r18 = r18.isNeedRead()     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x02d1
            r0 = r22
            java.util.List<org.java_websocket.WebSocketImpl> r0 = r0.iqueue     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            r0.add(r7)     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
        L_0x02d1:
            boolean r18 = r12.isWritable()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0123
            java.lang.Object r18 = r12.attachment()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r18
            org.java_websocket.WebSocketImpl r0 = (org.java_websocket.WebSocketImpl) r0     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r7 = r0
            java.nio.channels.ByteChannel r0 = r7.channel     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            boolean r18 = org.java_websocket.SocketChannelIOHelper.batch(r7, r0)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0123
            boolean r18 = r12.isValid()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0123
            r18 = 1
            r0 = r18
            r12.interestOps(r0)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            goto L_0x0123
        L_0x02fb:
            r8 = move-exception
            r22.stopConnectionLostTimer()
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            if (r18 == 0) goto L_0x03ee
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            java.util.Iterator r18 = r18.iterator()
        L_0x0311:
            boolean r19 = r18.hasNext()
            if (r19 == 0) goto L_0x03ee
            java.lang.Object r17 = r18.next()
            org.java_websocket.server.WebSocketServer$WebSocketWorker r17 = (org.java_websocket.server.WebSocketServer.WebSocketWorker) r17
            r17.interrupt()
            goto L_0x0311
        L_0x0321:
            r0 = r22
            r0.pushBuffer(r4)     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            goto L_0x02d1
        L_0x0327:
            r8 = move-exception
            r0 = r22
            r0.pushBuffer(r4)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            throw r8     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
        L_0x032e:
            r18 = move-exception
            r22.stopConnectionLostTimer()
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r19 = r0
            if (r19 == 0) goto L_0x04c4
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r19 = r0
            java.util.Iterator r19 = r19.iterator()
        L_0x0344:
            boolean r20 = r19.hasNext()
            if (r20 == 0) goto L_0x04c4
            java.lang.Object r17 = r19.next()
            org.java_websocket.server.WebSocketServer$WebSocketWorker r17 = (org.java_websocket.server.WebSocketServer.WebSocketWorker) r17
            r17.interrupt()
            goto L_0x0344
        L_0x0354:
            r0 = r22
            r0.pushBuffer(r4)     // Catch:{ IOException -> 0x0327, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            goto L_0x02d1
        L_0x035b:
            r0 = r22
            java.util.List<org.java_websocket.WebSocketImpl> r0 = r0.iqueue     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            boolean r18 = r18.isEmpty()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            if (r18 != 0) goto L_0x00d7
            r0 = r22
            java.util.List<org.java_websocket.WebSocketImpl> r0 = r0.iqueue     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r18 = r0
            r19 = 0
            java.lang.Object r18 = r18.remove(r19)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r0 = r18
            org.java_websocket.WebSocketImpl r0 = (org.java_websocket.WebSocketImpl) r0     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            r7 = r0
            java.nio.channels.ByteChannel r5 = r7.channel     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            org.java_websocket.WrappedByteChannel r5 = (org.java_websocket.WrappedByteChannel) r5     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            java.nio.ByteBuffer r4 = r22.takeBuffer()     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            boolean r18 = org.java_websocket.SocketChannelIOHelper.readMore(r4, r7, r5)     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x0391
            r0 = r22
            java.util.List<org.java_websocket.WebSocketImpl> r0 = r0.iqueue     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            r0.add(r7)     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
        L_0x0391:
            boolean r18 = r4.hasRemaining()     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            if (r18 == 0) goto L_0x03ad
            java.util.concurrent.BlockingQueue<java.nio.ByteBuffer> r0 = r7.inQueue     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r18 = r0
            r0 = r18
            r0.put(r4)     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            r0 = r22
            r0.queue(r7)     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            goto L_0x035b
        L_0x03a6:
            r8 = move-exception
            r0 = r22
            r0.pushBuffer(r4)     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
            throw r8     // Catch:{ CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, IOException -> 0x024f, InterruptedException -> 0x02fb }
        L_0x03ad:
            r0 = r22
            r0.pushBuffer(r4)     // Catch:{ IOException -> 0x03a6, CancelledKeyException -> 0x014b, ClosedByInterruptException -> 0x01fd, InterruptedException -> 0x02fb }
            goto L_0x035b
        L_0x03b3:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector
            r18 = r0
            if (r18 == 0) goto L_0x03c4
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ IOException -> 0x03e3 }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x03e3 }
        L_0x03c4:
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server
            r18 = r0
            if (r18 == 0) goto L_0x0044
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x03d7 }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x03d7 }
            goto L_0x0044
        L_0x03d7:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x0044
        L_0x03e3:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x03c4
        L_0x03ee:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector
            r18 = r0
            if (r18 == 0) goto L_0x03ff
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ IOException -> 0x041e }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x041e }
        L_0x03ff:
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server
            r18 = r0
            if (r18 == 0) goto L_0x0044
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x0412 }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x0412 }
            goto L_0x0044
        L_0x0412:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x0044
        L_0x041e:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x03ff
        L_0x0429:
            r22.stopConnectionLostTimer()
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            if (r18 == 0) goto L_0x044e
            r0 = r22
            java.util.List<org.java_websocket.server.WebSocketServer$WebSocketWorker> r0 = r0.decoders
            r18 = r0
            java.util.Iterator r18 = r18.iterator()
        L_0x043e:
            boolean r19 = r18.hasNext()
            if (r19 == 0) goto L_0x044e
            java.lang.Object r17 = r18.next()
            org.java_websocket.server.WebSocketServer$WebSocketWorker r17 = (org.java_websocket.server.WebSocketServer.WebSocketWorker) r17
            r17.interrupt()
            goto L_0x043e
        L_0x044e:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector
            r18 = r0
            if (r18 == 0) goto L_0x045f
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ IOException -> 0x047e }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x047e }
        L_0x045f:
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server
            r18 = r0
            if (r18 == 0) goto L_0x0044
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x0472 }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x0472 }
            goto L_0x0044
        L_0x0472:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x0044
        L_0x047e:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x045f
        L_0x0489:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector
            r18 = r0
            if (r18 == 0) goto L_0x049a
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ IOException -> 0x04b9 }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x04b9 }
        L_0x049a:
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server
            r18 = r0
            if (r18 == 0) goto L_0x0044
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x04ad }
            r18 = r0
            r18.close()     // Catch:{ IOException -> 0x04ad }
            goto L_0x0044
        L_0x04ad:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x0044
        L_0x04b9:
            r8 = move-exception
            r18 = 0
            r0 = r22
            r1 = r18
            r0.onError(r1, r8)
            goto L_0x049a
        L_0x04c4:
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector
            r19 = r0
            if (r19 == 0) goto L_0x04d5
            r0 = r22
            java.nio.channels.Selector r0 = r0.selector     // Catch:{ IOException -> 0x04e7 }
            r19 = r0
            r19.close()     // Catch:{ IOException -> 0x04e7 }
        L_0x04d5:
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server
            r19 = r0
            if (r19 == 0) goto L_0x04e6
            r0 = r22
            java.nio.channels.ServerSocketChannel r0 = r0.server     // Catch:{ IOException -> 0x04f2 }
            r19 = r0
            r19.close()     // Catch:{ IOException -> 0x04f2 }
        L_0x04e6:
            throw r18
        L_0x04e7:
            r8 = move-exception
            r19 = 0
            r0 = r22
            r1 = r19
            r0.onError(r1, r8)
            goto L_0x04d5
        L_0x04f2:
            r8 = move-exception
            r19 = 0
            r0 = r22
            r1 = r19
            r0.onError(r1, r8)
            goto L_0x04e6
        */
        throw new UnsupportedOperationException("Method not decompiled: org.java_websocket.server.WebSocketServer.run():void");
    }

    /* access modifiers changed from: protected */
    public void allocateBuffers(WebSocket c) throws InterruptedException {
        if (this.queuesize.get() < (this.decoders.size() * 2) + 1) {
            this.queuesize.incrementAndGet();
            this.buffers.put(createBuffer());
        }
    }

    /* access modifiers changed from: protected */
    public void releaseBuffers(WebSocket c) throws InterruptedException {
    }

    public ByteBuffer createBuffer() {
        return ByteBuffer.allocate(WebSocketImpl.RCVBUF);
    }

    /* access modifiers changed from: protected */
    public void queue(WebSocketImpl ws) throws InterruptedException {
        if (ws.workerThread == null) {
            ws.workerThread = this.decoders.get(this.queueinvokes % this.decoders.size());
            this.queueinvokes++;
        }
        ws.workerThread.put(ws);
    }

    private ByteBuffer takeBuffer() throws InterruptedException {
        return this.buffers.take();
    }

    /* access modifiers changed from: private */
    public void pushBuffer(ByteBuffer buf) throws InterruptedException {
        if (this.buffers.size() <= this.queuesize.intValue()) {
            this.buffers.put(buf);
        }
    }

    private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
        SelectableChannel channel;
        if (conn != null) {
            conn.closeConnection(CloseFrame.ABNORMAL_CLOSE, ex.getMessage());
        } else if (key != null && (channel = key.channel()) != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
            }
            if (WebSocketImpl.DEBUG) {
                System.out.println("Connection closed because of " + ex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFatal(WebSocket conn, Exception e) {
        onError(conn, e);
        if (this.decoders != null) {
            for (WebSocketWorker w : this.decoders) {
                w.interrupt();
            }
        }
        if (this.selectorthread != null) {
            this.selectorthread.interrupt();
        }
        try {
            stop();
        } catch (IOException e1) {
            onError((WebSocket) null, e1);
        } catch (InterruptedException e12) {
            Thread.currentThread().interrupt();
            onError((WebSocket) null, e12);
        }
    }

    public final void onWebsocketMessage(WebSocket conn, String message) {
        onMessage(conn, message);
    }

    @Deprecated
    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
        onFragment(conn, frame);
    }

    public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
        onMessage(conn, blob);
    }

    public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {
        if (addConnection(conn)) {
            onOpen(conn, (ClientHandshake) handshake);
        }
    }

    public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
        this.selector.wakeup();
        try {
            if (removeConnection(conn)) {
                onClose(conn, code, reason, remote);
            }
            try {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            try {
                releaseBuffers(conn);
            } catch (InterruptedException e2) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean removeConnection(WebSocket ws) {
        boolean removed = false;
        synchronized (this.connections) {
            if (this.connections.contains(ws)) {
                removed = this.connections.remove(ws);
            } else if (WebSocketImpl.DEBUG) {
                System.out.println("Removing connection which is not in the connections collection! Possible no handshake recieved! " + ws);
            }
        }
        if (this.isclosed.get() && this.connections.size() == 0) {
            this.selectorthread.interrupt();
        }
        return removed;
    }

    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    }

    /* access modifiers changed from: protected */
    public boolean addConnection(WebSocket ws) {
        boolean succ;
        if (!this.isclosed.get()) {
            synchronized (this.connections) {
                succ = this.connections.add(ws);
                if (!$assertionsDisabled && !succ) {
                    throw new AssertionError();
                }
            }
            return succ;
        }
        ws.close(1001);
        return true;
    }

    public final void onWebsocketError(WebSocket conn, Exception ex) {
        onError(conn, ex);
    }

    public final void onWriteDemand(WebSocket w) {
        WebSocketImpl conn = (WebSocketImpl) w;
        try {
            conn.key.interestOps(5);
        } catch (CancelledKeyException e) {
            conn.outQueue.clear();
        }
        this.selector.wakeup();
    }

    public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
        onCloseInitiated(conn, code, reason);
    }

    public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
        onClosing(conn, code, reason, remote);
    }

    public void onCloseInitiated(WebSocket conn, int code, String reason) {
    }

    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
    }

    public final void setWebSocketFactory(WebSocketServerFactory wsf2) {
        this.wsf = wsf2;
    }

    public final WebSocketFactory getWebSocketFactory() {
        return this.wsf;
    }

    /* access modifiers changed from: protected */
    public boolean onConnect(SelectionKey key) {
        return true;
    }

    private Socket getSocket(WebSocket conn) {
        return ((SocketChannel) ((WebSocketImpl) conn).key.channel()).socket();
    }

    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return (InetSocketAddress) getSocket(conn).getLocalSocketAddress();
    }

    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        return (InetSocketAddress) getSocket(conn).getRemoteSocketAddress();
    }

    public void onMessage(WebSocket conn, ByteBuffer message) {
    }

    @Deprecated
    public void onFragment(WebSocket conn, Framedata fragment) {
    }

    public void broadcast(String text) {
        broadcast(text, this.connections);
    }

    public void broadcast(byte[] data) {
        broadcast(data, this.connections);
    }

    public void broadcast(byte[] data, Collection<WebSocket> clients) {
        if (data == null || clients == null) {
            throw new IllegalArgumentException();
        }
        Map<Draft, List<Framedata>> draftFrames = new HashMap<>();
        ByteBuffer byteBufferData = ByteBuffer.wrap(data);
        synchronized (clients) {
            for (WebSocket client : clients) {
                if (client != null) {
                    Draft draft = client.getDraft();
                    if (!draftFrames.containsKey(draft)) {
                        draftFrames.put(draft, draft.createFrames(byteBufferData, false));
                    }
                    try {
                        client.sendFrame((Collection<Framedata>) draftFrames.get(draft));
                    } catch (WebsocketNotConnectedException e) {
                    }
                }
            }
        }
    }

    public void broadcast(String text, Collection<WebSocket> clients) {
        if (text == null || clients == null) {
            throw new IllegalArgumentException();
        }
        Map<Draft, List<Framedata>> draftFrames = new HashMap<>();
        synchronized (clients) {
            for (WebSocket client : clients) {
                if (client != null) {
                    Draft draft = client.getDraft();
                    if (!draftFrames.containsKey(draft)) {
                        draftFrames.put(draft, draft.createFrames(text, false));
                    }
                    try {
                        client.sendFrame((Collection<Framedata>) draftFrames.get(draft));
                    } catch (WebsocketNotConnectedException e) {
                    }
                }
            }
        }
    }

    public class WebSocketWorker extends Thread {
        static final /* synthetic */ boolean $assertionsDisabled = (!WebSocketServer.class.desiredAssertionStatus());
        private BlockingQueue<WebSocketImpl> iqueue = new LinkedBlockingQueue();

        public WebSocketWorker() {
            setName("WebSocketWorker-" + getId());
            setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(WebSocketServer.this) {
                public void uncaughtException(Thread t, Throwable e) {
                    System.err.print("Uncaught exception in thread \"" + t.getName() + "\":");
                    e.printStackTrace(System.err);
                }
            });
        }

        public void put(WebSocketImpl ws) throws InterruptedException {
            this.iqueue.put(ws);
        }

        /* JADX INFO: finally extract failed */
        public void run() {
            WebSocketImpl ws = null;
            while (true) {
                try {
                    ws = this.iqueue.take();
                    ByteBuffer buf = (ByteBuffer) ws.inQueue.poll();
                    if ($assertionsDisabled || buf != null) {
                        try {
                            ws.decode(buf);
                            WebSocketServer.this.pushBuffer(buf);
                        } catch (Exception e) {
                            System.err.println("Error while reading from remote connection: " + e);
                            e.printStackTrace();
                            WebSocketServer.this.pushBuffer(buf);
                        } catch (Throwable th) {
                            WebSocketServer.this.pushBuffer(buf);
                            throw th;
                        }
                    } else {
                        throw new AssertionError();
                    }
                } catch (InterruptedException e2) {
                    return;
                } catch (RuntimeException e3) {
                    WebSocketServer.this.handleFatal(ws, e3);
                    return;
                }
            }
        }
    }
}
