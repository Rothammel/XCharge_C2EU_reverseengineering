package org.eclipse.paho.client.mqttv3;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;
import org.eclipse.paho.client.mqttv3.internal.ConnectActionListener;
import org.eclipse.paho.client.mqttv3.internal.DisconnectedMessageBuffer;
import org.eclipse.paho.client.mqttv3.internal.ExceptionHelper;
import org.eclipse.paho.client.mqttv3.internal.NetworkModule;
import org.eclipse.paho.client.mqttv3.internal.SSLNetworkModule;
import org.eclipse.paho.client.mqttv3.internal.TCPNetworkModule;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.internal.websocket.WebSocketNetworkModule;
import org.eclipse.paho.client.mqttv3.internal.websocket.WebSocketSecureNetworkModule;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttDisconnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubscribe;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.util.Debug;
import org.java_websocket.WebSocket;

public class MqttAsyncClient implements IMqttAsyncClient {
    /* access modifiers changed from: private */
    public static final String CLASS_NAME = MqttAsyncClient.class.getName();
    private static final String CLIENT_ID_PREFIX = "paho";
    private static final long DISCONNECT_TIMEOUT = 10000;
    private static final char MAX_HIGH_SURROGATE = '?';
    private static final char MIN_HIGH_SURROGATE = '?';
    private static final long QUIESCE_TIMEOUT = 30000;
    /* access modifiers changed from: private */
    public static Object clientLock = new Object();
    /* access modifiers changed from: private */
    public static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    /* access modifiers changed from: private */
    public static int reconnectDelay = 1000;
    /* access modifiers changed from: private */
    public String clientId;
    protected ClientComms comms;
    /* access modifiers changed from: private */
    public MqttConnectOptions connOpts;
    private ScheduledExecutorService executorService;
    private MqttCallback mqttCallback;
    private MqttClientPersistence persistence;
    /* access modifiers changed from: private */
    public Timer reconnectTimer;
    /* access modifiers changed from: private */
    public boolean reconnecting;
    private String serverURI;
    private Hashtable topics;
    private Object userContext;

    public MqttAsyncClient(String serverURI2, String clientId2) throws MqttException {
        this(serverURI2, clientId2, new MqttDefaultFilePersistence());
    }

    public MqttAsyncClient(String serverURI2, String clientId2, MqttClientPersistence persistence2) throws MqttException {
        this(serverURI2, clientId2, persistence2, new TimerPingSender());
    }

    public MqttAsyncClient(String serverURI2, String clientId2, MqttClientPersistence persistence2, MqttPingSender pingSender) throws MqttException {
        this(serverURI2, clientId2, persistence2, pingSender, (ScheduledExecutorService) null);
    }

    public MqttAsyncClient(String serverURI2, String clientId2, MqttClientPersistence persistence2, MqttPingSender pingSender, ScheduledExecutorService executorService2) throws MqttException {
        this.reconnecting = false;
        log.setResourceName(clientId2);
        if (clientId2 == null) {
            throw new IllegalArgumentException("Null clientId");
        }
        int clientIdLength = 0;
        int i = 0;
        while (i < clientId2.length() - 1) {
            if (Character_isHighSurrogate(clientId2.charAt(i))) {
                i++;
            }
            clientIdLength++;
            i++;
        }
        if (clientIdLength > 65535) {
            throw new IllegalArgumentException("ClientId longer than 65535 characters");
        }
        MqttConnectOptions.validateURI(serverURI2);
        this.serverURI = serverURI2;
        this.clientId = clientId2;
        this.persistence = persistence2;
        if (this.persistence == null) {
            this.persistence = new MemoryPersistence();
        }
        this.executorService = executorService2;
        if (this.executorService == null) {
            this.executorService = Executors.newScheduledThreadPool(10);
        }
        log.fine(CLASS_NAME, "MqttAsyncClient", "101", new Object[]{clientId2, serverURI2, persistence2});
        this.persistence.open(clientId2, serverURI2);
        this.comms = new ClientComms(this, this.persistence, pingSender, this.executorService);
        this.persistence.close();
        this.topics = new Hashtable();
    }

    protected static boolean Character_isHighSurrogate(char ch) {
        return ch >= 55296 && ch <= 56319;
    }

