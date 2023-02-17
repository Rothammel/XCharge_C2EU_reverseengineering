package com.xcharge.charger.protocol.ocpp.bean;

import com.xcharge.common.bean.JsonBean;

public class SecureConfig extends JsonBean<SecureConfig> {
    private String clientKeyPassword = null;
    private String clientKeystorePassword = null;
    private String clientKeystorePath = null;
    private String clientKeystoreType = null;
    private String secureProtocol = "TLS";
    private String serverTrustKeystorePassword = null;
    private String serverTrustKeystorePath = null;
    private String serverTrustKeystoreType = null;

    public String getSecureProtocol() {
        return this.secureProtocol;
    }

    public void setSecureProtocol(String secureProtocol2) {
        this.secureProtocol = secureProtocol2;
    }

    public String getClientKeystorePath() {
        return this.clientKeystorePath;
    }

    public void setClientKeystorePath(String clientKeystorePath2) {
        this.clientKeystorePath = clientKeystorePath2;
    }

    public String getClientKeystoreType() {
        return this.clientKeystoreType;
    }

    public void setClientKeystoreType(String clientKeystoreType2) {
        this.clientKeystoreType = clientKeystoreType2;
    }

    public String getClientKeystorePassword() {
        return this.clientKeystorePassword;
    }

    public void setClientKeystorePassword(String clientKeystorePassword2) {
        this.clientKeystorePassword = clientKeystorePassword2;
    }

    public String getClientKeyPassword() {
        return this.clientKeyPassword;
    }

    public void setClientKeyPassword(String clientKeyPassword2) {
        this.clientKeyPassword = clientKeyPassword2;
    }

    public String getServerTrustKeystorePath() {
        return this.serverTrustKeystorePath;
    }

    public void setServerTrustKeystorePath(String serverTrustKeystorePath2) {
        this.serverTrustKeystorePath = serverTrustKeystorePath2;
    }

    public String getServerTrustKeystoreType() {
        return this.serverTrustKeystoreType;
    }

    public void setServerTrustKeystoreType(String serverTrustKeystoreType2) {
        this.serverTrustKeystoreType = serverTrustKeystoreType2;
    }

    public String getServerTrustKeystorePassword() {
        return this.serverTrustKeystorePassword;
    }

    public void setServerTrustKeystorePassword(String serverTrustKeystorePassword2) {
        this.serverTrustKeystorePassword = serverTrustKeystorePassword2;
    }
}
