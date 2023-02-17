package net.xcharger.sdk.device;

import com.xcharge.charger.data.bean.type.SERVICE_REGION;
import org.apache.http.util.TextUtils;

/* loaded from: classes.dex */
public class MessageProxyOptions {
    private int keepAlive = 60;
    private int connectionTimeout = 30;
    private boolean devMode = false;
    private int msgIdCacheSize = 100;
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

    public int getMsgIdCacheSize() {
        return this.msgIdCacheSize;
    }

    public void setMsgIdCacheSize(int msgIdCacheSize) {
        this.msgIdCacheSize = msgIdCacheSize;
    }

    public boolean isDevMode() {
        return this.devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
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

    public String getAddrUrl() {
        if (TextUtils.isEmpty(this.broker)) {
            if (this.devMode) {
                return "http://xcloud.dev.xcharger.net/Addr/getBrokerForDevice/v1.0";
            }
            if (SERVICE_REGION.Europe.equals(this.region)) {
                return "http://eu-addr.xcloud.xcharger.net/getBrokerForDevice/v1.0";
            }
            return "http://addr.xcloud.xcharger.net/getBrokerForDevice/v1.0";
        }
        return null;
    }
}
