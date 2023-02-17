package com.xcharge.charger.protocol.ocpp.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class SecureConfig extends JsonBean<SecureConfig> {
    private String secureProtocol = "TLS";
    private String clientKeystorePath = null;
    private String clientKeystoreType = null;
    private String clientKeystorePassword = null;
    private String clientKeyPassword = null;
    private String serverTrustKeystorePath = null;
    private String serverTrustKeystoreType = null;
    private String serverTrustKeystorePassword = null;

    public String getSecureProtocol() {
        return this.secureProtocol;
    }

    public void setSecureProtocol(String secureProtocol) {
        this.secureProtocol = secureProtocol;
    }

    public String getClientKeystorePath() {
        return this.clientKeystorePath;
    }

    public void setClientKeystorePath(String clientKeystorePath) {
        this.clientKeystorePath = clientKeystorePath;
    }

    public String getClientKeystoreType() {
        return this.clientKeystoreType;
    }

    public void setClientKeystoreType(String clientKeystoreType) {
        this.clientKeystoreType = clientKeystoreType;
    }

    public String getClientKeystorePassword() {
        return this.clientKeystorePassword;
    }

    public void setClientKeystorePassword(String clientKeystorePassword) {
        this.clientKeystorePassword = clientKeystorePassword;
    }

    public String getClientKeyPassword() {
        return this.clientKeyPassword;
    }

    public void setClientKeyPassword(String clientKeyPassword) {
        this.clientKeyPassword = clientKeyPassword;
    }

    public String getServerTrustKeystorePath() {
        return this.serverTrustKeystorePath;
    }

    public void setServerTrustKeystorePath(String serverTrustKeystorePath) {
        this.serverTrustKeystorePath = serverTrustKeystorePath;
    }

    public String getServerTrustKeystoreType() {
        return this.serverTrustKeystoreType;
    }

    public void setServerTrustKeystoreType(String serverTrustKeystoreType) {
        this.serverTrustKeystoreType = serverTrustKeystoreType;
    }

    public String getServerTrustKeystorePassword() {
        return this.serverTrustKeystorePassword;
    }

    public void setServerTrustKeystorePassword(String serverTrustKeystorePassword) {
        this.serverTrustKeystorePassword = serverTrustKeystorePassword;
    }
}
