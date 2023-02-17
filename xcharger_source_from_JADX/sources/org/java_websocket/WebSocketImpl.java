package org.java_websocket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.IncompleteHandshakeException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.Charsetfunctions;
import p010it.sauronsoftware.ftp4j.FTPCodes;

public class WebSocketImpl implements WebSocket {
    static final /* synthetic */ boolean $assertionsDisabled = (!WebSocketImpl.class.desiredAssertionStatus());
    public static boolean DEBUG = false;
    public static int RCVBUF = 16384;
    private static final Object synchronizeWriteObject = new Object();
    private Object attachment;
    public ByteChannel channel;
    private Integer closecode;
    private Boolean closedremotely;
    private String closemessage;
    private Draft draft;
    private volatile boolean flushandclosestate;
    private ClientHandshake handshakerequest;
    public final BlockingQueue<ByteBuffer> inQueue;
    public SelectionKey key;
    private List<Draft> knownDrafts;
    private long lastPong;
    public final BlockingQueue<ByteBuffer> outQueue;
    private PingFrame pingFrame;
    private WebSocket.READYSTATE readystate;
    private String resourceDescriptor;
    private WebSocket.Role role;
    private ByteBuffer tmpHandshakeBytes;
    public volatile WebSocketServer.WebSocketWorker workerThread;
    private final WebSocketListener wsl;

    public WebSocketImpl(WebSocketListener listener, List<Draft> drafts) {
        this(listener, (Draft) null);
        this.role = WebSocket.Role.SERVER;
        if (drafts == null || drafts.isEmpty()) {
            this.knownDrafts = new ArrayList();
            this.knownDrafts.add(new Draft_6455());
            return;
        }
        this.knownDrafts = drafts;
    }

    public WebSocketImpl(WebSocketListener listener, Draft draft2) {
        this.flushandclosestate = false;
        this.readystate = WebSocket.READYSTATE.NOT_YET_CONNECTED;
        this.draft = null;
        this.tmpHandshakeBytes = ByteBuffer.allocate(0);
        this.handshakerequest = null;
        this.closemessage = null;
        this.closecode = null;
        this.closedremotely = null;
        this.resourceDescriptor = null;
        this.lastPong = System.currentTimeMillis();
        if (listener == null || (draft2 == null && this.role == WebSocket.Role.SERVER)) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        this.outQueue = new LinkedBlockingQueue();
        this.inQueue = new LinkedBlockingQueue();
        this.wsl = listener;
        this.role = WebSocket.Role.CLIENT;
        if (draft2 != null) {
            this.draft = draft2.copyInstance();
        }
    }

    @Deprecated
    public WebSocketImpl(WebSocketListener listener, Draft draft2, Socket socket) {
        this(listener, draft2);
    }

    @Deprecated
    public WebSocketImpl(WebSocketListener listener, List<Draft> drafts, Socket socket) {
        this(listener, drafts);
    }