    /* access modifiers changed from: protected */
    public NetworkModule[] createNetworkModules(String address, MqttConnectOptions options) throws MqttException, MqttSecurityException {
        log.fine(CLASS_NAME, "createNetworkModules", "116", new Object[]{address});
        String[] serverURIs = options.getServerURIs();
        String[] array = serverURIs == null ? new String[]{address} : serverURIs.length == 0 ? new String[]{address} : serverURIs;
        NetworkModule[] networkModules = new NetworkModule[array.length];
        for (int i = 0; i < array.length; i++) {
            networkModules[i] = createNetworkModule(array[i], options);
        }
        log.fine(CLASS_NAME, "createNetworkModules", "108");
        return networkModules;
    }

    private NetworkModule createNetworkModule(String address, MqttConnectOptions options) throws MqttException, MqttSecurityException {
        String[] enabledCiphers;
        String[] enabledCiphers2;
        Exception e;
        log.fine(CLASS_NAME, "createNetworkModule", "115", new Object[]{address});
        SocketFactory factory = options.getSocketFactory();
        int serverURIType = MqttConnectOptions.validateURI(address);
        try {
            URI uri = new URI(address);
            if (uri.getHost() == null && address.contains("_")) {
                try {
                    Field hostField = URI.class.getDeclaredField("host");
                    hostField.setAccessible(true);
                    hostField.set(uri, getHostName(address.substring(uri.getScheme().length() + 3)));
                } catch (NoSuchFieldException e2) {
                    e = e2;
                    throw ExceptionHelper.createMqttException(e.getCause());
                } catch (SecurityException e3) {
                    e = e3;
                    throw ExceptionHelper.createMqttException(e.getCause());
                } catch (IllegalArgumentException e4) {
                    e = e4;
                    throw ExceptionHelper.createMqttException(e.getCause());
                } catch (IllegalAccessException e5) {
                    e = e5;
                    throw ExceptionHelper.createMqttException(e.getCause());
                }
            }
            String host = uri.getHost();
            int port = uri.getPort();
            switch (serverURIType) {
                case 0:
                    if (port == -1) {
                        port = 1883;
                    }
                    if (factory == null) {
                        factory = SocketFactory.getDefault();
                    } else if (factory instanceof SSLSocketFactory) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    TCPNetworkModule tCPNetworkModule = new TCPNetworkModule(factory, host, port, this.clientId);
                    tCPNetworkModule.setConnectTimeout(options.getConnectionTimeout());
                    return tCPNetworkModule;
                case 1:
                    if (port == -1) {
                        port = 8883;
                    }
                    SSLSocketFactoryFactory factoryFactory = null;
                    if (factory == null) {
                        factoryFactory = new SSLSocketFactoryFactory();
                        Properties sslClientProps = options.getSSLProperties();
                        if (sslClientProps != null) {
                            factoryFactory.initialize(sslClientProps, (String) null);
                        }
                        factory = factoryFactory.createSocketFactory((String) null);
                    } else if (!(factory instanceof SSLSocketFactory)) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    SSLNetworkModule sSLNetworkModule = new SSLNetworkModule((SSLSocketFactory) factory, host, port, this.clientId);
                    sSLNetworkModule.setSSLhandshakeTimeout(options.getConnectionTimeout());
                    sSLNetworkModule.setSSLHostnameVerifier(options.getSSLHostnameVerifier());
                    if (factoryFactory == null || (enabledCiphers2 = factoryFactory.getEnabledCipherSuites((String) null)) == null) {
                        return sSLNetworkModule;
                    }
                    sSLNetworkModule.setEnabledCiphers(enabledCiphers2);
                    return sSLNetworkModule;
                case 3:
                    if (port == -1) {
                        port = 80;
                    }
                    if (factory == null) {
                        factory = SocketFactory.getDefault();
                    } else if (factory instanceof SSLSocketFactory) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    WebSocketNetworkModule webSocketNetworkModule = new WebSocketNetworkModule(factory, address, host, port, this.clientId);
                    webSocketNetworkModule.setConnectTimeout(options.getConnectionTimeout());
                    return webSocketNetworkModule;
                case 4:
                    if (port == -1) {
                        port = WebSocket.DEFAULT_WSS_PORT;
                    }
                    SSLSocketFactoryFactory wSSFactoryFactory = null;
                    if (factory == null) {
                        wSSFactoryFactory = new SSLSocketFactoryFactory();
                        Properties sslClientProps2 = options.getSSLProperties();
                        if (sslClientProps2 != null) {
                            wSSFactoryFactory.initialize(sslClientProps2, (String) null);
                        }
                        factory = wSSFactoryFactory.createSocketFactory((String) null);
                    } else if (!(factory instanceof SSLSocketFactory)) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    WebSocketSecureNetworkModule webSocketSecureNetworkModule = new WebSocketSecureNetworkModule((SSLSocketFactory) factory, address, host, port, this.clientId);
                    webSocketSecureNetworkModule.setSSLhandshakeTimeout(options.getConnectionTimeout());
                    if (wSSFactoryFactory == null || (enabledCiphers = wSSFactoryFactory.getEnabledCipherSuites((String) null)) == null) {
                        return webSocketSecureNetworkModule;
                    }
                    webSocketSecureNetworkModule.setEnabledCiphers(enabledCiphers);
                    return webSocketSecureNetworkModule;
                default:
                    log.fine(CLASS_NAME, "createNetworkModule", "119", new Object[]{address});
                    return null;
            }
        } catch (URISyntaxException e6) {
            throw new IllegalArgumentException("Malformed URI: " + address + ", " + e6.getMessage());
        }
    }

