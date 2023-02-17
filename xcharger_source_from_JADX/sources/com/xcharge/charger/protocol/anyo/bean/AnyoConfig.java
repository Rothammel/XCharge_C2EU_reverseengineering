package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.common.bean.JsonBean;

public class AnyoConfig extends JsonBean<AnyoConfig> {
    private String cloudHost = "cdlink.anyocharging.com";
    private int cloudPort = 8003;
    private long magicNumber = 3347882661L;
    private String protocolVersion = "2.01";
    private int provider = 108;
    private String qrcode = null;
    private String softwareVersion = "1.01.01";

    public int getProvider() {
        return this.provider;
    }

    public void setProvider(int provider2) {
        this.provider = provider2;
    }

    public long getMagicNumber() {
        return this.magicNumber;
    }

    public void setMagicNumber(long magicNumber2) {
        this.magicNumber = magicNumber2;
    }

    public String getCloudHost() {
        return this.cloudHost;
    }

    public void setCloudHost(String cloudHost2) {
        this.cloudHost = cloudHost2;
    }

    public int getCloudPort() {
        return this.cloudPort;
    }

    public void setCloudPort(int cloudPort2) {
        this.cloudPort = cloudPort2;
    }

    public String getSoftwareVersion() {
        return this.softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion2) {
        this.softwareVersion = softwareVersion2;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion2) {
        this.protocolVersion = protocolVersion2;
    }

    public String getQrcode() {
        return this.qrcode;
    }

    public void setQrcode(String qrcode2) {
        this.qrcode = qrcode2;
    }
}
