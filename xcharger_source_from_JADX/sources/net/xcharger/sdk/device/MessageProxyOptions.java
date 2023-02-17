package net.xcharger.sdk.device;

import com.xcharge.charger.data.bean.type.SERVICE_REGION;
import org.apache.http.util.TextUtils;

public class MessageProxyOptions {
    private boolean binaryMode = false;
    private String broker = null;
    private String clientId = null;
    private int connectionTimeout = 30;
    private boolean devMode = false;
    private String downTopic = null;
    private int keepAlive = 60;
    private int msgIdCacheSize = 100;
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

    public int getMsgIdCacheSize() {
        return this.msgIdCacheSize;
    }

    public void setMsgIdCacheSize(int msgIdCacheSize2) {
        this.msgIdCacheSize = msgIdCacheSize2;
    }

    public boolean isDevMode() {
        return this.devMode;
    }

    public void setDevMode(boolean devMode2) {
        this.devMode = devMode2;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout2) {
        this.connectionTimeout = connectionTimeout2;
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

    public String getAddrUrl() {
        if (!TextUtils.isEmpty(this.broker)) {
            return null;
        }
        if (this.devMode) {
            return "http://xcloud.dev.xcharger.net/Addr/getBrokerForDevice/v1.0";
        }
        if (SERVICE_REGION.Europe.equals(this.region)) {
            return "http://eu-addr.xcloud.xcharger.net/getBrokerForDevice/v1.0";
        }
        return "http://addr.xcloud.xcharger.net/getBrokerForDevice/v1.0";
    }
}
