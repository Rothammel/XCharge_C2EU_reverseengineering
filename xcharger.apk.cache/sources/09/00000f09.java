package org.java_websocket;

import it.sauronsoftware.ftp4j.FTPCodes;
import java.io.IOException;
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
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.Charsetfunctions;

/* loaded from: classes.dex */
public class WebSocketImpl implements WebSocket {
    static final /* synthetic */ boolean $assertionsDisabled;
    public static boolean DEBUG;
    public static int RCVBUF;
    private static final Object synchronizeWriteObject;
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

    static {
        $assertionsDisabled = !WebSocketImpl.class.desiredAssertionStatus();
        RCVBUF = 16384;
        DEBUG = false;
        synchronizeWriteObject = new Object();
    }

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

    public WebSocketImpl(WebSocketListener listener, Draft draft) {
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
        if (listener == null || (draft == null && this.role == WebSocket.Role.SERVER)) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        this.outQueue = new LinkedBlockingQueue();
        this.inQueue = new LinkedBlockingQueue();
        this.wsl = listener;
        this.role = WebSocket.Role.CLIENT;
        if (draft != null) {
            this.draft = draft.copyInstance();
        }
    }

    @Deprecated
    public WebSocketImpl(WebSocketListener listener, Draft draft, Socket socket) {
        this(listener, draft);
    }

    @Deprecated
    public WebSocketImpl(WebSocketListener listener, List<Draft> drafts, Socket socket) {
        this(listener, drafts);
    }

