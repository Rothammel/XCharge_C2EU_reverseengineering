package org.java_websocket.drafts;

import android.support.p000v4.media.TransportMediator;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import org.apache.http.HttpHeaders;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.IncompleteException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.LimitExedeedException;
import org.java_websocket.exceptions.NotSendableException;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.BinaryFrame;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.framing.TextFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.util.Base64;
import org.java_websocket.util.Charsetfunctions;

public class Draft_6455 extends Draft {
    static final /* synthetic */ boolean $assertionsDisabled = (!Draft_6455.class.desiredAssertionStatus());
    private List<ByteBuffer> byteBufferList;
    private Framedata current_continuous_frame;
    private IExtension extension;
    private ByteBuffer incompleteframe;
    private List<IExtension> knownExtensions;
    private List<IProtocol> knownProtocols;
    private IProtocol protocol;
    private final Random reuseableRandom;

    public Draft_6455() {
        this((List<IExtension>) Collections.emptyList());
    }

    public Draft_6455(IExtension inputExtension) {
        this((List<IExtension>) Collections.singletonList(inputExtension));
    }

    public Draft_6455(List<IExtension> inputExtensions) {
        this(inputExtensions, Collections.singletonList(new Protocol("")));
    }

    public Draft_6455(List<IExtension> inputExtensions, List<IProtocol> inputProtocols) {
        this.extension = new DefaultExtension();
        this.reuseableRandom = new Random();
        if (inputExtensions == null || inputProtocols == null) {
            throw new IllegalArgumentException();
        }
        this.knownExtensions = new ArrayList(inputExtensions.size());
        this.knownProtocols = new ArrayList(inputProtocols.size());
        boolean hasDefault = false;
        this.byteBufferList = new ArrayList();
        for (IExtension inputExtension : inputExtensions) {
            if (inputExtension.getClass().equals(DefaultExtension.class)) {
                hasDefault = true;
            }
        }
        this.knownExtensions.addAll(inputExtensions);
        if (!hasDefault) {
            this.knownExtensions.add(this.knownExtensions.size(), this.extension);
        }
        this.knownProtocols.addAll(inputProtocols);
    }

