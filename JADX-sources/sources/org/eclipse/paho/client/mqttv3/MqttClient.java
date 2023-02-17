package org.eclipse.paho.client.mqttv3;

import java.util.concurrent.ScheduledExecutorService;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.util.Debug;

/* loaded from: classes.dex */
public class MqttClient implements IMqttClient {
    protected MqttAsyncClient aClient;
    protected long timeToWait;

    public MqttClient(String serverURI, String clientId) throws MqttException {
        this(serverURI, clientId, new MqttDefaultFilePersistence());
    }

    public MqttClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        this.aClient = null;
        this.timeToWait = -1L;
        this.aClient = new MqttAsyncClient(serverURI, clientId, persistence);
    }

    public MqttClient(String serverURI, String clientId, MqttClientPersistence persistence, ScheduledExecutorService executorService) throws MqttException {
        this.aClient = null;
        this.timeToWait = -1L;
        this.aClient = new MqttAsyncClient(serverURI, clientId, persistence, new ScheduledExecutorPingSender(executorService), executorService);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void connect() throws MqttSecurityException, MqttException {
        connect(new MqttConnectOptions());
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void connect(MqttConnectOptions options) throws MqttSecurityException, MqttException {
        this.aClient.connect(options, null, null).waitForCompletion(getTimeToWait());
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken connectWithResult(MqttConnectOptions options) throws MqttSecurityException, MqttException {
        IMqttToken tok = this.aClient.connect(options, null, null);
        tok.waitForCompletion(getTimeToWait());
        return tok;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void disconnect() throws MqttException {
        this.aClient.disconnect().waitForCompletion();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void disconnect(long quiesceTimeout) throws MqttException {
        this.aClient.disconnect(quiesceTimeout, null, null).waitForCompletion();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void disconnectForcibly() throws MqttException {
        this.aClient.disconnectForcibly();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void disconnectForcibly(long disconnectTimeout) throws MqttException {
        this.aClient.disconnectForcibly(disconnectTimeout);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        this.aClient.disconnectForcibly(quiesceTimeout, disconnectTimeout);
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout, boolean sendDisconnectPacket) throws MqttException {
        this.aClient.disconnectForcibly(quiesceTimeout, disconnectTimeout, sendDisconnectPacket);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String topicFilter) throws MqttException {
        subscribe(new String[]{topicFilter}, new int[]{1});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String[] topicFilters) throws MqttException {
        int[] qos = new int[topicFilters.length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = 1;
        }
        subscribe(topicFilters, qos);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String topicFilter, int qos) throws MqttException {
        subscribe(new String[]{topicFilter}, new int[]{qos});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String[] topicFilters, int[] qos) throws MqttException {
        IMqttToken tok = this.aClient.subscribe(topicFilters, qos, (Object) null, (IMqttActionListener) null);
        tok.waitForCompletion(getTimeToWait());
        int[] grantedQos = tok.getGrantedQos();
        for (int i = 0; i < grantedQos.length; i++) {
            qos[i] = grantedQos[i];
        }
        if (grantedQos.length == 1 && qos[0] == 128) {
            throw new MqttException(128);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String topicFilter, IMqttMessageListener messageListener) throws MqttException {
        subscribe(new String[]{topicFilter}, new int[]{1}, new IMqttMessageListener[]{messageListener});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String[] topicFilters, IMqttMessageListener[] messageListeners) throws MqttException {
        int[] qos = new int[topicFilters.length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = 1;
        }
        subscribe(topicFilters, qos, messageListeners);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException {
        subscribe(new String[]{topicFilter}, new int[]{qos}, new IMqttMessageListener[]{messageListener});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
        subscribe(topicFilters, qos);
        for (int i = 0; i < topicFilters.length; i++) {
            this.aClient.comms.setMessageListener(topicFilters[i], messageListeners[i]);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String topicFilter) throws MqttException {
        return subscribeWithResponse(new String[]{topicFilter}, new int[]{1});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String topicFilter, IMqttMessageListener messageListener) throws MqttException {
        return subscribeWithResponse(new String[]{topicFilter}, new int[]{1}, new IMqttMessageListener[]{messageListener});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String topicFilter, int qos) throws MqttException {
        return subscribeWithResponse(new String[]{topicFilter}, new int[]{qos});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException {
        return subscribeWithResponse(new String[]{topicFilter}, new int[]{qos}, new IMqttMessageListener[]{messageListener});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String[] topicFilters) throws MqttException {
        int[] qos = new int[topicFilters.length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = 1;
        }
        return subscribeWithResponse(topicFilters, qos);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String[] topicFilters, IMqttMessageListener[] messageListeners) throws MqttException {
        int[] qos = new int[topicFilters.length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = 1;
        }
        return subscribeWithResponse(topicFilters, qos, messageListeners);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String[] topicFilters, int[] qos) throws MqttException {
        IMqttToken tok = this.aClient.subscribe(topicFilters, qos, (Object) null, (IMqttActionListener) null);
        tok.waitForCompletion(getTimeToWait());
        return tok;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttToken subscribeWithResponse(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
        IMqttToken tok = subscribeWithResponse(topicFilters, qos);
        for (int i = 0; i < topicFilters.length; i++) {
            this.aClient.comms.setMessageListener(topicFilters[i], messageListeners[i]);
        }
        return tok;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void unsubscribe(String topicFilter) throws MqttException {
        unsubscribe(new String[]{topicFilter});
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void unsubscribe(String[] topicFilters) throws MqttException {
        this.aClient.unsubscribe(topicFilters, (Object) null, (IMqttActionListener) null).waitForCompletion(getTimeToWait());
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException, MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        publish(topic, message);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void publish(String topic, MqttMessage message) throws MqttException, MqttPersistenceException {
        this.aClient.publish(topic, message, (Object) null, (IMqttActionListener) null).waitForCompletion(getTimeToWait());
    }

    public void setTimeToWait(long timeToWaitInMillis) throws IllegalArgumentException {
        if (timeToWaitInMillis < -1) {
            throw new IllegalArgumentException();
        }
        this.timeToWait = timeToWaitInMillis;
    }

    public long getTimeToWait() {
        return this.timeToWait;
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void close() throws MqttException {
        this.aClient.close(false);
    }

    public void close(boolean force) throws MqttException {
        this.aClient.close(force);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public String getClientId() {
        return this.aClient.getClientId();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.aClient.getPendingDeliveryTokens();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public String getServerURI() {
        return this.aClient.getServerURI();
    }

    public String getCurrentServerURI() {
        return this.aClient.getCurrentServerURI();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public MqttTopic getTopic(String topic) {
        return this.aClient.getTopic(topic);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public boolean isConnected() {
        return this.aClient.isConnected();
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void setCallback(MqttCallback callback) {
        this.aClient.setCallback(callback);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void setManualAcks(boolean manualAcks) {
        this.aClient.setManualAcks(manualAcks);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttClient
    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        this.aClient.messageArrivedComplete(messageId, qos);
    }

    public static String generateClientId() {
        return MqttAsyncClient.generateClientId();
    }

    public void reconnect() throws MqttException {
        this.aClient.reconnect();
    }

    public Debug getDebug() {
        return this.aClient.getDebug();
    }
}