    public void decode(ByteBuffer socketBuffer) {
        if (!$assertionsDisabled && !socketBuffer.hasRemaining()) {
            throw new AssertionError();
        }
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
            }
            if (socketBuffer.hasRemaining()) {
                decodeFrames(socketBuffer);
            } else if (this.tmpHandshakeBytes.hasRemaining()) {
                decodeFrames(this.tmpHandshakeBytes);
            }
        }
        if (!$assertionsDisabled && !isClosing() && !isFlushAndClose() && socketBuffer.hasRemaining()) {
            throw new AssertionError();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r10v51, types: [java.util.Iterator] */
    /* JADX WARN: Type inference failed for: r10v55 */
    /* JADX WARN: Type inference failed for: r10v56 */
    /* JADX WARN: Type inference failed for: r10v58, types: [boolean] */
    /* JADX WARN: Type inference failed for: r10v59 */
    /* JADX WARN: Type inference failed for: r10v60 */
    /* JADX WARN: Type inference failed for: r10v61 */
    private boolean decodeHandshake(ByteBuffer socketBufferNew) {
        ByteBuffer socketBuffer;
        Handshakedata tmphandshake;
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
            try {
            } catch (InvalidHandshakeException e) {
                close(e);
            }
        } catch (IncompleteHandshakeException e2) {
            if (this.tmpHandshakeBytes.capacity() == 0) {
                socketBuffer.reset();
                int newsize = e2.getPreferedSize();
                if (newsize == 0) {
                    newsize = socketBuffer.capacity() + 16;
                } else if (!$assertionsDisabled && e2.getPreferedSize() < socketBuffer.remaining()) {
                    throw new AssertionError();
                }
                this.tmpHandshakeBytes = ByteBuffer.allocate(newsize);
                this.tmpHandshakeBytes.put(socketBufferNew);
            } else {
                this.tmpHandshakeBytes.position(this.tmpHandshakeBytes.limit());
                this.tmpHandshakeBytes.limit(this.tmpHandshakeBytes.capacity());
            }
        }
        if (this.role == WebSocket.Role.SERVER) {
            if (this.draft == null) {
                boolean it2 = this.knownDrafts.iterator();
                while (it2.hasNext()) {
                    Draft d = ((Draft) it2.next()).copyInstance();
                    try {
                        d.setParseMode(this.role);
                        socketBuffer.reset();
                        tmphandshake = d.translateHandshake(socketBuffer);
                    } catch (InvalidHandshakeException e3) {
                    }
                    if (!(tmphandshake instanceof ClientHandshake)) {
                        closeConnectionDueToWrongHandshake(new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "wrong http function"));
                        it2 = 0;
                    } else {
                        ClientHandshake handshake = (ClientHandshake) tmphandshake;
                        Draft.HandshakeState handshakestate = d.acceptHandshakeAsServer(handshake);
                        if (handshakestate == Draft.HandshakeState.MATCHED) {
                            this.resourceDescriptor = handshake.getResourceDescriptor();
                            try {
                                ServerHandshakeBuilder response = this.wsl.onWebsocketHandshakeReceivedAsServer(this, d, handshake);
                                write(d.createHandshake(d.postProcessHandshakeResponseAsServer(handshake, response), this.role));
                                this.draft = d;
                                open(handshake);
                                it2 = 1;
                            } catch (RuntimeException e4) {
                                this.wsl.onWebsocketError(this, e4);
                                closeConnectionDueToInternalServerError(e4);
                                it2 = 0;
                            } catch (InvalidDataException e5) {
                                closeConnectionDueToWrongHandshake(e5);
                                it2 = 0;
                            }
                        } else {
                            continue;
                        }
                    }
                    return it2;
                }
                if (this.draft == null) {
                    closeConnectionDueToWrongHandshake(new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "no draft matches"));
                }
                return false;
            }
            Handshakedata tmphandshake2 = this.draft.translateHandshake(socketBuffer);
            if (!(tmphandshake2 instanceof ClientHandshake)) {
                flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                return false;
            }
            ClientHandshake handshake2 = (ClientHandshake) tmphandshake2;
            Draft.HandshakeState handshakestate2 = this.draft.acceptHandshakeAsServer(handshake2);
            if (handshakestate2 == Draft.HandshakeState.MATCHED) {
                open(handshake2);
                return true;
            }
            close(CloseFrame.PROTOCOL_ERROR, "the handshake did finaly not match");
            return false;
        }
        if (this.role == WebSocket.Role.CLIENT) {
            this.draft.setParseMode(this.role);
            Handshakedata tmphandshake3 = this.draft.translateHandshake(socketBuffer);
            if (!(tmphandshake3 instanceof ServerHandshake)) {
                flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                return false;
            }
            ServerHandshake handshake3 = (ServerHandshake) tmphandshake3;
            Draft.HandshakeState handshakestate3 = this.draft.acceptHandshakeAsClient(this.handshakerequest, handshake3);
            if (handshakestate3 == Draft.HandshakeState.MATCHED) {
                try {
                    this.wsl.onWebsocketHandshakeReceivedAsClient(this, this.handshakerequest, handshake3);
                    open(handshake3);
                    return true;
                } catch (RuntimeException e6) {
                    this.wsl.onWebsocketError(this, e6);
                    flushAndClose(-1, e6.getMessage(), false);
                    return false;
                } catch (InvalidDataException e7) {
                    flushAndClose(e7.getCloseCode(), e7.getMessage(), false);
                    return false;
                }
            }
            close(CloseFrame.PROTOCOL_ERROR, "draft " + this.draft + " refuses handshake");
        }
        return false;
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        try {
            List<Framedata> frames = this.draft.translateFrame(socketBuffer);
            for (Framedata f : frames) {
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

    public synchronized void close(int code, String message, boolean remote) {
        if (getReadyState() != WebSocket.READYSTATE.CLOSING && this.readystate != WebSocket.READYSTATE.CLOSED) {
            if (getReadyState() == WebSocket.READYSTATE.OPEN) {
                if (code == 1006) {
                    if (!$assertionsDisabled && remote) {
                        throw new AssertionError();
                    }
                    setReadyState(WebSocket.READYSTATE.CLOSING);
                    flushAndClose(code, message, false);
                } else {
                    if (this.draft.getCloseHandshakeType() != Draft.CloseHandshakeType.NONE) {
                        if (!remote) {
                            try {
                                try {
                                    this.wsl.onWebsocketCloseInitiated(this, code, message);
                                } catch (InvalidDataException e) {
                                    this.wsl.onWebsocketError(this, e);
                                    flushAndClose(CloseFrame.ABNORMAL_CLOSE, "generated frame is invalid", false);
                                }
                            } catch (RuntimeException e2) {
                                this.wsl.onWebsocketError(this, e2);
                            }
                        }
                        if (isOpen()) {
                            CloseFrame closeFrame = new CloseFrame();
                            closeFrame.setReason(message);
                            closeFrame.setCode(code);
                            closeFrame.isValid();
                            sendFrame(closeFrame);
                        }
                    }
                    flushAndClose(code, message, remote);
                }
            } else if (code == -3) {
                if (!$assertionsDisabled && !remote) {
                    throw new AssertionError();
                }
                flushAndClose(-3, message, true);
            } else if (code == 1002) {
                flushAndClose(code, message, remote);
            } else {
                flushAndClose(-1, message, false);
            }
            setReadyState(WebSocket.READYSTATE.CLOSING);
            this.tmpHandshakeBytes = null;
        }
    }

    @Override // org.java_websocket.WebSocket
    public void close(int code, String message) {
        close(code, message, false);
    }

    public synchronized void closeConnection(int code, String message, boolean remote) {
        if (getReadyState() != WebSocket.READYSTATE.CLOSED) {
            if (getReadyState() == WebSocket.READYSTATE.OPEN && code == 1006) {
                setReadyState(WebSocket.READYSTATE.CLOSING);
            }
            if (this.key != null) {
                this.key.cancel();
            }
            if (this.channel != null) {
                try {
                    this.channel.close();
                } catch (IOException e) {
                    if (e.getMessage().equals("Broken pipe")) {
                        if (DEBUG) {
                            System.out.println("Caught IOException: Broken pipe during closeConnection()");
                        }
                    } else {
                        this.wsl.onWebsocketError(this, e);
                    }
                }
            }
            try {
                this.wsl.onWebsocketClose(this, code, message, remote);
            } catch (RuntimeException e2) {
                this.wsl.onWebsocketError(this, e2);
            }
            if (this.draft != null) {
                this.draft.reset();
            }
            this.handshakerequest = null;
            setReadyState(WebSocket.READYSTATE.CLOSED);
        }
    }

    protected void closeConnection(int code, boolean remote) {
        closeConnection(code, "", remote);
    }

    public void closeConnection() {
        if (this.closedremotely == null) {
            throw new IllegalStateException("this method must be used in conjuction with flushAndClose");
        }
        closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
    }

    @Override // org.java_websocket.WebSocket
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
    }

    public void eot() {
        if (getReadyState() == WebSocket.READYSTATE.NOT_YET_CONNECTED) {
            closeConnection(-1, true);
        } else if (this.flushandclosestate) {
            closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
        } else if (this.draft.getCloseHandshakeType() == Draft.CloseHandshakeType.NONE) {
            closeConnection(1000, true);
        } else if (this.draft.getCloseHandshakeType() == Draft.CloseHandshakeType.ONEWAY) {
            if (this.role == WebSocket.Role.SERVER) {
                closeConnection(CloseFrame.ABNORMAL_CLOSE, true);
            } else {
                closeConnection(1000, true);
            }
        } else {
            closeConnection(CloseFrame.ABNORMAL_CLOSE, true);
        }
    }

    @Override // org.java_websocket.WebSocket
    public void close(int code) {
        close(code, "", false);
    }

    public void close(InvalidDataException e) {
        close(e.getCloseCode(), e.getMessage(), false);
    }

    @Override // org.java_websocket.WebSocket
    public void send(String text) throws WebsocketNotConnectedException {
        if (text == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        send(this.draft.createFrames(text, this.role == WebSocket.Role.CLIENT));
    }

    @Override // org.java_websocket.WebSocket
    public void send(ByteBuffer bytes) throws IllegalArgumentException, WebsocketNotConnectedException {
        if (bytes == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        send(this.draft.createFrames(bytes, this.role == WebSocket.Role.CLIENT));
    }

    @Override // org.java_websocket.WebSocket
    public void send(byte[] bytes) throws IllegalArgumentException, WebsocketNotConnectedException {
        send(ByteBuffer.wrap(bytes));
    }

    private void send(Collection<Framedata> frames) {
        if (!isOpen()) {
            throw new WebsocketNotConnectedException();
        }
        if (frames == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<ByteBuffer> outgoingFrames = new ArrayList<>();
        for (Framedata f : frames) {
            if (DEBUG) {
                System.out.println("send frame: " + f);
            }
            outgoingFrames.add(this.draft.createBinaryFrame(f));
        }
        write(outgoingFrames);
    }

    @Override // org.java_websocket.WebSocket
    public void sendFragmentedFrame(Framedata.Opcode op, ByteBuffer buffer, boolean fin) {
        send(this.draft.continuousFrame(op, buffer, fin));
    }

    @Override // org.java_websocket.WebSocket
    public void sendFrame(Collection<Framedata> frames) {
        send(frames);
    }

    @Override // org.java_websocket.WebSocket
    public void sendFrame(Framedata framedata) {
        send(Collections.singletonList(framedata));
    }

    @Override // org.java_websocket.WebSocket
    public void sendPing() throws NotYetConnectedException {
        if (this.pingFrame == null) {
            this.pingFrame = new PingFrame();
        }
        sendFrame(this.pingFrame);
    }

    @Override // org.java_websocket.WebSocket
    public boolean hasBufferedData() {
        return !this.outQueue.isEmpty();
    }

    public void startHandshake(ClientHandshakeBuilder handshakedata) throws InvalidHandshakeException {
        if (!$assertionsDisabled && getReadyState() == WebSocket.READYSTATE.CONNECTING) {
            throw new AssertionError("shall only be called once");
        }
        this.handshakerequest = this.draft.postProcessHandshakeRequestAsClient(handshakedata);
        this.resourceDescriptor = handshakedata.getResourceDescriptor();
        if (!$assertionsDisabled && this.resourceDescriptor == null) {
            throw new AssertionError();
        }
        try {
            this.wsl.onWebsocketHandshakeSentAsClient(this, this.handshakerequest);
            write(this.draft.createHandshake(this.handshakerequest, this.role));
        } catch (RuntimeException e) {
            this.wsl.onWebsocketError(this, e);
            throw new InvalidHandshakeException("rejected because of" + e);
        } catch (InvalidDataException e2) {
            throw new InvalidHandshakeException("Handshake data rejected by client.");
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

    @Override // org.java_websocket.WebSocket
    public boolean isConnecting() {
        if ($assertionsDisabled || !this.flushandclosestate || getReadyState() == WebSocket.READYSTATE.CONNECTING) {
            return getReadyState() == WebSocket.READYSTATE.CONNECTING;
        }
        throw new AssertionError();
    }

    @Override // org.java_websocket.WebSocket
    public boolean isOpen() {
        if (!$assertionsDisabled && getReadyState() == WebSocket.READYSTATE.OPEN && this.flushandclosestate) {
            throw new AssertionError();
        }
        return getReadyState() == WebSocket.READYSTATE.OPEN;
    }

    @Override // org.java_websocket.WebSocket
    public boolean isClosing() {
        return getReadyState() == WebSocket.READYSTATE.CLOSING;
    }

    @Override // org.java_websocket.WebSocket
    public boolean isFlushAndClose() {
        return this.flushandclosestate;
    }

    @Override // org.java_websocket.WebSocket
    public boolean isClosed() {
        return getReadyState() == WebSocket.READYSTATE.CLOSED;
    }

    @Override // org.java_websocket.WebSocket
    public WebSocket.READYSTATE getReadyState() {
        return this.readystate;
    }

    private void setReadyState(WebSocket.READYSTATE readystate) {
        this.readystate = readystate;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString();
    }

    @Override // org.java_websocket.WebSocket
    public InetSocketAddress getRemoteSocketAddress() {
        return this.wsl.getRemoteSocketAddress(this);
    }

    @Override // org.java_websocket.WebSocket
    public InetSocketAddress getLocalSocketAddress() {
        return this.wsl.getLocalSocketAddress(this);
    }

    @Override // org.java_websocket.WebSocket
    public Draft getDraft() {
        return this.draft;
    }

    @Override // org.java_websocket.WebSocket
    public void close() {
        close(1000);
    }

    @Override // org.java_websocket.WebSocket
    public String getResourceDescriptor() {
        return this.resourceDescriptor;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long getLastPong() {
        return this.lastPong;
    }

    public void updateLastPong() {
        this.lastPong = System.currentTimeMillis();
    }

    public WebSocketListener getWebSocketListener() {
        return this.wsl;
    }

    @Override // org.java_websocket.WebSocket
    public <T> T getAttachment() {
        return (T) this.attachment;
    }

    @Override // org.java_websocket.WebSocket
    public <T> void setAttachment(T attachment) {
        this.attachment = attachment;
    }
}