    private String getHostName(String uri) {
        int portIndex = uri.indexOf(58);
        if (portIndex == -1) {
            portIndex = uri.indexOf(47);
        }
        if (portIndex == -1) {
            portIndex = uri.length();
        }
        return uri.substring(0, portIndex);
    }

    public IMqttToken connect(Object userContext2, IMqttActionListener callback) throws MqttException, MqttSecurityException {
        return connect(new MqttConnectOptions(), userContext2, callback);
    }

    public IMqttToken connect() throws MqttException, MqttSecurityException {
        return connect((Object) null, (IMqttActionListener) null);
    }

    public IMqttToken connect(MqttConnectOptions options) throws MqttException, MqttSecurityException {
        return connect(options, (Object) null, (IMqttActionListener) null);
    }

    public IMqttToken connect(MqttConnectOptions options, Object userContext2, IMqttActionListener callback) throws MqttException, MqttSecurityException {
        if (this.comms.isConnected()) {
            throw ExceptionHelper.createMqttException(32100);
        } else if (this.comms.isConnecting()) {
            throw new MqttException(32110);
        } else if (this.comms.isDisconnecting()) {
            throw new MqttException(32102);
        } else if (this.comms.isClosed()) {
            throw new MqttException(32111);
        } else {
            if (options == null) {
                options = new MqttConnectOptions();
            }
            this.connOpts = options;
            this.userContext = userContext2;
            boolean automaticReconnect = options.isAutomaticReconnect();
            Logger logger = log;
            String str = CLASS_NAME;
            Object[] objArr = new Object[8];
            objArr[0] = Boolean.valueOf(options.isCleanSession());
            objArr[1] = new Integer(options.getConnectionTimeout());
            objArr[2] = new Integer(options.getKeepAliveInterval());
            objArr[3] = options.getUserName();
            objArr[4] = options.getPassword() == null ? "[null]" : "[notnull]";
            objArr[5] = options.getWillMessage() == null ? "[null]" : "[notnull]";
            objArr[6] = userContext2;
            objArr[7] = callback;
            logger.fine(str, "connect", "103", objArr);
            this.comms.setNetworkModules(createNetworkModules(this.serverURI, options));
            this.comms.setReconnectCallback(new MqttReconnectCallback(automaticReconnect));
            MqttToken userToken = new MqttToken(getClientId());
            ConnectActionListener connectActionListener = new ConnectActionListener(this, this.persistence, this.comms, options, userToken, userContext2, callback, this.reconnecting);
            userToken.setActionCallback(connectActionListener);
            userToken.setUserContext(this);
            if (this.mqttCallback instanceof MqttCallbackExtended) {
                connectActionListener.setMqttCallbackExtended((MqttCallbackExtended) this.mqttCallback);
            }
            this.comms.setNetworkModuleIndex(0);
            connectActionListener.connect();
            return userToken;
        }
    }

    public IMqttToken disconnect(Object userContext2, IMqttActionListener callback) throws MqttException {
        return disconnect(30000, userContext2, callback);
    }

    public IMqttToken disconnect() throws MqttException {
        return disconnect((Object) null, (IMqttActionListener) null);
    }

    public IMqttToken disconnect(long quiesceTimeout) throws MqttException {
        return disconnect(quiesceTimeout, (Object) null, (IMqttActionListener) null);
    }

