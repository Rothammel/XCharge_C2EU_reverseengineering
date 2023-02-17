package org.eclipse.paho.client.mqttv3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import org.eclipse.paho.client.mqttv3.util.Debug;

/* loaded from: classes.dex */
public class MqttConnectOptions {
    public static final boolean CLEAN_SESSION_DEFAULT = true;
    public static final int CONNECTION_TIMEOUT_DEFAULT = 30;
    public static final int KEEP_ALIVE_INTERVAL_DEFAULT = 60;
    public static final int MAX_INFLIGHT_DEFAULT = 10;
    public static final int MQTT_VERSION_3_1 = 3;
    public static final int MQTT_VERSION_3_1_1 = 4;
    public static final int MQTT_VERSION_DEFAULT = 0;
    protected static final int URI_TYPE_LOCAL = 2;
    protected static final int URI_TYPE_SSL = 1;
    protected static final int URI_TYPE_TCP = 0;
    protected static final int URI_TYPE_WS = 3;
    protected static final int URI_TYPE_WSS = 4;
    private char[] password;
    private SocketFactory socketFactory;
    private String userName;
    private int keepAliveInterval = 60;
    private int maxInflight = 10;
    private String willDestination = null;
    private MqttMessage willMessage = null;
    private Properties sslClientProps = null;
    private HostnameVerifier sslHostnameVerifier = null;
    private boolean cleanSession = true;
    private int connectionTimeout = 30;
    private String[] serverURIs = null;
    private int MqttVersion = 0;
    private boolean automaticReconnect = false;

    public char[] getPassword() {
        return this.password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        if (userName != null && userName.trim().equals("")) {
            throw new IllegalArgumentException();
        }
        this.userName = userName;
    }

    public void setWill(MqttTopic topic, byte[] payload, int qos, boolean retained) {
        String topicS = topic.getName();
        validateWill(topicS, payload);
        setWill(topicS, new MqttMessage(payload), qos, retained);
    }

    public void setWill(String topic, byte[] payload, int qos, boolean retained) {
        validateWill(topic, payload);
        setWill(topic, new MqttMessage(payload), qos, retained);
    }

    private void validateWill(String dest, Object payload) {
        if (dest == null || payload == null) {
            throw new IllegalArgumentException();
        }
        MqttTopic.validate(dest, false);
    }

    protected void setWill(String topic, MqttMessage msg, int qos, boolean retained) {
        this.willDestination = topic;
        this.willMessage = msg;
        this.willMessage.setQos(qos);
        this.willMessage.setRetained(retained);
        this.willMessage.setMutable(false);
    }

    public int getKeepAliveInterval() {
        return this.keepAliveInterval;
    }

    public int getMqttVersion() {
        return this.MqttVersion;
    }

    public void setKeepAliveInterval(int keepAliveInterval) throws IllegalArgumentException {
        if (keepAliveInterval < 0) {
            throw new IllegalArgumentException();
        }
        this.keepAliveInterval = keepAliveInterval;
    }

    public int getMaxInflight() {
        return this.maxInflight;
    }

    public void setMaxInflight(int maxInflight) {
        if (maxInflight < 0) {
            throw new IllegalArgumentException();
        }
        this.maxInflight = maxInflight;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        if (connectionTimeout < 0) {
            throw new IllegalArgumentException();
        }
        this.connectionTimeout = connectionTimeout;
    }

    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }

    public void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public String getWillDestination() {
        return this.willDestination;
    }

    public MqttMessage getWillMessage() {
        return this.willMessage;
    }

    public Properties getSSLProperties() {
        return this.sslClientProps;
    }

    public void setSSLProperties(Properties props) {
        this.sslClientProps = props;
    }

    public HostnameVerifier getSSLHostnameVerifier() {
        return this.sslHostnameVerifier;
    }

    public void setSSLHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.sslHostnameVerifier = hostnameVerifier;
    }

    public boolean isCleanSession() {
        return this.cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public String[] getServerURIs() {
        return this.serverURIs;
    }

    public void setServerURIs(String[] array) {
        for (String str : array) {
            validateURI(str);
        }
        this.serverURIs = array;
    }

    public static int validateURI(String srvURI) {
        try {
            URI vURI = new URI(srvURI);
            if ("ws".equals(vURI.getScheme())) {
                return 3;
            }
            if ("wss".equals(vURI.getScheme())) {
                return 4;
            }
            if (vURI.getPath() != null && !vURI.getPath().isEmpty()) {
                throw new IllegalArgumentException(srvURI);
            }
            if ("tcp".equals(vURI.getScheme())) {
                return 0;
            }
            if ("ssl".equals(vURI.getScheme())) {
                return 1;
            }
            if ("local".equals(vURI.getScheme())) {
                return 2;
            }
            throw new IllegalArgumentException(srvURI);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(srvURI);
        }
    }

    public void setMqttVersion(int MqttVersion) throws IllegalArgumentException {
        if (MqttVersion != 0 && MqttVersion != 3 && MqttVersion != 4) {
            throw new IllegalArgumentException();
        }
        this.MqttVersion = MqttVersion;
    }

    public boolean isAutomaticReconnect() {
        return this.automaticReconnect;
    }

    public void setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    public Properties getDebug() {
        Properties p = new Properties();
        p.put("MqttVersion", new Integer(getMqttVersion()));
        p.put("CleanSession", Boolean.valueOf(isCleanSession()));
        p.put("ConTimeout", new Integer(getConnectionTimeout()));
        p.put("KeepAliveInterval", new Integer(getKeepAliveInterval()));
        p.put("UserName", getUserName() == null ? "null" : getUserName());
        p.put("WillDestination", getWillDestination() == null ? "null" : getWillDestination());
        if (getSocketFactory() == null) {
            p.put("SocketFactory", "null");
        } else {
            p.put("SocketFactory", getSocketFactory());
        }
        if (getSSLProperties() == null) {
            p.put("SSLProperties", "null");
        } else {
            p.put("SSLProperties", getSSLProperties());
        }
        return p;
    }

    public String toString() {
        return Debug.dumpProperties(getDebug(), "Connection options");
    }
}
