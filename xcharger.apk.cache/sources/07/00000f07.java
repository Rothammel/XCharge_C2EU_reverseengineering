package org.java_websocket;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.framing.PongFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

/* loaded from: classes.dex */
public abstract class WebSocketAdapter implements WebSocketListener {
    @Override // org.java_websocket.WebSocketListener
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return new HandshakeImpl1Server();
    }

    @Override // org.java_websocket.WebSocketListener
    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {
    }

    @Override // org.java_websocket.WebSocketListener
    public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
    }

    @Override // org.java_websocket.WebSocketListener
    @Deprecated
    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
    }

    @Override // org.java_websocket.WebSocketListener
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        conn.sendFrame(new PongFrame((PingFrame) f));
    }

    @Override // org.java_websocket.WebSocketListener
    public void onWebsocketPong(WebSocket conn, Framedata f) {
    }
}