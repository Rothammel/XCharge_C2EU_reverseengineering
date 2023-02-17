package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import org.java_websocket.util.ByteBufferUtils;

public class SSLSocketChannel implements WrappedByteChannel, ByteChannel {
    private final SSLEngine engine;
    private ExecutorService executor;
    private ByteBuffer myAppData;
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private ByteBuffer peerNetData;
    private final SocketChannel socketChannel;

    public SSLSocketChannel(SocketChannel inputSocketChannel, SSLEngine inputEngine, ExecutorService inputExecutor, SelectionKey key) throws IOException {
        if (inputSocketChannel == null || inputEngine == null || this.executor == inputExecutor) {
            throw new IllegalArgumentException("parameter must not be null");
        }
        this.socketChannel = inputSocketChannel;
        this.engine = inputEngine;
        this.executor = inputExecutor;
        this.myNetData = ByteBuffer.allocate(this.engine.getSession().getPacketBufferSize());
        this.peerNetData = ByteBuffer.allocate(this.engine.getSession().getPacketBufferSize());
        this.engine.beginHandshake();
        if (!doHandshake()) {
            try {
                this.socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (key != null) {
            key.interestOps(key.interestOps() | 4);
        }
    }

    public synchronized int read(ByteBuffer dst) throws IOException {
        int bytesRead;
        if (!dst.hasRemaining()) {
            bytesRead = 0;
        } else if (this.peerAppData.hasRemaining()) {
            this.peerAppData.flip();
            bytesRead = ByteBufferUtils.transferByteBuffer(this.peerAppData, dst);
        } else {
            this.peerNetData.compact();
            bytesRead = this.socketChannel.read(this.peerNetData);
            if (bytesRead > 0 || this.peerNetData.hasRemaining()) {
                this.peerNetData.flip();
                while (true) {
                    if (this.peerNetData.hasRemaining()) {
                        this.peerAppData.compact();
                        try {
                            SSLEngineResult result = this.engine.unwrap(this.peerNetData, this.peerAppData);
                            switch (C06201.$SwitchMap$javax$net$ssl$SSLEngineResult$Status[result.getStatus().ordinal()]) {
                                case 1:
                                    this.peerAppData.flip();
                                    bytesRead = ByteBufferUtils.transferByteBuffer(this.peerAppData, dst);
                                    break;
                                case 2:
                                    this.peerAppData.flip();
                                    bytesRead = ByteBufferUtils.transferByteBuffer(this.peerAppData, dst);
                                    break;
                                case 3:
                                    this.peerAppData = enlargeApplicationBuffer(this.peerAppData);
                                case 4:
                                    closeConnection();
                                    dst.clear();
                                    bytesRead = -1;
                                    break;
                                default:
                                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                            }
                        } catch (SSLException e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                }
            } else if (bytesRead < 0) {
                handleEndOfStream();
            }
            ByteBufferUtils.transferByteBuffer(this.peerAppData, dst);
        }
        return bytesRead;
    }

    public synchronized int write(ByteBuffer output) throws IOException {
        int num;
        num = 0;
        while (true) {
            if (output.hasRemaining()) {
                this.myNetData.clear();
                SSLEngineResult result = this.engine.wrap(output, this.myNetData);
                switch (C06201.$SwitchMap$javax$net$ssl$SSLEngineResult$Status[result.getStatus().ordinal()]) {
                    case 1:
                        this.myNetData.flip();
                        while (this.myNetData.hasRemaining()) {
                            num += this.socketChannel.write(this.myNetData);
                        }
                        continue;
                    case 2:
                        throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                    case 3:
                        this.myNetData = enlargePacketBuffer(this.myNetData);
                        continue;
                    case 4:
                        closeConnection();
                        num = 0;
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }
        }
        return num;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0199, code lost:
        if (r5 == null) goto L_0x01a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01a1, code lost:
        r2 = r10.engine.getHandshakeStatus();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doHandshake() throws java.io.IOException {
        /*
            r10 = this;
            r6 = 0
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLSession r7 = r7.getSession()
            int r0 = r7.getApplicationBufferSize()
            java.nio.ByteBuffer r7 = java.nio.ByteBuffer.allocate(r0)
            r10.myAppData = r7
            java.nio.ByteBuffer r7 = java.nio.ByteBuffer.allocate(r0)
            r10.peerAppData = r7
            java.nio.ByteBuffer r7 = r10.myNetData
            r7.clear()
            java.nio.ByteBuffer r7 = r10.peerNetData
            r7.clear()
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
        L_0x0027:
            javax.net.ssl.SSLEngineResult$HandshakeStatus r7 = javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED
            if (r2 == r7) goto L_0x01a9
            javax.net.ssl.SSLEngineResult$HandshakeStatus r7 = javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
            if (r2 == r7) goto L_0x01a9
            int[] r7 = org.java_websocket.SSLSocketChannel.C06201.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus
            int r8 = r2.ordinal()
            r7 = r7[r8]
            switch(r7) {
                case 1: goto L_0x0053;
                case 2: goto L_0x00fa;
                case 3: goto L_0x0193;
                case 4: goto L_0x0027;
                case 5: goto L_0x0027;
                default: goto L_0x003a;
            }
        L_0x003a:
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Invalid SSL status: "
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.StringBuilder r7 = r7.append(r2)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x0053:
            java.nio.channels.SocketChannel r7 = r10.socketChannel
            java.nio.ByteBuffer r8 = r10.peerNetData
            int r7 = r7.read(r8)
            if (r7 >= 0) goto L_0x007f
            javax.net.ssl.SSLEngine r7 = r10.engine
            boolean r7 = r7.isInboundDone()
            if (r7 == 0) goto L_0x006e
            javax.net.ssl.SSLEngine r7 = r10.engine
            boolean r7 = r7.isOutboundDone()
            if (r7 == 0) goto L_0x006e
        L_0x006d:
            return r6
        L_0x006e:
            javax.net.ssl.SSLEngine r7 = r10.engine     // Catch:{ SSLException -> 0x01ac }
            r7.closeInbound()     // Catch:{ SSLException -> 0x01ac }
        L_0x0073:
            javax.net.ssl.SSLEngine r7 = r10.engine
            r7.closeOutbound()
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
            goto L_0x0027
        L_0x007f:
            java.nio.ByteBuffer r7 = r10.peerNetData
            r7.flip()
            javax.net.ssl.SSLEngine r7 = r10.engine     // Catch:{ SSLException -> 0x00c3 }
            java.nio.ByteBuffer r8 = r10.peerNetData     // Catch:{ SSLException -> 0x00c3 }
            java.nio.ByteBuffer r9 = r10.peerAppData     // Catch:{ SSLException -> 0x00c3 }
            javax.net.ssl.SSLEngineResult r3 = r7.unwrap(r8, r9)     // Catch:{ SSLException -> 0x00c3 }
            java.nio.ByteBuffer r7 = r10.peerNetData     // Catch:{ SSLException -> 0x00c3 }
            r7.compact()     // Catch:{ SSLException -> 0x00c3 }
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r3.getHandshakeStatus()     // Catch:{ SSLException -> 0x00c3 }
            int[] r7 = org.java_websocket.SSLSocketChannel.C06201.$SwitchMap$javax$net$ssl$SSLEngineResult$Status
            javax.net.ssl.SSLEngineResult$Status r8 = r3.getStatus()
            int r8 = r8.ordinal()
            r7 = r7[r8]
            switch(r7) {
                case 1: goto L_0x0027;
                case 2: goto L_0x00db;
                case 3: goto L_0x00d1;
                case 4: goto L_0x00e5;
                default: goto L_0x00a6;
            }
        L_0x00a6:
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Invalid SSL status: "
            java.lang.StringBuilder r7 = r7.append(r8)
            javax.net.ssl.SSLEngineResult$Status r8 = r3.getStatus()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x00c3:
            r4 = move-exception
            javax.net.ssl.SSLEngine r7 = r10.engine
            r7.closeOutbound()
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
            goto L_0x0027
        L_0x00d1:
            java.nio.ByteBuffer r7 = r10.peerAppData
            java.nio.ByteBuffer r7 = r10.enlargeApplicationBuffer(r7)
            r10.peerAppData = r7
            goto L_0x0027
        L_0x00db:
            java.nio.ByteBuffer r7 = r10.peerNetData
            java.nio.ByteBuffer r7 = r10.handleBufferUnderflow(r7)
            r10.peerNetData = r7
            goto L_0x0027
        L_0x00e5:
            javax.net.ssl.SSLEngine r7 = r10.engine
            boolean r7 = r7.isOutboundDone()
            if (r7 != 0) goto L_0x006d
            javax.net.ssl.SSLEngine r7 = r10.engine
            r7.closeOutbound()
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
            goto L_0x0027
        L_0x00fa:
            java.nio.ByteBuffer r7 = r10.myNetData
            r7.clear()
            javax.net.ssl.SSLEngine r7 = r10.engine     // Catch:{ SSLException -> 0x0139 }
            java.nio.ByteBuffer r8 = r10.myAppData     // Catch:{ SSLException -> 0x0139 }
            java.nio.ByteBuffer r9 = r10.myNetData     // Catch:{ SSLException -> 0x0139 }
            javax.net.ssl.SSLEngineResult r3 = r7.wrap(r8, r9)     // Catch:{ SSLException -> 0x0139 }
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r3.getHandshakeStatus()     // Catch:{ SSLException -> 0x0139 }
            int[] r7 = org.java_websocket.SSLSocketChannel.C06201.$SwitchMap$javax$net$ssl$SSLEngineResult$Status
            javax.net.ssl.SSLEngineResult$Status r8 = r3.getStatus()
            int r8 = r8.ordinal()
            r7 = r7[r8]
            switch(r7) {
                case 1: goto L_0x0147;
                case 2: goto L_0x0166;
                case 3: goto L_0x015c;
                case 4: goto L_0x016e;
                default: goto L_0x011c;
            }
        L_0x011c:
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Invalid SSL status: "
            java.lang.StringBuilder r7 = r7.append(r8)
            javax.net.ssl.SSLEngineResult$Status r8 = r3.getStatus()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x0139:
            r4 = move-exception
            javax.net.ssl.SSLEngine r7 = r10.engine
            r7.closeOutbound()
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
            goto L_0x0027
        L_0x0147:
            java.nio.ByteBuffer r7 = r10.myNetData
            r7.flip()
        L_0x014c:
            java.nio.ByteBuffer r7 = r10.myNetData
            boolean r7 = r7.hasRemaining()
            if (r7 == 0) goto L_0x0027
            java.nio.channels.SocketChannel r7 = r10.socketChannel
            java.nio.ByteBuffer r8 = r10.myNetData
            r7.write(r8)
            goto L_0x014c
        L_0x015c:
            java.nio.ByteBuffer r7 = r10.myNetData
            java.nio.ByteBuffer r7 = r10.enlargePacketBuffer(r7)
            r10.myNetData = r7
            goto L_0x0027
        L_0x0166:
            javax.net.ssl.SSLException r6 = new javax.net.ssl.SSLException
            java.lang.String r7 = "Buffer underflow occured after a wrap. I don't think we should ever get here."
            r6.<init>(r7)
            throw r6
        L_0x016e:
            java.nio.ByteBuffer r7 = r10.myNetData     // Catch:{ Exception -> 0x0183 }
            r7.flip()     // Catch:{ Exception -> 0x0183 }
        L_0x0173:
            java.nio.ByteBuffer r7 = r10.myNetData     // Catch:{ Exception -> 0x0183 }
            boolean r7 = r7.hasRemaining()     // Catch:{ Exception -> 0x0183 }
            if (r7 == 0) goto L_0x018c
            java.nio.channels.SocketChannel r7 = r10.socketChannel     // Catch:{ Exception -> 0x0183 }
            java.nio.ByteBuffer r8 = r10.myNetData     // Catch:{ Exception -> 0x0183 }
            r7.write(r8)     // Catch:{ Exception -> 0x0183 }
            goto L_0x0173
        L_0x0183:
            r1 = move-exception
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
            goto L_0x0027
        L_0x018c:
            java.nio.ByteBuffer r7 = r10.peerNetData     // Catch:{ Exception -> 0x0183 }
            r7.clear()     // Catch:{ Exception -> 0x0183 }
            goto L_0x0027
        L_0x0193:
            javax.net.ssl.SSLEngine r7 = r10.engine
            java.lang.Runnable r5 = r7.getDelegatedTask()
            if (r5 == 0) goto L_0x01a1
            java.util.concurrent.ExecutorService r7 = r10.executor
            r7.execute(r5)
            goto L_0x0193
        L_0x01a1:
            javax.net.ssl.SSLEngine r7 = r10.engine
            javax.net.ssl.SSLEngineResult$HandshakeStatus r2 = r7.getHandshakeStatus()
            goto L_0x0027
        L_0x01a9:
            r6 = 1
            goto L_0x006d
        L_0x01ac:
            r7 = move-exception
            goto L_0x0073
        */
        throw new UnsupportedOperationException("Method not decompiled: org.java_websocket.SSLSocketChannel.doHandshake():boolean");
    }

    /* renamed from: org.java_websocket.SSLSocketChannel$1 */
    static /* synthetic */ class C06201 {
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus = new int[SSLEngineResult.HandshakeStatus.values().length];
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$Status = new int[SSLEngineResult.Status.values().length];

        static {
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_UNWRAP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_WRAP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_TASK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.FINISHED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.OK.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.BUFFER_UNDERFLOW.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.BUFFER_OVERFLOW.ordinal()] = 3;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.CLOSED.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    private ByteBuffer enlargePacketBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, this.engine.getSession().getPacketBufferSize());
    }

    private ByteBuffer enlargeApplicationBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, this.engine.getSession().getApplicationBufferSize());
    }

    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            return ByteBuffer.allocate(sessionProposedCapacity);
        }
        return ByteBuffer.allocate(buffer.capacity() * 2);
    }

    private ByteBuffer handleBufferUnderflow(ByteBuffer buffer) {
        if (this.engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        }
        ByteBuffer replaceBuffer = enlargePacketBuffer(buffer);
        buffer.flip();
        replaceBuffer.put(buffer);
        return replaceBuffer;
    }

    private void closeConnection() throws IOException {
        this.engine.closeOutbound();
        try {
            doHandshake();
        } catch (IOException e) {
        }
        this.socketChannel.close();
    }

    private void handleEndOfStream() throws IOException {
        try {
            this.engine.closeInbound();
        } catch (Exception e) {
            System.err.println("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
        }
        closeConnection();
    }

    public boolean isNeedWrite() {
        return false;
    }

    public void writeMore() throws IOException {
    }

    public boolean isNeedRead() {
        return this.peerNetData.hasRemaining() || this.peerAppData.hasRemaining();
    }

    public int readMore(ByteBuffer dst) throws IOException {
        return read(dst);
    }

    public boolean isBlocking() {
        return this.socketChannel.isBlocking();
    }

    public boolean isOpen() {
        return this.socketChannel.isOpen();
    }

    public void close() throws IOException {
        closeConnection();
    }
}