    public void decode(ByteBuffer socketBuffer) {
        if ($assertionsDisabled || socketBuffer.hasRemaining()) {
            if (DEBUG) {
                System.out.println("process(" + socketBuffer.remaining() + "): {" + (socketBuffer.remaining() > 1000 ? "too big to display" : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining())) + '}');
            }
            if (getReadyState() != WebSocket.READYSTATE.NOT_YET_CONNECTED) {
                if (getReadyState() == WebSocket.READYSTATE.OPEN) {
                    decodeFrames(socketBuffer);
                }
            } else if (decodeHandshake(socketBuffer) && !isClosing() && !isClosed()) {
                if (!$assertionsDisabled && this.tmpHandshakeBytes.hasRemaining() == socketBuffer.hasRemaining() && socketBuffer.hasRemaining()) {
                    throw new AssertionError();
                } else if (socketBuffer.hasRemaining()) {
                    decodeFrames(socketBuffer);
                } else if (this.tmpHandshakeBytes.hasRemaining()) {
                    decodeFrames(this.tmpHandshakeBytes);
                }
            }
            if (!$assertionsDisabled && !isClosing() && !isFlushAndClose() && socketBuffer.hasRemaining()) {
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }

    private boolean decodeHandshake(ByteBuffer socketBufferNew) {
        ByteBuffer socketBuffer;
        if (this.tmpHandshakeBytes.capacity() == 0) {
            socketBuffer = socketBufferNew;
        } else {
            if (this.tmpHandshakeBytes.remaining() < socketBufferNew.remaining()) {
                ByteBuffer buf = ByteBuffer.allocate(this.tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
                this.tmpHandshakeBytes.flip();
                buf.put(this.tmpHandshakeBytes);
                this.tmpHandshakeBytes = buf;
            }
            this.tmpHandshakeBytes.put(socketBufferNew);
            this.tmpHandshakeBytes.flip();
            socketBuffer = this.tmpHandshakeBytes;
        }
        socketBuffer.mark();
        try {
            if (this.role != WebSocket.Role.SERVER) {
                if (this.role == WebSocket.Role.CLIENT) {
                    this.draft.setParseMode(this.role);
                    Handshakedata tmphandshake = this.draft.translateHandshake(socketBuffer);
                    if (!(tmphandshake instanceof ServerHandshake)) {
                        flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                        return false;
                    }
                    ServerHandshake handshake = (ServerHandshake) tmphandshake;
                    if (this.draft.acceptHandshakeAsClient(this.handshakerequest, handshake) == Draft.HandshakeState.MATCHED) {
                        try {
                            this.wsl.onWebsocketHandshakeReceivedAsClient(this, this.handshakerequest, handshake);
                            open(handshake);
                            return true;
                        } catch (InvalidDataException e) {
                            flushAndClose(e.getCloseCode(), e.getMessage(), false);
                            return false;
                        } catch (RuntimeException e2) {
                            this.wsl.onWebsocketError(this, e2);
                            flushAndClose(-1, e2.getMessage(), false);
                            return false;
                        }
                    } else {
                        close(CloseFrame.PROTOCOL_ERROR, "draft " + this.draft + " refuses handshake");
                    }
                }
                return false;
            } else if (this.draft == null) {
                for (Draft d : this.knownDrafts) {
                    Draft d2 = d.copyInstance();
                    try {
                        d2.setParseMode(this.role);
                        socketBuffer.reset();
                        Handshakedata tmphandshake2 = d2.translateHandshake(socketBuffer);
                        if (!(tmphandshake2 instanceof ClientHandshake)) {
                            closeConnectionDueToWrongHandshake(new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "wrong http function"));
                            return false;
                        }
                        ClientHandshake handshake2 = (ClientHandshake) tmphandshake2;
                        if (d2.acceptHandshakeAsServer(handshake2) == Draft.HandshakeState.MATCHED) {
                            this.resourceDescriptor = handshake2.getResourceDescriptor();
                            try {
                                write(d2.createHandshake(d2.postProcessHandshakeResponseAsServer(handshake2, this.wsl.onWebsocketHandshakeReceivedAsServer(this, d2, handshake2)), this.role));
                                this.draft = d2;
                                open(handshake2);
                                return true;
                            } catch (InvalidDataException e3) {
                                closeConnectionDueToWrongHandshake(e3);
                                return false;
                            } catch (RuntimeException e4) {
                                this.wsl.onWebsocketError(this, e4);
                                closeConnectionDueToInternalServerError(e4);
                                return false;
                            }
                        } else {
                            continue;
                        }
                    } catch (InvalidHandshakeException e5) {
                    }
                }
                if (this.draft == null) {
                    closeConnectionDueToWrongHandshake(new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "no draft matches"));
                }
                return false;
            } else {
                Handshakedata tmphandshake3 = this.draft.translateHandshake(socketBuffer);
                if (!(tmphandshake3 instanceof ClientHandshake)) {
                    flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                    return false;
                }
                ClientHandshake handshake3 = (ClientHandshake) tmphandshake3;
                if (this.draft.acceptHandshakeAsServer(handshake3) == Draft.HandshakeState.MATCHED) {
                    open(handshake3);
                    return true;
                }
                close(CloseFrame.PROTOCOL_ERROR, "the handshake did finaly not match");
                return false;
            }
        } catch (InvalidHandshakeException e6) {
            try {
                close((InvalidDataException) e6);
            } catch (IncompleteHandshakeException e7) {
                if (this.tmpHandshakeBytes.capacity() == 0) {
                    socketBuffer.reset();
                    int newsize = e7.getPreferedSize();
                    if (newsize == 0) {
                        newsize = socketBuffer.capacity() + 16;
                    } else if (!$assertionsDisabled && e7.getPreferedSize() < socketBuffer.remaining()) {
                        throw new AssertionError();
                    }
                    this.tmpHandshakeBytes = ByteBuffer.allocate(newsize);
                    this.tmpHandshakeBytes.put(socketBufferNew);
                } else {
                    this.tmpHandshakeBytes.position(this.tmpHandshakeBytes.limit());
                    this.tmpHandshakeBytes.limit(this.tmpHandshakeBytes.capacity());
                }
            }
        }
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        try {
            for (Framedata f : this.draft.translateFrame(socketBuffer)) {
                if (DEBUG) {
                    System.out.println("matched frame: " + f);
                }
                this.draft.processFrame(this, f);
            }
        } catch (InvalidDataException e1) {
            this.wsl.onWebsocketError(this, e1);
            close(e1);
        }
    }