    public IMqttToken disconnect(long quiesceTimeout, Object userContext2, IMqttActionListener callback) throws MqttException {
        log.fine(CLASS_NAME, "disconnect", "104", new Object[]{new Long(quiesceTimeout), userContext2, callback});
        MqttToken token = new MqttToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext2);
        try {
            this.comms.disconnect(new MqttDisconnect(), quiesceTimeout, token);
            log.fine(CLASS_NAME, "disconnect", "108");
            return token;
        } catch (MqttException ex) {
            log.fine(CLASS_NAME, "disconnect", "105", (Object[]) null, ex);
            throw ex;
        }
    }

    public void disconnectForcibly() throws MqttException {
        disconnectForcibly(30000, 10000);
    }

    public void disconnectForcibly(long disconnectTimeout) throws MqttException {
        disconnectForcibly(30000, disconnectTimeout);
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        this.comms.disconnectForcibly(quiesceTimeout, disconnectTimeout);
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout, boolean sendDisconnectPacket) throws MqttException {
        this.comms.disconnectForcibly(quiesceTimeout, disconnectTimeout, sendDisconnectPacket);
    }

    public boolean isConnected() {
        return this.comms.isConnected();
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getServerURI() {
        return this.serverURI;
    }

    public String getCurrentServerURI() {
        return this.comms.getNetworkModules()[this.comms.getNetworkModuleIndex()].getServerURI();
    }

    /* access modifiers changed from: protected */
    public MqttTopic getTopic(String topic) {
        MqttTopic.validate(topic, false);
        MqttTopic result = (MqttTopic) this.topics.get(topic);
        if (result != null) {
            return result;
        }
        MqttTopic result2 = new MqttTopic(topic, this.comms);
        this.topics.put(topic, result2);
        return result2;
    }

    public IMqttToken checkPing(Object userContext2, IMqttActionListener callback) throws MqttException {
        log.fine(CLASS_NAME, "ping", "117");
        MqttToken token = this.comms.checkForActivity();
        log.fine(CLASS_NAME, "ping", "118");
        return token;
    }

    public IMqttToken subscribe(String topicFilter, int qos, Object userContext2, IMqttActionListener callback) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, userContext2, callback);
    }

    public IMqttToken subscribe(String topicFilter, int qos) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, (Object) null, (IMqttActionListener) null);
    }

    public IMqttToken subscribe(String[] topicFilters, int[] qos) throws MqttException {
        return subscribe(topicFilters, qos, (Object) null, (IMqttActionListener) null);
    }

    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext2, IMqttActionListener callback) throws MqttException {
        if (topicFilters.length != qos.length) {
            throw new IllegalArgumentException();
        }
        for (String removeMessageListener : topicFilters) {
            this.comms.removeMessageListener(removeMessageListener);
        }
        if (log.isLoggable(5)) {
            StringBuffer subs = new StringBuffer();
            for (int i = 0; i < topicFilters.length; i++) {
                if (i > 0) {
                    subs.append(", ");
                }
                subs.append("topic=").append(topicFilters[i]).append(" qos=").append(qos[i]);
                MqttTopic.validate(topicFilters[i], true);
            }
            log.fine(CLASS_NAME, "subscribe", "106", new Object[]{subs.toString(), userContext2, callback});
        }
        MqttToken token = new MqttToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext2);
        token.internalTok.setTopics(topicFilters);
        this.comms.sendNoWait(new MqttSubscribe(topicFilters, qos), token);
        log.fine(CLASS_NAME, "subscribe", "109");
        return token;
    }

    public IMqttToken subscribe(String topicFilter, int qos, Object userContext2, IMqttActionListener callback, IMqttMessageListener messageListener) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, userContext2, callback, new IMqttMessageListener[]{messageListener});
    }

    public IMqttToken subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, (Object) null, (IMqttActionListener) null, new IMqttMessageListener[]{messageListener});
    }

    public IMqttToken subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
        return subscribe(topicFilters, qos, (Object) null, (IMqttActionListener) null, messageListeners);
    }

    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext2, IMqttActionListener callback, IMqttMessageListener[] messageListeners) throws MqttException {
        if (messageListeners.length == qos.length && qos.length == topicFilters.length) {
            IMqttToken token = subscribe(topicFilters, qos, userContext2, callback);
            for (int i = 0; i < topicFilters.length; i++) {
                this.comms.setMessageListener(topicFilters[i], messageListeners[i]);
            }
            return token;
        }
        throw new IllegalArgumentException();
    }

    public IMqttToken unsubscribe(String topicFilter, Object userContext2, IMqttActionListener callback) throws MqttException {
        return unsubscribe(new String[]{topicFilter}, userContext2, callback);
    }

    public IMqttToken unsubscribe(String topicFilter) throws MqttException {
        return unsubscribe(new String[]{topicFilter}, (Object) null, (IMqttActionListener) null);
    }

    public IMqttToken unsubscribe(String[] topicFilters) throws MqttException {
        return unsubscribe(topicFilters, (Object) null, (IMqttActionListener) null);
    }

    public IMqttToken unsubscribe(String[] topicFilters, Object userContext2, IMqttActionListener callback) throws MqttException {
        if (log.isLoggable(5)) {
            String subs = "";
            for (int i = 0; i < topicFilters.length; i++) {
                if (i > 0) {
                    subs = String.valueOf(subs) + ", ";
                }
                subs = String.valueOf(subs) + topicFilters[i];
            }
            log.fine(CLASS_NAME, "unsubscribe", "107", new Object[]{subs, userContext2, callback});
        }
        for (String validate : topicFilters) {
            MqttTopic.validate(validate, true);
        }
        for (String removeMessageListener : topicFilters) {
            this.comms.removeMessageListener(removeMessageListener);
        }
        MqttToken token = new MqttToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext2);
        token.internalTok.setTopics(topicFilters);
        this.comms.sendNoWait(new MqttUnsubscribe(topicFilters), token);
        log.fine(CLASS_NAME, "unsubscribe", "110");
        return token;
    }

    public void setCallback(MqttCallback callback) {
        this.mqttCallback = callback;
        this.comms.setCallback(callback);
    }

    public void setManualAcks(boolean manualAcks) {
        this.comms.setManualAcks(manualAcks);
    }

    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        this.comms.messageArrivedComplete(messageId, qos);
    }

    public static String generateClientId() {
        return CLIENT_ID_PREFIX + System.nanoTime();
    }

    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.comms.getPendingDeliveryTokens();
    }

    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained, Object userContext2, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        return publish(topic, message, userContext2, callback);
    }

    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException, MqttPersistenceException {
        return publish(topic, payload, qos, retained, (Object) null, (IMqttActionListener) null);
    }

    public IMqttDeliveryToken publish(String topic, MqttMessage message) throws MqttException, MqttPersistenceException {
        return publish(topic, message, (Object) null, (IMqttActionListener) null);
    }

    public IMqttDeliveryToken publish(String topic, MqttMessage message, Object userContext2, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        log.fine(CLASS_NAME, "publish", "111", new Object[]{topic, userContext2, callback});
        MqttTopic.validate(topic, false);
        MqttDeliveryToken token = new MqttDeliveryToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext2);
        token.setMessage(message);
        token.internalTok.setTopics(new String[]{topic});
        this.comms.sendNoWait(new MqttPublish(topic, message), token);
        log.fine(CLASS_NAME, "publish", "112");
        return token;
    }

    public void reconnect() throws MqttException {
        log.fine(CLASS_NAME, "reconnect", "500", new Object[]{this.clientId});
        if (this.comms.isConnected()) {
            throw ExceptionHelper.createMqttException(32100);
        } else if (this.comms.isConnecting()) {
            throw new MqttException(32110);
        } else if (this.comms.isDisconnecting()) {
            throw new MqttException(32102);
        } else if (this.comms.isClosed()) {
            throw new MqttException(32111);
        } else {
            stopReconnectCycle();
            attemptReconnect();
        }
    }

    /* access modifiers changed from: private */
    public void attemptReconnect() {
        log.fine(CLASS_NAME, "attemptReconnect", "500", new Object[]{this.clientId});
        try {
            connect(this.connOpts, this.userContext, new MqttReconnectActionListener("attemptReconnect"));
        } catch (MqttSecurityException ex) {
            log.fine(CLASS_NAME, "attemptReconnect", "804", (Object[]) null, ex);
        } catch (MqttException ex2) {
            log.fine(CLASS_NAME, "attemptReconnect", "804", (Object[]) null, ex2);
        }
    }

    /* access modifiers changed from: private */
    public void startReconnectCycle() {
        log.fine(CLASS_NAME, "startReconnectCycle", "503", new Object[]{this.clientId, new Long((long) reconnectDelay)});
        this.reconnectTimer = new Timer("MQTT Reconnect: " + this.clientId);
        this.reconnectTimer.schedule(new ReconnectTask(this, (ReconnectTask) null), (long) reconnectDelay);
    }

    /* access modifiers changed from: private */
    public void stopReconnectCycle() {
        log.fine(CLASS_NAME, "stopReconnectCycle", "504", new Object[]{this.clientId});
        synchronized (clientLock) {
            if (this.connOpts.isAutomaticReconnect()) {
                if (this.reconnectTimer != null) {
                    this.reconnectTimer.cancel();
                    this.reconnectTimer = null;
                }
                reconnectDelay = 1000;
            }
        }
    }

    private class ReconnectTask extends TimerTask {
        private static final String methodName = "ReconnectTask.run";

        private ReconnectTask() {
        }

        /* synthetic */ ReconnectTask(MqttAsyncClient mqttAsyncClient, ReconnectTask reconnectTask) {
            this();
        }

        public void run() {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, methodName, "506");
            MqttAsyncClient.this.attemptReconnect();
        }
    }

    class MqttReconnectCallback implements MqttCallbackExtended {
        final boolean automaticReconnect;

        MqttReconnectCallback(boolean isAutomaticReconnect) {
            this.automaticReconnect = isAutomaticReconnect;
        }

        public void connectionLost(Throwable cause) {
            if (this.automaticReconnect) {
                MqttAsyncClient.this.comms.setRestingState(true);
                MqttAsyncClient.this.reconnecting = true;
                MqttAsyncClient.this.startReconnectCycle();
            }
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
        }

        public void connectComplete(boolean reconnect, String serverURI) {
        }
    }

    class MqttReconnectActionListener implements IMqttActionListener {
        final String methodName;

        MqttReconnectActionListener(String methodName2) {
            this.methodName = methodName2;
        }

        public void onSuccess(IMqttToken asyncActionToken) {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, this.methodName, "501", new Object[]{asyncActionToken.getClient().getClientId()});
            MqttAsyncClient.this.comms.setRestingState(false);
            MqttAsyncClient.this.stopReconnectCycle();
        }

        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, this.methodName, "502", new Object[]{asyncActionToken.getClient().getClientId()});
            if (MqttAsyncClient.reconnectDelay < 128000) {
                MqttAsyncClient.reconnectDelay = MqttAsyncClient.reconnectDelay * 2;
            }
            rescheduleReconnectCycle(MqttAsyncClient.reconnectDelay);
        }

        private void rescheduleReconnectCycle(int delay) {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, String.valueOf(this.methodName) + ":rescheduleReconnectCycle", "505", new Object[]{MqttAsyncClient.this.clientId, String.valueOf(MqttAsyncClient.reconnectDelay)});
            synchronized (MqttAsyncClient.clientLock) {
                if (MqttAsyncClient.this.connOpts.isAutomaticReconnect()) {
                    if (MqttAsyncClient.this.reconnectTimer != null) {
                        MqttAsyncClient.this.reconnectTimer.schedule(new ReconnectTask(MqttAsyncClient.this, (ReconnectTask) null), (long) delay);
                    } else {
                        MqttAsyncClient.reconnectDelay = delay;
                        MqttAsyncClient.this.startReconnectCycle();
                    }
                }
            }
        }
    }

    public void setBufferOpts(DisconnectedBufferOptions bufferOpts) {
        this.comms.setDisconnectedMessageBuffer(new DisconnectedMessageBuffer(bufferOpts));
    }

    public int getBufferedMessageCount() {
        return this.comms.getBufferedMessageCount();
    }

    public MqttMessage getBufferedMessage(int bufferIndex) {
        return this.comms.getBufferedMessage(bufferIndex);
    }

    public void deleteBufferedMessage(int bufferIndex) {
        this.comms.deleteBufferedMessage(bufferIndex);
    }

    public int getInFlightMessageCount() {
        return this.comms.getActualInFlight();
    }

    public void close() throws MqttException {
        close(false);
    }

    public void close(boolean force) throws MqttException {
        log.fine(CLASS_NAME, "close", "113");
        this.comms.close(force);
        log.fine(CLASS_NAME, "close", "114");
    }

    public Debug getDebug() {
        return new Debug(this.clientId, this.comms);
    }
}
