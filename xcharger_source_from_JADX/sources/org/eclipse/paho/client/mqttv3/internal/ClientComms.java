package org.eclipse.paho.client.mqttv3.internal;

import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.BufferedMessage;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnack;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttDisconnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class ClientComms {
    public static String BUILD_LEVEL = "L${build.level}";
    /* access modifiers changed from: private */
    public static final String CLASS_NAME = ClientComms.class.getName();
    private static final byte CLOSED = 4;
    private static final byte CONNECTED = 0;
    private static final byte CONNECTING = 1;
    private static final byte DISCONNECTED = 3;
    private static final byte DISCONNECTING = 2;
    public static String VERSION = "${project.version}";
    /* access modifiers changed from: private */
    public static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    /* access modifiers changed from: private */
    public CommsCallback callback;
    private IMqttAsyncClient client;
    /* access modifiers changed from: private */
    public ClientState clientState;
    private boolean closePending;
    private Object conLock;
    private MqttConnectOptions conOptions;
    private byte conState;
    private DisconnectedMessageBuffer disconnectedMessageBuffer;
    /* access modifiers changed from: private */
    public ExecutorService executorService;
    /* access modifiers changed from: private */
    public int networkModuleIndex;
    /* access modifiers changed from: private */
    public NetworkModule[] networkModules;
    private MqttClientPersistence persistence;
    private MqttPingSender pingSender;
    /* access modifiers changed from: private */
    public CommsReceiver receiver;
    private boolean resting;
    /* access modifiers changed from: private */
    public CommsSender sender;
    private boolean stoppingComms;
    /* access modifiers changed from: private */
    public CommsTokenStore tokenStore;

    public ClientComms(IMqttAsyncClient client2, MqttClientPersistence persistence2, MqttPingSender pingSender2, ExecutorService executorService2) throws MqttException {
        this.stoppingComms = false;
        this.conState = 3;
        this.conLock = new Object();
        this.closePending = false;
        this.resting = false;
        this.conState = 3;
        this.client = client2;
        this.persistence = persistence2;
        this.pingSender = pingSender2;
        this.pingSender.init(this);
        this.executorService = executorService2;
        this.tokenStore = new CommsTokenStore(getClient().getClientId());
        this.callback = new CommsCallback(this);
        this.clientState = new ClientState(persistence2, this.tokenStore, this.callback, this, pingSender2);
        this.callback.setClientState(this.clientState);
        log.setResourceName(getClient().getClientId());
    }

    /* access modifiers changed from: package-private */
    public CommsReceiver getReceiver() {
        return this.receiver;
    }

    private void shutdownExecutorService() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
                if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    log.fine(CLASS_NAME, "shutdownExecutorService", "executorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /* access modifiers changed from: package-private */
    public void internalSend(MqttWireMessage message, MqttToken token) throws MqttException {
        log.fine(CLASS_NAME, "internalSend", "200", new Object[]{message.getKey(), message, token});
        if (token.getClient() == null) {
            token.internalTok.setClient(getClient());
            try {
                this.clientState.send(message, token);
            } catch (MqttException e) {
                if (message instanceof MqttPublish) {
                    this.clientState.undo((MqttPublish) message);
                }
                throw e;
            }
        } else {
            log.fine(CLASS_NAME, "internalSend", "213", new Object[]{message.getKey(), message, token});
            throw new MqttException(32201);
        }
    }

    public void sendNoWait(MqttWireMessage message, MqttToken token) throws MqttException {
        if (isConnected() || ((!isConnected() && (message instanceof MqttConnect)) || (isDisconnecting() && (message instanceof MqttDisconnect)))) {
            if (this.disconnectedMessageBuffer == null || this.disconnectedMessageBuffer.getMessageCount() == 0) {
                internalSend(message, token);
                return;
            }
            log.fine(CLASS_NAME, "sendNoWait", "507", new Object[]{message.getKey()});
            if (this.disconnectedMessageBuffer.isPersistBuffer()) {
                this.clientState.persistBufferedMessage(message);
            }
            this.disconnectedMessageBuffer.putMessage(message, token);
        } else if (this.disconnectedMessageBuffer != null) {
            log.fine(CLASS_NAME, "sendNoWait", "508", new Object[]{message.getKey()});
            if (this.disconnectedMessageBuffer.isPersistBuffer()) {
                this.clientState.persistBufferedMessage(message);
            }
            this.disconnectedMessageBuffer.putMessage(message, token);
        } else {
            log.fine(CLASS_NAME, "sendNoWait", "208");
            throw ExceptionHelper.createMqttException(32104);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close(boolean r6) throws org.eclipse.paho.client.mqttv3.MqttException {
        /*
            r5 = this;
            java.lang.Object r1 = r5.conLock
            monitor-enter(r1)
            boolean r0 = r5.isClosed()     // Catch:{ all -> 0x002a }
            if (r0 != 0) goto L_0x006b
            boolean r0 = r5.isDisconnected()     // Catch:{ all -> 0x002a }
            if (r0 == 0) goto L_0x0011
            if (r6 == 0) goto L_0x0045
        L_0x0011:
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = log     // Catch:{ all -> 0x002a }
            java.lang.String r2 = CLASS_NAME     // Catch:{ all -> 0x002a }
            java.lang.String r3 = "close"
            java.lang.String r4 = "224"
            r0.fine(r2, r3, r4)     // Catch:{ all -> 0x002a }
            boolean r0 = r5.isConnecting()     // Catch:{ all -> 0x002a }
            if (r0 == 0) goto L_0x002d
            org.eclipse.paho.client.mqttv3.MqttException r0 = new org.eclipse.paho.client.mqttv3.MqttException     // Catch:{ all -> 0x002a }
            r2 = 32110(0x7d6e, float:4.4996E-41)
            r0.<init>((int) r2)     // Catch:{ all -> 0x002a }
            throw r0     // Catch:{ all -> 0x002a }
        L_0x002a:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x002a }
            throw r0
        L_0x002d:
            boolean r0 = r5.isConnected()     // Catch:{ all -> 0x002a }
            if (r0 == 0) goto L_0x003a
            r0 = 32100(0x7d64, float:4.4982E-41)
            org.eclipse.paho.client.mqttv3.MqttException r0 = org.eclipse.paho.client.mqttv3.internal.ExceptionHelper.createMqttException((int) r0)     // Catch:{ all -> 0x002a }
            throw r0     // Catch:{ all -> 0x002a }
        L_0x003a:
            boolean r0 = r5.isDisconnecting()     // Catch:{ all -> 0x002a }
            if (r0 == 0) goto L_0x0045
            r0 = 1
            r5.closePending = r0     // Catch:{ all -> 0x002a }
            monitor-exit(r1)     // Catch:{ all -> 0x002a }
        L_0x0044:
            return
        L_0x0045:
            r0 = 4
            r5.conState = r0     // Catch:{ all -> 0x002a }
            r5.shutdownExecutorService()     // Catch:{ all -> 0x002a }
            org.eclipse.paho.client.mqttv3.internal.ClientState r0 = r5.clientState     // Catch:{ all -> 0x002a }
            r0.close()     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.clientState = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.callback = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.persistence = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.sender = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.pingSender = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.receiver = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.networkModules = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.conOptions = r0     // Catch:{ all -> 0x002a }
            r0 = 0
            r5.tokenStore = r0     // Catch:{ all -> 0x002a }
        L_0x006b:
            monitor-exit(r1)     // Catch:{ all -> 0x002a }
            goto L_0x0044
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.ClientComms.close(boolean):void");
    }

    public void connect(MqttConnectOptions options, MqttToken token) throws MqttException {
        synchronized (this.conLock) {
            if (!isDisconnected() || this.closePending) {
                log.fine(CLASS_NAME, "connect", "207", new Object[]{new Byte(this.conState)});
                if (isClosed() || this.closePending) {
                    throw new MqttException(32111);
                } else if (isConnecting()) {
                    throw new MqttException(32110);
                } else if (isDisconnecting()) {
                    throw new MqttException(32102);
                } else {
                    throw ExceptionHelper.createMqttException(32100);
                }
            } else {
                log.fine(CLASS_NAME, "connect", "214");
                this.conState = 1;
                this.conOptions = options;
                MqttConnect connect = new MqttConnect(this.client.getClientId(), this.conOptions.getMqttVersion(), this.conOptions.isCleanSession(), this.conOptions.getKeepAliveInterval(), this.conOptions.getUserName(), this.conOptions.getPassword(), this.conOptions.getWillMessage(), this.conOptions.getWillDestination());
                this.clientState.setKeepAliveSecs((long) this.conOptions.getKeepAliveInterval());
                this.clientState.setCleanSession(this.conOptions.isCleanSession());
                this.clientState.setMaxInflight(this.conOptions.getMaxInflight());
                this.tokenStore.open();
                new ConnectBG(this, token, connect, this.executorService).start();
            }
        }
    }

    public void connectComplete(MqttConnack cack, MqttException mex) throws MqttException {
        int rc = cack.getReturnCode();
        synchronized (this.conLock) {
            if (rc == 0) {
                log.fine(CLASS_NAME, "connectComplete", "215");
                this.conState = 0;
                return;
            }
            log.fine(CLASS_NAME, "connectComplete", "204", new Object[]{new Integer(rc)});
            throw mex;
        }
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processHandlersOutBlocks(RegionMaker.java:1008)
        	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:978)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
        */
    public void shutdownConnection(org.eclipse.paho.client.mqttv3.MqttToken r11, org.eclipse.paho.client.mqttv3.MqttException r12) {
        /*
            r10 = this;
            r5 = 0
            r4 = 1
            r0 = 0
            java.lang.Object r6 = r10.conLock
            monitor-enter(r6)
            boolean r3 = r10.stoppingComms     // Catch:{ all -> 0x00e7 }
            if (r3 != 0) goto L_0x0014
            boolean r3 = r10.closePending     // Catch:{ all -> 0x00e7 }
            if (r3 != 0) goto L_0x0014
            boolean r3 = r10.isClosed()     // Catch:{ all -> 0x00e7 }
            if (r3 == 0) goto L_0x0016
        L_0x0014:
            monitor-exit(r6)     // Catch:{ all -> 0x00e7 }
        L_0x0015:
            return
        L_0x0016:
            r3 = 1
            r10.stoppingComms = r3     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.logging.Logger r3 = log     // Catch:{ all -> 0x00e7 }
            java.lang.String r7 = CLASS_NAME     // Catch:{ all -> 0x00e7 }
            java.lang.String r8 = "shutdownConnection"
            java.lang.String r9 = "216"
            r3.fine(r7, r8, r9)     // Catch:{ all -> 0x00e7 }
            boolean r3 = r10.isConnected()     // Catch:{ all -> 0x00e7 }
            if (r3 != 0) goto L_0x00e4
            boolean r3 = r10.isDisconnecting()     // Catch:{ all -> 0x00e7 }
            if (r3 != 0) goto L_0x00e4
            r2 = r5
        L_0x0031:
            r3 = 2
            r10.conState = r3     // Catch:{ all -> 0x00e7 }
            monitor-exit(r6)     // Catch:{ all -> 0x00e7 }
            if (r11 == 0) goto L_0x0042
            boolean r3 = r11.isComplete()
            if (r3 != 0) goto L_0x0042
            org.eclipse.paho.client.mqttv3.internal.Token r3 = r11.internalTok
            r3.setException(r12)
        L_0x0042:
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r3 = r10.callback
            if (r3 == 0) goto L_0x004b
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r3 = r10.callback
            r3.stop()
        L_0x004b:
            org.eclipse.paho.client.mqttv3.internal.CommsReceiver r3 = r10.receiver
            if (r3 == 0) goto L_0x0054
            org.eclipse.paho.client.mqttv3.internal.CommsReceiver r3 = r10.receiver
            r3.stop()
        L_0x0054:
            org.eclipse.paho.client.mqttv3.internal.NetworkModule[] r3 = r10.networkModules     // Catch:{ Exception -> 0x00f7 }
            if (r3 == 0) goto L_0x0063
            org.eclipse.paho.client.mqttv3.internal.NetworkModule[] r3 = r10.networkModules     // Catch:{ Exception -> 0x00f7 }
            int r6 = r10.networkModuleIndex     // Catch:{ Exception -> 0x00f7 }
            r1 = r3[r6]     // Catch:{ Exception -> 0x00f7 }
            if (r1 == 0) goto L_0x0063
            r1.stop()     // Catch:{ Exception -> 0x00f7 }
        L_0x0063:
            org.eclipse.paho.client.mqttv3.internal.CommsTokenStore r3 = r10.tokenStore
            org.eclipse.paho.client.mqttv3.MqttException r6 = new org.eclipse.paho.client.mqttv3.MqttException
            r7 = 32102(0x7d66, float:4.4984E-41)
            r6.<init>((int) r7)
            r3.quiesce(r6)
            org.eclipse.paho.client.mqttv3.MqttToken r0 = r10.handleOldTokens(r11, r12)
            org.eclipse.paho.client.mqttv3.internal.ClientState r3 = r10.clientState     // Catch:{ Exception -> 0x00f5 }
            r3.disconnected(r12)     // Catch:{ Exception -> 0x00f5 }
            org.eclipse.paho.client.mqttv3.internal.ClientState r3 = r10.clientState     // Catch:{ Exception -> 0x00f5 }
            boolean r3 = r3.getCleanSession()     // Catch:{ Exception -> 0x00f5 }
            if (r3 == 0) goto L_0x0085
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r3 = r10.callback     // Catch:{ Exception -> 0x00f5 }
            r3.removeMessageListeners()     // Catch:{ Exception -> 0x00f5 }
        L_0x0085:
            org.eclipse.paho.client.mqttv3.internal.CommsSender r3 = r10.sender
            if (r3 == 0) goto L_0x008e
            org.eclipse.paho.client.mqttv3.internal.CommsSender r3 = r10.sender
            r3.stop()
        L_0x008e:
            org.eclipse.paho.client.mqttv3.MqttPingSender r3 = r10.pingSender
            if (r3 == 0) goto L_0x0097
            org.eclipse.paho.client.mqttv3.MqttPingSender r3 = r10.pingSender
            r3.stop()
        L_0x0097:
            org.eclipse.paho.client.mqttv3.internal.DisconnectedMessageBuffer r3 = r10.disconnectedMessageBuffer     // Catch:{ Exception -> 0x00f3 }
            if (r3 != 0) goto L_0x00a4
            org.eclipse.paho.client.mqttv3.MqttClientPersistence r3 = r10.persistence     // Catch:{ Exception -> 0x00f3 }
            if (r3 == 0) goto L_0x00a4
            org.eclipse.paho.client.mqttv3.MqttClientPersistence r3 = r10.persistence     // Catch:{ Exception -> 0x00f3 }
            r3.close()     // Catch:{ Exception -> 0x00f3 }
        L_0x00a4:
            java.lang.Object r6 = r10.conLock
            monitor-enter(r6)
            org.eclipse.paho.client.mqttv3.logging.Logger r3 = log     // Catch:{ all -> 0x00ea }
            java.lang.String r7 = CLASS_NAME     // Catch:{ all -> 0x00ea }
            java.lang.String r8 = "shutdownConnection"
            java.lang.String r9 = "217"
            r3.fine(r7, r8, r9)     // Catch:{ all -> 0x00ea }
            r3 = 3
            r10.conState = r3     // Catch:{ all -> 0x00ea }
            r3 = 0
            r10.stoppingComms = r3     // Catch:{ all -> 0x00ea }
            monitor-exit(r6)     // Catch:{ all -> 0x00ea }
            if (r0 == 0) goto L_0x00ed
            r3 = r4
        L_0x00bc:
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r6 = r10.callback
            if (r6 == 0) goto L_0x00ef
        L_0x00c0:
            r3 = r3 & r4
            if (r3 == 0) goto L_0x00c8
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r3 = r10.callback
            r3.asyncOperationComplete(r0)
        L_0x00c8:
            if (r2 == 0) goto L_0x00d3
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r3 = r10.callback
            if (r3 == 0) goto L_0x00d3
            org.eclipse.paho.client.mqttv3.internal.CommsCallback r3 = r10.callback
            r3.connectionLost(r12)
        L_0x00d3:
            java.lang.Object r4 = r10.conLock
            monitor-enter(r4)
            boolean r3 = r10.closePending     // Catch:{ all -> 0x00e1 }
            if (r3 == 0) goto L_0x00de
            r3 = 1
            r10.close(r3)     // Catch:{ Exception -> 0x00f1 }
        L_0x00de:
            monitor-exit(r4)     // Catch:{ all -> 0x00e1 }
            goto L_0x0015
        L_0x00e1:
            r3 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x00e1 }
            throw r3
        L_0x00e4:
            r2 = r4
            goto L_0x0031
        L_0x00e7:
            r3 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x00e7 }
            throw r3
        L_0x00ea:
            r3 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x00ea }
            throw r3
        L_0x00ed:
            r3 = r5
            goto L_0x00bc
        L_0x00ef:
            r4 = r5
            goto L_0x00c0
        L_0x00f1:
            r3 = move-exception
            goto L_0x00de
        L_0x00f3:
            r3 = move-exception
            goto L_0x00a4
        L_0x00f5:
            r3 = move-exception
            goto L_0x0085
        L_0x00f7:
            r3 = move-exception
            goto L_0x0063
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.ClientComms.shutdownConnection(org.eclipse.paho.client.mqttv3.MqttToken, org.eclipse.paho.client.mqttv3.MqttException):void");
    }

    private MqttToken handleOldTokens(MqttToken token, MqttException reason) {
        log.fine(CLASS_NAME, "handleOldTokens", "222");
        MqttToken tokToNotifyLater = null;
        if (token != null) {
            try {
                if (this.tokenStore.getToken(token.internalTok.getKey()) == null) {
                    this.tokenStore.saveToken(token, token.internalTok.getKey());
                }
            } catch (Exception e) {
            }
        }
        Enumeration toksToNotE = this.clientState.resolveOldTokens(reason).elements();
        while (toksToNotE.hasMoreElements()) {
            MqttToken tok = (MqttToken) toksToNotE.nextElement();
            if (tok.internalTok.getKey().equals(MqttDisconnect.KEY) || tok.internalTok.getKey().equals("Con")) {
                tokToNotifyLater = tok;
            } else {
                this.callback.asyncOperationComplete(tok);
            }
        }
        return tokToNotifyLater;
    }

    public void disconnect(MqttDisconnect disconnect, long quiesceTimeout, MqttToken token) throws MqttException {
        synchronized (this.conLock) {
            if (isClosed()) {
                log.fine(CLASS_NAME, "disconnect", "223");
                throw ExceptionHelper.createMqttException(32111);
            } else if (isDisconnected()) {
                log.fine(CLASS_NAME, "disconnect", "211");
                throw ExceptionHelper.createMqttException(32101);
            } else if (isDisconnecting()) {
                log.fine(CLASS_NAME, "disconnect", "219");
                throw ExceptionHelper.createMqttException(32102);
            } else if (Thread.currentThread() == this.callback.getThread()) {
                log.fine(CLASS_NAME, "disconnect", "210");
                throw ExceptionHelper.createMqttException(32107);
            } else {
                log.fine(CLASS_NAME, "disconnect", "218");
                this.conState = 2;
                new DisconnectBG(disconnect, quiesceTimeout, token, this.executorService).start();
            }
        }
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        disconnectForcibly(quiesceTimeout, disconnectTimeout, true);
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout, boolean sendDisconnectPacket) throws MqttException {
        if (this.clientState != null) {
            this.clientState.quiesce(quiesceTimeout);
        }
        MqttToken token = new MqttToken(this.client.getClientId());
        if (sendDisconnectPacket) {
            try {
                internalSend(new MqttDisconnect(), token);
                token.waitForCompletion(disconnectTimeout);
            } catch (Exception e) {
                token.internalTok.markComplete((MqttWireMessage) null, (MqttException) null);
                shutdownConnection(token, (MqttException) null);
                return;
            } catch (Throwable th) {
                token.internalTok.markComplete((MqttWireMessage) null, (MqttException) null);
                shutdownConnection(token, (MqttException) null);
                throw th;
            }
        }
        token.internalTok.markComplete((MqttWireMessage) null, (MqttException) null);
        shutdownConnection(token, (MqttException) null);
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.conLock) {
            z = this.conState == 0;
        }
        return z;
    }

    public boolean isConnecting() {
        boolean z = true;
        synchronized (this.conLock) {
            if (this.conState != 1) {
                z = false;
            }
        }
        return z;
    }

    public boolean isDisconnected() {
        boolean z;
        synchronized (this.conLock) {
            z = this.conState == 3;
        }
        return z;
    }

    public boolean isDisconnecting() {
        boolean z;
        synchronized (this.conLock) {
            z = this.conState == 2;
        }
        return z;
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this.conLock) {
            z = this.conState == 4;
        }
        return z;
    }

    public boolean isResting() {
        boolean z;
        synchronized (this.conLock) {
            z = this.resting;
        }
        return z;
    }

    public void setCallback(MqttCallback mqttCallback) {
        this.callback.setCallback(mqttCallback);
    }

    public void setReconnectCallback(MqttCallbackExtended callback2) {
        this.callback.setReconnectCallback(callback2);
    }

    public void setManualAcks(boolean manualAcks) {
        this.callback.setManualAcks(manualAcks);
    }

    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        this.callback.messageArrivedComplete(messageId, qos);
    }

    public void setMessageListener(String topicFilter, IMqttMessageListener messageListener) {
        this.callback.setMessageListener(topicFilter, messageListener);
    }

    public void removeMessageListener(String topicFilter) {
        this.callback.removeMessageListener(topicFilter);
    }

    /* access modifiers changed from: protected */
    public MqttTopic getTopic(String topic) {
        return new MqttTopic(topic, this);
    }

    public void setNetworkModuleIndex(int index) {
        this.networkModuleIndex = index;
    }

    public int getNetworkModuleIndex() {
        return this.networkModuleIndex;
    }

    public NetworkModule[] getNetworkModules() {
        return this.networkModules;
    }

    public void setNetworkModules(NetworkModule[] networkModules2) {
        this.networkModules = networkModules2;
    }

    public MqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.tokenStore.getOutstandingDelTokens();
    }

    /* access modifiers changed from: protected */
    public void deliveryComplete(MqttPublish msg) throws MqttPersistenceException {
        this.clientState.deliveryComplete(msg);
    }

    /* access modifiers changed from: protected */
    public void deliveryComplete(int messageId) throws MqttPersistenceException {
        this.clientState.deliveryComplete(messageId);
    }

    public IMqttAsyncClient getClient() {
        return this.client;
    }

    public long getKeepAlive() {
        return this.clientState.getKeepAlive();
    }

    public ClientState getClientState() {
        return this.clientState;
    }

    public MqttConnectOptions getConOptions() {
        return this.conOptions;
    }

    public Properties getDebug() {
        Properties props = new Properties();
        props.put("conState", new Integer(this.conState));
        props.put("serverURI", getClient().getServerURI());
        props.put("callback", this.callback);
        props.put("stoppingComms", new Boolean(this.stoppingComms));
        return props;
    }

    private class ConnectBG implements Runnable {
        ClientComms clientComms = null;
        MqttConnect conPacket;
        MqttToken conToken;
        private String threadName;

        ConnectBG(ClientComms cc, MqttToken cToken, MqttConnect cPacket, ExecutorService executorService) {
            this.clientComms = cc;
            this.conToken = cToken;
            this.conPacket = cPacket;
            this.threadName = "MQTT Con: " + ClientComms.this.getClient().getClientId();
        }

        /* access modifiers changed from: package-private */
        public void start() {
            ClientComms.this.executorService.execute(this);
        }

        public void run() {
            Thread.currentThread().setName(this.threadName);
            MqttException mqttEx = null;
            ClientComms.log.fine(ClientComms.CLASS_NAME, "connectBG:run", "220");
            try {
                MqttDeliveryToken[] toks = ClientComms.this.tokenStore.getOutstandingDelTokens();
                for (MqttDeliveryToken mqttDeliveryToken : toks) {
                    mqttDeliveryToken.internalTok.setException((MqttException) null);
                }
                ClientComms.this.tokenStore.saveToken(this.conToken, (MqttWireMessage) this.conPacket);
                NetworkModule networkModule = ClientComms.this.networkModules[ClientComms.this.networkModuleIndex];
                networkModule.start();
                ClientComms.this.receiver = new CommsReceiver(this.clientComms, ClientComms.this.clientState, ClientComms.this.tokenStore, networkModule.getInputStream());
                ClientComms.this.receiver.start("MQTT Rec: " + ClientComms.this.getClient().getClientId(), ClientComms.this.executorService);
                ClientComms.this.sender = new CommsSender(this.clientComms, ClientComms.this.clientState, ClientComms.this.tokenStore, networkModule.getOutputStream());
                ClientComms.this.sender.start("MQTT Snd: " + ClientComms.this.getClient().getClientId(), ClientComms.this.executorService);
                ClientComms.this.callback.start("MQTT Call: " + ClientComms.this.getClient().getClientId(), ClientComms.this.executorService);
                ClientComms.this.internalSend(this.conPacket, this.conToken);
            } catch (MqttException ex) {
                ClientComms.log.fine(ClientComms.CLASS_NAME, "connectBG:run", "212", (Object[]) null, ex);
                mqttEx = ex;
            } catch (Exception ex2) {
                ClientComms.log.fine(ClientComms.CLASS_NAME, "connectBG:run", "209", (Object[]) null, ex2);
                mqttEx = ExceptionHelper.createMqttException((Throwable) ex2);
            }
            if (mqttEx != null) {
                ClientComms.this.shutdownConnection(this.conToken, mqttEx);
            }
        }
    }

    private class DisconnectBG implements Runnable {
        MqttDisconnect disconnect;
        long quiesceTimeout;
        private String threadName;
        MqttToken token;

        DisconnectBG(MqttDisconnect disconnect2, long quiesceTimeout2, MqttToken token2, ExecutorService executorService) {
            this.disconnect = disconnect2;
            this.quiesceTimeout = quiesceTimeout2;
            this.token = token2;
        }

        /* access modifiers changed from: package-private */
        public void start() {
            this.threadName = "MQTT Disc: " + ClientComms.this.getClient().getClientId();
            ClientComms.this.executorService.execute(this);
        }

        public void run() {
            Thread.currentThread().setName(this.threadName);
            ClientComms.log.fine(ClientComms.CLASS_NAME, "disconnectBG:run", "221");
            ClientComms.this.clientState.quiesce(this.quiesceTimeout);
            try {
                ClientComms.this.internalSend(this.disconnect, this.token);
                this.token.internalTok.waitUntilSent();
            } catch (MqttException e) {
            } finally {
                this.token.internalTok.markComplete((MqttWireMessage) null, (MqttException) null);
                ClientComms.this.shutdownConnection(this.token, (MqttException) null);
            }
        }
    }

    public MqttToken checkForActivity() {
        return checkForActivity((IMqttActionListener) null);
    }

    public MqttToken checkForActivity(IMqttActionListener pingCallback) {
        try {
            return this.clientState.checkForActivity(pingCallback);
        } catch (MqttException e) {
            handleRunException(e);
            return null;
        } catch (Exception e2) {
            handleRunException(e2);
            return null;
        }
    }

    private void handleRunException(Exception ex) {
        MqttException mex;
        log.fine(CLASS_NAME, "handleRunException", "804", (Object[]) null, ex);
        if (!(ex instanceof MqttException)) {
            mex = new MqttException(32109, ex);
        } else {
            mex = (MqttException) ex;
        }
        shutdownConnection((MqttToken) null, mex);
    }

    public void setRestingState(boolean resting2) {
        this.resting = resting2;
    }

    public void setDisconnectedMessageBuffer(DisconnectedMessageBuffer disconnectedMessageBuffer2) {
        this.disconnectedMessageBuffer = disconnectedMessageBuffer2;
    }

    public int getBufferedMessageCount() {
        return this.disconnectedMessageBuffer.getMessageCount();
    }

    public MqttMessage getBufferedMessage(int bufferIndex) {
        return ((MqttPublish) this.disconnectedMessageBuffer.getMessage(bufferIndex).getMessage()).getMessage();
    }

    public void deleteBufferedMessage(int bufferIndex) {
        this.disconnectedMessageBuffer.deleteMessage(bufferIndex);
    }

    public void notifyConnect() {
        if (this.disconnectedMessageBuffer != null) {
            log.fine(CLASS_NAME, "notifyConnect", "509");
            this.disconnectedMessageBuffer.setPublishCallback(new ReconnectDisconnectedBufferCallback("notifyConnect"));
            this.executorService.execute(this.disconnectedMessageBuffer);
        }
    }

    class ReconnectDisconnectedBufferCallback implements IDisconnectedBufferCallback {
        final String methodName;

        ReconnectDisconnectedBufferCallback(String methodName2) {
            this.methodName = methodName2;
        }

        public void publishBufferedMessage(BufferedMessage bufferedMessage) throws MqttException {
            if (ClientComms.this.isConnected()) {
                while (ClientComms.this.clientState.getActualInFlight() >= ClientComms.this.clientState.getMaxInFlight() - 1) {
                    Thread.yield();
                }
                ClientComms.log.fine(ClientComms.CLASS_NAME, this.methodName, "510", new Object[]{bufferedMessage.getMessage().getKey()});
                ClientComms.this.internalSend(bufferedMessage.getMessage(), bufferedMessage.getToken());
                ClientComms.this.clientState.unPersistBufferedMessage(bufferedMessage.getMessage());
                return;
            }
            ClientComms.log.fine(ClientComms.CLASS_NAME, this.methodName, "208");
            throw ExceptionHelper.createMqttException(32104);
        }
    }

    public int getActualInFlight() {
        return this.clientState.getActualInFlight();
    }
}