    private void closeConnectionDueToWrongHandshake(InvalidDataException exception) {
        write(generateHttpResponseDueToError(404));
        flushAndClose(exception.getCloseCode(), exception.getMessage(), false);
    }

    private void closeConnectionDueToInternalServerError(RuntimeException exception) {
        write(generateHttpResponseDueToError(FTPCodes.SYNTAX_ERROR));
        flushAndClose(-1, exception.getMessage(), false);
    }

    private ByteBuffer generateHttpResponseDueToError(int errorCode) {
        String errorCodeDescription;
        switch (errorCode) {
            case 404:
                errorCodeDescription = "404 WebSocket Upgrade Failure";
                break;
            default:
                errorCodeDescription = "500 Internal Server Error";
                break;
        }
        return ByteBuffer.wrap(Charsetfunctions.asciiBytes("HTTP/1.1 " + errorCodeDescription + "\r\nContent-Type: text/html\nServer: TooTallNate Java-WebSocket\r\nContent-Length: " + (errorCodeDescription.length() + 48) + "\r\n\r\n<html><head></head><body><h1>" + errorCodeDescription + "</h1></body></html>"));
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:18:0x002b=Splitter:B:18:0x002b, B:32:0x005e=Splitter:B:32:0x005e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void close(int r7, java.lang.String r8, boolean r9) {
        /*
            r6 = this;
            r5 = 1006(0x3ee, float:1.41E-42)
            r4 = -3
            monitor-enter(r6)
            org.java_websocket.WebSocket$READYSTATE r2 = r6.getReadyState()     // Catch:{ all -> 0x0028 }
            org.java_websocket.WebSocket$READYSTATE r3 = org.java_websocket.WebSocket.READYSTATE.CLOSING     // Catch:{ all -> 0x0028 }
            if (r2 == r3) goto L_0x0034
            org.java_websocket.WebSocket$READYSTATE r2 = r6.readystate     // Catch:{ all -> 0x0028 }
            org.java_websocket.WebSocket$READYSTATE r3 = org.java_websocket.WebSocket.READYSTATE.CLOSED     // Catch:{ all -> 0x0028 }
            if (r2 == r3) goto L_0x0034
            org.java_websocket.WebSocket$READYSTATE r2 = r6.getReadyState()     // Catch:{ all -> 0x0028 }
            org.java_websocket.WebSocket$READYSTATE r3 = org.java_websocket.WebSocket.READYSTATE.OPEN     // Catch:{ all -> 0x0028 }
            if (r2 != r3) goto L_0x0080
            if (r7 != r5) goto L_0x0036
            boolean r2 = $assertionsDisabled     // Catch:{ all -> 0x0028 }
            if (r2 != 0) goto L_0x002b
            if (r9 == 0) goto L_0x002b
            java.lang.AssertionError r2 = new java.lang.AssertionError     // Catch:{ all -> 0x0028 }
            r2.<init>()     // Catch:{ all -> 0x0028 }
            throw r2     // Catch:{ all -> 0x0028 }
        L_0x0028:
            r2 = move-exception
            monitor-exit(r6)
            throw r2
        L_0x002b:
            org.java_websocket.WebSocket$READYSTATE r2 = org.java_websocket.WebSocket.READYSTATE.CLOSING     // Catch:{ all -> 0x0028 }
            r6.setReadyState(r2)     // Catch:{ all -> 0x0028 }
            r2 = 0
            r6.flushAndClose(r7, r8, r2)     // Catch:{ all -> 0x0028 }
        L_0x0034:
            monitor-exit(r6)
            return
        L_0x0036:
            org.java_websocket.drafts.Draft r2 = r6.draft     // Catch:{ all -> 0x0028 }
            org.java_websocket.drafts.Draft$CloseHandshakeType r2 = r2.getCloseHandshakeType()     // Catch:{ all -> 0x0028 }
            org.java_websocket.drafts.Draft$CloseHandshakeType r3 = org.java_websocket.drafts.Draft.CloseHandshakeType.NONE     // Catch:{ all -> 0x0028 }
            if (r2 == r3) goto L_0x005e
            if (r9 != 0) goto L_0x0047
            org.java_websocket.WebSocketListener r2 = r6.wsl     // Catch:{ RuntimeException -> 0x006a }
            r2.onWebsocketCloseInitiated(r6, r7, r8)     // Catch:{ RuntimeException -> 0x006a }
        L_0x0047:
            boolean r2 = r6.isOpen()     // Catch:{ InvalidDataException -> 0x0071 }
            if (r2 == 0) goto L_0x005e
            org.java_websocket.framing.CloseFrame r0 = new org.java_websocket.framing.CloseFrame     // Catch:{ InvalidDataException -> 0x0071 }
            r0.<init>()     // Catch:{ InvalidDataException -> 0x0071 }
            r0.setReason(r8)     // Catch:{ InvalidDataException -> 0x0071 }
            r0.setCode(r7)     // Catch:{ InvalidDataException -> 0x0071 }
            r0.isValid()     // Catch:{ InvalidDataException -> 0x0071 }
            r6.sendFrame((org.java_websocket.framing.Framedata) r0)     // Catch:{ InvalidDataException -> 0x0071 }
        L_0x005e:
            r6.flushAndClose(r7, r8, r9)     // Catch:{ all -> 0x0028 }
        L_0x0061:
            org.java_websocket.WebSocket$READYSTATE r2 = org.java_websocket.WebSocket.READYSTATE.CLOSING     // Catch:{ all -> 0x0028 }
            r6.setReadyState(r2)     // Catch:{ all -> 0x0028 }
            r2 = 0
            r6.tmpHandshakeBytes = r2     // Catch:{ all -> 0x0028 }
            goto L_0x0034
        L_0x006a:
            r1 = move-exception
            org.java_websocket.WebSocketListener r2 = r6.wsl     // Catch:{ InvalidDataException -> 0x0071 }
            r2.onWebsocketError(r6, r1)     // Catch:{ InvalidDataException -> 0x0071 }
            goto L_0x0047
        L_0x0071:
            r1 = move-exception
            org.java_websocket.WebSocketListener r2 = r6.wsl     // Catch:{ all -> 0x0028 }
            r2.onWebsocketError(r6, r1)     // Catch:{ all -> 0x0028 }
            r2 = 1006(0x3ee, float:1.41E-42)
            java.lang.String r3 = "generated frame is invalid"
            r4 = 0
            r6.flushAndClose(r2, r3, r4)     // Catch:{ all -> 0x0028 }
            goto L_0x005e
        L_0x0080:
            if (r7 != r4) goto L_0x0094
            boolean r2 = $assertionsDisabled     // Catch:{ all -> 0x0028 }
            if (r2 != 0) goto L_0x008e
            if (r9 != 0) goto L_0x008e
            java.lang.AssertionError r2 = new java.lang.AssertionError     // Catch:{ all -> 0x0028 }
            r2.<init>()     // Catch:{ all -> 0x0028 }
            throw r2     // Catch:{ all -> 0x0028 }
        L_0x008e:
            r2 = -3
            r3 = 1
            r6.flushAndClose(r2, r8, r3)     // Catch:{ all -> 0x0028 }
            goto L_0x0061
        L_0x0094:
            r2 = 1002(0x3ea, float:1.404E-42)
            if (r7 != r2) goto L_0x009c
            r6.flushAndClose(r7, r8, r9)     // Catch:{ all -> 0x0028 }
            goto L_0x0061
        L_0x009c:
            r2 = -1
            r3 = 0
            r6.flushAndClose(r2, r8, r3)     // Catch:{ all -> 0x0028 }
            goto L_0x0061
        */
        throw new UnsupportedOperationException("Method not decompiled: org.java_websocket.WebSocketImpl.close(int, java.lang.String, boolean):void");
    }

    public void close(int code, String message) {
        close(code, message, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0067, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0068, code lost:
        r3.wsl.onWebsocketError(r3, r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void closeConnection(int r4, java.lang.String r5, boolean r6) {
        /*
            r3 = this;
            monitor-enter(r3)
            org.java_websocket.WebSocket$READYSTATE r1 = r3.getReadyState()     // Catch:{ all -> 0x0045 }
            org.java_websocket.WebSocket$READYSTATE r2 = org.java_websocket.WebSocket.READYSTATE.CLOSED     // Catch:{ all -> 0x0045 }
            if (r1 != r2) goto L_0x000b
        L_0x0009:
            monitor-exit(r3)
            return
        L_0x000b:
            org.java_websocket.WebSocket$READYSTATE r1 = r3.getReadyState()     // Catch:{ all -> 0x0045 }
            org.java_websocket.WebSocket$READYSTATE r2 = org.java_websocket.WebSocket.READYSTATE.OPEN     // Catch:{ all -> 0x0045 }
            if (r1 != r2) goto L_0x001c
            r1 = 1006(0x3ee, float:1.41E-42)
            if (r4 != r1) goto L_0x001c
            org.java_websocket.WebSocket$READYSTATE r1 = org.java_websocket.WebSocket.READYSTATE.CLOSING     // Catch:{ all -> 0x0045 }
            r3.setReadyState(r1)     // Catch:{ all -> 0x0045 }
        L_0x001c:
            java.nio.channels.SelectionKey r1 = r3.key     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x0025
            java.nio.channels.SelectionKey r1 = r3.key     // Catch:{ all -> 0x0045 }
            r1.cancel()     // Catch:{ all -> 0x0045 }
        L_0x0025:
            java.nio.channels.ByteChannel r1 = r3.channel     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x002e
            java.nio.channels.ByteChannel r1 = r3.channel     // Catch:{ IOException -> 0x0048 }
            r1.close()     // Catch:{ IOException -> 0x0048 }
        L_0x002e:
            org.java_websocket.WebSocketListener r1 = r3.wsl     // Catch:{ RuntimeException -> 0x0067 }
            r1.onWebsocketClose(r3, r4, r5, r6)     // Catch:{ RuntimeException -> 0x0067 }
        L_0x0033:
            org.java_websocket.drafts.Draft r1 = r3.draft     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x003c
            org.java_websocket.drafts.Draft r1 = r3.draft     // Catch:{ all -> 0x0045 }
            r1.reset()     // Catch:{ all -> 0x0045 }
        L_0x003c:
            r1 = 0
            r3.handshakerequest = r1     // Catch:{ all -> 0x0045 }
            org.java_websocket.WebSocket$READYSTATE r1 = org.java_websocket.WebSocket.READYSTATE.CLOSED     // Catch:{ all -> 0x0045 }
            r3.setReadyState(r1)     // Catch:{ all -> 0x0045 }
            goto L_0x0009
        L_0x0045:
            r1 = move-exception
            monitor-exit(r3)
            throw r1
        L_0x0048:
            r0 = move-exception
            java.lang.String r1 = r0.getMessage()     // Catch:{ all -> 0x0045 }
            java.lang.String r2 = "Broken pipe"
            boolean r1 = r1.equals(r2)     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x0061
            boolean r1 = DEBUG     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x002e
            java.io.PrintStream r1 = java.lang.System.out     // Catch:{ all -> 0x0045 }
            java.lang.String r2 = "Caught IOException: Broken pipe during closeConnection()"
            r1.println(r2)     // Catch:{ all -> 0x0045 }
            goto L_0x002e
        L_0x0061:
            org.java_websocket.WebSocketListener r1 = r3.wsl     // Catch:{ all -> 0x0045 }
            r1.onWebsocketError(r3, r0)     // Catch:{ all -> 0x0045 }
            goto L_0x002e
        L_0x0067:
            r0 = move-exception
            org.java_websocket.WebSocketListener r1 = r3.wsl     // Catch:{ all -> 0x0045 }
            r1.onWebsocketError(r3, r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0033
        */
        throw new UnsupportedOperationException("Method not decompiled: org.java_websocket.WebSocketImpl.closeConnection(int, java.lang.String, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void closeConnection(int code, boolean remote) {
        closeConnection(code, "", remote);
    }

    public void closeConnection() {
        if (this.closedremotely == null) {
            throw new IllegalStateException("this method must be used in conjuction with flushAndClose");
        }
        closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
    }

    public void closeConnection(int code, String message) {
        closeConnection(code, message, false);
    }

    public synchronized void flushAndClose(int code, String message, boolean remote) {
        if (!this.flushandclosestate) {
            this.closecode = Integer.valueOf(code);
            this.closemessage = message;
            this.closedremotely = Boolean.valueOf(remote);
            this.flushandclosestate = true;
            this.wsl.onWriteDemand(this);
            try {
                this.wsl.onWebsocketClosing(this, code, message, remote);
            } catch (RuntimeException e) {
                this.wsl.onWebsocketError(this, e);
            }
            if (this.draft != null) {
                this.draft.reset();
            }
            this.handshakerequest = null;
        }
        return;
    }

    public void eot() {
        if (getReadyState() == WebSocket.READYSTATE.NOT_YET_CONNECTED) {
            closeConnection(-1, true);
        } else if (this.flushandclosestate) {
            closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
        } else if (this.draft.getCloseHandshakeType() == Draft.CloseHandshakeType.NONE) {
            closeConnection(1000, true);
        } else if (this.draft.getCloseHandshakeType() != Draft.CloseHandshakeType.ONEWAY) {
            closeConnection((int) CloseFrame.ABNORMAL_CLOSE, true);
        } else if (this.role == WebSocket.Role.SERVER) {
            closeConnection((int) CloseFrame.ABNORMAL_CLOSE, true);
        } else {
            closeConnection(1000, true);
        }
    }

    public void close(int code) {
        close(code, "", false);
    }

    public void close(InvalidDataException e) {
        close(e.getCloseCode(), e.getMessage(), false);
    }

    public void send(String text) throws WebsocketNotConnectedException {
        if (text == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        send((Collection<Framedata>) this.draft.createFrames(text, this.role == WebSocket.Role.CLIENT));
    }

    public void send(ByteBuffer bytes) throws IllegalArgumentException, WebsocketNotConnectedException {
        if (bytes == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        send((Collection<Framedata>) this.draft.createFrames(bytes, this.role == WebSocket.Role.CLIENT));
    }

    public void send(byte[] bytes) throws IllegalArgumentException, WebsocketNotConnectedException {
        send(ByteBuffer.wrap(bytes));
    }

    private void send(Collection<Framedata> frames) {
        if (!isOpen()) {
            throw new WebsocketNotConnectedException();
        } else if (frames == null) {
            throw new IllegalArgumentException();
        } else {
            ArrayList<ByteBuffer> outgoingFrames = new ArrayList<>();
            for (Framedata f : frames) {
                if (DEBUG) {
                    System.out.println("send frame: " + f);
                }
                outgoingFrames.add(this.draft.createBinaryFrame(f));
            }
            write((List<ByteBuffer>) outgoingFrames);
        }
    }

    public void sendFragmentedFrame(Framedata.Opcode op, ByteBuffer buffer, boolean fin) {
        send((Collection<Framedata>) this.draft.continuousFrame(op, buffer, fin));
    }

    public void sendFrame(Collection<Framedata> frames) {
        send(frames);
    }

    public void sendFrame(Framedata framedata) {
        send((Collection<Framedata>) Collections.singletonList(framedata));
    }

    public void sendPing() throws NotYetConnectedException {
        if (this.pingFrame == null) {
            this.pingFrame = new PingFrame();
        }
        sendFrame((Framedata) this.pingFrame);
    }

    public boolean hasBufferedData() {
        return !this.outQueue.isEmpty();
    }

    public void startHandshake(ClientHandshakeBuilder handshakedata) throws InvalidHandshakeException {
        if ($assertionsDisabled || getReadyState() != WebSocket.READYSTATE.CONNECTING) {
            this.handshakerequest = this.draft.postProcessHandshakeRequestAsClient(handshakedata);
            this.resourceDescriptor = handshakedata.getResourceDescriptor();
            if ($assertionsDisabled || this.resourceDescriptor != null) {
                try {
                    this.wsl.onWebsocketHandshakeSentAsClient(this, this.handshakerequest);
                    write(this.draft.createHandshake(this.handshakerequest, this.role));
                } catch (InvalidDataException e) {
                    throw new InvalidHandshakeException("Handshake data rejected by client.");
                } catch (RuntimeException e2) {
                    this.wsl.onWebsocketError(this, e2);
                    throw new InvalidHandshakeException("rejected because of" + e2);
                }
            } else {
                throw new AssertionError();
            }
        } else {
            throw new AssertionError("shall only be called once");
        }
    }

    private void write(ByteBuffer buf) {
        if (DEBUG) {
            System.out.println("write(" + buf.remaining() + "): {" + (buf.remaining() > 1000 ? "too big to display" : new String(buf.array())) + '}');
        }
        this.outQueue.add(buf);
        this.wsl.onWriteDemand(this);
    }

    private void write(List<ByteBuffer> bufs) {
        synchronized (synchronizeWriteObject) {
            for (ByteBuffer b : bufs) {
                write(b);
            }
        }
    }

    private void open(Handshakedata d) {
        if (DEBUG) {
            System.out.println("open using draft: " + this.draft);
        }
        setReadyState(WebSocket.READYSTATE.OPEN);
        try {
            this.wsl.onWebsocketOpen(this, d);
        } catch (RuntimeException e) {
            this.wsl.onWebsocketError(this, e);
        }
    }

    public boolean isConnecting() {
        if ($assertionsDisabled || !this.flushandclosestate || getReadyState() == WebSocket.READYSTATE.CONNECTING) {
            return getReadyState() == WebSocket.READYSTATE.CONNECTING;
        }
        throw new AssertionError();
    }

    public boolean isOpen() {
        if ($assertionsDisabled || getReadyState() != WebSocket.READYSTATE.OPEN || !this.flushandclosestate) {
            return getReadyState() == WebSocket.READYSTATE.OPEN;
        }
        throw new AssertionError();
    }

    public boolean isClosing() {
        return getReadyState() == WebSocket.READYSTATE.CLOSING;
    }

    public boolean isFlushAndClose() {
        return this.flushandclosestate;
    }

    public boolean isClosed() {
        return getReadyState() == WebSocket.READYSTATE.CLOSED;
    }

    public WebSocket.READYSTATE getReadyState() {
        return this.readystate;
    }

    private void setReadyState(WebSocket.READYSTATE readystate2) {
        this.readystate = readystate2;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString();
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return this.wsl.getRemoteSocketAddress(this);
    }

    public InetSocketAddress getLocalSocketAddress() {
        return this.wsl.getLocalSocketAddress(this);
    }

    public Draft getDraft() {
        return this.draft;
    }

    public void close() {
        close(1000);
    }

    public String getResourceDescriptor() {
        return this.resourceDescriptor;
    }

    /* access modifiers changed from: package-private */
    public long getLastPong() {
        return this.lastPong;
    }

    public void updateLastPong() {
        this.lastPong = System.currentTimeMillis();
    }

    public WebSocketListener getWebSocketListener() {
        return this.wsl;
    }

    public <T> T getAttachment() {
        return this.attachment;
    }

    public <T> void setAttachment(T attachment2) {
        this.attachment = attachment2;
    }
}
