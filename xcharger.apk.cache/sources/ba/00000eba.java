package org.eclipse.paho.client.mqttv3.internal;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
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

/* loaded from: classes.dex */
public class ClientComms {
    private static final byte CLOSED = 4;
    private static final byte CONNECTED = 0;
    private static final byte CONNECTING = 1;
    private static final byte DISCONNECTED = 3;
    private static final byte DISCONNECTING = 2;
    private CommsCallback callback;
    private IMqttAsyncClient client;
    private ClientState clientState;
    private MqttConnectOptions conOptions;
    private byte conState;
    private DisconnectedMessageBuffer disconnectedMessageBuffer;
    private ExecutorService executorService;
    private int networkModuleIndex;
    private NetworkModule[] networkModules;
    private MqttClientPersistence persistence;
    private MqttPingSender pingSender;
    private CommsReceiver receiver;
    private CommsSender sender;
    private CommsTokenStore tokenStore;
    public static String VERSION = "${project.version}";
    public static String BUILD_LEVEL = "L${build.level}";
    private static final String CLASS_NAME = ClientComms.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private boolean stoppingComms = false;
    private Object conLock = new Object();
    private boolean closePending = false;
    private boolean resting = false;

    public ClientComms(IMqttAsyncClient client, MqttClientPersistence persistence, MqttPingSender pingSender, ExecutorService executorService) throws MqttException {
        this.conState = (byte) 3;
        this.conState = (byte) 3;
        this.client = client;
        this.persistence = persistence;
        this.pingSender = pingSender;
        this.pingSender.init(this);
        this.executorService = executorService;
        this.tokenStore = new CommsTokenStore(getClient().getClientId());
        this.callback = new CommsCallback(this);
        this.clientState = new ClientState(persistence, this.tokenStore, this.callback, this, pingSender);
        this.callback.setClientState(this.clientState);
        log.setResourceName(getClient().getClientId());
    }

    CommsReceiver getReceiver() {
        return this.receiver;
    }

