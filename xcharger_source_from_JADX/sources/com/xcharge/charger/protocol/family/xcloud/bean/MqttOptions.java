package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.charger.data.bean.type.SERVICE_REGION;
import com.xcharge.common.bean.JsonBean;

public class MqttOptions extends JsonBean<MqttOptions> {
    private boolean binaryMode = false;
    private String broker = null;
    private String clientId = null;
    private int connectionTimeout = 120;
    private boolean debugMode = false;
    private String downTopic = null;
    private int keepAlive = 60;
    private int maxMemorySize = 50;
    private String password = null;
    private SERVICE_REGION region = null;
    private String upTopic = null;
    private String userName = null;

    public int getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(int keepAlive2) {
        this.keepAlive = keepAlive2;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode2) {
        this.debugMode = debugMode2;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout2) {
        this.connectionTimeout = connectionTimeout2;
    }

    public int getMaxMemorySize() {
        return this.maxMemorySize;
    }

    public void setMaxMemorySize(int maxMemorySize2) {
        this.maxMemorySize = maxMemorySize2;
    }

    public boolean isBinaryMode() {
        return this.binaryMode;
    }

    public void setBinaryMode(boolean binaryMode2) {
        this.binaryMode = binaryMode2;
    }

    public SERVICE_REGION getRegion() {
        return this.region;
    }

    public void setRegion(SERVICE_REGION region2) {
        this.region = region2;
    }

    public String getBroker() {
        return this.broker;
    }

    public void setBroker(String broker2) {
        this.broker = broker2;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName2) {
        this.userName = userName2;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId2) {
        this.clientId = clientId2;
    }

    public String getUpTopic() {
        return this.upTopic;
    }

    public void setUpTopic(String upTopic2) {
        this.upTopic = upTopic2;
    }

    public String getDownTopic() {
        return this.downTopic;
    }

    public void setDownTopic(String downTopic2) {
        this.downTopic = downTopic2;
    }
}
