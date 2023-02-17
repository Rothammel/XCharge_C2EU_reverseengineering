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

/* loaded from: classes.dex */
public class MqttAsyncClient implements IMqttAsyncClient {
    private static final String CLIENT_ID_PREFIX = "paho";
    private static final long DISCONNECT_TIMEOUT = 10000;
    private static final char MAX_HIGH_SURROGATE = 56319;
    private static final char MIN_HIGH_SURROGATE = 55296;
    private static final long QUIESCE_TIMEOUT = 30000;
    private String clientId;
    protected ClientComms comms;
    private MqttConnectOptions connOpts;
    private ScheduledExecutorService executorService;
    private MqttCallback mqttCallback;
    private MqttClientPersistence persistence;
    private Timer reconnectTimer;
    private boolean reconnecting;
    private String serverURI;
    private Hashtable topics;
    private Object userContext;
    private static final String CLASS_NAME = MqttAsyncClient.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private static int reconnectDelay = 1000;
    private static Object clientLock = new Object();

    public MqttAsyncClient(String serverURI, String clientId) throws MqttException {
        this(serverURI, clientId, new MqttDefaultFilePersistence());
    }

    public MqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        this(serverURI, clientId, persistence, new TimerPingSender());
    }

    public MqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender) throws MqttException {
        this(serverURI, clientId, persistence, pingSender, null);
    }

    public MqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender, ScheduledExecutorService executorService) throws MqttException {
        this.reconnecting = false;
        log.setResourceName(clientId);
        if (clientId == null) {
            throw new IllegalArgumentException("Null clientId");
        }
        int clientIdLength = 0;
        int i = 0;
        while (i < clientId.length() - 1) {
            if (Character_isHighSurrogate(clientId.charAt(i))) {
                i++;
            }
            clientIdLength++;
            i++;
        }
        if (clientIdLength > 65535) {
            throw new IllegalArgumentException("ClientId longer than 65535 characters");
        }
        MqttConnectOptions.validateURI(serverURI);
        this.serverURI = serverURI;
        this.clientId = clientId;
        this.persistence = persistence;
        if (this.persistence == null) {
            this.persistence = new MemoryPersistence();
        }
        this.executorService = executorService;
        if (this.executorService == null) {
            this.executorService = Executors.newScheduledThreadPool(10);
        }
        log.fine(CLASS_NAME, "MqttAsyncClient", "101", new Object[]{clientId, serverURI, persistence});
        this.persistence.open(clientId, serverURI);
        this.comms = new ClientComms(this, this.persistence, pingSender, this.executorService);
        this.persistence.close();
        this.topics = new Hashtable();
    }

    protected static boolean Character_isHighSurrogate(char ch) {
        return ch >= 55296 && ch <= 56319;
    }

    protected NetworkModule[] createNetworkModules(String address, MqttConnectOptions options) throws MqttException, MqttSecurityException {
        String[] array;
        log.fine(CLASS_NAME, "createNetworkModules", "116", new Object[]{address});
        String[] serverURIs = options.getServerURIs();
        if (serverURIs == null) {
            array = new String[]{address};
        } else {
            array = serverURIs.length == 0 ? new String[]{address} : serverURIs;
        }
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
                    String shortAddress = address.substring(uri.getScheme().length() + 3);
                    hostField.set(uri, getHostName(shortAddress));
                } catch (IllegalAccessException e2) {
                    e = e2;
                    throw ExceptionHelper.createMqttException(e.getCause());
                } catch (IllegalArgumentException e3) {
                    e = e3;
                    throw ExceptionHelper.createMqttException(e.getCause());
                } catch (NoSuchFieldException e4) {
                    e = e4;
                    throw ExceptionHelper.createMqttException(e.getCause());
                } catch (SecurityException e5) {
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
                    NetworkModule netModule = new TCPNetworkModule(factory, host, port, this.clientId);
                    ((TCPNetworkModule) netModule).setConnectTimeout(options.getConnectionTimeout());
                    return netModule;
                case 1:
                    if (port == -1) {
                        port = 8883;
                    }
                    SSLSocketFactoryFactory factoryFactory = null;
                    if (factory == null) {
                        factoryFactory = new SSLSocketFactoryFactory();
                        Properties sslClientProps = options.getSSLProperties();
                        if (sslClientProps != null) {
                            factoryFactory.initialize(sslClientProps, null);
                        }
                        factory = factoryFactory.createSocketFactory(null);
                    } else if (!(factory instanceof SSLSocketFactory)) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    NetworkModule netModule2 = new SSLNetworkModule((SSLSocketFactory) factory, host, port, this.clientId);
                    ((SSLNetworkModule) netModule2).setSSLhandshakeTimeout(options.getConnectionTimeout());
                    ((SSLNetworkModule) netModule2).setSSLHostnameVerifier(options.getSSLHostnameVerifier());
                    if (factoryFactory != null && (enabledCiphers2 = factoryFactory.getEnabledCipherSuites(null)) != null) {
                        ((SSLNetworkModule) netModule2).setEnabledCiphers(enabledCiphers2);
                        return netModule2;
                    }
                    return netModule2;
                case 2:
                default:
                    log.fine(CLASS_NAME, "createNetworkModule", "119", new Object[]{address});
                    return null;
                case 3:
                    if (port == -1) {
                        port = 80;
                    }
                    if (factory == null) {
                        factory = SocketFactory.getDefault();
                    } else if (factory instanceof SSLSocketFactory) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    NetworkModule netModule3 = new WebSocketNetworkModule(factory, address, host, port, this.clientId);
                    ((WebSocketNetworkModule) netModule3).setConnectTimeout(options.getConnectionTimeout());
                    return netModule3;
                case 4:
                    if (port == -1) {
                        port = WebSocket.DEFAULT_WSS_PORT;
                    }
                    SSLSocketFactoryFactory wSSFactoryFactory = null;
                    if (factory == null) {
                        wSSFactoryFactory = new SSLSocketFactoryFactory();
                        Properties sslClientProps2 = options.getSSLProperties();
                        if (sslClientProps2 != null) {
                            wSSFactoryFactory.initialize(sslClientProps2, null);
                        }
                        factory = wSSFactoryFactory.createSocketFactory(null);
                    } else if (!(factory instanceof SSLSocketFactory)) {
                        throw ExceptionHelper.createMqttException(32105);
                    }
                    NetworkModule netModule4 = new WebSocketSecureNetworkModule((SSLSocketFactory) factory, address, host, port, this.clientId);
                    ((WebSocketSecureNetworkModule) netModule4).setSSLhandshakeTimeout(options.getConnectionTimeout());
                    if (wSSFactoryFactory != null && (enabledCiphers = wSSFactoryFactory.getEnabledCipherSuites(null)) != null) {
                        ((SSLNetworkModule) netModule4).setEnabledCiphers(enabledCiphers);
                        return netModule4;
                    }
                    return netModule4;
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

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken connect(Object userContext, IMqttActionListener callback) throws MqttException, MqttSecurityException {
        return connect(new MqttConnectOptions(), userContext, callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken connect() throws MqttException, MqttSecurityException {
        return connect(null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken connect(MqttConnectOptions options) throws MqttException, MqttSecurityException {
        return connect(options, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback) throws MqttException, MqttSecurityException {
        if (this.comms.isConnected()) {
            throw ExceptionHelper.createMqttException(32100);
        }
        if (this.comms.isConnecting()) {
            throw new MqttException(32110);
        }
        if (this.comms.isDisconnecting()) {
            throw new MqttException(32102);
        }
        if (this.comms.isClosed()) {
            throw new MqttException(32111);
        }
        if (options == null) {
            options = new MqttConnectOptions();
        }
        this.connOpts = options;
        this.userContext = userContext;
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
        objArr[6] = userContext;
        objArr[7] = callback;
        logger.fine(str, "connect", "103", objArr);
        this.comms.setNetworkModules(createNetworkModules(this.serverURI, options));
        this.comms.setReconnectCallback(new MqttReconnectCallback(automaticReconnect));
        MqttToken userToken = new MqttToken(getClientId());
        ConnectActionListener connectActionListener = new ConnectActionListener(this, this.persistence, this.comms, options, userToken, userContext, callback, this.reconnecting);
        userToken.setActionCallback(connectActionListener);
        userToken.setUserContext(this);
        if (this.mqttCallback instanceof MqttCallbackExtended) {
            connectActionListener.setMqttCallbackExtended((MqttCallbackExtended) this.mqttCallback);
        }
        this.comms.setNetworkModuleIndex(0);
        connectActionListener.connect();
        return userToken;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken disconnect(Object userContext, IMqttActionListener callback) throws MqttException {
        return disconnect(30000L, userContext, callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken disconnect() throws MqttException {
        return disconnect(null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken disconnect(long quiesceTimeout) throws MqttException {
        return disconnect(quiesceTimeout, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken disconnect(long quiesceTimeout, Object userContext, IMqttActionListener callback) throws MqttException {
        log.fine(CLASS_NAME, "disconnect", "104", new Object[]{new Long(quiesceTimeout), userContext, callback});
        MqttToken token = new MqttToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext);
        MqttDisconnect disconnect = new MqttDisconnect();
        try {
            this.comms.disconnect(disconnect, quiesceTimeout, token);
            log.fine(CLASS_NAME, "disconnect", "108");
            return token;
        } catch (MqttException ex) {
            log.fine(CLASS_NAME, "disconnect", "105", null, ex);
            throw ex;
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public void disconnectForcibly() throws MqttException {
        disconnectForcibly(30000L, 10000L);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public void disconnectForcibly(long disconnectTimeout) throws MqttException {
        disconnectForcibly(30000L, disconnectTimeout);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        this.comms.disconnectForcibly(quiesceTimeout, disconnectTimeout);
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout, boolean sendDisconnectPacket) throws MqttException {
        this.comms.disconnectForcibly(quiesceTimeout, disconnectTimeout, sendDisconnectPacket);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public boolean isConnected() {
        return this.comms.isConnected();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public String getClientId() {
        return this.clientId;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public String getServerURI() {
        return this.serverURI;
    }

    public String getCurrentServerURI() {
        return this.comms.getNetworkModules()[this.comms.getNetworkModuleIndex()].getServerURI();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public MqttTopic getTopic(String topic) {
        MqttTopic.validate(topic, false);
        MqttTopic result = (MqttTopic) this.topics.get(topic);
        if (result == null) {
            MqttTopic result2 = new MqttTopic(topic, this.comms);
            this.topics.put(topic, result2);
            return result2;
        }
        return result;
    }

    public IMqttToken checkPing(Object userContext, IMqttActionListener callback) throws MqttException {
        log.fine(CLASS_NAME, "ping", "117");
        MqttToken token = this.comms.checkForActivity();
        log.fine(CLASS_NAME, "ping", "118");
        return token;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, userContext, callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String topicFilter, int qos) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, (Object) null, (IMqttActionListener) null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String[] topicFilters, int[] qos) throws MqttException {
        return subscribe(topicFilters, qos, (Object) null, (IMqttActionListener) null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext, IMqttActionListener callback) throws MqttException {
        if (topicFilters.length != qos.length) {
            throw new IllegalArgumentException();
        }
        for (String str : topicFilters) {
            this.comms.removeMessageListener(str);
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
            log.fine(CLASS_NAME, "subscribe", "106", new Object[]{subs.toString(), userContext, callback});
        }
        MqttToken token = new MqttToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext);
        token.internalTok.setTopics(topicFilters);
        MqttSubscribe register = new MqttSubscribe(topicFilters, qos);
        this.comms.sendNoWait(register, token);
        log.fine(CLASS_NAME, "subscribe", "109");
        return token;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback, IMqttMessageListener messageListener) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, userContext, callback, new IMqttMessageListener[]{messageListener});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException {
        return subscribe(new String[]{topicFilter}, new int[]{qos}, (Object) null, (IMqttActionListener) null, new IMqttMessageListener[]{messageListener});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
        return subscribe(topicFilters, qos, (Object) null, (IMqttActionListener) null, messageListeners);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext, IMqttActionListener callback, IMqttMessageListener[] messageListeners) throws MqttException {
        if (messageListeners.length != qos.length || qos.length != topicFilters.length) {
            throw new IllegalArgumentException();
        }
        IMqttToken token = subscribe(topicFilters, qos, userContext, callback);
        for (int i = 0; i < topicFilters.length; i++) {
            this.comms.setMessageListener(topicFilters[i], messageListeners[i]);
        }
        return token;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken unsubscribe(String topicFilter, Object userContext, IMqttActionListener callback) throws MqttException {
        return unsubscribe(new String[]{topicFilter}, userContext, callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken unsubscribe(String topicFilter) throws MqttException {
        return unsubscribe(new String[]{topicFilter}, (Object) null, (IMqttActionListener) null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken unsubscribe(String[] topicFilters) throws MqttException {
        return unsubscribe(topicFilters, (Object) null, (IMqttActionListener) null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttToken unsubscribe(String[] topicFilters, Object userContext, IMqttActionListener callback) throws MqttException {
        if (log.isLoggable(5)) {
            String subs = "";
            for (int i = 0; i < topicFilters.length; i++) {
                if (i > 0) {
                    subs = String.valueOf(subs) + ", ";
                }
                subs = String.valueOf(subs) + topicFilters[i];
            }
            log.fine(CLASS_NAME, "unsubscribe", "107", new Object[]{subs, userContext, callback});
        }
        for (String str : topicFilters) {
            MqttTopic.validate(str, true);
        }
        for (String str2 : topicFilters) {
            this.comms.removeMessageListener(str2);
        }
        MqttToken token = new MqttToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext);
        token.internalTok.setTopics(topicFilters);
        MqttUnsubscribe unregister = new MqttUnsubscribe(topicFilters);
        this.comms.sendNoWait(unregister, token);
        log.fine(CLASS_NAME, "unsubscribe", "110");
        return token;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public void setCallback(MqttCallback callback) {
        this.mqttCallback = callback;
        this.comms.setCallback(callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public void setManualAcks(boolean manualAcks) {
        this.comms.setManualAcks(manualAcks);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        this.comms.messageArrivedComplete(messageId, qos);
    }

    public static String generateClientId() {
        return CLIENT_ID_PREFIX + System.nanoTime();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.comms.getPendingDeliveryTokens();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained, Object userContext, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        return publish(topic, message, userContext, callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException, MqttPersistenceException {
        return publish(topic, payload, qos, retained, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttDeliveryToken publish(String topic, MqttMessage message) throws MqttException, MqttPersistenceException {
        return publish(topic, message, (Object) null, (IMqttActionListener) null);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
    public IMqttDeliveryToken publish(String topic, MqttMessage message, Object userContext, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        log.fine(CLASS_NAME, "publish", "111", new Object[]{topic, userContext, callback});
        MqttTopic.validate(topic, false);
        MqttDeliveryToken token = new MqttDeliveryToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext);
        token.setMessage(message);
        token.internalTok.setTopics(new String[]{topic});
        MqttPublish pubMsg = new MqttPublish(topic, message);
        this.comms.sendNoWait(pubMsg, token);
        log.fine(CLASS_NAME, "publish", "112");
        return token;
    }

    public void reconnect() throws MqttException {
        log.fine(CLASS_NAME, "reconnect", "500", new Object[]{this.clientId});
        if (this.comms.isConnected()) {
            throw ExceptionHelper.createMqttException(32100);
        }
        if (this.comms.isConnecting()) {
            throw new MqttException(32110);
        }
        if (this.comms.isDisconnecting()) {
            throw new MqttException(32102);
        }
        if (this.comms.isClosed()) {
            throw new MqttException(32111);
        }
        stopReconnectCycle();
        attemptReconnect();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void attemptReconnect() {
        log.fine(CLASS_NAME, "attemptReconnect", "500", new Object[]{this.clientId});
        try {
            connect(this.connOpts, this.userContext, new MqttReconnectActionListener("attemptReconnect"));
        } catch (MqttSecurityException ex) {
            log.fine(CLASS_NAME, "attemptReconnect", "804", null, ex);
        } catch (MqttException ex2) {
            log.fine(CLASS_NAME, "attemptReconnect", "804", null, ex2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startReconnectCycle() {
        log.fine(CLASS_NAME, "startReconnectCycle", "503", new Object[]{this.clientId, new Long(reconnectDelay)});
        this.reconnectTimer = new Timer("MQTT Reconnect: " + this.clientId);
        this.reconnectTimer.schedule(new ReconnectTask(this, null), reconnectDelay);
    }

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ReconnectTask extends TimerTask {
        private static final String methodName = "ReconnectTask.run";

        private ReconnectTask() {
        }

        /* synthetic */ ReconnectTask(MqttAsyncClient mqttAsyncClient, ReconnectTask reconnectTask) {
            this();
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, methodName, "506");
            MqttAsyncClient.this.attemptReconnect();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class MqttReconnectCallback implements MqttCallbackExtended {
        final boolean automaticReconnect;

        MqttReconnectCallback(boolean isAutomaticReconnect) {
            this.automaticReconnect = isAutomaticReconnect;
        }

        @Override // org.eclipse.paho.client.mqttv3.MqttCallback
        public void connectionLost(Throwable cause) {
            if (this.automaticReconnect) {
                MqttAsyncClient.this.comms.setRestingState(true);
                MqttAsyncClient.this.reconnecting = true;
                MqttAsyncClient.this.startReconnectCycle();
            }
        }

        @Override // org.eclipse.paho.client.mqttv3.MqttCallback
        public void messageArrived(String topic, MqttMessage message) throws Exception {
        }

        @Override // org.eclipse.paho.client.mqttv3.MqttCallback
        public void deliveryComplete(IMqttDeliveryToken token) {
        }

        @Override // org.eclipse.paho.client.mqttv3.MqttCallbackExtended
        public void connectComplete(boolean reconnect, String serverURI) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class MqttReconnectActionListener implements IMqttActionListener {
        final String methodName;

        MqttReconnectActionListener(String methodName) {
            this.methodName = methodName;
        }

        @Override // org.eclipse.paho.client.mqttv3.IMqttActionListener
        public void onSuccess(IMqttToken asyncActionToken) {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, this.methodName, "501", new Object[]{asyncActionToken.getClient().getClientId()});
            MqttAsyncClient.this.comms.setRestingState(false);
            MqttAsyncClient.this.stopReconnectCycle();
        }

        @Override // org.eclipse.paho.client.mqttv3.IMqttActionListener
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, this.methodName, "502", new Object[]{asyncActionToken.getClient().getClientId()});
            if (MqttAsyncClient.reconnectDelay < 128000) {
                MqttAsyncClient.reconnectDelay *= 2;
            }
            rescheduleReconnectCycle(MqttAsyncClient.reconnectDelay);
        }

        private void rescheduleReconnectCycle(int delay) {
            String reschedulemethodName = String.valueOf(this.methodName) + ":rescheduleReconnectCycle";
            MqttAsyncClient.log.fine(MqttAsyncClient.CLASS_NAME, reschedulemethodName, "505", new Object[]{MqttAsyncClient.this.clientId, String.valueOf(MqttAsyncClient.reconnectDelay)});
            synchronized (MqttAsyncClient.clientLock) {
                if (MqttAsyncClient.this.connOpts.isAutomaticReconnect()) {
                    if (MqttAsyncClient.this.reconnectTimer != null) {
                        MqttAsyncClient.this.reconnectTimer.schedule(new ReconnectTask(MqttAsyncClient.this, null), delay);
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

    @Override // org.eclipse.paho.client.mqttv3.IMqttAsyncClient
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