    private void shutdownExecutorService() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(1L, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
                if (!this.executorService.awaitTermination(1L, TimeUnit.SECONDS)) {
                    log.fine(CLASS_NAME, "shutdownExecutorService", "executorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void internalSend(MqttWireMessage message, MqttToken token) throws MqttException {
        log.fine(CLASS_NAME, "internalSend", "200", new Object[]{message.getKey(), message, token});
        if (token.getClient() == null) {
            token.internalTok.setClient(getClient());
            try {
                this.clientState.send(message, token);
                return;
            } catch (MqttException e) {
                if (message instanceof MqttPublish) {
                    this.clientState.undo((MqttPublish) message);
                }
                throw e;
            }
        }
        log.fine(CLASS_NAME, "internalSend", "213", new Object[]{message.getKey(), message, token});
        throw new MqttException(32201);
    }

    public void sendNoWait(MqttWireMessage message, MqttToken token) throws MqttException {
        if (isConnected() || ((!isConnected() && (message instanceof MqttConnect)) || (isDisconnecting() && (message instanceof MqttDisconnect)))) {
            if (this.disconnectedMessageBuffer != null && this.disconnectedMessageBuffer.getMessageCount() != 0) {
                log.fine(CLASS_NAME, "sendNoWait", "507", new Object[]{message.getKey()});
                if (this.disconnectedMessageBuffer.isPersistBuffer()) {
                    this.clientState.persistBufferedMessage(message);
                }
                this.disconnectedMessageBuffer.putMessage(message, token);
                return;
            }
            internalSend(message, token);
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

    public void close(boolean force) throws MqttException {
        synchronized (this.conLock) {
            if (!isClosed()) {
                if (!isDisconnected() || force) {
                    log.fine(CLASS_NAME, "close", "224");
                    if (isConnecting()) {
                        throw new MqttException(32110);
                    }
                    if (isConnected()) {
                        throw ExceptionHelper.createMqttException(32100);
                    }
                    if (isDisconnecting()) {
                        this.closePending = true;
                        return;
                    }
                }
                this.conState = (byte) 4;
                shutdownExecutorService();
                this.clientState.close();
                this.clientState = null;
                this.callback = null;
                this.persistence = null;
                this.sender = null;
                this.pingSender = null;
                this.receiver = null;
                this.networkModules = null;
                this.conOptions = null;
                this.tokenStore = null;
            }
        }
    }

    public void connect(MqttConnectOptions options, MqttToken token) throws MqttException {
        synchronized (this.conLock) {
            if (isDisconnected() && !this.closePending) {
                log.fine(CLASS_NAME, "connect", "214");
                this.conState = (byte) 1;
                this.conOptions = options;
                MqttConnect connect = new MqttConnect(this.client.getClientId(), this.conOptions.getMqttVersion(), this.conOptions.isCleanSession(), this.conOptions.getKeepAliveInterval(), this.conOptions.getUserName(), this.conOptions.getPassword(), this.conOptions.getWillMessage(), this.conOptions.getWillDestination());
                this.clientState.setKeepAliveSecs(this.conOptions.getKeepAliveInterval());
                this.clientState.setCleanSession(this.conOptions.isCleanSession());
                this.clientState.setMaxInflight(this.conOptions.getMaxInflight());
                this.tokenStore.open();
                ConnectBG conbg = new ConnectBG(this, token, connect, this.executorService);
                conbg.start();
            } else {
                log.fine(CLASS_NAME, "connect", "207", new Object[]{new Byte(this.conState)});
                if (isClosed() || this.closePending) {
                    throw new MqttException(32111);
                }
                if (isConnecting()) {
                    throw new MqttException(32110);
                }
                if (isDisconnecting()) {
                    throw new MqttException(32102);
                }
                throw ExceptionHelper.createMqttException(32100);
            }
        }
    }

    public void connectComplete(MqttConnack cack, MqttException mex) throws MqttException {
        int rc = cack.getReturnCode();
        synchronized (this.conLock) {
            if (rc == 0) {
                log.fine(CLASS_NAME, "connectComplete", "215");
                this.conState = (byte) 0;
                return;
            }
            log.fine(CLASS_NAME, "connectComplete", "204", new Object[]{new Integer(rc)});
            throw mex;
        }
    }

    public void shutdownConnection(MqttToken token, MqttException reason) {
        NetworkModule networkModule;
        synchronized (this.conLock) {
            if (!this.stoppingComms && !this.closePending && !isClosed()) {
                this.stoppingComms = true;
                log.fine(CLASS_NAME, "shutdownConnection", "216");
                boolean wasConnected = isConnected() || isDisconnecting();
                this.conState = (byte) 2;
                if (token != null && !token.isComplete()) {
                    token.internalTok.setException(reason);
                }
                if (this.callback != null) {
                    this.callback.stop();
                }
                if (this.receiver != null) {
                    this.receiver.stop();
                }
                try {
                    if (this.networkModules != null && (networkModule = this.networkModules[this.networkModuleIndex]) != null) {
                        networkModule.stop();
                    }
                } catch (Exception e) {
                }
                this.tokenStore.quiesce(new MqttException(32102));
                MqttToken endToken = handleOldTokens(token, reason);
                try {
                    this.clientState.disconnected(reason);
                    if (this.clientState.getCleanSession()) {
                        this.callback.removeMessageListeners();
                    }
                } catch (Exception e2) {
                }
                if (this.sender != null) {
                    this.sender.stop();
                }
                if (this.pingSender != null) {
                    this.pingSender.stop();
                }
                try {
                    if (this.disconnectedMessageBuffer == null && this.persistence != null) {
                        this.persistence.close();
                    }
                } catch (Exception e3) {
                }
                synchronized (this.conLock) {
                    log.fine(CLASS_NAME, "shutdownConnection", "217");
                    this.conState = (byte) 3;
                    this.stoppingComms = false;
                }
                if ((endToken != null) & (this.callback != null)) {
                    this.callback.asyncOperationComplete(endToken);
                }
                if (wasConnected && this.callback != null) {
                    this.callback.connectionLost(reason);
                }
                synchronized (this.conLock) {
                    if (this.closePending) {
                        try {
                            close(true);
                        } catch (Exception e4) {
                        }
                    }
                }
            }
        }
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
        Vector toksToNot = this.clientState.resolveOldTokens(reason);
        Enumeration toksToNotE = toksToNot.elements();
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
                this.conState = (byte) 2;
                DisconnectBG discbg = new DisconnectBG(disconnect, quiesceTimeout, token, this.executorService);
                discbg.start();
            }
        }
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        disconnectForcibly(quiesceTimeout, disconnectTimeout, true);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v0, types: [org.eclipse.paho.client.mqttv3.MqttException, org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage] */
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
            } finally {
                token.internalTok.markComplete(null, null);
                shutdownConnection(token, null);
            }
        }
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.conLock) {
            z = this.conState == 0;
        }
        return z;
    }

    public boolean isConnecting() {
        boolean z;
        synchronized (this.conLock) {
            z = this.conState == 1;
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

    public void setReconnectCallback(MqttCallbackExtended callback) {
        this.callback.setReconnectCallback(callback);
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

    protected MqttTopic getTopic(String topic) {
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

    public void setNetworkModules(NetworkModule[] networkModules) {
        this.networkModules = networkModules;
    }

    public MqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.tokenStore.getOutstandingDelTokens();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void deliveryComplete(MqttPublish msg) throws MqttPersistenceException {
        this.clientState.deliveryComplete(msg);
    }

    /* JADX INFO: Access modifiers changed from: protected */
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

    /* loaded from: classes.dex */
    private class ConnectBG implements Runnable {
        ClientComms clientComms;
        MqttConnect conPacket;
        MqttToken conToken;
        private String threadName;

        ConnectBG(ClientComms cc, MqttToken cToken, MqttConnect cPacket, ExecutorService executorService) {
            this.clientComms = null;
            this.clientComms = cc;
            this.conToken = cToken;
            this.conPacket = cPacket;
            this.threadName = "MQTT Con: " + ClientComms.this.getClient().getClientId();
        }

        void start() {
            ClientComms.this.executorService.execute(this);
        }

        @Override // java.lang.Runnable
        public void run() {
            Thread.currentThread().setName(this.threadName);
            MqttException mqttEx = null;
            ClientComms.log.fine(ClientComms.CLASS_NAME, "connectBG:run", "220");
            try {
                MqttDeliveryToken[] toks = ClientComms.this.tokenStore.getOutstandingDelTokens();
                for (MqttDeliveryToken mqttDeliveryToken : toks) {
                    mqttDeliveryToken.internalTok.setException(null);
                }
                ClientComms.this.tokenStore.saveToken(this.conToken, this.conPacket);
                NetworkModule networkModule = ClientComms.this.networkModules[ClientComms.this.networkModuleIndex];
                networkModule.start();
                ClientComms.this.receiver = new CommsReceiver(this.clientComms, ClientComms.this.clientState, ClientComms.this.tokenStore, networkModule.getInputStream());
                ClientComms.this.receiver.start("MQTT Rec: " + ClientComms.this.getClient().getClientId(), ClientComms.this.executorService);
                ClientComms.this.sender = new CommsSender(this.clientComms, ClientComms.this.clientState, ClientComms.this.tokenStore, networkModule.getOutputStream());
                ClientComms.this.sender.start("MQTT Snd: " + ClientComms.this.getClient().getClientId(), ClientComms.this.executorService);
                ClientComms.this.callback.start("MQTT Call: " + ClientComms.this.getClient().getClientId(), ClientComms.this.executorService);
                ClientComms.this.internalSend(this.conPacket, this.conToken);
            } catch (MqttException ex) {
                ClientComms.log.fine(ClientComms.CLASS_NAME, "connectBG:run", "212", null, ex);
                mqttEx = ex;
            } catch (Exception ex2) {
                ClientComms.log.fine(ClientComms.CLASS_NAME, "connectBG:run", "209", null, ex2);
                mqttEx = ExceptionHelper.createMqttException(ex2);
            }
            if (mqttEx != null) {
                ClientComms.this.shutdownConnection(this.conToken, mqttEx);
            }
        }
    }

    /* loaded from: classes.dex */
    private class DisconnectBG implements Runnable {
        MqttDisconnect disconnect;
        long quiesceTimeout;
        private String threadName;
        MqttToken token;

        DisconnectBG(MqttDisconnect disconnect, long quiesceTimeout, MqttToken token, ExecutorService executorService) {
            this.disconnect = disconnect;
            this.quiesceTimeout = quiesceTimeout;
            this.token = token;
        }

        void start() {
            this.threadName = "MQTT Disc: " + ClientComms.this.getClient().getClientId();
            ClientComms.this.executorService.execute(this);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r4v0, types: [org.eclipse.paho.client.mqttv3.MqttException, org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage] */
        @Override // java.lang.Runnable
        public void run() {
            Thread.currentThread().setName(this.threadName);
            ClientComms.log.fine(ClientComms.CLASS_NAME, "disconnectBG:run", "221");
            ClientComms.this.clientState.quiesce(this.quiesceTimeout);
            try {
                ClientComms.this.internalSend(this.disconnect, this.token);
                this.token.internalTok.waitUntilSent();
            } catch (MqttException e) {
            } finally {
                this.token.internalTok.markComplete(null, null);
                ClientComms.this.shutdownConnection(this.token, null);
            }
        }
    }

    public MqttToken checkForActivity() {
        return checkForActivity(null);
    }

    public MqttToken checkForActivity(IMqttActionListener pingCallback) {
        try {
            MqttToken token = this.clientState.checkForActivity(pingCallback);
            return token;
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
        log.fine(CLASS_NAME, "handleRunException", "804", null, ex);
        if (!(ex instanceof MqttException)) {
            mex = new MqttException(32109, ex);
        } else {
            mex = (MqttException) ex;
        }
        shutdownConnection(null, mex);
    }

    public void setRestingState(boolean resting) {
        this.resting = resting;
    }

    public void setDisconnectedMessageBuffer(DisconnectedMessageBuffer disconnectedMessageBuffer) {
        this.disconnectedMessageBuffer = disconnectedMessageBuffer;
    }

    public int getBufferedMessageCount() {
        return this.disconnectedMessageBuffer.getMessageCount();
    }

    public MqttMessage getBufferedMessage(int bufferIndex) {
        MqttPublish send = (MqttPublish) this.disconnectedMessageBuffer.getMessage(bufferIndex).getMessage();
        return send.getMessage();
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

    /* loaded from: classes.dex */
    class ReconnectDisconnectedBufferCallback implements IDisconnectedBufferCallback {
        final String methodName;

        ReconnectDisconnectedBufferCallback(String methodName) {
            this.methodName = methodName;
        }

        @Override // org.eclipse.paho.client.mqttv3.internal.IDisconnectedBufferCallback
        public void publishBufferedMessage(BufferedMessage bufferedMessage) throws MqttException {
            if (!ClientComms.this.isConnected()) {
                ClientComms.log.fine(ClientComms.CLASS_NAME, this.methodName, "208");
                throw ExceptionHelper.createMqttException(32104);
            }
            while (ClientComms.this.clientState.getActualInFlight() >= ClientComms.this.clientState.getMaxInFlight() - 1) {
                Thread.yield();
            }
            ClientComms.log.fine(ClientComms.CLASS_NAME, this.methodName, "510", new Object[]{bufferedMessage.getMessage().getKey()});
            ClientComms.this.internalSend(bufferedMessage.getMessage(), bufferedMessage.getToken());
            ClientComms.this.clientState.unPersistBufferedMessage(bufferedMessage.getMessage());
        }
    }

    public int getActualInFlight() {
        return this.clientState.getActualInFlight();
    }
}