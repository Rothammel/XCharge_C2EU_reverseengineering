package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.charger.data.bean.type.SERVICE_REGION;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class MqttOptions extends JsonBean<MqttOptions> {
    private int keepAlive = 60;
    private boolean debugMode = false;
    private int connectionTimeout = 120;
    private int maxMemorySize = 50;
    private boolean binaryMode = false;
    private SERVICE_REGION region = null;
    private String broker = null;
    private String userName = null;
    private String password = null;
    private String clientId = null;
    private String upTopic = null;
    private String downTopic = null;

    public int getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxMemorySize() {
        return this.maxMemorySize;
    }

    public void setMaxMemorySize(int maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

    public boolean isBinaryMode() {
        return this.binaryMode;
    }

    public void setBinaryMode(boolean binaryMode) {
        this.binaryMode = binaryMode;
    }

    public SERVICE_REGION getRegion() {
        return this.region;
    }

    public void setRegion(SERVICE_REGION region) {
        this.region = region;
    }

    public String getBroker() {
        return this.broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUpTopic() {
        return this.upTopic;
    }

    public void setUpTopic(String upTopic) {
        this.upTopic = upTopic;
    }

    public String getDownTopic() {
        return this.downTopic;
    }

    public void setDownTopic(String downTopic) {
        this.downTopic = downTopic;
    }
}