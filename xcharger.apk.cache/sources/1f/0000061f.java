package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AnyoConfig extends JsonBean<AnyoConfig> {
    private int provider = 108;
    private long magicNumber = 3347882661L;
    private String cloudHost = "cdlink.anyocharging.com";
    private int cloudPort = 8003;
    private String softwareVersion = "1.01.01";
    private String protocolVersion = "2.01";
    private String qrcode = null;

    public int getProvider() {
        return this.provider;
    }

    public void setProvider(int provider) {
        this.provider = provider;
    }

    public long getMagicNumber() {
        return this.magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public String getCloudHost() {
        return this.cloudHost;
    }

    public void setCloudHost(String cloudHost) {
        this.cloudHost = cloudHost;
    }

    public int getCloudPort() {
        return this.cloudPort;
    }

    public void setCloudPort(int cloudPort) {
        this.cloudPort = cloudPort;
    }

    public String getSoftwareVersion() {
        return this.softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getQrcode() {
        return this.qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}