    public Draft.HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata) throws InvalidHandshakeException {
        if (readVersion(handshakedata) != 13) {
            return Draft.HandshakeState.NOT_MATCHED;
        }
        Draft.HandshakeState extensionState = Draft.HandshakeState.NOT_MATCHED;
        String requestedExtension = handshakedata.getFieldValue("Sec-WebSocket-Extensions");
        Iterator<IExtension> it = this.knownExtensions.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            IExtension knownExtension = it.next();
            if (knownExtension.acceptProvidedExtensionAsServer(requestedExtension)) {
                this.extension = knownExtension;
                extensionState = Draft.HandshakeState.MATCHED;
                break;
            }
        }
        Draft.HandshakeState protocolState = Draft.HandshakeState.NOT_MATCHED;
        String requestedProtocol = handshakedata.getFieldValue("Sec-WebSocket-Protocol");
        Iterator<IProtocol> it2 = this.knownProtocols.iterator();
        while (true) {
            if (!it2.hasNext()) {
                break;
            }
            IProtocol knownProtocol = it2.next();
            if (knownProtocol.acceptProvidedProtocol(requestedProtocol)) {
                this.protocol = knownProtocol;
                protocolState = Draft.HandshakeState.MATCHED;
                break;
            }
        }
        if (protocolState == Draft.HandshakeState.MATCHED && extensionState == Draft.HandshakeState.MATCHED) {
            return Draft.HandshakeState.MATCHED;
        }
        return Draft.HandshakeState.NOT_MATCHED;
    }

    public Draft.HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) throws InvalidHandshakeException {
        if (!basicAccept(response)) {
            return Draft.HandshakeState.NOT_MATCHED;
        }
        if (!request.hasFieldValue("Sec-WebSocket-Key") || !response.hasFieldValue("Sec-WebSocket-Accept")) {
            return Draft.HandshakeState.NOT_MATCHED;
        }
        if (!generateFinalKey(request.getFieldValue("Sec-WebSocket-Key")).equals(response.getFieldValue("Sec-WebSocket-Accept"))) {
            return Draft.HandshakeState.NOT_MATCHED;
        }
        Draft.HandshakeState extensionState = Draft.HandshakeState.NOT_MATCHED;
        String requestedExtension = response.getFieldValue("Sec-WebSocket-Extensions");
        Iterator<IExtension> it = this.knownExtensions.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            IExtension knownExtension = it.next();
            if (knownExtension.acceptProvidedExtensionAsClient(requestedExtension)) {
                this.extension = knownExtension;
                extensionState = Draft.HandshakeState.MATCHED;
                break;
            }
        }
        Draft.HandshakeState protocolState = Draft.HandshakeState.NOT_MATCHED;
        String requestedProtocol = response.getFieldValue("Sec-WebSocket-Protocol");
        Iterator<IProtocol> it2 = this.knownProtocols.iterator();
        while (true) {
            if (!it2.hasNext()) {
                break;
            }
            IProtocol knownProtocol = it2.next();
            if (knownProtocol.acceptProvidedProtocol(requestedProtocol)) {
                this.protocol = knownProtocol;
                protocolState = Draft.HandshakeState.MATCHED;
                break;
            }
        }
        if (protocolState == Draft.HandshakeState.MATCHED && extensionState == Draft.HandshakeState.MATCHED) {
            return Draft.HandshakeState.MATCHED;
        }
        return Draft.HandshakeState.NOT_MATCHED;
    }

    public IExtension getExtension() {
        return this.extension;
    }

    public List<IExtension> getKnownExtensions() {
        return this.knownExtensions;
    }

    public IProtocol getProtocol() {
        return this.protocol;
    }

    public List<IProtocol> getKnownProtocols() {
        return this.knownProtocols;
    }

    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        request.put(HttpHeaders.UPGRADE, "websocket");
        request.put(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE);
        byte[] random = new byte[16];
        this.reuseableRandom.nextBytes(random);
        request.put("Sec-WebSocket-Key", Base64.encodeBytes(random));
        request.put("Sec-WebSocket-Version", "13");
        StringBuilder requestedExtensions = new StringBuilder();
        for (IExtension knownExtension : this.knownExtensions) {
            if (!(knownExtension.getProvidedExtensionAsClient() == null || knownExtension.getProvidedExtensionAsClient().length() == 0)) {
                if (requestedExtensions.length() > 0) {
                    requestedExtensions.append(", ");
                }
                requestedExtensions.append(knownExtension.getProvidedExtensionAsClient());
            }
        }
        if (requestedExtensions.length() != 0) {
            request.put("Sec-WebSocket-Extensions", requestedExtensions.toString());
        }
        StringBuilder requestedProtocols = new StringBuilder();
        for (IProtocol knownProtocol : this.knownProtocols) {
            if (knownProtocol.getProvidedProtocol().length() != 0) {
                if (requestedProtocols.length() > 0) {
                    requestedProtocols.append(", ");
                }
                requestedProtocols.append(knownProtocol.getProvidedProtocol());
            }
        }
        if (requestedProtocols.length() != 0) {
            request.put("Sec-WebSocket-Protocol", requestedProtocols.toString());
        }
        return request;
    }

    public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request, ServerHandshakeBuilder response) throws InvalidHandshakeException {
        response.put(HttpHeaders.UPGRADE, "websocket");
        response.put(HttpHeaders.CONNECTION, request.getFieldValue(HttpHeaders.CONNECTION));
        String seckey = request.getFieldValue("Sec-WebSocket-Key");
        if (seckey == null) {
            throw new InvalidHandshakeException("missing Sec-WebSocket-Key");
        }
        response.put("Sec-WebSocket-Accept", generateFinalKey(seckey));
        if (getExtension().getProvidedExtensionAsServer().length() != 0) {
            response.put("Sec-WebSocket-Extensions", getExtension().getProvidedExtensionAsServer());
        }
        if (!(getProtocol() == null || getProtocol().getProvidedProtocol().length() == 0)) {
            response.put("Sec-WebSocket-Protocol", getProtocol().getProvidedProtocol());
        }
        response.setHttpStatusMessage("Web Socket Protocol Handshake");
        response.put(HttpHeaders.SERVER, "TooTallNate Java-WebSocket");
        response.put("Date", getServerTime());
        return response;
    }

    public Draft copyInstance() {
        ArrayList<IExtension> newExtensions = new ArrayList<>();
        for (IExtension extension2 : getKnownExtensions()) {
            newExtensions.add(extension2.copyInstance());
        }
        ArrayList<IProtocol> newProtocols = new ArrayList<>();
        for (IProtocol protocol2 : getKnownProtocols()) {
            newProtocols.add(protocol2.copyInstance());
        }
        return new Draft_6455(newExtensions, newProtocols);
    }

    public ByteBuffer createBinaryFrame(Framedata framedata) {
        getExtension().encodeFrame(framedata);
        if (WebSocketImpl.DEBUG) {
            System.out.println("afterEnconding(" + framedata.getPayloadData().remaining() + "): {" + (framedata.getPayloadData().remaining() > 1000 ? "too big to display" : new String(framedata.getPayloadData().array())) + '}');
        }
        return createByteBufferFromFramedata(framedata);
    }

    private ByteBuffer createByteBufferFromFramedata(Framedata framedata) {
        int i;
        ByteBuffer mes = framedata.getPayloadData();
        boolean mask = this.role == WebSocket.Role.CLIENT;
        int sizebytes = mes.remaining() <= 125 ? 1 : mes.remaining() <= 65535 ? 2 : 8;
        if (sizebytes > 1) {
            i = sizebytes + 1;
        } else {
            i = sizebytes;
        }
        ByteBuffer buf = ByteBuffer.allocate((mask ? 4 : 0) + i + 1 + mes.remaining());
        buf.put((byte) (((byte) (framedata.isFin() ? -128 : 0)) | fromOpcode(framedata.getOpcode())));
        byte[] payloadlengthbytes = toByteArray((long) mes.remaining(), sizebytes);
        if ($assertionsDisabled || payloadlengthbytes.length == sizebytes) {
            if (sizebytes == 1) {
                buf.put((byte) ((mask ? AnyoMessage.CMD_RESET_CHARGE : 0) | payloadlengthbytes[0]));
            } else if (sizebytes == 2) {
                buf.put((byte) ((mask ? -128 : 0) | TransportMediator.KEYCODE_MEDIA_PLAY));
                buf.put(payloadlengthbytes);
            } else if (sizebytes == 8) {
                buf.put((byte) ((mask ? -128 : 0) | TransportMediator.KEYCODE_MEDIA_PAUSE));
                buf.put(payloadlengthbytes);
            } else {
                throw new RuntimeException("Size representation not supported/specified");
            }
            if (mask) {
                ByteBuffer maskkey = ByteBuffer.allocate(4);
                maskkey.putInt(this.reuseableRandom.nextInt());
                buf.put(maskkey.array());
                int i2 = 0;
                while (mes.hasRemaining()) {
                    buf.put((byte) (mes.get() ^ maskkey.get(i2 % 4)));
                    i2++;
                }
            } else {
                buf.put(mes);
                mes.flip();
            }
            if ($assertionsDisabled || buf.remaining() == 0) {
                buf.flip();
                return buf;
            }
            throw new AssertionError(buf.remaining());
        }
        throw new AssertionError();
    }

    public Framedata translateSingleFrame(ByteBuffer buffer) throws IncompleteException, InvalidDataException {
        int maxpacketsize = buffer.remaining();
        int realpacketsize = 2;
        if (maxpacketsize < 2) {
            throw new IncompleteException(2);
        }
        byte b1 = buffer.get();
        boolean FIN = (b1 >> 8) != 0;
        boolean rsv1 = false;
        boolean rsv2 = false;
        boolean rsv3 = false;
        if ((b1 & AnyoMessage.CMD_QUERY_CHARGE_SETTING) != 0) {
            rsv1 = true;
        }
        if ((b1 & 32) != 0) {
            rsv2 = true;
        }
        if ((b1 & 16) != 0) {
            rsv3 = true;
        }
        byte b2 = buffer.get();
        boolean MASK = (b2 & AnyoMessage.CMD_RESET_CHARGE) != 0;
        int payloadlength = (byte) (b2 & Byte.MAX_VALUE);
        Framedata.Opcode optcode = toOpcode((byte) (b1 & HeartBeatRequest.PORT_STATUS_FAULT));
        if (payloadlength < 0 || payloadlength > 125) {
            if (optcode == Framedata.Opcode.PING || optcode == Framedata.Opcode.PONG || optcode == Framedata.Opcode.CLOSING) {
                throw new InvalidFrameException("more than 125 octets");
            } else if (payloadlength == 126) {
                realpacketsize = 2 + 2;
                if (maxpacketsize < realpacketsize) {
                    throw new IncompleteException(realpacketsize);
                }
                byte[] sizebytes = new byte[3];
                sizebytes[1] = buffer.get();
                sizebytes[2] = buffer.get();
                payloadlength = new BigInteger(sizebytes).intValue();
            } else {
                realpacketsize = 2 + 8;
                if (maxpacketsize < realpacketsize) {
                    throw new IncompleteException(realpacketsize);
                }
                byte[] bytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    bytes[i] = buffer.get();
                }
                long length = new BigInteger(bytes).longValue();
                if (length > 2147483647L) {
                    throw new LimitExedeedException("Payloadsize is to big...");
                }
                payloadlength = (int) length;
            }
        }
        int realpacketsize2 = realpacketsize + (MASK ? 4 : 0) + payloadlength;
        if (maxpacketsize < realpacketsize2) {
            throw new IncompleteException(realpacketsize2);
        }
        ByteBuffer payload = ByteBuffer.allocate(checkAlloc(payloadlength));
        if (MASK) {
            byte[] maskskey = new byte[4];
            buffer.get(maskskey);
            for (int i2 = 0; i2 < payloadlength; i2++) {
                payload.put((byte) (buffer.get() ^ maskskey[i2 % 4]));
            }
        } else {
            payload.put(buffer.array(), buffer.position(), payload.limit());
            buffer.position(buffer.position() + payload.limit());
        }
        FramedataImpl1 frame = FramedataImpl1.get(optcode);
        frame.setFin(FIN);
        frame.setRSV1(rsv1);
        frame.setRSV2(rsv2);
        frame.setRSV3(rsv3);
        payload.flip();
        frame.setPayload(payload);
        getExtension().isFrameValid(frame);
        getExtension().decodeFrame(frame);
        if (WebSocketImpl.DEBUG) {
            System.out.println("afterDecoding(" + frame.getPayloadData().remaining() + "): {" + (frame.getPayloadData().remaining() > 1000 ? "too big to display" : new String(frame.getPayloadData().array())) + '}');
        }
        frame.isValid();
        return frame;
    }

    public List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException {
        List<Framedata> frames;
        while (true) {
            frames = new LinkedList<>();
            if (this.incompleteframe == null) {
                break;
            }
            try {
                buffer.mark();
                int available_next_byte_count = buffer.remaining();
                int expected_next_byte_count = this.incompleteframe.remaining();
                if (expected_next_byte_count > available_next_byte_count) {
                    this.incompleteframe.put(buffer.array(), buffer.position(), available_next_byte_count);
                    buffer.position(buffer.position() + available_next_byte_count);
                    return Collections.emptyList();
                }
                this.incompleteframe.put(buffer.array(), buffer.position(), expected_next_byte_count);
                buffer.position(buffer.position() + expected_next_byte_count);
                frames.add(translateSingleFrame((ByteBuffer) this.incompleteframe.duplicate().position(0)));
                this.incompleteframe = null;
            } catch (IncompleteException e) {
                ByteBuffer extendedframe = ByteBuffer.allocate(checkAlloc(e.getPreferredSize()));
                if ($assertionsDisabled || extendedframe.limit() > this.incompleteframe.limit()) {
                    this.incompleteframe.rewind();
                    extendedframe.put(this.incompleteframe);
                    this.incompleteframe = extendedframe;
                } else {
                    throw new AssertionError();
                }
            }
        }
        while (buffer.hasRemaining()) {
            buffer.mark();
            try {
                frames.add(translateSingleFrame(buffer));
            } catch (IncompleteException e2) {
                buffer.reset();
                this.incompleteframe = ByteBuffer.allocate(checkAlloc(e2.getPreferredSize()));
                this.incompleteframe.put(buffer);
                return frames;
            }
        }
        return frames;
    }

    public List<Framedata> createFrames(ByteBuffer binary, boolean mask) {
        BinaryFrame curframe = new BinaryFrame();
        curframe.setPayload(binary);
        curframe.setTransferemasked(mask);
        try {
            curframe.isValid();
            return Collections.singletonList(curframe);
        } catch (InvalidDataException e) {
            throw new NotSendableException((Throwable) e);
        }
    }

    public List<Framedata> createFrames(String text, boolean mask) {
        TextFrame curframe = new TextFrame();
        curframe.setPayload(ByteBuffer.wrap(Charsetfunctions.utf8Bytes(text)));
        curframe.setTransferemasked(mask);
        try {
            curframe.isValid();
            return Collections.singletonList(curframe);
        } catch (InvalidDataException e) {
            throw new NotSendableException((Throwable) e);
        }
    }

    public void reset() {
        this.incompleteframe = null;
        if (this.extension != null) {
            this.extension.reset();
        }
        this.extension = new DefaultExtension();
        this.protocol = null;
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private String generateFinalKey(String in) {
        try {
            return Base64.encodeBytes(MessageDigest.getInstance("SHA1").digest((in.trim() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = (bytecount * 8) - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) ((int) (val >>> (highest - (i * 8))));
        }
        return buffer;
    }

    private byte fromOpcode(Framedata.Opcode opcode) {
        if (opcode == Framedata.Opcode.CONTINUOUS) {
            return 0;
        }
        if (opcode == Framedata.Opcode.TEXT) {
            return 1;
        }
        if (opcode == Framedata.Opcode.BINARY) {
            return 2;
        }
        if (opcode == Framedata.Opcode.CLOSING) {
            return 8;
        }
        if (opcode == Framedata.Opcode.PING) {
            return 9;
        }
        if (opcode == Framedata.Opcode.PONG) {
            return 10;
        }
        throw new IllegalArgumentException("Don't know how to handle " + opcode.toString());
    }

    private Framedata.Opcode toOpcode(byte opcode) throws InvalidFrameException {
        switch (opcode) {
            case 0:
                return Framedata.Opcode.CONTINUOUS;
            case 1:
                return Framedata.Opcode.TEXT;
            case 2:
                return Framedata.Opcode.BINARY;
            case 8:
                return Framedata.Opcode.CLOSING;
            case 9:
                return Framedata.Opcode.PING;
            case 10:
                return Framedata.Opcode.PONG;
            default:
                throw new InvalidFrameException("Unknown opcode " + ((short) opcode));
        }
    }

    public void processFrame(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
        Framedata.Opcode curop = frame.getOpcode();
        if (curop == Framedata.Opcode.CLOSING) {
            int code = CloseFrame.NOCODE;
            String reason = "";
            if (frame instanceof CloseFrame) {
                CloseFrame cf = (CloseFrame) frame;
                code = cf.getCloseCode();
                reason = cf.getMessage();
            }
            if (webSocketImpl.getReadyState() == WebSocket.READYSTATE.CLOSING) {
                webSocketImpl.closeConnection(code, reason, true);
            } else if (getCloseHandshakeType() == Draft.CloseHandshakeType.TWOWAY) {
                webSocketImpl.close(code, reason, true);
            } else {
                webSocketImpl.flushAndClose(code, reason, false);
            }
        } else if (curop == Framedata.Opcode.PING) {
            webSocketImpl.getWebSocketListener().onWebsocketPing(webSocketImpl, frame);
        } else if (curop == Framedata.Opcode.PONG) {
            webSocketImpl.updateLastPong();
            webSocketImpl.getWebSocketListener().onWebsocketPong(webSocketImpl, frame);
        } else if (!frame.isFin() || curop == Framedata.Opcode.CONTINUOUS) {
            if (curop != Framedata.Opcode.CONTINUOUS) {
                if (this.current_continuous_frame != null) {
                    throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Previous continuous frame sequence not completed.");
                }
                this.current_continuous_frame = frame;
                this.byteBufferList.add(frame.getPayloadData());
            } else if (frame.isFin()) {
                if (this.current_continuous_frame == null) {
                    throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence was not started.");
                }
                this.byteBufferList.add(frame.getPayloadData());
                if (this.current_continuous_frame.getOpcode() == Framedata.Opcode.TEXT) {
                    ((FramedataImpl1) this.current_continuous_frame).setPayload(getPayloadFromByteBufferList());
                    ((FramedataImpl1) this.current_continuous_frame).isValid();
                    try {
                        webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket) webSocketImpl, Charsetfunctions.stringUtf8(this.current_continuous_frame.getPayloadData()));
                    } catch (RuntimeException e) {
                        webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
                    }
                } else if (this.current_continuous_frame.getOpcode() == Framedata.Opcode.BINARY) {
                    ((FramedataImpl1) this.current_continuous_frame).setPayload(getPayloadFromByteBufferList());
                    ((FramedataImpl1) this.current_continuous_frame).isValid();
                    try {
                        webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket) webSocketImpl, this.current_continuous_frame.getPayloadData());
                    } catch (RuntimeException e2) {
                        webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e2);
                    }
                }
                this.current_continuous_frame = null;
                this.byteBufferList.clear();
            } else if (this.current_continuous_frame == null) {
                throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence was not started.");
            }
            if (curop == Framedata.Opcode.TEXT && !Charsetfunctions.isValidUTF8(frame.getPayloadData())) {
                throw new InvalidDataException(CloseFrame.NO_UTF8);
            } else if (curop == Framedata.Opcode.CONTINUOUS && this.current_continuous_frame != null) {
                this.byteBufferList.add(frame.getPayloadData());
            }
        } else if (this.current_continuous_frame != null) {
            throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence not completed.");
        } else if (curop == Framedata.Opcode.TEXT) {
            try {
                webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket) webSocketImpl, Charsetfunctions.stringUtf8(frame.getPayloadData()));
            } catch (RuntimeException e3) {
                webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e3);
            }
        } else if (curop == Framedata.Opcode.BINARY) {
            try {
                webSocketImpl.getWebSocketListener().onWebsocketMessage((WebSocket) webSocketImpl, frame.getPayloadData());
            } catch (RuntimeException e4) {
                webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e4);
            }
        } else {
            throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "non control or continious frame expected");
        }
    }

    public Draft.CloseHandshakeType getCloseHandshakeType() {
        return Draft.CloseHandshakeType.TWOWAY;
    }

    public String toString() {
        String result = super.toString();
        if (getExtension() != null) {
            result = result + " extension: " + getExtension().toString();
        }
        if (getProtocol() != null) {
            return result + " protocol: " + getProtocol().toString();
        }
        return result;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Draft_6455 that = (Draft_6455) o;
        if (this.extension == null ? that.extension != null : !this.extension.equals(that.extension)) {
            return false;
        }
        if (this.protocol != null) {
            return this.protocol.equals(that.protocol);
        }
        if (that.protocol != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.extension != null) {
            result = this.extension.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.protocol != null) {
            i = this.protocol.hashCode();
        }
        return i2 + i;
    }

    private ByteBuffer getPayloadFromByteBufferList() throws LimitExedeedException {
        long totalSize = 0;
        for (ByteBuffer buffer : this.byteBufferList) {
            totalSize += (long) buffer.limit();
        }
        if (totalSize > 2147483647L) {
            throw new LimitExedeedException("Payloadsize is to big...");
        }
        ByteBuffer resultingByteBuffer = ByteBuffer.allocate((int) totalSize);
        for (ByteBuffer buffer2 : this.byteBufferList) {
            resultingByteBuffer.put(buffer2);
        }
        resultingByteBuffer.flip();
        return resultingByteBuffer;
    }
}
